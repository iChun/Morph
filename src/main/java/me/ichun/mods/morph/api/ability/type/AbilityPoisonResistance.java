package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPoisonResistance extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/poison_resistance.png");

    @Override
    public String getType()
    {
        return "poisonResistance";
    }

    @Override
    public void tick()
    {
        if(this.getParent().isPotionActive(Potion.getPotionById(19)))
            this.getParent().removePotionEffect(Potion.getPotionById(19));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
