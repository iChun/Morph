package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.entity.MobEntity;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HostileTrait extends Trait<HostileTrait>
    implements IEventBusRequired
{
    public HostileTrait()
    {
        type = "traitHostile";
    }

    @Override
    public void tick(float strength)
    {
    }

    @Override
    public HostileTrait copy()
    {
        return new HostileTrait();
    }

    @SubscribeEvent
    public void onLivingSetTarget(LivingSetAttackTargetEvent event)
    {
        //if the target is the player and it's not the revenge target/entity attacking it, cancel
        if(event.getTarget() == player && event.getEntityLiving() instanceof MobEntity && !(event.getEntityLiving().getRevengeTarget() == player || event.getEntityLiving().getAttackingEntity() == player))
        {
            ((MobEntity)event.getEntityLiving()).setAttackTarget(null);
        }
    }
}
