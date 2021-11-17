package me.ichun.mods.morph.common.morph.mode;

import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public interface MorphMode
{
    void handleMurderEvent(ServerPlayerEntity player, LivingEntity living);

    boolean canMorph(PlayerEntity player); //if the player has the ability to morph

    boolean canAcquireMorph(PlayerEntity player, LivingEntity living, @Nullable MorphVariant variant); //NOT FOR BLACKLISTING! Variant creation and acquire morph already checks the blacklist. This is for other reasons eg upgrade related stuff or range related stuff

    int getMorphingDuration(PlayerEntity player);

    boolean hasUnlockedBiomass(PlayerEntity player);

    boolean canAcquireBiomass(PlayerEntity player, LivingEntity living);

    double getBiomassAmount(PlayerEntity player, LivingEntity living);

    boolean isClassicMode();
}
