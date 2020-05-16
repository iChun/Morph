package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySink extends Ability
{
    public boolean isInWater;
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/sink.png");

    @Override
    public String getType()
    {
        return "sink";
    }

    @Override
    public void tick()
    {
        boolean flying = false;
        if(getParent() instanceof EntityPlayer)
        {
            flying = ((EntityPlayer)getParent()).capabilities.isFlying;
        }

        if(getParent().isInWater())
        {
            if(getParent().collidedHorizontally)
            {
                getParent().motionY = 0.07D;
            }
            else if(getParent().motionY > -0.07D && !flying)
            {
                getParent().motionY = -0.07D;
            }
        }
        if(!getParent().isInWater() && isInWater && !flying)
        {
            getParent().motionY = 0.32D;
        }
        isInWater = getParent().isInWater();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
