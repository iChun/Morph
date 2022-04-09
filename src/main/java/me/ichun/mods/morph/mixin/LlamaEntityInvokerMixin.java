package me.ichun.mods.morph.mixin;

import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LlamaEntity.class)
public interface LlamaEntityInvokerMixin
{
    @Invoker
    void callSetColor(DyeColor color);
}
