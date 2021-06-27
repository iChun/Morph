package me.ichun.mods.morph.common.morph.mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface MorphMode
{
    void handleMurderEvent(ServerPlayerEntity player, LivingEntity living);

    boolean canAcquireBiomass(PlayerEntity player, LivingEntity living);

    boolean canAcquireMorph(PlayerEntity player, LivingEntity living);

    int getMorphingDuration(PlayerEntity player);
}
