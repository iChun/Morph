package me.ichun.mods.morph.common.morph;

import com.google.common.base.Splitter;
import me.ichun.mods.morph.api.IApi;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.mode.ClassicMode;
import me.ichun.mods.morph.common.morph.mode.DefaultMode;
import me.ichun.mods.morph.common.morph.mode.MorphMode;
import me.ichun.mods.morph.common.morph.nbt.NbtModifier;
import me.ichun.mods.morph.common.morph.save.MorphSavedData;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MorphHandler implements IApi
{
    public static final HashMap<Class<? extends LivingEntity>, NbtModifier> NBT_MODIFIERS = new HashMap<>();
    public static final Splitter ON_SEMI_COLON = Splitter.on(";").trimResults().omitEmptyStrings();
    public static final ResourceLocation TEX_MORPH_SKIN = new ResourceLocation("morph", "textures/skin/morphskin.png");

    private MorphMode currentMode;
    private MorphSavedData saveData;

    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        currentMode.handleMurderEvent(player, living);
    }

    public void setMorphMode(boolean isClassic)
    {
        if(isClassic)
        {
            currentMode = new ClassicMode();
        }
        else
        {
            currentMode = new DefaultMode();
        }
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
        return saveData.playerMorphs.computeIfAbsent(player.getGameProfile().getId(), k -> new PlayerMorphData(player.getGameProfile().getId()));
    }

    //API overrides
    public static final MorphHandler INSTANCE = new MorphHandler();
    @Override
    @Nonnull
    public MorphInfo getMorphInfo(PlayerEntity player)
    {
        return player.getCapability(MorphInfo.CAPABILITY_INSTANCE).orElseThrow(() -> new IllegalArgumentException("Player " + player.getName().getUnformattedComponentText() + " has no morph state capabilities"));
    }

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        return currentMode != null ? currentMode.canAcquireBiomass(player, living) : IApi.super.canAcquireBiomass(player, living);
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living)
    {
        return currentMode != null ? currentMode.canAcquireMorph(player, living) : IApi.super.canAcquireMorph(player, living);
    }

    @Override
    public MorphVariant createVariant(LivingEntity living)
    {
        for(ResourceLocation rl : Morph.configServer.disabledMobsRL)
        {
            if(rl.equals(living.getType().getRegistryName()))
            {
                return null;
            }
        }

        if(!living.getType().isSerializable())
        {
            return null;
        }

        MorphVariant variant = new MorphVariant(living.getType().getRegistryName());

        if(living instanceof PlayerEntity)
        {
            variant.thisVariant = new MorphVariant.Variant();
            variant.thisVariant.playerUUID = ((PlayerEntity)living).getGameProfile().getId();
        }
        else
        {
            CompoundNBT tag = new CompoundNBT();

            //Write the supported attributes to our Morph NBT
            variant.writeSupportedAttributes(living);

            //write the default info
            variant.writeDefaults(living, tag);

            living.writeAdditional(tag);
            //we have the default info

            variant.writeSpecialTags(living, tag);

            //time to apply the NBT modifiers
            NbtModifier nbtModifier = getNbtModifierFor(living);
            nbtModifier.apply(tag);

            variant.setLiving(tag);

            variant.thisVariant = new MorphVariant.Variant();
        }

        variant.variants.add(variant.thisVariant);

        return variant;
    }

    @Override
    public Map<ResourceLocation, Boolean> getSupportedAttributes()
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

    //NBT Modifier stuff
    public static NbtModifier getNbtModifierFor(LivingEntity living)
    {
        NbtModifier modifier = getNbtModifierFor(living.getClass());

        //we're about to use this modifier. Set up the modifier values
        modifier.setupValues();

        return modifier;
    }

    private static NbtModifier getNbtModifierFor(Class clz)
    {
        NbtModifier modifier;
        if(NBT_MODIFIERS.containsKey(clz))
        {
            modifier = NBT_MODIFIERS.get(clz);
            if(modifier.toStrip != null) // it's been set up;
            {
                return modifier;
            }
        }
        else
        {
            modifier = new NbtModifier();
            NBT_MODIFIERS.put(clz, modifier);
        }

        modifier.toStrip = new HashSet<>();
        modifier.keyToModifier = new HashMap<>();

        if(clz != LivingEntity.class)
        {
            //get the parent class's modifier and add their modifiers
            NbtModifier parentModifier = getNbtModifierFor(clz.getSuperclass());

            modifier.toStrip.addAll(parentModifier.toStrip);
            modifier.keyToModifier.putAll(parentModifier.keyToModifier);
        }

        //setup adds this class' own modifiers.
        modifier.setup();

        return modifier;
    }
}
