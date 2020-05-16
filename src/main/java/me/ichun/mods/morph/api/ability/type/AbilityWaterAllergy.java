package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityWaterAllergy extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/water_allergy.png");

    @Override
    public String getType()
    {
        return "waterAllergy";
    }

    @Override
    public void tick()
    {
        if(getParent().isWet())
        {
            getParent().attackEntityFrom(DamageSource.DROWN, 1.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
