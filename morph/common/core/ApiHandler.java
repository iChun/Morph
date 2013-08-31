package morph.common.core;

import net.minecraft.entity.EntityLivingBase;

public class ApiHandler 
{

	public static boolean hasMorph(String playerName, boolean isClient)
	{
		return false;
	}
	
	public static float morphProgress(String playerName, boolean isClient)
	{
		return 1.0F;
	}
	
	public static EntityLivingBase getMorphEntity(String playerName, boolean isClient)
	{
		return null;
	}
	
	public static void blacklistEntity(Class<? extends EntityLivingBase> clz)
	{
	}
	
}
