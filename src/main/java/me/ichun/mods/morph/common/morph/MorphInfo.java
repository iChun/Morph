package me.ichun.mods.morph.common.morph;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class MorphInfo
{
    public EntityPlayer player;

    public MorphState prevState;
    public MorphState nextState; //Should never be null.

    public int morphTime;

    public boolean firstUpdate = true;

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
            //DO STUFF HERE. LIKE SETTING THE PLAYER SIZE AND WHATNOT.
        }
    }

    public boolean isMorphing()
    {
        return morphTime < Morph.config.morphTime;
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
