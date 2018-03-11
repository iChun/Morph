package me.ichun.mods.morph.common.handler;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.api.ability.AbilityApi;
import me.ichun.mods.morph.api.ability.IAbilityHandler;
import me.ichun.mods.morph.api.ability.type.*;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;

import java.util.ArrayList;
import java.util.HashMap;

public class AbilityHandler implements IAbilityHandler
{
    private static final AbilityHandler INSTANCE = new AbilityHandler();

    public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> ABILITY_MAP = new HashMap<>();
    public final static HashMap<String, Class<? extends Ability>> STRING_TO_CLASS_MAP = new HashMap<>();

    public static AbilityHandler getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        AbilityApi.setApiImpl(INSTANCE);
        INSTANCE.registerAbility("float", AbilityFloat.class);
        INSTANCE.registerAbility("flightFlap", AbilityFlightFlap.class);
        INSTANCE.registerAbility("hostile", AbilityHostile.class);
        INSTANCE.registerAbility("fallNegate", AbilityFallNegate.class);
        INSTANCE.registerAbility("climb", AbilityClimb.class);

        //TODO REMOVE THESE LINES
        INSTANCE.mapAbilities(EntityBat.class, new AbilityFlightFlap());
        INSTANCE.mapAbilities(EntityChicken.class, new AbilityFloat());
        INSTANCE.mapAbilities(EntityCreeper.class, new AbilityHostile());
        INSTANCE.mapAbilities(EntityGiantZombie.class, new AbilityHostile());
        INSTANCE.mapAbilities(EntityGolem.class, new AbilityFallNegate());
        INSTANCE.mapAbilities(EntitySilverfish.class, new AbilityHostile());
        INSTANCE.mapAbilities(EntitySpider.class, new AbilityClimb(), new AbilityHostile());
    }

    @Override
    public void registerAbility(String name, Class<? extends Ability> clz)
    {
        STRING_TO_CLASS_MAP.put(name, clz);
    }

    @Override
    public void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability... abilities)
    {
        ArrayList<Ability> abilityList = ABILITY_MAP.computeIfAbsent(entClass, k -> new ArrayList<>());
        for(Ability ability : abilities)
        {
            if(ability == null)
            {
                continue;
            }
            boolean added = false;
            if(!STRING_TO_CLASS_MAP.containsKey(ability.getType()))
            {
                registerAbility(ability.getType(), ability.getClass());
                Morph.LOGGER.warn("Ability type \"" + ability.getType() + "\" is not registered! Registering.");
            }
            for(int i = 0; i < abilityList.size(); i++)
            {
                Ability ab = abilityList.get(i);
                if(ab.getType().equals(ability.getType()))
                {
                    abilityList.remove(i);
                    abilityList.add(i, ability);
                    added = true;
                }
            }
            if(!added)
            {
                abilityList.add(ability);
            }
        }
    }

    @Override
    public void removeAbility(Class<? extends EntityLivingBase> entClass, String type)
    {
        ArrayList<Ability> abilityList = ABILITY_MAP.get(entClass);
        if(abilityList != null)
        {
            for(int i = abilityList.size() - 1; i >= 0; i--)
            {
                Ability ability = abilityList.get(i);
                if(ability.getType().equalsIgnoreCase(type))
                {
                    abilityList.remove(i);
                }
            }
        }
    }

    @Override
    public boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type)
    {
        return hasAbility(getEntityAbilities(entClass), type);
    }

    public boolean hasAbility(ArrayList<Ability> abilities, String type)
    {
        for(Ability ability : abilities)
        {
            if(ability.getType().equalsIgnoreCase(type))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Ability createNewAbilityByType(String type, String json)
    {
        if(STRING_TO_CLASS_MAP.containsKey(type))
        {
            try
            {
                return AbilityApi.GSON.fromJson(json, STRING_TO_CLASS_MAP.get(type));
            }
            catch(Exception e)
            {
                Morph.LOGGER.warn("Error creating ability of type \"" + type + "\" with json: " + json);
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            Morph.LOGGER.warn("Error creating ability of type \"" + type + "\". Such ability type is not registered.");
            return null;
        }
    }

    @Override
    public ArrayList<Ability> getEntityAbilities(Class<? extends EntityLivingBase> entClass)
    {
        if(Morph.config.abilities == 1)
        {
            ArrayList<Ability> abilitiesDefault = ABILITY_MAP.get(entClass);
            if(abilitiesDefault == null)
            {
                Class superClz = entClass.getSuperclass();
                if(superClz != EntityLivingBase.class)
                {
                    ABILITY_MAP.put(entClass, getEntityAbilities(superClz));
                    return getEntityAbilities(entClass);
                }
            }
            else
            {
                ArrayList<Ability> abilities = new ArrayList<>();
                for(Ability ability : abilitiesDefault)
                {
                    abilities.add(ability.clone());
                }
                String[] disabledAbilities = Morph.config.disabledAbilities;
                for(int i = abilities.size() - 1; i >= 0 ; i--)
                {
                    Ability ab = abilities.get(i);
                    for(String s : disabledAbilities)
                    {
                        if(!s.isEmpty() && ab.getType().equals(s))
                        {
                            abilities.remove(i);
                            break;
                        }
                    }
                }
                return abilities;
            }
        }
        return new ArrayList<>();
    }
}
