package me.ichun.mods.morph.api.mixin;

import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir)
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
        if(info.isMorphed())
        {
            cir.setReturnValue(info.getHurtSound(source));
        }

    }

    @Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
    private void getDeathSound(CallbackInfoReturnable<SoundEvent> cir)
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
        if(info.isMorphed())
        {
            cir.setReturnValue(info.getDeathSound());
        }

    }

    @Inject(method = "getFallSound", at = @At("HEAD"), cancellable = true)
    private void getFallSound(int height, CallbackInfoReturnable<SoundEvent> cir)
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
        if(info.isMorphed())
        {
            cir.setReturnValue(info.getFallSound(height));
        }

    }
}
