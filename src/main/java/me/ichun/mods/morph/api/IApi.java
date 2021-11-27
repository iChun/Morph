package me.ichun.mods.morph.api;

import com.google.common.collect.ImmutableList;
import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.Ability;
import me.ichun.mods.morph.api.morph.AttributeConfig;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public interface IApi
{
    //Mod GameMode
    @Nonnull
    default String getMorphModeName() { return ""; }

    //Shared stuff
    default void spawnAnimation(PlayerEntity player, LivingEntity living, boolean isMorphAcquisition) {}

    //Morph Stuff
    @Nullable
    default MorphInfo getMorphInfo(PlayerEntity player)
    {
        return null;
    }

    @Nullable
    default LivingEntity getActiveMorphEntity(PlayerEntity player) { return null; }

    default boolean isEntityAMorph(LivingEntity living) { return false; }

    @Nullable
    default UUID getUuidOfPlayerForMorph(LivingEntity living) { return null; }

    default boolean canMorph(PlayerEntity player) { return false; }

    default boolean canAcquireMorph(PlayerEntity player, LivingEntity living)
    {
        //Checks if the situation is ideal to acquire the morph, not exclusively for if the player can morph to the entity. Cancel acquiring with the MorphEvent.CanAcquire event
        return false;
    }

    @Nullable
    default MorphVariant createVariant(LivingEntity living)
    {
        return null;
    }

    default boolean acquireMorph(ServerPlayerEntity player, MorphVariant variant) { return false; }

    default boolean morphTo(ServerPlayerEntity player, MorphVariant variant) { return false; }

    default boolean demorph(ServerPlayerEntity player) { return true; }

    default Map<ResourceLocation, AttributeConfig> getSupportedAttributes() { return Collections.emptyMap(); }

    @Nullable
    default ResourceLocation getMorphSkinTexture() { return null; }

    default List<BiConsumer<LivingEntity, PlayerEntity>> getModPlayerMorphSyncConsumers() { return ImmutableList.of(); }

    //Mob Data Stuff
    default void registerMobData(@Nonnull ResourceLocation rl, @Nonnull MobData data) {}

    default void registerTrait(@Nonnull String type, @Nonnull Class<? extends Trait> clz) {}

    @Nonnull
    default ArrayList<Trait<?>> getTraitsForVariant(MorphVariant variant, PlayerEntity player) { return new ArrayList<>(); }

    default boolean canUseAbility(PlayerEntity player, Ability<?> ability) { return false; }

    //Biomass Stuff
    default boolean hasUnlockedBiomass(PlayerEntity player)
    {
        return false;
    }

    default boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        return false;
    }

    default double getBiomassAmount(PlayerEntity player, LivingEntity living) { return 0D; }


    //Biomass Upgrade Info
    @Nullable
    default BiomassUpgradeInfo getBiomassUpgradeInfo(String entityId, String id) { return null; }

    @Nullable
    default BiomassUpgrade getBiomassUpgrade(PlayerEntity player, String id) { return null; } //null means no upgrade.
}
