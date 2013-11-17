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
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;

public class AbilityHandler 
{

	public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
	public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> trackedMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
	public final static HashMap<String, Class<? extends Ability>> stringToClassMap = new HashMap<String, Class<? extends Ability>>();
	public final static ArrayList<Class<? extends EntityLivingBase>> abilityClassList = new ArrayList<Class<? extends EntityLivingBase>>();
	
	static
	{
		registerAbility("climb"			  , AbilityClimb.class		);
		registerAbility("fly"			  , AbilityFly.class			);
		registerAbility("float"			  , AbilityFloat.class		);
		registerAbility("fireImmunity"	  , AbilityFireImmunity.class	);
		registerAbility("hostile"		  , AbilityHostile.class		);
		registerAbility("sunburn"		  , AbilitySunburn.class		);
		registerAbility("swim"			  , AbilitySwim.class			);
		registerAbility("waterAllergy"    , AbilityWaterAllergy.class	);
		registerAbility("poisonResistance", AbilityPoisonResistance.class);
		
		mapAbilities(EntityBat.class, new AbilityFly());
		mapAbilities(EntityBlaze.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityChicken.class, new AbilityFloat(-0.1141748D, true));
		mapAbilities(EntityCreeper.class, new AbilityHostile());
		mapAbilities(EntityDragon.class, new AbilityFly(), new AbilityHostile());
		mapAbilities(EntityEnderman.class, new AbilityWaterAllergy(), new AbilityHostile());
		mapAbilities(EntityGhast.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityGiantZombie.class, new AbilityHostile());
		mapAbilities(EntityIronGolem.class, new AbilitySwim(true));
		mapAbilities(EntityMagmaCube.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityPigZombie.class, new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntitySilverfish.class, new AbilityHostile());
		mapAbilities(EntitySkeleton.class, new AbilityFireImmunity(), new AbilityHostile(), new AbilitySunburn());
		mapAbilities(EntitySlime.class, new AbilityHostile());
		mapAbilities(EntitySnowman.class, new AbilityWaterAllergy());
		mapAbilities(EntitySpider.class, new AbilityClimb(), new AbilityHostile());
		mapAbilities(EntitySquid.class, new AbilitySwim(false));
		mapAbilities(EntityWither.class, new AbilityFly(), new AbilityFireImmunity(), new AbilityHostile());
		mapAbilities(EntityZombie.class, new AbilityHostile(), new AbilitySunburn());
		mapAbilities(EntityCaveSpider.class, new AbilityClimb(), new AbilityHostile(), new AbilityPoisonResistance());
		
		AbilitySupport.getInstance().mapAbilities();
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
	
	public static void mapTrackedAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities)
	{
		ArrayList<Ability> abilityList = trackedMap.get(entClass);
		if(abilityList == null)
		{
			abilityList = new ArrayList<Ability>();
			trackedMap.put(entClass, abilityList);
		}
		for(Ability ability : abilities)
		{
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
				updatePlayerOfAbility(null, entClass);
			}
		}
		
