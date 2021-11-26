package me.ichun.mods.morph.common.config;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.morph.api.morph.AttributeConfig;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigServer extends ConfigBase
{
    @CategoryDivider(name = "morph")
    @Prop(min = 1) //1 second so that morphs can complete
    public int morphTime = 100; // 5 seconds

    public boolean aggressiveSizeRecalculation = false;

    private List<String> supportedAttributes = Util.make(new ArrayList<>(), list -> {
        list.add("minecraft:generic.max_health;more;20");
        list.add("minecraft:generic.knockback_resistance;more");
        list.add("minecraft:generic.movement_speed;more;0.1");
        list.add("minecraft:generic.attack_damage;more");
        list.add("minecraft:generic.attack_knockback;more");
        list.add("minecraft:generic.attack_speed;more");
        list.add("minecraft:generic.armor;more");
        list.add("minecraft:generic.luck;more");
        list.add("minecraft:horse.jump_strength;more");

        //Taken from ForgeMod
        list.add("forge:swim_speed;more");
        list.add("forge:reach_distance;more");
    });

    public List<String> disabledTraits = new ArrayList<>();

    @CategoryDivider(name = "biomass")
    @Prop(min = 0)
    public double biomassValue = 0.3D; //how much of the space the entity takes up to actually consider as biomass. Also essentially a configurable ratio

    public boolean biomassBypassAdvancement = false;

    @CategoryDivider(name = "gameplay")
    private List<String> disabledMobs = Util.make(new ArrayList<>(), list -> {
        list.add("minecraft:armor_stand");
    });

    public boolean morphClassic = false;

    public boolean biomassSkinWhilstInvisible = true;

    //======================================================//

    public transient ArrayList<ResourceLocation> disabledMobsRL = new ArrayList<>();
    public transient HashMap<ResourceLocation, AttributeConfig> supportedAttributesMap = new HashMap<>();

    @Override
    public void onConfigLoaded()
    {
        if(EffectiveSide.get().isClient() && (ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().isSinglePlayer())) //we're on single player, let's not reload the pool.
        {
            return;
        }

        MorphHandler.INSTANCE.setMorphMode(true || morphClassic); //TODO force true if I want to lock to classic mode

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
            if(split.size() < 2)
            {
                Morph.LOGGER.error("Error parsing supported attribute config: {}", supportedAttribute);
                continue;
            }

            ResourceLocation rl = new ResourceLocation(split.get(0));
            boolean more = split.get(1).equalsIgnoreCase("more");
            Double cap = null;

            if(split.size() == 3)
            {
                try
                {
                    cap = Double.parseDouble(split.get(2));
                }
                catch(NumberFormatException e)
                {
                    Morph.LOGGER.error("Error parsing supported attribute config, invalid cap: {}", supportedAttribute);
                }
            }

            supportedAttributesMap.put(rl, new AttributeConfig(more, cap));
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

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.SERVER;
    }
}
