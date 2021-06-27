package me.ichun.mods.morph.api.mixin;

import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
    @Inject(method = "getSoundVolume", at = @At("HEAD"), cancellable = true)
    public void getSoundVolume(CallbackInfoReturnable<Float> cir)
    {
        if(((LivingEntity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                cir.setReturnValue(info.getSoundVolume());
            }
        }
    }

    @Inject(method = "getSoundPitch", at = @At("HEAD"), cancellable = true)
    public void getSoundPitch(CallbackInfoReturnable<Float> cir)
    {
        if(((LivingEntity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                cir.setReturnValue(info.getSoundPitch());
            }
        }
    }

    @Inject(method = "getDrinkSound", at = @At("HEAD"), cancellable = true)
    private void getDrinkSound(ItemStack stack, CallbackInfoReturnable<SoundEvent> cir)
    {
        if(((LivingEntity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                cir.setReturnValue(info.getDrinkSound(stack));
            }
        }
    }

    @Inject(method = "getEatSound", at = @At("HEAD"), cancellable = true)
    private void getEatSound(ItemStack stack, CallbackInfoReturnable<SoundEvent> cir)
    {
        if(((LivingEntity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                cir.setReturnValue(info.getEatSound(stack));
            }
        }
    }
}
