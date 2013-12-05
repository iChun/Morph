package morph.common.core;

import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class ApiHandler 
{

	public static boolean hasMorph(String playerName, boolean isClient)
	{
		if(isClient)
		{
			return Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(playerName);
		}
		else
		{
			return Morph.proxy.tickHandlerServer.playerMorphInfo.containsKey(playerName);
		}
	}
	
	public static float morphProgress(String playerName, boolean isClient)
	{
		MorphInfo info;
		if(isClient)
		{
			info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(playerName);
		}
		else
		{
			info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(playerName);
		}
		if(info != null)
		{
			float prog = (float)info.morphProgress / 80F;
			if(prog > 1.0F)
			{
				prog = 1.0F;
			}
			return prog;
		}
		return 1.0F;
	}
	
	public static EntityLivingBase getMorphEntity(String playerName, boolean isClient)
	{
		MorphInfo info;
		if(isClient)
		{
			info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(playerName);
		}
		else
		{
			info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(playerName);
		}
		if(info != null)
		{
			return info.nextState.entInstance;
		}
		return null;
	}
	
	public static void blacklistEntity(Class<? extends EntityLivingBase> clz)
	{
		if(!Morph.blacklistedClasses.contains(clz))
		{
			Morph.blacklistedClasses.add(clz);
		}
	}
	
	public static boolean forceMorph(EntityPlayerMP player, EntityLivingBase living)
	{
		return EntityHelper.morphPlayer(player, living, false, true);
	}
	
	public static void forceDemorph(EntityPlayerMP player)
	{
		EntityHelper.demorphPlayer(player);
	}
	
}
