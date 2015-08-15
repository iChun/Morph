package me.ichun.mods.morph.common.morph;

import net.minecraft.entity.EntityLivingBase;

public class MorphState
{
    public MorphVariant currentVariant;  //The current morph variant we are using to create the entity instance
    public EntityLivingBase entInstance; //Entity Instance to be used for rendering
}
