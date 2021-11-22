package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterBreatherTrait extends Trait<WaterBreatherTrait>
        implements IEventBusRequired
{
    public Boolean suffocatesOnLand;

    public transient int air = -100;

    public WaterBreatherTrait()
    {
        type = "traitWaterBreather";
    }

    @Override
    public void tick(float strength)
    {
        if(strength == 1F && player.isAlive())
        {
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
            else if (suffocatesOnLand != null && suffocatesOnLand) //if the player is on land and the entity suffocates
            {
                //taken from decreaseAirSupply in Living Entity
                int i = EnchantmentHelper.getRespirationModifier(player);
                air = i > 0 && player.getRNG().nextInt(i + 1) > 0 ? air : air - 1;

                if(air == -20)
                {
                    air = 0;

                    player.attackEntityFrom(DamageSource.DROWN, 2F);
                }

                player.setAir(air);
            }
        }
    }

    @Override
    public WaterBreatherTrait copy()
    {
        WaterBreatherTrait trait = new WaterBreatherTrait();
        trait.suffocatesOnLand = this.suffocatesOnLand;
        return trait;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.AIR)
        {
            //No need to draw the air bubbles if air < 300, default GUI already does that.
            if(player.areEyesInFluid(FluidTags.WATER) && air >= 300) //player's in water but also max air.
            {
                event.setCanceled(true);
            }
        }
    }
}
