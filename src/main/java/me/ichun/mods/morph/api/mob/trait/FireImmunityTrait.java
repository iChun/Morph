package me.ichun.mods.morph.api.mob.trait;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FireImmunityTrait extends Trait<FireImmunityTrait>
        implements IEventBusRequired
{
    public FireImmunityTrait()
    {
        type = "traitImmunityFire";
    }

    @Override
    public void tick(float strength)
    {
        player.extinguish();
    }

    @Override
    public FireImmunityTrait copy()
    {
        return new FireImmunityTrait();
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.getEntityLiving() == player && event.getSource().isFireDamage())
        {
            event.setCanceled(true);
        }
    }
}
