package me.ichun.mods.morph.common.morph;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;

public class MorphInfo
{
    public EntityPlayer player;

    public MorphState prevState;
    public MorphState nextState; //Should never be null.

    public int morphTime;

    public void tick()
    {

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
}
