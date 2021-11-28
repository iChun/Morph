package me.ichun.mods.morph.api.event;

import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class MorphEvent extends PlayerEvent
{
    private final MorphVariant variant;
    private MorphEvent(PlayerEntity player, MorphVariant variant)
    {
        super(player);
        this.variant = variant;
    }

    public MorphVariant getVariant()
    {
        return variant;
    }

    @Cancelable
    public static class CanAcquire extends MorphEvent
    {
        public CanAcquire(PlayerEntity player, MorphVariant variant)
        {
            super(player, variant);
        }
    }

    @Cancelable
    public static class Acquire extends MorphEvent
    {
        public Acquire(PlayerEntity player, MorphVariant variant)
        {
            super(player, variant);
        }
    }

    @Cancelable
    public static class Morph extends MorphEvent
    {
        public Morph(PlayerEntity player, MorphVariant variant)
        {
            super(player, variant);
        }
    }
}
