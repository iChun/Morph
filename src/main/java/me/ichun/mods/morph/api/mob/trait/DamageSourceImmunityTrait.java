package me.ichun.mods.morph.api.mob.trait;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DamageSourceImmunityTrait extends Trait<DamageSourceImmunityTrait>
        implements IEventBusRequired
{
    public String damageType;

    public DamageSourceImmunityTrait()
    {
        type = "traitImmunityDamageSource";
    }

    @Override
    public void tick(float strength)
    {
    }

    @Override
    public DamageSourceImmunityTrait copy()
    {
        DamageSourceImmunityTrait ds = new DamageSourceImmunityTrait();
        ds.damageType = this.damageType;
        return ds;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.getEntityLiving() == player && event.getSource().damageType.equals(damageType))
        {
            event.setCanceled(true);
        }
    }
}
