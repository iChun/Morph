package me.ichun.mods.morph.common.mob;

import com.google.gson.*;
import me.ichun.mods.morph.api.mob.trait.*;
import me.ichun.mods.morph.api.mob.trait.ability.*;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.util.Util;

import java.lang.reflect.Type;
import java.util.HashMap;

public class TraitHandler
{
    private static final HashMap<String, Class<? extends Trait>> TRAITS = Util.make(new HashMap<>(), m -> {
        //Register the default traits (no fields)
        m.put("traitFallNegate", FallNegateTrait.class);
        m.put("traitFloat", FloatTrait.class);
        m.put("traitHostile", HostileTrait.class);
        m.put("traitImmunityExplosive", ExplosiveImmunityTrait.class);
        m.put("traitImmunityFire", FireImmunityTrait.class);
        m.put("traitImmunityMagic", MagicImmunityTrait.class);
        m.put("traitSink", SinkTrait.class);
        m.put("traitSwimmer", SwimmerTrait.class);
        m.put("traitWaterSensitivity", WaterSensitivityTrait.class);
        m.put("traitUndead", UndeadTrait.class);

        //These have fields
        m.put("traitEffectResistance", EffectResistanceTrait.class);
        m.put("traitImmunityDamageSource", DamageSourceImmunityTrait.class);
        m.put("traitIntimidate", IntimidateTrait.class);
        m.put("traitMoistSkin", MoistSkinTrait.class);
        m.put("traitStepHeight", StepHeightTrait.class);
        m.put("traitSunburn", SunburnTrait.class);
        m.put("traitWaterBreather", WaterBreatherTrait.class);

        //TODO test all MC mobs in Multiplayer
        //TODO strip all NBT by default rather than to select which to strip
        //TODO acquire when you kill a morphed player??
        //TODO add localisation
        //TODO sync potion effects!
        //TODO check chicken item drop (entity item drops outside death)
        //TODO sync wither invul time?

        //Register the default abilities
        m.put("abilityClimb", ClimbAbility.class);

        //These have fields
        m.put("abilityEffectAttack", EffectAttackAbility.class);
        m.put("abilityFlight", FlyAbility.class);
        m.put("abilityFlightFlap", FlightFlapAbility.class);
        m.put("abilityRideable", RideableAbility.class); //TODO test
        m.put("abilitySlowFall", SlowFallAbility.class);
    });

    public static void registerTrait(String type, Class<? extends Trait> clz)
    {
        if(TRAITS.containsKey(type))
        {
            Morph.LOGGER.warn("We already have another Trait of type {} with class {}. This is a mod-level override so we shall acknowledge it. Overriding with class {}", type, TRAITS.get(type).getName(), clz.getName());
        }

        TRAITS.put(type, clz);
    }

    public static class TraitDeserialiser implements JsonDeserializer<Trait>, JsonSerializer<Trait>
    {
        @Override
        public Trait deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return _deserialize(json, typeOfT, context);
        }

        public static Trait _deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        {
            JsonObject jsonObject = (JsonObject)json;
            String type = jsonObject.get("type").getAsString();
            if(TRAITS.containsKey(type))
            {
                Trait trait = ResourceHandler.GSON.fromJson(jsonObject.toString(), TRAITS.get(type));
                if(trait != null)
                {
                    return trait;
                }
                else
                {
                    Morph.LOGGER.error("Invalid trait: " + jsonObject.toString());
                }
            }
            else
            {
                Morph.LOGGER.error("Unknown trait type: " + type);
            }
            return null;
        }

        @Override
        public JsonElement serialize(Trait src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src);
        }
    }
}
