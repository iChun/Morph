package me.ichun.mods.morph.api.mob.trait;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExplosiveImmunityTrait extends Trait<ExplosiveImmunityTrait>
        implements IEventBusRequired
{
    public transient float lastStrength = 0F;

    public ExplosiveImmunityTrait()
    {
        type = "traitImmunityExplosive";
    }

    @Override
    public void tick(float strength)
    {
        lastStrength = strength;
    }

    @Override
    public ExplosiveImmunityTrait copy()
    {
        return new ExplosiveImmunityTrait();
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(lastStrength == 1F && event.getEntityLiving() == player && event.getSource().isExplosion())
        {
            event.setCanceled(true);
        }
    }
}
