package me.ichun.mods.morph.common.morph;

import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
            //DO STUFF HERE. LIKE SETTING THE PLAYER SIZE AND WHATNOT.
        }
        if(isMorphing()) //for this to be possible, the player has to be defined anyways.
        {
            morphTime++;

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
    }

    public void setPlayer(EntityPlayer player)
    {
        this.player = player;

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
            EntityLivingBase prevEnt = prevState.getEntInstance(player.worldObj);
            EntityLivingBase nextEnt = nextState.getEntInstance(player.worldObj);

            float newWidth = EntityHelper.interpolateValues(prevEnt.width, nextEnt.width, morphTransition);
            float newHeight = EntityHelper.interpolateValues(prevEnt.height, nextEnt.height, morphTransition);

            if(!(newWidth == player.width))
            {
                player.moveEntity(-(newWidth - player.width) / 2D, 0D, -(newWidth - player.width) / 2D);
            }

            player.setSize(newWidth, newHeight);

            player.eyeHeight = EntityHelper.interpolateValues(prevEnt.getEyeHeight(), nextEnt.getEyeHeight(), morphTransition);
        }
        else
        {
            EntityLivingBase nextEnt = nextState.getEntInstance(player.worldObj);

            player.setSize(nextEnt.width, nextEnt.height);

            player.eyeHeight = nextEnt.getEyeHeight();
        }
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
        return MathHelper.clamp_float((morphTime + renderTick) /  Morph.config.morphTime, 0.0F, 1.0F);
    }

    public float getMorphTransitionProgress(float renderTick) //use 0 for serverside. This is for the transitioning between models, and therefore, size.
    {
        return (float)Math.sin(Math.toRadians(MathHelper.clamp_float((morphTime - 10 + renderTick) / (Morph.config.morphTime - 20F), 0.0F, 1.0F) * 90F));
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
