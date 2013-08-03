package morph.common.core;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandler 
{

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onSoundLoad(SoundLoadEvent event)
	{
		for(int i = 1; i <= 6; i++)
		{
			event.manager.soundPoolSounds.addSound("morph:morph" + i + ".ogg");
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderPlayer(RenderPlayerEvent.Pre event)
	{
		if(Morph.proxy.tickHandlerClient.renderingMorph)
		{
			event.setCanceled(true);
			return;
		}
		if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.username))
		{
			event.setCanceled(true);
			
			double par2 = Morph.proxy.tickHandlerClient.renderTick;
	        double d0 = event.entityPlayer.lastTickPosX + (event.entityPlayer.posX - event.entityPlayer.lastTickPosX) * (double)par2;
	        double d1 = event.entityPlayer.lastTickPosY + (event.entityPlayer.posY - event.entityPlayer.lastTickPosY) * (double)par2;
	        double d2 = event.entityPlayer.lastTickPosZ + (event.entityPlayer.posZ - event.entityPlayer.lastTickPosZ) * (double)par2;
	        float f1 = event.entityPlayer.prevRotationYaw + (event.entityPlayer.rotationYaw - event.entityPlayer.prevRotationYaw) * (float)par2;
	        int i = event.entityPlayer.getBrightnessForRender((float)par2);

	        if (event.entityPlayer.isBurning())
	        {
	            i = 15728880;
	        }

	        int j = i % 65536;
	        int k = i / 65536;
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

	        MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(event.entityPlayer.username);
	        
	        if(info != null)
	        {
	        	Morph.proxy.tickHandlerClient.renderingMorph = true;
	        	GL11.glPushMatrix();
	        	
//	        	ObfuscationReflectionHelper.setPrivateValue(RendererLivingEntity.class, info.prevEntInfo.entRender, info.prevEntModel, ObfHelper.mainModel);
	        	
	        	GL11.glTranslated(1 * (d0 - RenderManager.renderPosX), 1 * (d1 - RenderManager.renderPosY), 1 * (d2 - RenderManager.renderPosZ));
	        	
//	        	GL11.glScalef(1.0F, -1.0F, -1.0F);
	        	
	        	if(info.morphProgress <= 40)
	        	{
	        		if(info.morphProgress < 10)
	        		{
			        	info.prevEntInfo.entRender.func_130000_a(info.prevEntInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
			        	
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress + Morph.proxy.tickHandlerClient.renderTick) / 10F;
				        	
				        	GL11.glEnable(GL11.GL_BLEND);
				        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				        	
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, progress);
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.prevEntInfo.entRender, info.prevEntInfo.entRender.getClass(), info.prevEntInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.prevEntInfo.entRender.func_130000_a(info.prevEntInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
				        	
				        	GL11.glDisable(GL11.GL_BLEND);
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        	}
	        		}
	        	}
	        	else
	        	{
	        		if(info.morphProgress >= 70)
	        		{
			        	info.nextEntInfo.entRender.func_130000_a(info.nextEntInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
			        	
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress - 70 + Morph.proxy.tickHandlerClient.renderTick) / 10F;
				        	
				        	GL11.glEnable(GL11.GL_BLEND);
				        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				        	
				        	if(progress > 1.0F)
				        	{
				        		progress = 1.0F;
				        	}
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F - progress);
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.nextEntInfo.entRender, info.nextEntInfo.entRender.getClass(), info.nextEntInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.nextEntInfo.entRender.func_130000_a(info.nextEntInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
				        	
				        	GL11.glDisable(GL11.GL_BLEND);
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        	}
	        		}
	        	}
	        	if(info.morphProgress >= 10 && info.morphProgress < 70)
	        	{
	        		info.prevEntInfo.entRender.func_130000_a(info.prevEntInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        		info.nextEntInfo.entRender.func_130000_a(info.nextEntInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.setMainModel(info.interimModel);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.doRender(event.entityPlayer, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        	}
	        	
	        	GL11.glPopMatrix();
	        	Morph.proxy.tickHandlerClient.renderingMorph = false;
	        }
		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event)
	{
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
		//TODO config for child entities
		Entity entTarget = event.target;
		
		if(event.target instanceof EntityDragonPart)
		{
			entTarget = (EntityDragon)((EntityDragonPart)event.target).entityDragonObj;
		}

		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && entTarget instanceof EntityLivingBase && !((EntityLivingBase)entTarget).isChild())
		{
			MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(event.entityPlayer.username);
			
//			EntityGiantZombie zomb = new EntityGiantZombie(entTarget.worldObj);
//			zomb.setLocationAndAngles(entTarget.posX, entTarget.posY, entTarget.posZ, entTarget.rotationYaw, entTarget.rotationPitch);
//			entTarget.worldObj.spawnEntityInWorld(zomb);
			
			if(!(entTarget.addEntityID(new NBTTagCompound()) && !(entTarget instanceof EntityPlayer) || entTarget instanceof EntityPlayer))
			{
				System.out.println("stop");
				return;
			}
			
			if(info == null)
			{
				info = new MorphInfo(event.entityPlayer.username, (EntityLivingBase)entTarget, event.entityPlayer);
			}
			else if(info.getMorphing() || info.nextEntInstance == entTarget)
			{
				System.out.println("stop1");
				return;
			}
			
			byte isPlayer = (byte)((info.nextEntInstance instanceof EntityPlayer && entTarget instanceof EntityPlayer) ? 3 : info.nextEntInstance instanceof EntityPlayer ? 1 : entTarget instanceof EntityPlayer ? 2 : 0);
			
			if(!(info.nextEntInstance instanceof EntityPlayer) && !info.nextEntInstance.addEntityID(new NBTTagCompound()))
			{
				System.out.println("stop2");
				return;
			}
			
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "morph:morph", 1.0F, 1.0F);
			
			String username1 = (isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)info.nextEntInstance).username : "";
			String username2 = (isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)entTarget).username : "";
			
			NBTTagCompound prevTag = new NBTTagCompound();
			NBTTagCompound nextTag = new NBTTagCompound();

			info.nextEntInstance.addEntityID(prevTag);
			entTarget.addEntityID(nextTag);
			
			EntityLivingBase prevEnt;
			EntityLivingBase nextEnt;
			
			if(username1.equalsIgnoreCase(""))
			{
				prevEnt = (EntityLivingBase)EntityList.createEntityFromNBT(prevTag, event.entityPlayer.worldObj);
			}
			else
			{
				prevEnt = event.entityPlayer;
			}
			
			if(username2.equalsIgnoreCase(""))
			{
				nextEnt = (EntityLivingBase)EntityList.createEntityFromNBT(nextTag, event.entityPlayer.worldObj);
			}
			else
			{
				nextEnt = event.entityPlayer;
			}
			
			MorphInfo info2 = new MorphInfo(event.entityPlayer.username, prevEnt, nextEnt);
			info2.setMorphing(true);
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(event.entityPlayer.username, info2);
			
			PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
		}
	}
	
	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.world.provider.dimensionId == 0)
		{
			WorldServer world = (WorldServer)event.world;
			NBTTagCompound tag = null;
	    	try
	    	{
	    		File file = new File(world.getChunkSaveLocation(), "morph.dat");
	    		if(file.exists())
	    		{
	    			tag = CompressedStreamTools.readCompressed(new FileInputStream(file));
	    			System.out.println(tag);
	    		}
	    	}
	    	catch(EOFException e)
	    	{
	    		Morph.console("Save data is corrupted! Attempting to read from backup.", true);
	    		try
	    		{
		    		File file = new File(world.getChunkSaveLocation(), "morph_backup.dat");
		    		if(!file.exists())
		    		{
		    			Morph.console("No backup detected!", true);
		    			return;
		    		}
		    		tag = CompressedStreamTools.readCompressed(new FileInputStream(file));

		    		File file1 = new File(world.getChunkSaveLocation(), "morph.dat");
		    		file1.delete();
		    		file.renameTo(file1);
		    		Morph.console("Restoring data from backup.", false);
	    		}
	    		catch(Exception e1)
	    		{
	    			Morph.console("Even your backup data is corrupted. What have you been doing?!", true);
	    		}
	    	}
	    	catch(IOException e)
	    	{
	    		Morph.console("Failed to read save data!", true);
	    	}
	    	
	    	System.out.println(tag);
	    	
	    	if(tag != null)
	    	{
	    		//read data
	    		int morphDataCount = tag.getInteger("morphDataCount");
	    		Morph.proxy.tickHandlerServer.playerMorphInfo.clear();
	    		for(int i = 1; i <= morphDataCount; i++)
	    		{
	    			NBTTagCompound tag1 = tag.getCompoundTag("morphData" + i);
	    			MorphInfo info = new MorphInfo();
	    			info.readNBT(tag1);
	    			Morph.proxy.tickHandlerServer.playerMorphInfo.put(info.playerName, info);
	    			System.out.println(info);
	    		}
	    	}
		}
	}

	@ForgeSubscribe
	public void onWorldSave(WorldEvent.Save event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.world.provider.dimensionId == 0)
		{
			WorldServer world = (WorldServer)event.world;
            try
            {
            	if(world.getChunkSaveLocation().exists())
            	{
	                File file = new File(world.getChunkSaveLocation(), "morph.dat");
	                if(file.exists())
	                {
	                	File file1 = new File(world.getChunkSaveLocation(), "morph_backup.dat");
	                	if(file1.exists())
	                	{
	                		if(file1.delete())
	                		{
	                			file.renameTo(file1);
	                		}
	                		else
	                		{
	                			Morph.console("Failed to delete mod backup data!", true);
	                		}
	                	}
	                	else
	                	{
	                		file.renameTo(file1);
	                	}
	                }
	                
	                //write data
	                
	    			NBTTagCompound tag = new NBTTagCompound();

	                int morphDataCount = Morph.proxy.tickHandlerServer.playerMorphInfo.size();
	                
	                tag.setInteger("morphDataCount", morphDataCount);
	                
	                int count = 0;
	                
	                for(Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet())
	                {
	                	count++;
	                	NBTTagCompound tag1 = new NBTTagCompound();
	                	e.getValue().writeNBT(tag1);
	                	tag.setCompoundTag("morphData" + count, tag1);
	                	System.out.println("doop");
	                }
	                System.out.println("Asdasd");
	                
	                System.out.println(tag);
	                //end write data
	                
	                CompressedStreamTools.writeCompressed(tag, new FileOutputStream(file));
            	}
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
                throw new RuntimeException("Failed to save morph data");
            }
		}
	}
	
}
