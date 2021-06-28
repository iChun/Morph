package me.ichun.mods.morph.common.config;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigServer extends ConfigBase
{
    @CategoryDivider(name = "morph")
    @Prop(min = 0)
    public int morphTime = 100; // 5 seconds

    private List<String> disabledMobs = new ArrayList<>();

    private List<String> supportedAttributes = Util.make(new ArrayList<>(), list -> {
        list.add("minecraft:generic.max_health;more");
        list.add("minecraft:generic.knockback_resistance;more");
        list.add("minecraft:generic.movement_speed;more");
        list.add("minecraft:generic.attack_damage;more");
        list.add("minecraft:generic.attack_knockback;more");
        list.add("minecraft:generic.attack_speed;more");
        list.add("minecraft:generic.armor;more");
        list.add("minecraft:generic.luck;more");
        list.add("minecraft:horse.jump_strength;more");
        list.add("forge:swim_speed;more");
        list.add("forge:reach_distance;more");
    });

    public boolean morphClassic = false;

    public boolean aggressiveSizeRecalculation = false;

    //======================================================//

    public transient ArrayList<ResourceLocation> disabledMobsRL = new ArrayList<>();
    public transient HashMap<ResourceLocation, Boolean> supportedAttributesMap = new HashMap<>();


    @Override
    public void onConfigLoaded()
    {
        if(EffectiveSide.get().isClient() && (ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().isSinglePlayer())) //we're on single player, let's not reload the pool.
        {
            return;
        }

        MorphHandler.INSTANCE.setMorphMode(morphClassic);

        parseDisabledMobs();

        parseSupportedAttributes();
    }

    private void parseDisabledMobs()
    {
        disabledMobsRL.clear();

        for(String disabledMob : disabledMobs)
        {
            disabledMobsRL.add(new ResourceLocation(disabledMob));
        }
    }

    private void parseSupportedAttributes()
    {
        supportedAttributesMap.clear();

        for(String supportedAttribute : supportedAttributes)
        {
            List<String> split = MorphHandler.ON_SEMI_COLON.splitToList(supportedAttribute);
            if(split.size() != 2)
            {
                Morph.LOGGER.error("Error parsing supported attribute config: {}", supportedAttribute);
                continue;
            }

            supportedAttributesMap.put(new ResourceLocation(split.get(0)), split.get(1).equalsIgnoreCase("more"));
        }
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Morph.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Morph.MOD_NAME;
    }
}
