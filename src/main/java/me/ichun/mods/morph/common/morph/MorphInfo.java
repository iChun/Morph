package me.ichun.mods.morph.common.morph;

import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MorphInfo
{
    protected EntityPlayer player; //Should ideally never be null, but can be.

    public MorphState prevState; //Can be null.
    public MorphState nextState; //Should never be null.

    public int morphTime;

    public boolean firstUpdate = true;

    public boolean wasSleeping;

    //TODO health offset save here.

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
                prevState = null;
            }

            setPlayerBoundingBox();
        }
        if(prevState != null && prevState.entInstance != null && isMorphing())
        {
            if(morphTime / (float)Morph.config.morphTime < 0.5F)
            {
                prevState.entInstance.onUpdate();
            }
            syncEntityWithPlayer(prevState.entInstance);
        }
        if(nextState.entInstance != null)
        {
            if(morphTime / (float)Morph.config.morphTime >= 0.5F)
            {
                nextState.entInstance.onUpdate();
            }
            syncEntityWithPlayer(nextState.entInstance);
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
        ent.posY = player.posY;
        ent.posZ = player.posZ;
        ent.dimension = player.dimension;
        ent.world = player.getEntityWorld();
        ent.setHealth(ent.getMaxHealth() * (player.getHealth() / player.getMaxHealth()));

        ent.entityCollisionReduction = 1.0F;
    }

    public void setPlayer(EntityPlayer player)
    {
        this.player = player;

        if(nextState.entInstance instanceof EntityLiving)
        {
            ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
        }
        setPlayerBoundingBox();
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }

    public void setPlayerBoundingBox()
    {
        float morphTransition = getMorphTransitionProgress(0F);

        if(prevState != null)
        {
            EntityLivingBase prevEnt = prevState.getEntInstance(player.getEntityWorld());
            EntityLivingBase nextEnt = nextState.getEntInstance(player.getEntityWorld());

            float newWidth = EntityHelper.interpolateValues(prevEnt.width, nextEnt.width, morphTransition);
            float newHeight = EntityHelper.interpolateValues(prevEnt.height, nextEnt.height, morphTransition);

            setPlayerSize(player, newWidth, newHeight);

            player.eyeHeight = EntityHelper.interpolateValues(prevEnt.getEyeHeight(), nextEnt.getEyeHeight(), morphTransition);
            if(nextState.entInstance instanceof EntityLiving)
            {
                ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
            }
        }
        else
        {
            EntityLivingBase nextEnt = nextState.getEntInstance(player.getEntityWorld());

            setPlayerSize(player, nextEnt.width, nextEnt.height);

            player.eyeHeight = nextEnt.getEyeHeight();
            if(nextState.entInstance instanceof EntityLiving)
            {
                ((EntityLiving)nextState.entInstance).setLeftHanded(player.getPrimaryHand() == EnumHandSide.LEFT);
            }
        }
    }

    public static void setPlayerSize(EntityPlayer player, float width, float height)
    {
        float f = (float)(player.getEntityBoundingBox().maxX - player.getEntityBoundingBox().minX);

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
        player.move(MoverType.SELF, difference, 0.0D, difference);
        player.move(MoverType.SELF, -(difference + difference), 0.0D, -(difference + difference));
        player.move(MoverType.SELF, difference, 0.0D, difference);

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
