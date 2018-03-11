package me.ichun.mods.morph.common.morph;

import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.AbilityHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.UUID;

public class MorphInfo
{
    public static final UUID MORPH_HEALTH_ID = UUID.fromString("965EEABB-8AD4-381E-8313-59FBCBAF16D6"); // UUID.nameUUIDFromBytes("Morph health modifier for morph balancing".getBytes()).toString().toUpperCase()

    protected EntityPlayer player; //Should ideally never be null, but can be.

    public MorphState prevState; //Can be null.
    public MorphState nextState; //Should never be null.

    public int morphTime;

    public boolean firstUpdate = true;

    public boolean wasSleeping;

    public MorphInfo(EntityPlayer player, MorphState prevState, MorphState nextState)
    {
        this.player = player;
        this.prevState = prevState;
        this.nextState = nextState;

        if(prevState == null)
        {
            morphTime = Morph.config.morphTime;
        }
    }

    public void tick()
    {
        if(firstUpdate)
        {
            firstUpdate = false;
            if(isMorphing() && prevState == null)
            {
                morphTime = Morph.config.morphTime;
            }
            if(prevState != null && prevState.entInstance != null)
            {
                syncEntityWithPlayer(prevState.entInstance);
            }
            if(nextState.entInstance != null)
            {
                syncEntityWithPlayer(nextState.entInstance);
            }
        }
        if(isMorphing()) //for this to be possible, the player has to be defined anyways.
        {
            morphTime++;

            if(!isMorphing())
            {
                if(prevState.abilities != null)
                {
                    for(Ability ability : prevState.abilities)
                    {
                        ability.kill(nextState.abilities != null ? nextState.abilities : new ArrayList<>());
                    }
                }
                prevState = null;
            }

            setPlayerHealth();
            setPlayerBoundingBox();
        }
        float morphTransition = getMorphTransitionProgress(0F);
        if(prevState != null && prevState.entInstance != null && isMorphing())
        {
            if(morphTime / (float)Morph.config.morphTime < 0.5F)
            {
                prevState.entInstance.onUpdate();
            }
            syncEntityWithPlayer(prevState.entInstance);
            for(Ability ability : prevState.abilities)
            {
                ability.setParent(getPlayer());
                if(ability.getParent() != null)
                {
                    if(!(nextState.abilities != null && AbilityHandler.getInstance().hasAbility(nextState.abilities, ability.type)))
                    {
                        ability.strength = 1.0F - morphTransition;
                    }
                    ability.tick();
                }
            }
        }
        if(nextState.entInstance != null)
        {
            if(morphTime / (float)Morph.config.morphTime >= 0.5F)
            {
                nextState.entInstance.onUpdate();
            }
            syncEntityWithPlayer(nextState.entInstance);
            for(Ability ability : nextState.abilities)
            {
                ability.setParent(getPlayer());
                if(ability.getParent() != null)
                {
                    if(isMorphing())
                    {
                        if(prevState != null && prevState.abilities != null && AbilityHandler.getInstance().hasAbility(prevState.abilities, ability.type)) //prev state has this ability. Set to full strength
                        {
                            ability.strength = 1.0F;
                        }
                        else
                        {
                            ability.strength = morphTransition;
                        }
                    }
                    else
                    {
                        ability.strength = 1.0F;
                    }
                    ability.tick();
                }
            }
        }
        if(player != null)
        {
            if(wasSleeping && !player.isPlayerSleeping())
            {
                setPlayerBoundingBox();
            }
            wasSleeping = player.isPlayerSleeping();
        }
    }

    public void syncEntityWithPlayer(EntityLivingBase ent)
    {
        if(player == null)
        {
            return;
        }

        ent.posX = player.posX;
        ent.posY = -500D; //This is done so the entity doesn't attack the player or pick up items or interact with the world ETC.
        ent.posZ = player.posZ;
        ent.dimension = player.dimension;
        ent.world = player.getEntityWorld();
        ent.setHealth(ent.getMaxHealth() * (player.getHealth() / player.getMaxHealth()));

        ent.entityCollisionReduction = 1.0F;
    }

