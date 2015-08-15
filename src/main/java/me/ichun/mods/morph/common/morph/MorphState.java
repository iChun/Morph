package me.ichun.mods.morph.common.morph;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class MorphState
{
    public final MorphVariant currentVariant;  //The current morph variant we are using to create the entity instance
    public EntityLivingBase entInstance; //Entity Instance to be used for rendering

    public MorphState(MorphVariant variant)
    {
        this.currentVariant = variant;
    }

    public EntityLivingBase getEntInstance(World world)
    {
        if(entInstance != null && entInstance.worldObj != world)
        {
            entInstance = null;
        }
        if(entInstance == null)
        {
            entInstance = currentVariant.createEntityInstance(world);
        }
        return entInstance;
    }
}
