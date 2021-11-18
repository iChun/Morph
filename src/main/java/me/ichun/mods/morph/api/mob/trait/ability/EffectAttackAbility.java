package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.mob.trait.IEventBusRequired;
import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectAttackAbility extends Ability<EffectAttackAbility>
        implements IEventBusRequired
{
    public String effectId;
    public Integer duration;
    public Integer amplifier;

    public transient Effect effectObj;

    public EffectAttackAbility()
    {
        type = "abilityEffectAttack";
    }

    @Override
    public void addHooks()
    {
        if(effectId != null)
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

    @Override
    public void tick(float strength)
    {
    }

    @Override
    public EffectAttackAbility copy()
    {
        EffectAttackAbility trait = new EffectAttackAbility();
        trait.effectId = this.effectId;
        return trait;
    }

    @Override
    public boolean canTransitionTo(Trait<?> trait)
    {
        if(trait instanceof EffectAttackAbility)
        {
            return effectId != null && effectId.equals(((EffectAttackAbility)trait).effectId);
        }
        return false;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event)
    {
        if(event.getSource().getImmediateSource() == player && MorphApi.getApiImpl().canUseAbility(player, this))
        {
            event.getEntityLiving().addPotionEffect(new EffectInstance(effectObj, duration != null ? duration : 200, amplifier != null ? amplifier: 0));
        }
    }
}