    public void setPlayer(@Nonnull EntityPlayer player)
    {
        this.player = player;

        if(nextState.entInstance instanceof EntityLiving)
        {
            ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
        }
        setPlayerHealth();
        setPlayerBoundingBox();

        if(prevState != null && prevState.abilities != null)
        {
            for(Ability ability : prevState.abilities)
            {
                ability.setParent(this.player);
                ability.init();
            }
        }
        if(nextState.abilities != null)
        {
            for(Ability ability : nextState.abilities)
            {
                ability.setParent(this.player);
                ability.init();
            }
        }
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }

    public void setPlayerHealth()
    {
        if(player == null || Morph.config.morphHealthBalancing == 0)
        {
            return;
        }

        AttributeModifier amm = createAttributeModifier();
        AttributeModifier current = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifier(MORPH_HEALTH_ID);
        if(current == null || current.getAmount() != amm.getAmount()) //health has not been set or doesn't match
        {
            player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(MORPH_HEALTH_ID);
            player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(amm);
        }
    }

    public AttributeModifier createAttributeModifier()
    {
        float morphTransition = getMorphTransitionProgress(0F);

        int calcHealth = 20;
        if(prevState != null)
        {
            double prevStateHealth = 20D;
            double nextStateHealth = 20D;
            if(!prevState.currentVariant.entId.startsWith("player:") && prevState.currentVariant.entTag.hasKey("Morph_HealthBalancing"))
            {
                prevStateHealth = prevState.currentVariant.entTag.getDouble("Morph_HealthBalancing");
            }
            if(!nextState.currentVariant.entId.startsWith("player:") && nextState.currentVariant.entTag.hasKey("Morph_HealthBalancing"))
            {
                nextStateHealth = nextState.currentVariant.entTag.getDouble("Morph_HealthBalancing");
            }
            calcHealth = Math.max(1, (int)Math.round(prevStateHealth + morphTransition * (nextStateHealth - prevStateHealth)));
        }
        else
        {
            double nextStateHealth = 20D;
            if(!nextState.currentVariant.entId.startsWith("player:") && nextState.currentVariant.entTag.hasKey("Morph_HealthBalancing"))
            {
                nextStateHealth = nextState.currentVariant.entTag.getDouble("Morph_HealthBalancing");
            }
            calcHealth = Math.max(1, (int)nextStateHealth);
        }

        return new AttributeModifier(MORPH_HEALTH_ID, "Morph health modifier", (double)calcHealth - 20D, 0);
    }

    public void setPlayerBoundingBox()
    {
        if(player == null)
        {
            return;
        }

        if(prevState != null)
        {
            float morphTransition = getMorphTransitionProgress(0F);

            EntityLivingBase prevEnt = prevState.getEntInstance(player.getEntityWorld());
            EntityLivingBase nextEnt = nextState.getEntInstance(player.getEntityWorld());

            float newWidth = EntityHelper.interpolateValues(prevEnt.width, nextEnt.width, morphTransition);
            float newHeight = EntityHelper.interpolateValues(prevEnt.height, nextEnt.height, morphTransition);

            setPlayerSize(player, this, newWidth, newHeight);

            player.eyeHeight = EntityHelper.interpolateValues(prevEnt.getEyeHeight(), nextEnt.getEyeHeight(), morphTransition);
            if(nextState.entInstance instanceof EntityLiving)
            {
                ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
            }
        }
        else
        {
            EntityLivingBase nextEnt = nextState.getEntInstance(player.getEntityWorld());

            setPlayerSize(player, this, nextEnt.width, nextEnt.height);

            player.eyeHeight = nextEnt.getEyeHeight();
            if(nextState.entInstance instanceof EntityLiving)
            {
                ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
            }
        }
    }

