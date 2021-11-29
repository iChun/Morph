package me.ichun.mods.morph.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvokerMixin
{
    @Invoker
    void callPlayStepSound(BlockPos pos, BlockState blockState);

    @Invoker
    void callPlaySwimSound(float volume);

    @Invoker
    float callPlayFlySound(float volume);
}
