package morph.common.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.client.render.RenderMorph;
import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

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
	
	public static EntityLivingBase getPrevMorphEntity(String playerName, boolean isClient)
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
		if(info != null && info.prevState != null)
		{
			return info.prevState.entInstance;
		}
		return null;
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
	
	public static String isEntityAMorph(EntityLivingBase living, boolean isClient)
	{
		HashMap infoMap;
		if(isClient)
		{
			infoMap = Morph.proxy.tickHandlerClient.playerMorphInfo;
		}
		else
		{
			infoMap = Morph.proxy.tickHandlerServer.playerMorphInfo;
		}
		Iterator ite = infoMap.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry e = (Entry)ite.next();
			if(((MorphInfo)e.getValue()).nextState.entInstance == living || ((MorphInfo)e.getValue()).prevState != null && ((MorphInfo)e.getValue()).prevState.entInstance == living)
			{
				return (String)e.getKey();
			}
		}
		return null;
	}
	
	public static void allowNextPlayerRender()
	{
		Morph.proxy.tickHandlerClient.allowRender = true;;
	}
	
	@SideOnly(Side.CLIENT)
	public static ResourceLocation getMorphSkinTexture()
	{
		return RenderMorph.morphSkin;
	}
}
