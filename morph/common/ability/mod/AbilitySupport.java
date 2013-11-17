package morph.common.ability.mod;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import net.minecraft.entity.EntityLivingBase;

import morph.common.ability.*;
import morph.api.Ability;
import morph.common.ability.AbilityHandler;

import com.google.gson.Gson;

public class AbilitySupport {
	
	public static AbilitySupport instance = null;
	private HashMap<String, String[]> customMobAbilityMapping = new HashMap<String, String[]>();

	public static AbilitySupport getInstance(){
		if(instance == null){
			try{
				Gson gson = new Gson();
				Reader fileIn = new InputStreamReader(new URL("https://dl.dropboxusercontent.com/u/17362162/morphsupport.json").openStream());
				instance = gson.fromJson(fileIn, AbilitySupport.class);
			}catch(Exception e){
				instance = new AbilitySupport();
			}
		}
		return instance;
	}
	
	public void mapAbilities(){
		String[] keys = customMobAbilityMapping.keySet().toArray(new String[0]);
		for(String key : keys){
			Class<? extends EntityLivingBase> entityClass = null;
			try{
				entityClass = (Class<? extends EntityLivingBase>) Class.forName(key);
			}catch(ClassNotFoundException e){
				System.out.println("Pruning non existant class from map: "+key);
				customMobAbilityMapping.remove(key);
			}
		}
		for(String mob : customMobAbilityMapping.keySet()){
			String[] abilities = customMobAbilityMapping.get(mob);
			ArrayList<Ability> abilityObjs = new ArrayList<Ability>();
			for(String ability : abilities){
				boolean hasArgs = false;
				if(ability.contains("|")){
					ArrayList<Object> argTypes = new ArrayList<Object>();
					ArrayList<Object> argVars = new ArrayList<Object>();
					hasArgs = true;
					String args = ability.split("\\|")[1];
					ability = ability.split("\\|")[0];
					if(args.contains(",")){
						for(String arg : args.split(",")){
							try{
								double val = Double.parseDouble(arg);
								System.out.println(val);
								argVars.add(val);
								argTypes.add(double.class);
							}catch(NumberFormatException e){
								if(arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")){
									argVars.add(Boolean.parseBoolean(arg));
									argTypes.add(boolean.class);
								}else{
									argVars.add(arg);
									argTypes.add(String.class);
								}
								
							}
						}
					}
					try {
						Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
						abilityObjs.add((Ability)abilityClass.getConstructor(argTypes.toArray(new Class[0])).newInstance(argVars.toArray(new Object[0])));
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				if(!ability.isEmpty() && !hasArgs){
					Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
					try {
						System.out.println(abilityClass.getSimpleName()+" added with no args");
						abilityObjs.add((Ability)abilityClass.getConstructor().newInstance());
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			if(abilityObjs.size() > 0){
				Class<? extends EntityLivingBase> entityClass = null;
				try{
					entityClass = (Class<? extends EntityLivingBase>) Class.forName(mob);
				}catch(ClassNotFoundException e){
					System.out.println("Did not add Ability Mappings for custom entity "+mob+" | not found!");

				}
				if(entityClass != null){
					for(Ability a : abilityObjs){
						if(a instanceof AbilityFloat){
							System.out.println(((AbilityFloat)a).terminalVelocity);
						}
					}
					System.out.println("Added Ability Mappings for found custom entity "+mob);
					AbilityHandler.mapAbilities(entityClass, abilityObjs.toArray(new Ability[0]));
				}
			}
		}
	}
	
	public void save(){
		customMobAbilityMapping = new HashMap<String, String[]>();
		customMobAbilityMapping.put("am2.bosses.EntityAirGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityArcaneGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityEarthGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityFireGuardian", new String[]{"fireImmunity", "hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityNatureGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityWaterGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.bosses.EntityWinterGuardian", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityBattleChicken", new String[]{"float|-0.114,true", "hostile"});
		customMobAbilityMapping.put("am2.entities.EntityDarkMage", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityDarkling", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityDryad", new String[]{""});
		customMobAbilityMapping.put("am2.entities.EntityEarthElemental", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityFireElemental", new String[]{"fireImmunity", "hostile"});
		customMobAbilityMapping.put("am2.entities.EntityHecate", new String[]{"sunburn", "fly", "hostile"});
		customMobAbilityMapping.put("am2.entities.EntityHellCow", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityLightMage", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityMageVillager", new String[]{""});
		customMobAbilityMapping.put("am2.entities.EntityManaCreeper", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityManaElemental", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityDarkMage", new String[]{"hostile"});
		customMobAbilityMapping.put("am2.entities.EntityWaterElemental", new String[]{"hostile"});
		customMobAbilityMapping.put("biomesoplenty.entities.EntityBird", new String[]{"fly"});
		customMobAbilityMapping.put("biomesoplenty.entities.EntityGlob", new String[]{"hostile"});
		customMobAbilityMapping.put("biomesoplenty.entities.EntityJungleSpider", new String[]{"climb", "hostile"});
		customMobAbilityMapping.put("biomesoplenty.entities.EntityRosester", new String[]{"float|-0.114,true"});
		customMobAbilityMapping.put("biomesoplenty.entities.EntityWasp", new String[]{"fly", "hostile"});
		customMobAbilityMapping.put("mods.natura.entity.BabyHeatscarSpider", new String[]{"climb", "fireImmunity", "hostile"});
		customMobAbilityMapping.put("mods.natura.entity.HeatscarSpider", new String[]{"climb", "fireImmunity", "hostile"});
		customMobAbilityMapping.put("mods.natura.entity.ImpEntity", new String[]{"fireImmunity", "hostile"});
		customMobAbilityMapping.put("mods.natura.entity.NitroCreeper", new String[]{"fireImmunity", "hostile"});
		customMobAbilityMapping.put("tconstruct.entity.BlueSlime", new String[]{"hostile"});
		customMobAbilityMapping.put("thaumcraft.common.entities.golems.EntityGolemBase", new String[]{""});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityBrainyZombie", new String[]{"sunburn", "hostile"});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityFireBat", new String[]{"fireImmunity", "fly", "hostile"});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityGiantBrainyZombie", new String[]{"sunburn", "hostile"});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityPech", new String[]{""});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityTaintChicken", new String[]{"float|-0.114,true", "hostile"});
		customMobAbilityMapping.put("thaumcraft.common.entities.monster.EntityWisp", new String[]{"fly", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFDeathTome", new String[]{"fly", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFFireBeetle", new String[]{"fireImmunity", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFHedgeSpider", new String[]{"climb", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFMiniGhast", new String[]{"fly", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFMosquitoSwarm", new String[]{"fly", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFPinchBeetle", new String[]{"hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFSwarmSpider", new String[]{"climb", "hostile"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFMobileFirefly", new String[]{"fly"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFRaven", new String[]{"fly"});
		customMobAbilityMapping.put("twilightforest.entity.EntityTFTinyBird", new String[]{"fly"});
		Gson gson = new Gson();	
		System.out.println(gson.toJson(this));
	}

}
