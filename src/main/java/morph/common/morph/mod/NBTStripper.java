package morph.common.morph.mod;

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

public class NBTStripper {
	
	private static final String jsonPath = "/assets/morph/mod/NBTStripper.json";
	private static HashMap<Class<? extends EntityLivingBase>, ArrayList<String>> stripperMappings = new HashMap<Class<? extends EntityLivingBase>, ArrayList<String>>();
	public static NBTStripper instance = null;

	private HashMap<String, String[]> modNBTStripper = new HashMap<String, String[]>();
	
	public static NBTStripper getInstance()
	{
		if(instance == null)
		{
			Gson gson = new Gson();
			Reader fileIn = null;
			try
			{
				fileIn = new InputStreamReader(new URL("https://raw.github.com/iChun/Morph/master" + jsonPath).openStream());
			}
			catch(Exception e)
			{
				Morph.console("Failed to retrieve nbt stripper mappings from GitHub!", true);
				e.printStackTrace();
				try
				{
					fileIn = new InputStreamReader(Morph.class.getResourceAsStream(jsonPath));
				}
				catch(Exception e1)
				{
					fileIn = null;
					Morph.console("Failed to read local copy of nbt stripper mappings", true);
					e1.printStackTrace();
				}
			}
			
			if(fileIn == null)
			{
				instance = new NBTStripper();
			}
			else
			{
				instance = gson.fromJson(fileIn, NBTStripper.class);
			}
		}
		return instance;
	}
	
	public void mapStripperInfo(){
		String[] keys = modNBTStripper.keySet().toArray(new String[0]);
		for(String key : keys){
			Class<? extends EntityLivingBase> entityClass = null;
			try{
				entityClass = (Class<? extends EntityLivingBase>) Class.forName(key);
			}catch(ClassNotFoundException e){
				modNBTStripper.remove(key);
			}
		}
		for(String mob : modNBTStripper.keySet()){
			String[] tagNames = modNBTStripper.get(mob);
			try
			{
				addStripperMappings((Class<? extends EntityLivingBase>) Class.forName(mob), tagNames);
			}
			catch(ClassNotFoundException e)
			{
			}
		}
	}
	
	public static void addStripperMappings(Class<? extends EntityLivingBase> clz, String...tagNames)
	{
		ArrayList<String> mappings = getNBTTagsToStrip(clz);
		for(String tagName : tagNames)
		{
			if(!mappings.contains(tagName))
			{
				mappings.add(tagName);
			}
		}
	}
	
	public static ArrayList<String> getNBTTagsToStrip(EntityLivingBase ent)
	{
		ArrayList<String> tagsToStrip = new ArrayList<String>();
		Class clz = ent.getClass();
		while(clz != EntityLivingBase.class)
		{
			tagsToStrip.addAll(getNBTTagsToStrip(clz));
			clz = clz.getSuperclass();
		}
		return tagsToStrip;
	}
	
	public static ArrayList<String> getNBTTagsToStrip(Class<? extends EntityLivingBase> clz)
	{
		ArrayList<String> list = stripperMappings.get(clz);
		if(list == null)
		{
			list = new ArrayList<String>();
			stripperMappings.put(clz, list);
		}
		return list;
	}
}
