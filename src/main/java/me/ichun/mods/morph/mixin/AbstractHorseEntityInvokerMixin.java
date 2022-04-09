package me.ichun.mods.morph.mixin;

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractHorseEntity.class)
public interface AbstractHorseEntityInvokerMixin
{
    @Accessor
    Inventory getHorseChest();

    @Invoker
    void callSetHorseWatchableBoolean(int id, boolean flag);
}
