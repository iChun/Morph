package me.ichun.mods.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class ApiDummy implements IApi
{
    @Override
    public boolean canPlayerMorph(EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean hasMorph(String playerName, Side side)
    {
        return false;
    }

    @Override
    public float morphProgress(String playerName, Side side)
    {
        return 1.0F;
    }

    @Override
    public float timeToCompleteMorph()
    {
        return 80; //defaults. May be changed via config.
    }

    @Override
    public EntityLivingBase getPrevMorphEntity(World worldInstance, String playerName, Side side)
    {
        return null;
    }

    @Override
    public EntityLivingBase getMorphEntity(World worldInstance, String playerName, Side side)
    {
        return null;
    }

    @Override
    public boolean forceDemorph(EntityPlayerMP player)
    {
        return false;
    }

    @Override
    public boolean forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph)
    {
        return false;
    }

    @Override
    public boolean acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire, boolean forceMorph, boolean killEntityClientside)
    {
        return false;
    }

    @Override
    public ResourceLocation getMorphSkinTexture()
    {
        return new ResourceLocation("morph", "textures/skin/morphskin.png");
    }

    @Override
    public boolean isMorphApi()
    {
        return false;
    }
}
