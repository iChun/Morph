package me.ichun.mods.morph.common.morph;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.world.World;

public class MorphState
    implements Comparable<MorphState>
{
    public final MorphVariant currentVariant;  //The current morph variant we are using to create the entity instance
    protected EntityLivingBase entInstance; //Entity Instance to be used for rendering

    public MorphState(MorphVariant variant)
    {
        this.currentVariant = variant;
    }

    public EntityLivingBase getEntInstance(World world)
    {
        if(entInstance != null && entInstance.world != world)
        {
            entInstance = null;
        }
        if(entInstance == null)
        {
            entInstance = currentVariant.createEntityInstance(world);
            if(entInstance instanceof EntityDragon)
            {
                ((EntityDragon)entInstance).setNoAI(false);
            }
        }
        return entInstance;
    }

    public String getName()
    {
        if(entInstance != null)
        {
            return entInstance.getName();
        }
        return currentVariant.entId;
    }

    @Override
    public int compareTo(MorphState state)
    {
        if(getName().toLowerCase().equals(state.getName().toLowerCase()))
        {
            return currentVariant.compareTo(state.currentVariant);
        }
        return getName().toLowerCase().compareTo(state.getName().toLowerCase());
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof MorphState)
        {
            return currentVariant.equals(((MorphState)o).currentVariant);
        }
        return false;
    }
}
