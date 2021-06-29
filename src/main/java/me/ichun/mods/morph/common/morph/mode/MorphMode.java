package me.ichun.mods.morph.common.morph.mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface MorphMode
{
    void handleMurderEvent(ServerPlayerEntity player, LivingEntity living);

    boolean canMorph(PlayerEntity player);

    boolean canAcquireMorph(PlayerEntity player, LivingEntity living);

    int getMorphingDuration(PlayerEntity player);

    boolean canAcquireBiomass(PlayerEntity player, LivingEntity living);
}
