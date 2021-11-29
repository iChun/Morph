package me.ichun.mods.morph.mixin;

import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin
{
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    public void playStepSound(BlockPos pos, BlockState blockState, CallbackInfo ci)
    {
        if(((Entity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                info.playStepSound(pos, blockState);
                ci.cancel();
            }
        }
    }

    @Inject(method = "playSwimSound", at = @At("HEAD"), cancellable = true)
    public void playSwimSound(float volume, CallbackInfo ci)
    {
        if(((Entity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                info.playSwimSound(volume);
                ci.cancel();
            }
        }
    }

    @Inject(method = "playFlySound", at = @At("HEAD"), cancellable = true)
    private void playFlySound(float volume, CallbackInfoReturnable<Float> cir)
    {
        if(((Entity)(Object)this) instanceof PlayerEntity)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)(Object)this);
            if(info.isMorphed())
            {
                cir.setReturnValue(info.playFlySound(volume));
            }
        }
    }
}
