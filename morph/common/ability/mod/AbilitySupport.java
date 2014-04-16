package morph.common.ability.mod;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import net.minecraft.entity.EntityLivingBase;
import morph.common.Morph;
import morph.common.ability.*;
import morph.api.Ability;
import morph.common.ability.AbilityHandler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class AbilitySupport {
	
	private static final String jsonPath = "/assets/morph/mod/ModMobSupport.json";
	public static AbilitySupport instance = null;
	private HashMap<String, String[]> customMobAbilityMapping = new HashMap<String, String[]>();

	public static AbilitySupport getInstance()
	{
		if(instance == null)
		{
			Gson gson = new Gson();
			Reader fileIn = null;
			if(Morph.forceLocalModAbilityPatch != 1)
			{
				try
				{
					fileIn = new InputStreamReader(new URL(Morph.remoteModAbilityPatch + jsonPath).openStream());
				}
				catch(Exception e)
				{
					fileIn = null;
					Morph.console("Failed to retrieve mod mob ability mappings from GitHub!", true);
					e.printStackTrace();
				}
			}
			if(fileIn == null)
			{
				try
				{
					fileIn = new InputStreamReader(Morph.class.getResourceAsStream(jsonPath));
				}
				catch(Exception e)
				{
					fileIn = null;
					Morph.console("Failed to read local copy of mod mob ability mappings", true);
					e.printStackTrace();
				}
			}
			
			if(fileIn != null)
			{
				try
				{
					instance = gson.fromJson(fileIn, AbilitySupport.class);
				}
				catch(JsonSyntaxException e)
				{
					Morph.console("ModMobSupport has invalid formatting! Mod mob abilities will be affected! Report this if the local file isn't used.", true);
					e.printStackTrace();
				}
			}
			if(instance == null)
			{
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
//				System.out.println("Pruning non existant class from map: "+key);
				customMobAbilityMapping.remove(key);
			}
		}
		for(String mob : customMobAbilityMapping.keySet()){
			String[] abilities = customMobAbilityMapping.get(mob);
			ArrayList<Ability> abilityObjs = new ArrayList<Ability>();
			for(String ability : abilities){
				boolean hasArgs = false;
				if(ability.contains("|")){
					ArrayList<String> argVars = new ArrayList<String>();
					hasArgs = true;
					String args = ability.split("\\|")[1];
					ability = ability.split("\\|")[0];
					if(args.contains(",")){
						for(String arg : args.split(",")){
							argVars.add(arg.trim());
						}
					}
					else
					{
						argVars.add(args.trim());
					}
					try {
						Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
						Ability ab = ((Ability)abilityClass.getConstructor().newInstance());
						try
						{
							ab.parse(argVars.toArray(new String[0]));
						}
						catch(Exception e)
						{
							Morph.console("Mappings are erroring! These mappings are probably invalid or outdated: " + ability, true);
						}
						abilityObjs.add(ab);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				if(!ability.isEmpty() && !hasArgs){
					Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
					try {
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
				}catch(ClassNotFoundException e){}
				if(entityClass != null){
					if(AbilityHandler.abilityMap.containsKey(entityClass))
					{
						Morph.console("Ignoring ability mapping for " + mob + "! Already has abilities mapped!", true);
					}
					else
					{
						StringBuilder sb = new StringBuilder();
						sb.append("Adding ability mappings ");
						for (int i = 0; i < abilityObjs.size(); i++) {
							Ability a = abilityObjs.get(i);
							sb.append(a.getType());
							if(i != abilityObjs.size() - 1)
							{
								sb.append(", ");
							}
						}
						sb.append(" to ");
						sb.append(entityClass);
						
						Morph.console(sb.toString(), false);
						AbilityHandler.mapAbilities(entityClass, abilityObjs.toArray(new Ability[0]));
					}
				}
			}
		}
	}
}
