package morph.common.ability;

import java.util.ArrayList;
import java.util.HashMap;

import morph.api.Ability;
import morph.common.core.SessionState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySquid;

public class AbilityHandler 
{

	public final static String[] trackableAbilities = new String[] { "fly", "float", "swim", "fireImmunity" };
	private final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
	private final static HashMap<String, Class> stringToClassMap = new HashMap<String, Class>();
	
	static
	{
		registerAbility("climb"			, AbilityClimb.class		);
		registerAbility("fly"			, AbilityFly.class			);
		registerAbility("float"			, AbilityFloat.class		);
		registerAbility("fireImmunity"	, AbilityFireImmunity.class	);
		registerAbility("swim"			, AbilitySwim.class			);
		registerAbility("waterAllergy"	, AbilityWaterAllergy.class	);
		
		//TODO complete for vanilla mobs
		mapAbilities(EntityBat.class, new AbilityFly());
		mapAbilities(EntityBlaze.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityChicken.class, new AbilityFloat(-0.1141748D, true));
		mapAbilities(EntityCreeper.class, new AbilityHostile());
		mapAbilities(EntityDragon.class, new AbilityFly(), new AbilityHostile());
		mapAbilities(EntityEnderman.class, new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityGhast.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityGiantZombie.class, new AbilityHostile());
		mapAbilities(EntityMagmaCube.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityPigZombie.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntitySilverfish.class, new AbilityHostile());
		mapAbilities(EntitySkeleton.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntitySlime.class, new AbilityHostile());
		mapAbilities(EntitySnowman.class, new AbilityWaterAllergy());
		mapAbilities(EntitySpider.class, new AbilityClimb(), new AbilityHostile());
		mapAbilities(EntitySquid.class, new AbilitySwim(false));
		mapAbilities(EntityWither.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityZombie.class, new AbilityHostile());
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
		}
		for(Ability ability : abilities)
		{
			boolean added = false;
			if(!stringToClassMap.containsKey(ability.getType()))
			{
				registerAbility(ability.getType(), ability.getClass());
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

	public static ArrayList<Ability> getEntityAbilities(Class<? extends EntityLivingBase> entClass)
	{
		if(SessionState.abilities)
		{
			ArrayList<Ability> abilities = abilityMap.get(entClass);
			if(abilities == null)
			{
				Class superClz = entClass.getSuperclass();
				if(superClz != EntityLivingBase.class)
				{
					abilityMap.put(entClass, getEntityAbilities(superClz));
					return abilityMap.get(entClass);
				}
			}
			else
			{
				return abilities;
			}
		}
		return new ArrayList<Ability>();
	}

}
