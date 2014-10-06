package morph.common.ability;

import morph.api.Ability;
import morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;

public class AbilityHandler
{

    public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
    public final static HashMap<String, Class<? extends Ability>> stringToClassMap = new HashMap<String, Class<? extends Ability>>();
    public final static ArrayList<Class<? extends EntityLivingBase>> abilityClassList = new ArrayList<Class<? extends EntityLivingBase>>();

    static
    {
        registerAbility("climb"			  , AbilityClimb.class			    );
        registerAbility("fallNegate"	  , AbilityFallNegate.class		    );
        registerAbility("fear"	          , AbilityFear.class		        );
        registerAbility("fly"			  , AbilityFly.class			    );
        registerAbility("float"			  , AbilityFloat.class			    );
        registerAbility("fireImmunity"	  , AbilityFireImmunity.class	    );
        registerAbility("hostile"		  , AbilityHostile.class		    );
        registerAbility("poisonResistance", AbilityPoisonResistance.class   );
        registerAbility("potionEffect"    , AbilityPotionEffect.class       );
        registerAbility("sink"			  , AbilitySink.class			    );
        registerAbility("step"			  , AbilityStep.class			    );
        registerAbility("sunburn"		  , AbilitySunburn.class		    );
        registerAbility("swim"			  , AbilitySwim.class			    );
        registerAbility("waterAllergy"    , AbilityWaterAllergy.class	    );
        registerAbility("witherResistance", AbilityWitherResistance.class   );
    }

    public static void registerAbility(String name, Class<? extends Ability> clz)
    {
        stringToClassMap.put(name, clz);
    }

    public static void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities)
    {
        ArrayList<Ability> abilityList = abilityMap.get(entClass);
        if(abilityList == null)
        {
            abilityList = new ArrayList<Ability>();
            abilityMap.put(entClass, abilityList);
            if(!abilityClassList.contains(entClass))
            {
                abilityClassList.add(entClass);
            }
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
                Morph.console("Ability type \"" + ability.getType() + "\" is not registered! Registering.", true);
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

    public static void removeAbility(Class<? extends EntityLivingBase> entClass, String type)
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

    public static boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type)
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

    public static ArrayList<Ability> getEntityAbilities(Class<? extends EntityLivingBase> entClass)
    {
        if(Morph.config.getSessionInt("abilities") == 1)
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
                String[] disabledAbilities = Morph.config.getSessionString("disabledAbilities").split(",");
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
        return new ArrayList<Ability>();
    }

    public static Ability createNewAbilityByType(String type, String[] arguments)
    {
        try
        {
            Class abilityClass = AbilityHandler.stringToClassMap.get(type);
            if(abilityClass != null)
            {
                Ability ab = ((Ability)abilityClass.getConstructor().newInstance());
                try
                {
                    ab.parse(arguments);
                }
                catch(Exception e2)
                {
                    Morph.console("Arguments are erroring when trying to create ability by type: "  + abilityClass.getName() + ", "+ type + ", args: " + arguments == null ? "none" : "arg list of size " + arguments.length, true);
                    e2.printStackTrace();
                }
                return ab;
            }
            else
            {
                Morph.console("Ability type \"" + type + "\" does not exist!", true);
            }
        }
        catch(Exception e)
        {
            Morph.console("Error creating ability by type: " + type, true);
            e.printStackTrace();
        }
        return null;
    }
}
