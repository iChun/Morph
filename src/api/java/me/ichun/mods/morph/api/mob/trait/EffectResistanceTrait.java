package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectResistanceTrait extends Trait<EffectResistanceTrait>
        implements IEventBusRequired
{
    public String effectId;

    public transient Effect effectObj;
    public transient float lastStrength = 0F;

    public EffectResistanceTrait()
    {
        type = "traitEffectResistance";
    }

    @Override
    public void addHooks()
    {
        if(effectId != null)
        {
            if(effectId.equals("*")) //immune to all effects
            {
                super.addHooks();
            }
            else
            {
                ResourceLocation effectRL = new ResourceLocation(effectId);
                Effect theEffect = ForgeRegistries.POTIONS.getValue(effectRL);
                if(theEffect != null)
                {
                    effectObj = theEffect;
                    super.addHooks();
                }
            }
        }
    }

    @Override
    public void tick(float strength)
    {
        lastStrength = strength;

        if(effectObj != null && lastStrength == 1F)
        {
            if(effectId.equals("*")) //immune to all effects
            {
                for(EffectInstance effect : player.getActivePotionEffects())
                {
                    player.removePotionEffect(effect.getPotion());
                }
            }
            else
            {
                EffectInstance potion = player.getActivePotionEffect(effectObj);
                if(potion != null)
                {
                    player.removePotionEffect(effectObj);
                }
            }
        }
    }

    @Override
    public EffectResistanceTrait copy()
    {
        EffectResistanceTrait trait = new EffectResistanceTrait();
        trait.effectId = this.effectId;
        return trait;
    }

    @Override
    public boolean canTransitionTo(Trait<?> trait)
    {
        if(trait instanceof EffectResistanceTrait)
        {
            return effectId != null && effectId.equals(((EffectResistanceTrait)trait).effectId);
        }
        return false;
    }

    @SubscribeEvent
    public void onPotionApplicable(PotionEvent.PotionApplicableEvent event)
    {
        if(lastStrength == 1F && event.getEntityLiving() == player && (effectId != null && effectId.equals("*") || event.getPotionEffect().getPotion() == effectObj))
        {
            event.setResult(Event.Result.DENY);
        }
    }
}
