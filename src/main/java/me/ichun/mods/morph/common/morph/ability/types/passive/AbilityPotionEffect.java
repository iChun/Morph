package me.ichun.mods.morph.common.morph.ability.types.passive;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;

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
    public Ability clone()
    {
        return new AbilityPotionEffect(potionId, duration, amplifier, ambient);
    }

    @Override
    public boolean entityHasAbility(EntityLivingBase living)
    {
        if (living instanceof EntitySkeleton)
        {
            EntitySkeleton skele = (EntitySkeleton) living;
            if (skele.getSkeletonType() != SkeletonType.WITHER)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCharacteristic(EntityLivingBase living)
    {
        return true;
    }
}
