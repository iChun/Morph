package me.ichun.mods.morph.common.morph;

import com.google.common.base.Splitter;
import me.ichun.mods.morph.api.IApi;
import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.api.event.MorphEvent;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.Ability;
import me.ichun.mods.morph.api.morph.AttributeConfig;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.biomass.BiomassUpgradeHandler;
import me.ichun.mods.morph.common.biomass.Upgrades;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.mob.TraitHandler;
import me.ichun.mods.morph.common.mode.MorphMode;
import me.ichun.mods.morph.common.mode.MorphModeType;
import me.ichun.mods.morph.common.morph.nbt.NbtHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtModifier;
import me.ichun.mods.morph.common.morph.save.MorphSavedData;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketAcquisition;
import me.ichun.mods.morph.common.packet.PacketMorphInfo;
import me.ichun.mods.morph.common.packet.PacketUpdateBiomassValue;
import me.ichun.mods.morph.common.packet.PacketUpdateMorph;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class MorphHandler implements IApi
{
    public static final Splitter ON_SEMI_COLON = Splitter.on(";").trimResults().omitEmptyStrings();
    private static final ResourceLocation TEX_MORPH_SKIN = new ResourceLocation("morph", "textures/skin/morphskin.png"); //call the getter.

    private final ArrayList<BiConsumer<LivingEntity, PlayerEntity>> modPlayerMorphSyncConsumers = new ArrayList<>();

    private MorphMode currentMode;
    private MorphSavedData saveData;

    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        currentMode.handleMurderEvent(player, living);
    }

    public void setMorphMode(MorphModeType type)
    {
        currentMode = type.createMode();
    }

    public void setSaveData(MorphSavedData data)
    {
        saveData = data;
    }

    public MorphSavedData getSaveData()
    {
        return saveData;
    }

    public PlayerMorphData getPlayerMorphData(PlayerEntity player)
    {
        if(player.world.isRemote)
        {
            return Morph.eventHandlerClient.morphData;
        }
        return saveData.playerMorphs.computeIfAbsent(player.getGameProfile().getId(), k -> new PlayerMorphData(player.getGameProfile().getId()));
    }

    //API overrides
    public static final MorphHandler INSTANCE = new MorphHandler();

    @Override
    public String getMorphModeName()
    {
        return currentMode != null ? currentMode.getModeName() : IApi.super.getMorphModeName();
    }

    @Override
    public void spawnAnimation(PlayerEntity player, LivingEntity living, boolean isMorphAcquisition)
    {
        Morph.channel.sendTo(new PacketAcquisition(player.getEntityId(), living.getEntityId(), isMorphAcquisition), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
    }

    //Morph overrides
    @Override
    @Nonnull
    public MorphInfo getMorphInfo(PlayerEntity player)
    {
        return player.getCapability(MorphInfo.CAPABILITY_INSTANCE).orElse(new MorphInfo(player)); //if the player entity has no morph capabilities, just send a dummy morph info
    }

    @Override
    public boolean canMorph(PlayerEntity player)
    {
        return currentMode.canMorph(player);
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living)
    {
        return currentMode != null ? currentMode.canAcquireMorph(player, living, createVariant(living)) : IApi.super.canAcquireMorph(player, living);
    }

    @Override
    @Nullable
    public MorphVariant createVariant(LivingEntity living)
    {
        for(ResourceLocation rl : Morph.configServer.disabledMobsRL)
        {
            if(rl.equals(living.getType().getRegistryName()))
            {
                return null;
            }
        }

        boolean isPlayer = living instanceof PlayerEntity;
        if(!living.getType().isSerializable() && !isPlayer)
        {
            return null;
        }

        MorphVariant variant = new MorphVariant(living.getType().getRegistryName());

        if(isPlayer)
        {
            variant.thisVariant = new MorphVariant.Variant();
            variant.thisVariant.playerUUID = ((PlayerEntity)living).getGameProfile().getId();
        }
        else
        {
            CompoundNBT tag = new CompoundNBT();//TODO glint effect for ability??

            //Write the supported attributes to our Morph NBT
            variant.writeSupportedAttributes(living);

            //write the default info
            variant.writeDefaults(living, tag);

            living.writeAdditional(tag);
            //we have the default info

            variant.writeSpecialTags(living, tag);

            //time to apply the NBT modifiers
            NbtModifier nbtModifier = NbtHandler.getModifierFor(living);
            nbtModifier.apply(tag);

            //Clean empty tags
            NbtHandler.removeEmptyCompoundTags(tag);

            variant.setLiving(tag);

            variant.thisVariant = new MorphVariant.Variant();
        }

        return variant;
    }

    @Override
    public boolean acquireMorph(ServerPlayerEntity player, MorphVariant variant)
    {
        PlayerMorphData playerMorphData = MorphHandler.INSTANCE.getPlayerMorphData(player);
        if(!playerMorphData.containsVariant(variant))
        {
            if(Morph.configServer.disabledMobsRL.contains(variant.id)) return false;

            if(MinecraftForge.EVENT_BUS.post(new MorphEvent.Acquire(player, variant))) return false;

            MorphVariant parentVariant = playerMorphData.addVariant(variant);

            Morph.channel.sendTo(new PacketUpdateMorph(parentVariant.write(new CompoundNBT())), player);

            saveData.markDirty();

            return true;
        }
        return false;
    }

    @Override
    public boolean morphTo(ServerPlayerEntity player, MorphVariant variant)
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);

        //mid morph
        if(info.getMorphProgress(1F) < 1F) return false;

        if(MinecraftForge.EVENT_BUS.post(new MorphEvent.Morph(player, variant))) return false;

        info.setNextState(new MorphState(variant, player), Math.max(1, currentMode.getMorphingDuration(player)));

        Morph.channel.sendTo(new PacketMorphInfo(player.getEntityId(), info.write(new CompoundNBT())), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));

        return true;
    }

    @Override
    public boolean demorph(ServerPlayerEntity player)
    {
        MorphVariant variant = MorphVariant.createPlayerMorph(player.getGameProfile().getId(), true);
        variant.thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
        return morphTo(player, variant);
    }

    @Override
    public Map<ResourceLocation, AttributeConfig> getSupportedAttributes()
    {
        return Morph.configServer.supportedAttributesMap;
    }

    @Nullable
    @Override
    public LivingEntity getActiveMorphEntity(PlayerEntity player)
    {
        return getMorphInfo(player).getActiveMorphEntity();
    }

    @Nonnull
    @Override
    public ResourceLocation getMorphSkinTexture()
    {
        return TEX_MORPH_SKIN;
    }

    @Override
    public List<BiConsumer<LivingEntity, PlayerEntity>> getModPlayerMorphSyncConsumers()
    {
        return modPlayerMorphSyncConsumers;
    }

    @Override
    public void registerMobData(@Nonnull ResourceLocation rl, @Nonnull MobData data)
    {
        MobDataHandler.registerMobData(rl, data);
    }

    @Override
    public void registerTrait(@Nonnull String type, @Nonnull Class<? extends Trait> clz)
    {
        TraitHandler.registerTrait(type, clz);
    }

    @Override
    public ArrayList<Trait<?>> getTraitsForVariant(MorphVariant variant, PlayerEntity player)
    {
        return currentMode != null ? currentMode.getTraitsForVariant(player, variant) : IApi.super.getTraitsForVariant(variant, player);
    }

    @Override
    public boolean canUseAbility(PlayerEntity player, Ability<?> ability)
    {
        return currentMode != null ? currentMode.canUseAbility(player, ability) : IApi.super.canUseAbility(player, ability);
    }

    //Biomass overrides
    @Override
    public boolean hasUnlockedBiomass(PlayerEntity player)
    {
        return currentMode != null ? currentMode.hasUnlockedBiomass(player) : IApi.super.hasUnlockedBiomass(player);
    }

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        return currentMode != null ? currentMode.canAcquireBiomass(player, living) : IApi.super.canAcquireBiomass(player, living);
    }

    @Override
    public double getBiomassAmount(PlayerEntity player, LivingEntity living)
    {
        return currentMode != null ? currentMode.getBiomassAmount(player, living) :  IApi.super.getBiomassAmount(player, living);
    }

    @Nullable
    @Override
    public BiomassUpgradeInfo getBiomassUpgradeInfo(@Nullable String entityId, String id)
    {
        if(entityId == null)
        {
            return BiomassUpgradeHandler.BIOMASS_UPGRADES.get(id);
        }
        return null;
    }

    @Nullable
    @Override
    public BiomassUpgrade getBiomassUpgrade(PlayerEntity player, String id)
    {
        return getPlayerMorphData(player).getBiomassUpgrade(id);
    }

    public double getBiomassUpgradeValue(PlayerEntity player, String id)
    {
        BiomassUpgrade biomassUpgrade = getBiomassUpgrade(player, id);
        if(biomassUpgrade != null)
        {
            return biomassUpgrade.getValue();
        }
        return 0D;
    }

    public void setBiomassAmount(ServerPlayerEntity player, double value)
    {
        PlayerMorphData playerMorphData = getPlayerMorphData(player);
        playerMorphData.biomass = value;
        saveData.markDirty();

        Morph.channel.sendTo(new PacketUpdateBiomassValue(playerMorphData.biomass), player);
    }

    public void addBiomassAmount(ServerPlayerEntity player, double value)
    {
        PlayerMorphData playerMorphData = getPlayerMorphData(player);
        double cap = getBiomassUpgradeValue(player, Upgrades.ID_BIOMASS_CAPACITY) + getBiomassUpgradeValue(player, Upgrades.ID_BIOMASS_CRITICAL_CAPACITY);
        if(playerMorphData.biomass + value > cap)
        {
            value = cap - playerMorphData.biomass;
        }

        playerMorphData.biomass += value;

        saveData.markDirty();

        Morph.channel.sendTo(new PacketUpdateBiomassValue(playerMorphData.biomass), player);
    }

    //TODO a use biomass function
}
