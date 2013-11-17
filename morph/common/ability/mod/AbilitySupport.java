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

public class AbilitySupport {
	
	private static final String jsonPath = "/morph/common/ability/mod/ModMobSupport.json";
	public static AbilitySupport instance = null;
	private HashMap<String, String[]> customMobAbilityMapping = new HashMap<String, String[]>();

	public static AbilitySupport getInstance(){
		if(instance == null){
			Gson gson = new Gson();
			try{
				Reader fileIn = new InputStreamReader(new URL("https://raw.github.com/iChun/Morph/master" + jsonPath).openStream());
				instance = gson.fromJson(fileIn, AbilitySupport.class);
			}catch(Exception e){
				e.printStackTrace();
				try
				{
					Reader fileIn = new InputStreamReader(Morph.class.getResourceAsStream(jsonPath));
					instance = gson.fromJson(fileIn, AbilitySupport.class);
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
					instance = new AbilitySupport();
				}
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
					try {
						Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
						abilityObjs.add(((Ability)abilityClass.getConstructor().newInstance()).parse(argVars.toArray(new String[0])));
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
