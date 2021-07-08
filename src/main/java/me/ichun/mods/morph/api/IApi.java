package me.ichun.mods.morph.api;

import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public interface IApi
{
    //Shared stuff
    default void spawnAnimation(PlayerEntity player, LivingEntity living, boolean isMorphAcquisition) {} //TODO this

    //Morph Stuff
    @Nullable
    default MorphInfo getMorphInfo(PlayerEntity player)
    {
        return null;
    }

    @Nullable
    default LivingEntity getActiveMorphEntity(PlayerEntity player) { return null; }

    default boolean canMorph(PlayerEntity player) { return false; }

    default boolean canAcquireMorph(PlayerEntity player, LivingEntity living)
    {
        //Checks if the situation is ideal to acquire the morph, not exclusively for if the player can morph to the entity. Cancel acquiring the AcquireMorphEvent, triggered in acquireMorph below
        return false;
    }

    @Nullable
    default MorphVariant createVariant(LivingEntity living)
    {
        return null;
    }

    default void acquireMorph(ServerPlayerEntity player, MorphVariant variant){}

    default boolean morphTo(ServerPlayerEntity player, MorphVariant variant) { return false; }

    default boolean demorph(ServerPlayerEntity player) { return true; } //TODO this

    default Map<ResourceLocation, Boolean> getSupportedAttributes() { return Collections.emptyMap(); }

    @Nullable
    default ResourceLocation getMorphSkinTexture() { return null; }


    //Biomass Stuff
    default boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        return false;
    }

    @Nullable
    default BiomassUpgradeInfo getBiomassUpgradeInfo(String id) { return null; }

    @Nullable
    default BiomassUpgrade getBiomassUpgrade(PlayerEntity player, String id) { return null; } //null means no upgrade.
}