		//saving
		NBTTagCompound tag = getAbilitiesAsNBT();
        try
        {
        	if(Morph.configFolder.exists())
        	{
                File file = new File(Morph.configFolder, "morphAbilities.dat");
                if(file.exists())
                {
                	File file1 = new File(Morph.configFolder, "morphAbilities_backup.dat");
                	if(file1.exists())
                	{
                		if(file1.delete())
                		{
                			file.renameTo(file1);
                		}
                		else
                		{
                			Morph.console("Failed to delete mod ability backup data!", true);
                		}
                	}
                	else
                	{
                		file.renameTo(file1);
                	}
                }
                
                CompressedStreamTools.writeCompressed(tag, new FileOutputStream(file));
        	}
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
            throw new RuntimeException("Failed to save morph ability data");
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
				ArrayList<Ability> mergedAbilities = new ArrayList<Ability>(abilities);
				ArrayList<Ability> trackedAbilities = trackedMap.get(entClass);
				if(trackedAbilities != null)
				{
					for(Ability ability : trackedAbilities)
					{
						boolean isNew = true;
						for(Ability mappedAbility : abilities)
						{
							if(mappedAbility.getType().equalsIgnoreCase(ability.getType()))
							{
								isNew = false;
								break;
							}
						}
						if(isNew)
						{
							mergedAbilities.add(ability);
						}
					}
				}
				return mergedAbilities;
			}
		}
		return new ArrayList<Ability>();
	}

	public static void updatePlayerOfAbility(EntityPlayer player, Class clz)
	{
		if(player != null)
		{
			ArrayList<NBTTagCompound> tags = getAbilitiesAsMultipleNBT();
			for(int i = 0; i < tags.size(); i++)
			{
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				DataOutputStream stream = new DataOutputStream(bytes);
				try
				{
					stream.writeByte(2); //id
					
					stream.writeBoolean(i == 0);
					
					Morph.writeNBTTagCompound(tags.get(i), stream);
					
					PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Morph", bytes.toByteArray()), (Player)player);
				}
				catch(IOException e)
				{
				}
			}
		}
		else if(clz != null)
		{
			NBTTagCompound tag = getAbilityAsNBT(clz);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);
			try
			{
				stream.writeByte(2); //id
				
				stream.writeBoolean(false);
				
				Morph.writeNBTTagCompound(tag, stream);
				
				PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Morph", bytes.toByteArray()), (Player)player);
			}
			catch(IOException e)
			{
			}
		}
	}
	
	public static NBTTagCompound getAbilitiesAsNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setInteger("classTypeCount", trackedMap.size());
		
		int i = 0;
		for(Entry<Class<? extends EntityLivingBase>, ArrayList<Ability>> e : trackedMap.entrySet())
		{
			NBTTagCompound classTag = new NBTTagCompound();
			classTag.setString("abilityClass", e.getKey().getName());
			classTag.setInteger("abilityCount", e.getValue().size());
			
			for(int k = 0; k < e.getValue().size(); k++)
			{
				Ability ability = e.getValue().get(k);
				NBTTagCompound abilityTag = new NBTTagCompound();
				abilityTag.setString("type", ability.getType());
				ability.save(abilityTag);
				
				classTag.setCompoundTag("ability_" + k, abilityTag);
			}
			
			tag.setCompoundTag("class_" + i, classTag);
			i++;
		}
		return tag;
	}
	
	public static NBTTagCompound getAbilityAsNBT(Class clz)
	{
		NBTTagCompound classTag = new NBTTagCompound();
		classTag.setString("abilityClass", clz.getName());
		
		ArrayList<Ability> abilities = trackedMap.get(clz);
		
		classTag.setInteger("abilityCount", abilities.size());
		
		for(int k = 0; k < abilities.size(); k++)
		{
			Ability ability = abilities.get(k);
			NBTTagCompound abilityTag = new NBTTagCompound();
			abilityTag.setString("type", ability.getType());
			ability.save(abilityTag);
			
			classTag.setCompoundTag("ability_" + k, abilityTag);
		}
		
		return classTag;
	}
	
	public static ArrayList<NBTTagCompound> getAbilitiesAsMultipleNBT()
	{
		ArrayList<NBTTagCompound> tags = new ArrayList<NBTTagCompound>();
		
		int i = 0;
		for(Entry<Class<? extends EntityLivingBase>, ArrayList<Ability>> e : trackedMap.entrySet())
		{
			NBTTagCompound classTag = new NBTTagCompound();
			classTag.setString("abilityClass", e.getKey().getName());
			classTag.setInteger("abilityCount", e.getValue().size());
			
			for(int k = 0; k < e.getValue().size(); k++)
			{
				Ability ability = e.getValue().get(k);
				NBTTagCompound abilityTag = new NBTTagCompound();
				abilityTag.setString("type", ability.getType());
				ability.save(abilityTag);
				
				classTag.setCompoundTag("ability_" + k, abilityTag);
			}
			
			tags.add(classTag);
			i++;
		}
		
		return tags;
	}
	
	public static void readAbilityFromNBT(NBTTagCompound classTag)
	{
		try
		{
			Class clz = Class.forName(classTag.getString("abilityClass"));
			int abilityCount = classTag.getInteger("abilityCount");
			for(int k = 0; k < abilityCount; k++)
			{
				NBTTagCompound abilityTag = classTag.getCompoundTag("ability_" + k);
				String abilityType = abilityTag.getString("type");
				try
				{
					Class<? extends Ability> abilityClass = AbilityHandler.stringToClassMap.get(abilityType);
					Ability ability = abilityClass.newInstance();
					ability.load(abilityTag);
					
					AbilityHandler.mapTrackedAbilities(clz, ability);
				}
				catch(Exception e)
				{
					Morph.console("Failed to create Ability type " + abilityType + " for Class " + clz.getName(), true);
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			Morph.console("Failed to find Class " + classTag.getString("abilityClass"), true);
			e.printStackTrace();
		}
	}
	
	public static void readAbilitiesFromNBT(NBTTagCompound tag) 
	{
		trackedMap.clear();
		
    	if(tag != null)
    	{
    		int classTypeCount = tag.getInteger("classTypeCount");
    		for(int i = 0; i < classTypeCount; i++)
    		{
    			NBTTagCompound classTag = tag.getCompoundTag("class_" + i);
    			try
    			{
    				Class clz = Class.forName(classTag.getString("abilityClass"));
    				int abilityCount = classTag.getInteger("abilityCount");
    				for(int k = 0; k < abilityCount; k++)
    				{
    					NBTTagCompound abilityTag = classTag.getCompoundTag("ability_" + k);
    					String abilityType = abilityTag.getString("type");
    					try
    					{
    						Class<? extends Ability> abilityClass = AbilityHandler.stringToClassMap.get(abilityType);
    						Ability ability = abilityClass.newInstance();
    						ability.load(abilityTag);
    						
    						AbilityHandler.mapTrackedAbilities(clz, ability);
    					}
    					catch(Exception e)
    					{
    						Morph.console("Failed to create Ability type " + abilityType + " for Class " + clz.getName(), true);
    						e.printStackTrace();
    					}
    				}
    			}
    			catch(Exception e)
    			{
    				Morph.console("Failed to find Class " + classTag.getString("abilityClass"), true);
    				e.printStackTrace();
    			}
    		}
    	}
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
	
	public static Ability getNewAbilityFly()
	{
		return new AbilityFly();
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
