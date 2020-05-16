package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityWitherResistance extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/wither_resistance.png");

    @Override
    public String getType()
    {
        return "witherResistance";
    }

    @Override
    public void tick()
    {
        if (this.getParent().isPotionActive(Potion.getPotionById(20)))
            this.getParent().removePotionEffect(Potion.getPotionById(20));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
