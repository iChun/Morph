package me.ichun.mods.morph.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvokerMixin
{
    @Invoker
    SoundEvent callGetHurtSound(DamageSource source);

    @Invoker
    SoundEvent callGetDeathSound();

    //Are these sounds really necessary?
    @Invoker
    SoundEvent callGetFallSound(int height);

    @Invoker
    SoundEvent callGetDrinkSound(ItemStack stack);

    @Invoker
    float callGetSoundVolume();

    @Invoker
    float callGetSoundPitch();
}
