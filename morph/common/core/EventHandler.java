package morph.common.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import morph.client.entity.EntityMorphAcquisition;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
		if(Morph.proxy.tickHandlerClient.forceRender)
		{
			return;
		}
		if(Morph.proxy.tickHandlerClient.renderingMorph && Morph.proxy.tickHandlerClient.renderingPlayer > 1)
		{
			event.setCanceled(true);
			return;
		}
		
		Morph.proxy.tickHandlerClient.renderingPlayer++;

		if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.username))
		{
			if(Morph.proxy.tickHandlerClient.renderingPlayer != 2)
			{
				event.setCanceled(true);
			}
			else
			{
				Morph.proxy.tickHandlerClient.renderingPlayer--;
				return;
			}
			
			double par2 = Morph.proxy.tickHandlerClient.renderTick;
	        double d0 = event.entityPlayer.lastTickPosX + (event.entityPlayer.posX - event.entityPlayer.lastTickPosX) * (double)par2;
	        double d1 = event.entityPlayer.lastTickPosY + (event.entityPlayer.posY - event.entityPlayer.lastTickPosY) * (double)par2;
	        double d2 = event.entityPlayer.lastTickPosZ + (event.entityPlayer.posZ - event.entityPlayer.lastTickPosZ) * (double)par2;
	        float f1 = event.entityPlayer.prevRotationYaw + (event.entityPlayer.rotationYaw - event.entityPlayer.prevRotationYaw) * (float)par2;
	        MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(event.entityPlayer.username);
	        
	        int br1 = info.prevState != null ? info.prevState.entInstance.getBrightnessForRender((float)par2) : event.entityPlayer.getBrightnessForRender((float)par2);
	        int br2 = info.nextState.entInstance.getBrightnessForRender((float)par2);
	        
	        float prog = (float)(info.morphProgress + par2) / 80F;
	        
	        if(prog > 1.0F)
	        {
	        	prog = 1.0F;
	        }
	        
	        int i = br1 + (int)((float)(br2 - br1) * prog);

	        if (event.entityPlayer.isBurning())
	        {
	            i = 15728880;
	        }

	        int j = i % 65536;
	        int k = i / 65536;
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

	        if(info != null)
	        {
	        	Morph.proxy.tickHandlerClient.renderingMorph = true;
	        	GL11.glPushMatrix();
	        	
//	        	ObfuscationReflectionHelper.setPrivateValue(RendererLivingEntity.class, info.prevEntInfo.entRender, info.prevEntModel, ObfHelper.mainModel);
	        	
//	        	event.entityPlayer.yOffset -= Morph.proxy.tickHandlerClient.ySize;
	        	
	        	GL11.glTranslated(1 * (d0 - RenderManager.renderPosX), 1 * (d1 - RenderManager.renderPosY) + Morph.proxy.tickHandlerClient.ySize, 1 * (d2 - RenderManager.renderPosZ));
	        	
//	        	GL11.glScalef(1.0F, -1.0F, -1.0F);
	        	
	        	if(info.morphProgress <= 40)
	        	{
	        		if(info.morphProgress < 10)
	        		{
			        	info.prevEntInfo.entRender.func_130000_a(info.prevState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
			        	
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress + Morph.proxy.tickHandlerClient.renderTick) / 10F;
				        	
				        	GL11.glEnable(GL11.GL_BLEND);
				        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				        	
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, progress);
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.prevEntInfo.entRender, info.prevEntInfo.entRender.getClass(), info.prevState.entInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.prevEntInfo.entRender.func_130000_a(info.prevState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
				        	
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
			        	info.nextEntInfo.entRender.func_130000_a(info.nextState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
			        	
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
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.nextEntInfo.entRender, info.nextEntInfo.entRender.getClass(), info.nextState.entInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.nextEntInfo.entRender.func_130000_a(info.nextState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
				        	
				        	GL11.glDisable(GL11.GL_BLEND);
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        	}
	        		}
	        	}
	        	if(info.morphProgress >= 10 && info.morphProgress < 70)
	        	{
	        		info.prevEntInfo.entRender.func_130000_a(info.prevState.entInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        		info.nextEntInfo.entRender.func_130000_a(info.nextState.entInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.setMainModel(info.interimModel);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.doRender(event.entityPlayer, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        	}
	        	
//	        	event.entityPlayer.yOffset += Morph.proxy.tickHandlerClient.ySize;
	        	
	        	GL11.glPopMatrix();
	        	Morph.proxy.tickHandlerClient.renderingMorph = false;
	        }
		}
		Morph.proxy.tickHandlerClient.renderingPlayer--;
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderPlayerSpecials(RenderPlayerEvent.Specials.Pre event)
	{
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onDrawBlockHighlight(DrawBlockHighlightEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.renderViewEntity == mc.thePlayer)
		{
	        MorphInfo info1 = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.username);
			if(info1 != null && mc.renderViewEntity == mc.thePlayer)
			{
//				float prog = info1.morphProgress > 10 ? (((float)info1.morphProgress + event.partialTicks) / 60F) : 0.0F;
//				if(prog > 1.0F)
//				{
//					prog = 1.0F;
//				}
//				
//				prog = (float)Math.pow(prog, 2);
//				
//				float prev = info1.prevState != null && !(info1.prevState.entInstance instanceof EntityPlayer) ? info1.prevState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
//				float next = info1.nextState != null && !(info1.nextState.entInstance instanceof EntityPlayer) ? info1.nextState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
//				float ySize = mc.thePlayer.yOffset - (prev + (next - prev) * prog);
//				mc.thePlayer.lastTickPosY -= ySize;
//				mc.thePlayer.prevPosY -= ySize;
//				mc.thePlayer.posY -= ySize;
//				
//                double d0 = (double)mc.playerController.getBlockReachDistance();
//				event.context.drawSelectionBox(mc.thePlayer, mc.thePlayer.rayTrace(d0, (float)event.partialTicks), 0, event.partialTicks);
//				
//				mc.thePlayer.lastTickPosY += ySize;
//				mc.thePlayer.prevPosY += ySize;
//				mc.thePlayer.posY += ySize;
				
				event.setCanceled(true);
			}
		}
	}
	
	@ForgeSubscribe
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.source.getEntity() instanceof EntityPlayer && FMLCommonHandler.instance().getEffectiveSide().isServer() && (Morph.childMorphs == 1 || Morph.childMorphs == 0 && !event.entityLiving.isChild()))
		{
			EntityPlayer player = (EntityPlayer)event.source.getEntity();
			
			MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
			
//				EntityGiantZombie zomb = new EntityGiantZombie(event.entityLiving.worldObj);
//				zomb.setLocationAndAngles(event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, event.entityLiving.rotationYaw, event.entityLiving.rotationPitch);
//				event.entityLiving.worldObj.spawnEntityInWorld(zomb);
			
			if(!(event.entityLiving.addEntityID(new NBTTagCompound()) && !(event.entityLiving instanceof EntityPlayer) || event.entityLiving instanceof EntityPlayer))
			{
				System.out.println("stop");
				return;
			}
			
			if(info == null)
			{
				info = new MorphInfo(player.username, null, Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.username));
			}
			else if(info.getMorphing() || info.nextState.entInstance == event.entityLiving)
			{
				System.out.println("stop1");
				return;
			}
			
			byte isPlayer = (byte)((info.nextState.entInstance instanceof EntityPlayer && event.entityLiving instanceof EntityPlayer) ? 3 : info.nextState.entInstance instanceof EntityPlayer ? 1 : event.entityLiving instanceof EntityPlayer ? 2 : 0);
			
			if(!(info.nextState.entInstance instanceof EntityPlayer) && !info.nextState.entInstance.addEntityID(new NBTTagCompound()))
			{
				System.out.println("stop2");
				return;
			}
			
			String username1 = (isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)info.nextState.entInstance).username : "";
			String username2 = (isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)event.entityLiving).username : "";
			
			NBTTagCompound prevTag = new NBTTagCompound();
			NBTTagCompound nextTag = new NBTTagCompound();

			info.nextState.entInstance.addEntityID(prevTag);
			event.entityLiving.addEntityID(nextTag);
			
			MorphState prevState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username), new MorphState(player.worldObj, player.username, username1, prevTag, false));
			MorphState nextState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username), new MorphState(player.worldObj, player.username, username2, nextTag, false));
			
			if(nextState.identifier.equalsIgnoreCase(info.nextState.identifier))
			{
				System.out.println("stop3");
				return;
			}
			
			event.entityLiving.setDead();
			
			MorphInfo info2 = new MorphInfo(player.username, prevState, nextState);
			info2.setMorphing(true);
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);
			try
			{
				stream.writeInt(event.entityLiving.entityId);
				stream.writeInt(player.entityId);
				
				PacketDispatcher.sendPacketToAllInDimension(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()), player.dimension);
			}
			catch(IOException e)
			{
				
			}
			
			MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, nextState);
			PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
			
			player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
		}
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
//		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
//		{
//			System.out.println("asdasdasdsad");
//			if(event.target instanceof EntityLivingBase && !(event.target instanceof EntityMorphAcquisition))
//			{
//				event.entityPlayer.worldObj.spawnEntityInWorld(new EntityMorphAcquisition(event.entityPlayer.worldObj, (EntityLivingBase)event.target, event.entityPlayer));
//			}
//		}
//		else
//		{
//			return;
//		}
	}
	
	@ForgeSubscribe
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.world.provider.dimensionId == 0)
		{
			WorldServer world = (WorldServer)event.world;
	    	try
	    	{
	    		File file = new File(world.getChunkSaveLocation(), "morph.dat");
	    		if(!file.exists())
	    		{
	    			Morph.proxy.tickHandlerServer.saveData = new NBTTagCompound();
	    			Morph.console("Save data does not exist!", true);
	    			return;
	    		}
	    		Morph.proxy.tickHandlerServer.saveData = CompressedStreamTools.readCompressed(new FileInputStream(file));
	    	}
	    	catch(EOFException e)
	    	{
	    		Morph.console("Save data is corrupted! Attempting to read from backup.", true);
	    		try
	    		{
		    		File file = new File(world.getChunkSaveLocation(), "morph_backup.dat");
		    		if(!file.exists())
		    		{
		    			Morph.proxy.tickHandlerServer.saveData = new NBTTagCompound();
		    			Morph.console("No backup detected!", true);
		    			return;
		    		}
		    		Morph.proxy.tickHandlerServer.saveData = CompressedStreamTools.readCompressed(new FileInputStream(file));

		    		File file1 = new File(world.getChunkSaveLocation(), "morph.dat");
		    		file1.delete();
		    		file.renameTo(file1);
		    		Morph.console("Restoring data from backup.", false);
	    		}
	    		catch(Exception e1)
	    		{
	    			Morph.proxy.tickHandlerServer.saveData = new NBTTagCompound();
	    			Morph.console("Even your backup data is corrupted. What have you been doing?!", true);
	    			return;
	    		}
	    	}
	    	catch(IOException e)
	    	{
	    		Morph.proxy.tickHandlerServer.saveData = new NBTTagCompound();
	    		Morph.console("Failed to read save data!", true);
	    		return;
	    	}
	    	
	    	NBTTagCompound tag = Morph.proxy.tickHandlerServer.saveData;
	    	
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
	    			MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(event.world, info.playerName), info.nextState);
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
			if(Morph.proxy.tickHandlerServer.saveData == null)
			{
				Morph.proxy.tickHandlerServer.saveData = new NBTTagCompound();
			}
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
	                }
	                for(Entry<String, ArrayList<MorphState>> e : Morph.proxy.tickHandlerServer.playerMorphs.entrySet())
	                {
	                	String name = e.getKey();
	                	ArrayList<MorphState> states = e.getValue();
	                	tag.setInteger(name + "_morphStatesCount", states.size());
	                	for(int i = 0; i < states.size(); i++)
	                	{
	                		tag.setCompoundTag(name + "_morphState" + i, states.get(i).getTag());
	                	}
	                }
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
