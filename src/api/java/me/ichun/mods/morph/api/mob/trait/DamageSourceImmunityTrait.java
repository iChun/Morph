package me.ichun.mods.morph.api.mob.trait;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DamageSourceImmunityTrait extends Trait<DamageSourceImmunityTrait>
        implements IEventBusRequired
{
    public String damageType;

    public transient float lastStrength = 0F;

    public DamageSourceImmunityTrait()
    {
        type = "traitImmunityDamageSource";
    }

    @Override
    public void tick(float strength)
    {
        lastStrength = strength;
    }

    @Override
    public DamageSourceImmunityTrait copy()
    {
        DamageSourceImmunityTrait ds = new DamageSourceImmunityTrait();
        ds.damageType = this.damageType;
        return ds;
    }

    @Override
    public boolean canTransitionTo(Trait<?> trait)
    {
        if(trait instanceof DamageSourceImmunityTrait)
        {
            return damageType != null && damageType.equals(((DamageSourceImmunityTrait)trait).damageType);
        }
        return false;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(lastStrength == 1F && event.getEntityLiving() == player && event.getSource().damageType.equals(damageType))
        {
            event.setCanceled(true);
        }
    }
}
