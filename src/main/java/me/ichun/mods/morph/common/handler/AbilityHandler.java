package me.ichun.mods.morph.common.handler;

import com.google.gson.Gson;
import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.api.ability.IAbilityHandler;
import me.ichun.mods.morph.common.morph.ability.types.active.AbilityClimb;
import me.ichun.mods.morph.common.morph.ability.types.active.AbilityFloat;
import me.ichun.mods.morph.common.morph.ability.types.passive.AbilityFallNegate;
import me.ichun.mods.morph.common.morph.ability.types.passive.AbilityFireImmunity;
import me.ichun.mods.morph.common.morph.ability.types.passive.AbilityPotionEffect;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;

public class AbilityHandler implements IAbilityHandler
{
    private static final AbilityHandler INSTANCE = new AbilityHandler();

    public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<>();
    public final static HashMap<String, Class<? extends Ability>> stringToClassMap = new HashMap<>();

    public static AbilityHandler getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        Ability.setAbilityHandlerImpl(INSTANCE);
        INSTANCE.registerAbility("potionEffect"       , AbilityPotionEffect.class  );
        INSTANCE.registerAbility("climb"              , AbilityClimb.class         );
        INSTANCE.registerAbility("fallNegate"         , AbilityFallNegate.class    );
        INSTANCE.registerAbility("fireImmunity"	      , AbilityFireImmunity.class  );
        INSTANCE.registerAbility("float"              , AbilityFloat.class         );
    }

    @Override
    public void registerAbility(String name, Class<? extends Ability> clz)
    {
        stringToClassMap.put(name, clz);
    }

    @Override
    public void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability... abilities)
    {
        ArrayList<Ability> abilityList = abilityMap.get(entClass);
        if(abilityList == null)
        {
            abilityList = new ArrayList<>();
            abilityMap.put(entClass, abilityList);
        }
        for(Ability ability : abilities)
        {
            if(ability == null)
            {
                continue;
            }
            boolean added = false;
            if(!stringToClassMap.containsKey(ability.getType()))
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
        ArrayList<Ability> abilityList = abilityMap.get(entClass);
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
        ArrayList<Ability> abilities = getEntityAbilities(entClass);
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
        if(stringToClassMap.containsKey(type))
        {
            try
            {
                return (new Gson()).fromJson(json, stringToClassMap.get(type));
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
            ArrayList<Ability> abilities = abilityMap.get(entClass);
            if(abilities == null)
            {
                Class superClz = entClass.getSuperclass();
                if(superClz != EntityLivingBase.class)
                {
                    abilityMap.put(entClass, getEntityAbilities(superClz));
                    return getEntityAbilities(entClass);
                }
            }
            else
            {
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
