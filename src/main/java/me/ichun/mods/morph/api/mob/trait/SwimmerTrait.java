package me.ichun.mods.morph.api.mob.trait;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;
import java.util.UUID;

public class SwimmerTrait extends Trait<SwimmerTrait>
        implements IEventBusRequired
{
    public Float swimMultiplier;
    public Float landMultiplier;
    public Boolean doNotAffectFog;

    public transient float lastStrength = 0F;
    public transient Random rand = new Random();
    public transient float lastSwimMul = 1F;
    public transient boolean doNotRemoveAttribute;

    public SwimmerTrait()
    {
        type = "traitSwimmer";
    }

    @Override
    public void addHooks()
    {
        if(!(doNotAffectFog != null && doNotAffectFog))
        {
            super.addHooks();
        }

        if(swimMultiplier == null)
        {
            swimMultiplier = 1F;
        }

        if(landMultiplier == null)
        {
            landMultiplier = 1F;
        }
    }

    @Override
    public void removeHooks()
    {
        super.removeHooks();

        if(!doNotRemoveAttribute)
        {
            setSwimAttribute(1F);
        }
    }

    @Override
    public void tick(float strength)
    {
        lastStrength = strength;

        if(swimMultiplier != 0F)
        {
            if(player.isInWaterOrBubbleColumn())
            {
                setSwimAttribute(1F + ((swimMultiplier - 1F) * strength));
            }
        }

        if(landMultiplier != 0F)
        {
            if(!player.isInWaterOrBubbleColumn() && player.isOnGround())
            {
                multiplyMotion(1F + ((landMultiplier - 1F) * strength));
            }
        }
    }

    @Override
    public void transitionalTick(SwimmerTrait prevTrait, float transitionProgress)
    {
        prevTrait.doNotRemoveAttribute = true;

        float swimMul = MathHelper.lerp(transitionProgress, prevTrait.swimMultiplier, swimMultiplier);
        if(swimMul != 0F)
        {
            if(player.isInWaterOrBubbleColumn())
            {
                setSwimAttribute(swimMul);
            }
        }

        float landMul = MathHelper.lerp(transitionProgress, prevTrait.landMultiplier, landMultiplier);
        if(landMul != 0F)
        {
            if(!player.isInWaterOrBubbleColumn() && player.isOnGround())
            {
                multiplyMotion(landMul);
            }
        }
    }

    public void setSwimAttribute(float mul)
    {
        if(player.world.isRemote)
        {
            return;
        }

        final ModifiableAttributeInstance playerAttribute = player.getAttribute(ForgeMod.SWIM_SPEED.get());
        if(playerAttribute != null)
        {
            if(lastSwimMul != mul)
            {
                lastSwimMul = mul;

                rand.setSeed(Math.abs("MorphAttr".hashCode() * 1231543 + "traitSwimmer".hashCode() * 268));
                UUID uuid = MathHelper.getRandomUUID(rand);

                //you can't reapply the same modifier, so lets remove it
                playerAttribute.removePersistentModifier(uuid);

                if(mul != 1F)
                {
                    playerAttribute.applyPersistentModifier(new AttributeModifier(uuid, "MorphAttributeModifier:traitSwimmer", mul, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }

    public void multiplyMotion(float mul)
    {
        player.setMotion(player.getMotion().mul(mul, mul, mul));
    }

    @Override
    public boolean canTransitionTo(Trait<?> trait)
    {
        if(trait instanceof SwimmerTrait)
        {
            return doNotAffectFog == ((SwimmerTrait)trait).doNotAffectFog;
        }
        return false;
    }

    @Override
    public SwimmerTrait copy()
    {
        SwimmerTrait trait = new SwimmerTrait();
        trait.swimMultiplier = this.swimMultiplier;
        trait.landMultiplier = this.landMultiplier;
        trait.doNotAffectFog = this.doNotAffectFog;
        return trait;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event)
    {
        if(event.getInfo().getRenderViewEntity() == player)
        {
            FluidState fluidstate = event.getInfo().getFluidState();
            Entity entity = event.getInfo().getRenderViewEntity();

            //Taken from FogRenderer.setupFog

            //If the camera view is in water
            if (fluidstate.isTagged(FluidTags.WATER))
            {
                float fogDensity = 0.05F;

                if (entity instanceof ClientPlayerEntity) {
                    ClientPlayerEntity clientplayerentity = (ClientPlayerEntity)entity;
                    fogDensity -= clientplayerentity.getWaterBrightness() * clientplayerentity.getWaterBrightness() * 0.03F;
                    Biome biome = clientplayerentity.world.getBiome(clientplayerentity.getPosition());
                    if (biome.getCategory() == Biome.Category.SWAMP) {
                        fogDensity += 0.005F;
                    }
                }

                fogDensity *= 1F + (-0.5F * lastStrength);

                event.setDensity(fogDensity);
                RenderSystem.fogMode(GlStateManager.FogMode.EXP2);

                event.setCanceled(true);
            }
            else if(landMultiplier < 1F && !fluidstate.isTagged(FluidTags.LAVA)) //if there is a <1 land multiplier and you are on land
            {
                float farPlaneDistance = event.getType() == FogRenderer.FogType.FOG_SKY ? event.getRenderer().getFarPlaneDistance() : Math.max(event.getRenderer().getFarPlaneDistance() - 16.0F, 32.0F);
                float f1 = MathHelper.lerp(lastStrength, farPlaneDistance, 5.0F);
                float f2;
                float f3;
                if (event.getType() == FogRenderer.FogType.FOG_SKY) {
                    f2 = 0.0F;
                    f3 = f1 * 0.8F;
                } else {
                    f2 = f1 * 0.25F;
                    f3 = f1;
                }

                RenderSystem.fogStart(f2);
                RenderSystem.fogEnd(f3);
                RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
                RenderSystem.setupNvFogDistance();
                net.minecraftforge.client.ForgeHooksClient.onFogRender(event.getType(), event.getInfo(), (float)event.getRenderPartialTicks(), f3);

                event.setDensity(0F);
                event.setCanceled(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event)
    {
        if(event.getInfo().getRenderViewEntity() == player)
        {
            FluidState fluidstate = event.getInfo().getFluidState();
            if (fluidstate.isTagged(FluidTags.WATER))
            {
                float multi = 1F + 4F * lastStrength;
                event.setRed(event.getRed() * multi);
                event.setBlue(event.getBlue() * multi);
                event.setGreen(event.getGreen() * multi);
            }
        }
    }
}
