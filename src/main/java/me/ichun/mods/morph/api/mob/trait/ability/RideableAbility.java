package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.mob.trait.IEventBusRequired;
import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEquipable;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RideableAbility extends Ability<RideableAbility>
        implements IEventBusRequired
{
    public Boolean requiresSaddle;

    public transient float lastStrength;

    public RideableAbility()
    {
        type = "abilityRideable";
    }

    @Override
    public void tick(float strength)
    {
        if(lastStrength == 1F && strength != 1F || player.isSneaking()) //demorphing
        {
            player.removePassengers();
        }
        else if(livingInstance != null)
        {
            for(Entity passenger : player.getPassengers())
            {
                livingInstance.updatePassenger(passenger);
            }
        }
        lastStrength = strength;
    }

    @Override
    public void transitionalTick(RideableAbility prevTrait, float transitionProgress)
    {
        lastStrength = 1F;
        super.transitionalTick(prevTrait, transitionProgress);
    }

    @Override
    public boolean canTransitionTo(Trait<?> trait)
    {
        if(trait instanceof RideableAbility)
        {
            return requiresSaddle == ((RideableAbility)trait).requiresSaddle;
        }
        return false;
    }

    @Override
    public RideableAbility copy()
    {
        RideableAbility ability = new RideableAbility();
        ability.requiresSaddle = this.requiresSaddle;
        return ability;
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        if(lastStrength == 1F && event.getPlayer().ridingEntity == null && event.getTarget() == player && event.getTarget().getPassengers().isEmpty())
        {
            if((!(requiresSaddle != null && requiresSaddle) || livingInstance instanceof IEquipable && ((IEquipable)livingInstance).isHorseSaddled()) && event.getPlayer().startRiding(player))
            {
                event.setCancellationResult(ActionResultType.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
