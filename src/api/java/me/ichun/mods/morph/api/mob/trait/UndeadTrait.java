package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UndeadTrait extends Trait<UndeadTrait>
        implements IEventBusRequired
{
    public transient float lastStrength = 0F;
    public transient int air = -100;

    public UndeadTrait()
    {
        type = "traitUndead";
    }

    @Override
    public void tick(float strength)
    {
        lastStrength = strength;

        if(lastStrength == 1F)
        {
            //Remove potions
            EffectInstance potion = player.getActivePotionEffect(Effects.REGENERATION);
            if(potion != null)
            {
                player.removePotionEffect(Effects.REGENERATION);
            }
            potion = player.getActivePotionEffect(Effects.POISON);
            if(potion != null)
            {
                player.removePotionEffect(Effects.POISON);
            }


            //Breathe underwater
            if(air == -100)
            {
                air = player.getAir();
            }

            //if the player is in water, add air
            if (player.areEyesInFluid(FluidTags.WATER))
            {
                //Taken from determineNextAir in LivingEntity
                air = Math.min(air + 4, player.getMaxAir());
                player.setAir(air);
            }

        }
    }

    @Override
    public UndeadTrait copy()
    {
        return new UndeadTrait();
    }

    @SubscribeEvent
    public void onPotionApplicable(PotionEvent.PotionApplicableEvent event)
    {
        if(lastStrength == 1F && event.getEntityLiving() == player && (event.getPotionEffect().getPotion() == Effects.REGENERATION || event.getPotionEffect().getPotion() == Effects.POISON))
        {
            event.setResult(Event.Result.DENY);
        }
    }
}
