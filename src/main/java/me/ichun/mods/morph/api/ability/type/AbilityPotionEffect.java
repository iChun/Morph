package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPotionEffect extends Ability
{
    public int potionId;
    public int duration;
    public int amplifier;
    public boolean ambient;

    public AbilityPotionEffect()
    {
        this.potionId = 0;
        this.duration = 0;
        this.amplifier = 0;
        this.ambient = true;
    }

    public AbilityPotionEffect(int id, int dur, int amp, boolean amb)
    {
        this.potionId = id;
        this.duration = dur;
        this.amplifier = amp;
        this.ambient = amb;
    }

    @Override
    public String getType()
    {
        return "potionEffect";
    }

    @Override
    public Ability parse(String[] args)
    {
        potionId = Integer.parseInt(args[0]);
        duration = Integer.parseInt(args[1]);
        amplifier = Integer.parseInt(args[2]);
        ambient = Boolean.parseBoolean(args[3]);
        return this;
    }

    @Override
    public Ability clone()
    {
        return new AbilityPotionEffect(potionId, duration, amplifier, ambient);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return null;
    }
}
