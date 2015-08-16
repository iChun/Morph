package me.ichun.mods.morph.common.ability;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPotionEffect extends Ability
{
    public int potionId;
    public int duration;
    public int amplifier;
    public boolean ambient;

    //TODO redo this class properly. it's dragged in so rendering stuff would work.

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

    @SideOnly(Side.CLIENT)
    @Override
    public boolean entityHasAbility(EntityLivingBase living)
    {
        if (living instanceof EntitySkeleton)
        {
            EntitySkeleton skele = (EntitySkeleton) living;
            if (skele.getSkeletonType() != 1)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResourceLocation getIcon()
    {
        return null;
    }
}
