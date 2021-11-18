package me.ichun.mods.morph.api.mob.trait;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MagicImmunityTrait extends Trait<MagicImmunityTrait>
        implements IEventBusRequired
{
    public MagicImmunityTrait()
    {
        type = "traitImmunityMagic";
    }

    @Override
    public void tick(float strength)
    {
    }

    @Override
    public MagicImmunityTrait copy()
    {
        return new MagicImmunityTrait();
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.getEntityLiving() == player && event.getSource().isMagicDamage())
        {
            event.setCanceled(true);
        }
    }
}
