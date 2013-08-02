package morph.client.model;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ModelList 
{

	private static final HashMap<Class, ModelInfo> modelInfoMap = new HashMap<Class, ModelInfo>();
	
	public static void addModelInfo(Class clz, ModelInfo info)
	{
		if(EntityLivingBase.class.isAssignableFrom(clz))
		{
			modelInfoMap.put(clz, info);
		}
	}
	
	public static ModelInfo getModelInfo(Class clz)
	{
		ModelInfo info = modelInfoMap.get(clz);
		if(info == null)
		{
			try
			{
				Class clzz = clz.getSuperclass();
				while(clzz != Entity.class && Entity.class.isAssignableFrom(clzz) && info == null)
				{
					info = modelInfoMap.get(clzz);
					clzz = clzz.getSuperclass();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return info;
	}
	
}
