package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityFloat extends Ability
{
    public Double floatMotion;
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/float.png");

    @Override
    public String getType() {
        return "float";
    }

    @Override
    public void tick()
    {
        double floatm = floatMotion != null ? floatMotion : -0.114D;
        if(getParent().motionY < floatm)
        {
            getParent().motionY -= getParent().motionY * getStrength();
            getParent().motionY += floatm * getStrength();
        }
        getParent().fallDistance -= getParent().fallDistance * getStrength();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
