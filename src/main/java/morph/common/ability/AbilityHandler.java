package morph.common.ability;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import morph.api.Ability;
import morph.common.Morph;
import morph.common.ability.mod.AbilitySupport;
import morph.common.core.SessionState;
import morph.common.morph.MorphState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
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
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;

public class AbilityHandler 
{

	public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
	public final static HashMap<String, Class<? extends Ability>> stringToClassMap = new HashMap<String, Class<? extends Ability>>();
	public final static ArrayList<Class<? extends EntityLivingBase>> abilityClassList = new ArrayList<Class<? extends EntityLivingBase>>();
	
	static
	{
		registerAbility("climb"			  , AbilityClimb.class			);
		registerAbility("fly"			  , AbilityFly.class			);
		registerAbility("float"			  , AbilityFloat.class			);
		registerAbility("fireImmunity"	  , AbilityFireImmunity.class	);
		registerAbility("hostile"		  , AbilityHostile.class		);
		registerAbility("sunburn"		  , AbilitySunburn.class		);
		registerAbility("swim"			  , AbilitySwim.class			);
		registerAbility("waterAllergy"    , AbilityWaterAllergy.class	);
		registerAbility("poisonResistance", AbilityPoisonResistance.class);
		registerAbility("step"			  , AbilityStep.class			);
		registerAbility("witherResistance", AbilityWitherResistance.class);
		
		mapAbilities(EntityBat.class, new AbilityFly(true));
		mapAbilities(EntityBlaze.class, new AbilityFly(false), new AbilityFireImmunity(), new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityChicken.class, new AbilityFloat(-0.1141748D, true));
		mapAbilities(EntityCreeper.class, new AbilityHostile());
		mapAbilities(EntityDragon.class, new AbilityFly(false), new AbilityHostile());
		mapAbilities(EntityEnderman.class, new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityGhast.class, new AbilityFly(false), new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityGiantZombie.class, new AbilityHostile());
		mapAbilities(EntityIronGolem.class, new AbilitySwim(true));
		mapAbilities(EntityMagmaCube.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityPigZombie.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntitySilverfish.class, new AbilityHostile());
		mapAbilities(EntitySkeleton.class, new AbilityFireImmunity(), new AbilityHostile(), new AbilitySunburn(), new AbilityWitherResistance());
		mapAbilities(EntitySlime.class, new AbilityHostile());
		mapAbilities(EntitySnowman.class, new AbilityWaterAllergy());
		mapAbilities(EntitySpider.class, new AbilityClimb(), new AbilityHostile());
		mapAbilities(EntitySquid.class, new AbilitySwim(false, 1.2f, 0.4f, true));
		mapAbilities(EntityWither.class, new AbilityFly(false), new AbilityFireImmunity(), new AbilityHostile(), new AbilityWitherResistance());
		mapAbilities(EntityZombie.class, new AbilityHostile(), new AbilitySunburn());
		mapAbilities(EntityCaveSpider.class, new AbilityClimb(), new AbilityHostile(), new AbilityPoisonResistance());
		mapAbilities(EntityHorse.class, new AbilityStep(1.0F));
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
		if(SessionState.abilities)
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
				return abilities;
			}
		}
		return new ArrayList<Ability>();
	}
	
	public static Ability getNewAbilityClimb()
	{
		return new AbilityClimb();
	}
	
	public static Ability getNewAbilityFireImmunity()
	{
		return new AbilityFireImmunity();
	}
	
	public static Ability getNewAbilityFloat(float terminalVelocity, boolean negateFallDamage)
	{
		return new AbilityFloat(terminalVelocity, negateFallDamage);
	}
	
	public static Ability getNewAbilityFly(boolean slowdownInWater)
	{
		return new AbilityFly(slowdownInWater);
	}

	public static Ability getNewAbilityHostile()
	{
		return new AbilityHostile();
	}
	
	public static Ability getNewAbilitySunburn()
	{
		return new AbilitySunburn();
	}
	
	public static Ability getNewAbilitySwim(boolean canBreatheOnLand)
	{
		return new AbilitySwim(canBreatheOnLand);
	}
	
	public static Ability getNewAbilityWaterAllergy()
	{
		return new AbilityWaterAllergy();
	}

}