    public static void setPlayerSize(EntityPlayer player, MorphInfo info, float width, float height)
    {
        float f = (float)(player.getEntityBoundingBox().maxX - player.getEntityBoundingBox().minX);

        if(Math.abs(f - width) < 0.00001D && Math.abs((player.getEntityBoundingBox().maxY - player.getEntityBoundingBox().minY) - height) < 0.00001D)
        {
            return;
        }

        double d0 = (double)width / 2.0D;
        player.setEntityBoundingBox(new AxisAlignedBB(player.posX - d0, player.posY, player.posZ - d0, player.posX + d0, player.posY + (double)height, player.posZ + d0));

        if (((float)(player.getEntityBoundingBox().maxX - player.getEntityBoundingBox().minX)) < f)
        {
            return;
        }

        boolean collidedHorizontally = player.collidedHorizontally;
        boolean collidedVertically = player.collidedVertically;
        boolean onGround = player.onGround;
        boolean collided = player.collided;

        float difference = ((float)(player.getEntityBoundingBox().maxX - player.getEntityBoundingBox().minX)) - f;
        float distanceWalkedModified = player.distanceWalkedModified;
        float distanceWalkedOnStepModified = player.distanceWalkedOnStepModified;
        if(!player.world.isRemote || info.isMorphing())
        {
            player.move(MoverType.SELF, difference, 0.0D, difference);
            player.move(MoverType.SELF, -(difference + difference), 0.0D, -(difference + difference));
            player.move(MoverType.SELF, difference, 0.0D, difference);
        }
        player.distanceWalkedModified = distanceWalkedModified;
        player.distanceWalkedOnStepModified = distanceWalkedOnStepModified;

        player.collidedHorizontally = collidedHorizontally || player.collidedHorizontally;
        player.collidedVertically = collidedVertically || player.collidedVertically;
        player.onGround = onGround || player.onGround;
        player.collided = collided || player.collided;
    }

    public EntityLivingBase getEntity(MorphState state)
    {
        return state.entInstance;
    }

    public boolean isMorphing()
    {
        return morphTime < Morph.config.morphTime;
    }

    public float getMorphProgress(float renderTick) //use 0 for serverside. This is for overall morph progression.
    {
        return MathHelper.clamp((morphTime + renderTick) /  Morph.config.morphTime, 0.0F, 1.0F);
    }

    public float getMorphTransitionProgress(float renderTick) //use 0 for serverside. This is for the transitioning between models, and therefore, size.
    {
        return (float)Math.sin(Math.toRadians(MathHelper.clamp((morphTime - 10 + renderTick) / (Morph.config.morphTime - 20F), 0.0F, 1.0F) * 90F));
    }

    @SideOnly(Side.CLIENT)
    public float getMorphSkinAlpha(float renderTick)
    {
        return getMorphProgress(renderTick) < 0.5D ? ((float)Math.pow((morphTime + renderTick) / 10F, 2D)) : ((float)Math.pow(1F - ((morphTime + renderTick) - (Morph.config.morphTime - 10)) / 10F, 2D));
    }

    /**
     * Cleans the class for GC. Basically label the entInstance in states as null
     */
    public void clean()
    {
        prevState = null; //If we have to clean, prevState isn't even needed anymore.
        nextState.entInstance = null; //nextState should never be null so should never NPE.
    }

    public void read(NBTTagCompound tag)
    {
        if(tag.hasKey("prevStateVar"))
        {
            MorphVariant variant = new MorphVariant("");
            variant.read(tag.getCompoundTag("prevStateVar"));
            prevState = new MorphState(variant);
        }
        MorphVariant variant = new MorphVariant("");
        variant.read(tag.getCompoundTag("nextStateVar"));
        nextState = new MorphState(variant);

        morphTime = tag.getInteger("morphTime");
    }

    public NBTTagCompound write(NBTTagCompound tag)
    {
        if(prevState != null)
        {
            tag.setTag("prevStateVar", prevState.currentVariant.write(new NBTTagCompound()));
        }
        tag.setTag("nextStateVar", nextState.currentVariant.write(new NBTTagCompound()));
        tag.setInteger("morphTime", morphTime);

        return tag;
    }
}
