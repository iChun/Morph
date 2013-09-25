package morph.common.core;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import morph.api.Ability;
import morph.client.model.ModelHelper;
import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import morph.common.Morph;
import morph.common.ability.AbilityHandler;
import morph.common.entity.EntTracker;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumStatus;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
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
		float shadowSize = 0.5F;
		try
		{
			if(Morph.proxy.tickHandlerClient.playerRenderShadowSize < 0.0F)
			{
				Morph.proxy.tickHandlerClient.playerRenderShadowSize = (Float)ObfuscationReflectionHelper.getPrivateValue(Render.class, event.renderer, ObfHelper.shadowSize);
			}
		}
		catch(Exception e)
		{
			ObfHelper.obfWarning();
			e.printStackTrace();
		}
		
		
		if(Morph.proxy.tickHandlerClient.forceRender)
		{
			ObfuscationReflectionHelper.setPrivateValue(Render.class, event.renderer, Morph.proxy.tickHandlerClient.playerRenderShadowSize, ObfHelper.shadowSize);
			return;
		}
		if(Morph.proxy.tickHandlerClient.renderingMorph && Morph.proxy.tickHandlerClient.renderingPlayer > 1)
		{
			event.setCanceled(true);
			return;
		}
		
	    float bossHealthScale = BossStatus.healthScale;
	    int bossStatusBarTime = BossStatus.statusBarLength;
	    String bossName = BossStatus.bossName;
	    boolean randVar = BossStatus.field_82825_d;
		
		Morph.proxy.tickHandlerClient.renderingPlayer++;

		if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.username))
		{
	        MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(event.entityPlayer.username);
			
			double par2 = Morph.proxy.tickHandlerClient.renderTick;
	        int br1 = info.prevState != null ? info.prevState.entInstance.getBrightnessForRender((float)par2) : event.entityPlayer.getBrightnessForRender((float)par2);
	        int br2 = info.nextState.entInstance.getBrightnessForRender((float)par2);
	        
	        float prog = (float)(info.morphProgress + par2) / 80F;
	        
	        if(prog > 1.0F)
	        {
	        	prog = 1.0F;
	        }
	        
	        try
	        {
	        	float prevShadowSize = Morph.proxy.tickHandlerClient.playerRenderShadowSize;
	        	
	        	if(info.prevState != null && info.prevState.entInstance != null)
	        	{
		        	Render render = RenderManager.instance.getEntityRenderObject(info.prevState.entInstance);
		        	if(render != null)
		        	{
		        		prevShadowSize = ObfuscationReflectionHelper.getPrivateValue(Render.class, render, ObfHelper.shadowSize);
		        	}
	        	}
	        	
	        	Render render = RenderManager.instance.getEntityRenderObject(info.nextState.entInstance);
	        	if(render == event.renderer)
	        	{
	        		shadowSize = Morph.proxy.tickHandlerClient.playerRenderShadowSize;
	        	}
	        	else if(render != null)
	        	{
	        		shadowSize = ObfuscationReflectionHelper.getPrivateValue(Render.class, render, ObfHelper.shadowSize);
	        	}

	        	float shadowProg = prog;
	        	prog /= 0.8F;
	        	if(prog < 1.0F)
	        	{
	        		shadowSize = prevShadowSize + (shadowSize - prevShadowSize) * prog;
	        	}
	        	ObfuscationReflectionHelper.setPrivateValue(Render.class, event.renderer, shadowSize, ObfHelper.shadowSize);
	        }
	        catch(Exception e)
	        {
				ObfHelper.obfWarning();
				e.printStackTrace();
	        }
			
			if(Morph.proxy.tickHandlerClient.renderingPlayer != 2)
			{
				event.setCanceled(true);
			}
			else
			{
				Morph.proxy.tickHandlerClient.renderingPlayer--;
				return;
			}
			
	        double d0 = event.entityPlayer.lastTickPosX + (event.entityPlayer.posX - event.entityPlayer.lastTickPosX) * (double)par2;
	        double d1 = event.entityPlayer.lastTickPosY + (event.entityPlayer.posY - event.entityPlayer.lastTickPosY) * (double)par2;
	        double d2 = event.entityPlayer.lastTickPosZ + (event.entityPlayer.posZ - event.entityPlayer.lastTickPosZ) * (double)par2;
	        float f1 = event.entityPlayer.prevRotationYaw + (event.entityPlayer.rotationYaw - event.entityPlayer.prevRotationYaw) * (float)par2;
	        
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
	        	
	        	GL11.glTranslated(1 * (d0 - RenderManager.renderPosX), 1 * (d1 - RenderManager.renderPosY) + (event.entityPlayer == Minecraft.getMinecraft().thePlayer && !((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && RenderManager.instance.playerViewY == 180.0F) ? Morph.proxy.tickHandlerClient.ySize : 0D), 1 * (d2 - RenderManager.renderPosZ));
	        	
//	        	GL11.glScalef(1.0F, -1.0F, -1.0F);
	        	
	        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

	        	float renderTick = Morph.proxy.tickHandlerClient.renderTick;
	        	
	        	if(info.morphProgress <= 40)
	        	{
	        		if(info.prevModelInfo != null && info.morphProgress < 10)
	        		{
    		            float ff2 = info.prevState.entInstance.renderYawOffset;
    		            float ff3 = info.prevState.entInstance.rotationYaw;
    		            float ff4 = info.prevState.entInstance.rotationPitch;
    		            float ff5 = info.prevState.entInstance.prevRotationYawHead;
    		            float ff6 = info.prevState.entInstance.rotationYawHead;
	        			
	    	        	if((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && RenderManager.instance.playerViewY == 180.0F)
	    	        	{
	    	        		EntityLivingBase renderView = Minecraft.getMinecraft().renderViewEntity;
	    	        		
	    	        		info.prevState.entInstance.renderYawOffset = renderView.renderYawOffset;
	    	        		info.prevState.entInstance.rotationYaw = renderView.rotationYaw;
	    	        		info.prevState.entInstance.rotationPitch = renderView.rotationPitch;
	    	        		info.prevState.entInstance.prevRotationYawHead = renderView.prevRotationYawHead;
	    	        		info.prevState.entInstance.rotationYawHead = renderView.rotationYawHead;
	    	        		renderTick = 1.0F;
	    	        	}
	        			
			        	info.prevModelInfo.forceRender(info.prevState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
			        	
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress + Morph.proxy.tickHandlerClient.renderTick) / 10F;
				        	
				        	GL11.glEnable(GL11.GL_BLEND);
				        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				        	
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, progress);
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.prevModelInfo.getRenderer(), info.prevModelInfo.getRenderer().getClass(), info.prevState.entInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.prevModelInfo.forceRender(info.prevState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
				        	
				        	GL11.glDisable(GL11.GL_BLEND);
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        	}
			        	
    	        		info.prevState.entInstance.renderYawOffset = ff2;
    	        		info.prevState.entInstance.rotationYaw = ff3;
    	        		info.prevState.entInstance.rotationPitch = ff4;
    	        		info.prevState.entInstance.prevRotationYawHead = ff5;
    	        		info.prevState.entInstance.rotationYawHead = ff6;
	        		}
	        	}
	        	else
	        	{
	        		if(info.nextModelInfo != null && info.morphProgress >= 70)
	        		{
    		            float ff2 = info.nextState.entInstance.renderYawOffset;
    		            float ff3 = info.nextState.entInstance.rotationYaw;
    		            float ff4 = info.nextState.entInstance.rotationPitch;
    		            float ff5 = info.nextState.entInstance.prevRotationYawHead;
    		            float ff6 = info.nextState.entInstance.rotationYawHead;
	        			
	    	        	if((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && RenderManager.instance.playerViewY == 180.0F)
	    	        	{
	    	        		EntityLivingBase renderView = Minecraft.getMinecraft().renderViewEntity;
	    	        		
	    	        		info.nextState.entInstance.prevRenderYawOffset = info.nextState.entInstance.renderYawOffset = renderView.renderYawOffset;
	    	        		info.nextState.entInstance.rotationYaw = renderView.rotationYaw;
	    	        		info.nextState.entInstance.rotationPitch = renderView.rotationPitch;
	    	        		info.nextState.entInstance.prevRotationYawHead = renderView.prevRotationYawHead;
	    	        		info.nextState.entInstance.rotationYawHead = renderView.rotationYawHead;
	    	        		renderTick = 1.0F;
	    	        	}

			        	info.nextModelInfo.forceRender(info.nextState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
			        	
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
				        	
				        	ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.nextModelInfo.getRenderer(), info.nextModelInfo.getRenderer().getClass(), info.nextState.entInstance);
				        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
				        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
				        	
				        	info.nextModelInfo.forceRender(info.nextState.entInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
				        	
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
				        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
				        	
				        	GL11.glDisable(GL11.GL_BLEND);
				        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			        	}
			        	
    	        		info.nextState.entInstance.renderYawOffset = ff2;
    	        		info.nextState.entInstance.rotationYaw = ff3;
    	        		info.nextState.entInstance.rotationPitch = ff4;
    	        		info.nextState.entInstance.prevRotationYawHead = ff5;
    	        		info.nextState.entInstance.rotationYawHead = ff6;
	        		}
	        	}
	        	if(info.prevModelInfo != null && info.nextModelInfo != null && info.morphProgress >= 10 && info.morphProgress < 70)
	        	{
		            float ff2 = info.prevState.entInstance.renderYawOffset;
		            float ff3 = info.prevState.entInstance.rotationYaw;
		            float ff4 = info.prevState.entInstance.rotationPitch;
		            float ff5 = info.prevState.entInstance.prevRotationYawHead;
		            float ff6 = info.prevState.entInstance.rotationYawHead;
 
		            float fff2 = info.nextState.entInstance.renderYawOffset;
		            float fff3 = info.nextState.entInstance.rotationYaw;
		            float fff4 = info.nextState.entInstance.rotationPitch;
		            float fff5 = info.nextState.entInstance.prevRotationYawHead;
		            float fff6 = info.nextState.entInstance.rotationYawHead;

    	        	if((Minecraft.getMinecraft().currentScreen instanceof GuiInventory || Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && RenderManager.instance.playerViewY == 180.0F)
    	        	{
    	        		EntityLivingBase renderView = Minecraft.getMinecraft().renderViewEntity;
    	        		
    	        		info.nextState.entInstance.renderYawOffset = info.prevState.entInstance.renderYawOffset = renderView.renderYawOffset;
    	        		info.nextState.entInstance.rotationYaw = info.prevState.entInstance.rotationYaw = renderView.rotationYaw;
    	        		info.nextState.entInstance.rotationPitch = info.prevState.entInstance.rotationPitch = renderView.rotationPitch;
    	        		info.nextState.entInstance.prevRotationYawHead = info.prevState.entInstance.prevRotationYawHead = renderView.prevRotationYawHead;
    	        		info.nextState.entInstance.rotationYawHead = info.prevState.entInstance.rotationYawHead = renderView.rotationYawHead;
    	        		renderTick = 1.0F;
    	        	}

	        		info.prevModelInfo.forceRender(info.prevState.entInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
	        		info.nextModelInfo.forceRender(info.nextState.entInstance, 0.0D, -500.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
	        	
   	        		info.prevState.entInstance.renderYawOffset = ff2;
	        		info.prevState.entInstance.rotationYaw = ff3;
	        		info.prevState.entInstance.rotationPitch = ff4;
	        		info.prevState.entInstance.prevRotationYawHead = ff5;
	        		info.prevState.entInstance.rotationYawHead = ff6;
	        		
	        		info.nextState.entInstance.renderYawOffset = fff2;
	        		info.nextState.entInstance.rotationYaw = fff3;
	        		info.nextState.entInstance.rotationPitch = fff4;
	        		info.nextState.entInstance.prevRotationYawHead = fff5;
	        		info.nextState.entInstance.rotationYawHead = fff6;
	        		
	        		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.setMainModel(info.interimModel);
	        		
	        		Morph.proxy.tickHandlerClient.renderMorphInstance.doRender(event.entityPlayer, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, renderTick);
	        	}
	        	
//	        	event.entityPlayer.yOffset += Morph.proxy.tickHandlerClient.ySize;
	        	
	        	GL11.glPopMatrix();
	        	Morph.proxy.tickHandlerClient.renderingMorph = false;
	        }
		}
		else
		{
			ObfuscationReflectionHelper.setPrivateValue(Render.class, event.renderer, Morph.proxy.tickHandlerClient.playerRenderShadowSize, ObfHelper.shadowSize);
		}
		Morph.proxy.tickHandlerClient.renderingPlayer--;
		
	    BossStatus.healthScale = bossHealthScale;
	    BossStatus.statusBarLength = bossStatusBarTime;
	    BossStatus.bossName = bossName;
	    BossStatus.field_82825_d = randVar;
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(Morph.handRenderOverride == 1)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(mc.thePlayer.username))
			{
				if(!retoggleHandRender)
				{
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, mc.entityRenderer, 0.999D, ObfHelper.cameraZoom);
				}
				retoggleHandRender = true;

				MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.username);
				
				MovingObjectPosition mop = EntityHelper.getEntityLook(mc.renderViewEntity, mc.playerController.getBlockReachDistance(), false, event.partialTicks);
				
	            if (mc.renderViewEntity instanceof EntityPlayer && !mc.gameSettings.hideGUI && mop != null && !mc.renderViewEntity.isInsideOfMaterial(Material.water))
	            {
	                EntityPlayer entityplayer = (EntityPlayer)mc.renderViewEntity;
	                GL11.glDisable(GL11.GL_ALPHA_TEST);
	                if (!ForgeHooksClient.onDrawBlockHighlight(mc.renderGlobal, entityplayer, mop, 0, entityplayer.inventory.getCurrentItem(), event.partialTicks))
	                {
	                	mc.renderGlobal.drawSelectionBox(entityplayer, mop, 0, event.partialTicks);
	                }
	                GL11.glEnable(GL11.GL_ALPHA_TEST);
	            }
				
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				
	        	if(info.morphProgress <= 40)
	        	{
	        		if(info.prevModelInfo != null && info.morphProgress < 10)
	        		{
	        			RenderPlayer rend = (RenderPlayer)RenderManager.instance.getEntityRenderObject(mc.thePlayer);
	        			
	        			ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.prevModelInfo.getRenderer(), info.prevModelInfo.getRenderer().getClass(), info.prevState.entInstance);
	        			
	        			Morph.proxy.tickHandlerClient.renderHandInstance.progress = 1.0F;
	        			Morph.proxy.tickHandlerClient.renderHandInstance.setParent(rend);
	        			Morph.proxy.tickHandlerClient.renderHandInstance.resourceLoc = resourceLoc;
	        			Morph.proxy.tickHandlerClient.renderHandInstance.replacement = info.prevModelInfo.assumedArm;
	        			RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), Morph.proxy.tickHandlerClient.renderHandInstance);
	        			
	        			ObfHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);
	        			
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress + Morph.proxy.tickHandlerClient.renderTick) / 10F;
		        			Morph.proxy.tickHandlerClient.renderHandInstance.progress = progress;
				        	
		    	        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
		    	        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
	
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
		    	        	
		    	        	ObfHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);
		    	        	
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
		    	        	
			        	}
	    	        	RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), rend);
	        		}
	        	}
	        	else
	        	{
	        		if(info.nextModelInfo != null && info.morphProgress >= 70)
	        		{
	        			RenderPlayer rend = (RenderPlayer)RenderManager.instance.getEntityRenderObject(mc.thePlayer);
	        			
	        			ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(info.nextModelInfo.getRenderer(), info.nextModelInfo.getRenderer().getClass(), info.nextState.entInstance);
	        			
	        			Morph.proxy.tickHandlerClient.renderHandInstance.progress = 1.0F;
	        			Morph.proxy.tickHandlerClient.renderHandInstance.setParent(rend);
	        			Morph.proxy.tickHandlerClient.renderHandInstance.resourceLoc = resourceLoc;
	        			Morph.proxy.tickHandlerClient.renderHandInstance.replacement = info.nextModelInfo.assumedArm;
	        			RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), Morph.proxy.tickHandlerClient.renderHandInstance);
	        			
	        			ObfHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);
	        			
			        	if(info.getMorphing())
			        	{
				        	float progress = ((float)info.morphProgress - 70 + Morph.proxy.tickHandlerClient.renderTick) / 10F;
				        	
				        	if(progress > 1.0F)
				        	{
				        		progress = 1.0F;
				        	}
		        			Morph.proxy.tickHandlerClient.renderHandInstance.progress = 1.0F - progress;
				        	
		    	        	String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
		    	        	String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);
	
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);
		    	        	
		    	        	ObfHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);
		    	        	
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
		    	        	ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);
			        	}
	    	        	RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), rend);
	        		}
	        	}
	        	if(info.prevModelInfo != null && info.nextModelInfo != null && info.morphProgress >= 10 && info.morphProgress < 70)
	        	{
        			RenderPlayer rend = (RenderPlayer)RenderManager.instance.getEntityRenderObject(mc.thePlayer);
        			
        			ResourceLocation resourceLoc = RenderMorph.morphSkin;
        			
        			Morph.proxy.tickHandlerClient.renderHandInstance.progress = 1.0F;
        			Morph.proxy.tickHandlerClient.renderHandInstance.setParent(rend);
        			Morph.proxy.tickHandlerClient.renderHandInstance.resourceLoc = resourceLoc;
        			Morph.proxy.tickHandlerClient.renderHandInstance.replacement = ModelHelper.createMorphArm(info.interimModel, info.prevModelInfo.assumedArm, info.nextModelInfo.assumedArm, info.morphProgress, Morph.proxy.tickHandlerClient.renderTick); 
        			
        			RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), Morph.proxy.tickHandlerClient.renderHandInstance);
        			
        			ObfHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

    	        	RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), rend);
	        	}
			}
			else
			{
				if(retoggleHandRender)
				{
					retoggleHandRender = false;
					ObfuscationReflectionHelper.setPrivateValue(EntityRenderer.class, mc.entityRenderer, 1.0D, ObfHelper.cameraZoom);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onDrawBlockHighlight(DrawBlockHighlightEvent event)
	{
//		Minecraft mc = Minecraft.getMinecraft();
//		if(mc.renderViewEntity == mc.thePlayer)
//		{
//	        MorphInfo info1 = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.username);
//			if(info1 != null && mc.renderViewEntity == mc.thePlayer)
//			{
////				float prog = info1.morphProgress > 10 ? (((float)info1.morphProgress + event.partialTicks) / 60F) : 0.0F;
////				if(prog > 1.0F)
////				{
////					prog = 1.0F;
////				}
////				
////				prog = (float)Math.pow(prog, 2);
////				
////				float prev = info1.prevState != null && !(info1.prevState.entInstance instanceof EntityPlayer) ? info1.prevState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
////				float next = info1.nextState != null && !(info1.nextState.entInstance instanceof EntityPlayer) ? info1.nextState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
////				float ySize = mc.thePlayer.yOffset - (prev + (next - prev) * prog);
////				mc.thePlayer.lastTickPosY -= ySize;
////				mc.thePlayer.prevPosY -= ySize;
////				mc.thePlayer.posY -= ySize;
////				
////                double d0 = (double)mc.playerController.getBlockReachDistance();
////				event.context.drawSelectionBox(mc.thePlayer, mc.thePlayer.rayTrace(d0, (float)event.partialTicks), 0, event.partialTicks);
////				
////				mc.thePlayer.lastTickPosY += ySize;
////				mc.thePlayer.prevPosY += ySize;
////				mc.thePlayer.posY += ySize;
//				
//				event.setCanceled(true);
//			}
//		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onMouseEvent(MouseEvent event)
	{
		if(Morph.proxy.tickHandlerClient.selectorShow)
		{
			int k = event.dwheel;
			if(k != 0)
			{
				Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTimer = Morph.proxy.tickHandlerClient.scrollTime;
				
				if(GuiScreen.isShiftKeyDown())
				{
					Morph.proxy.tickHandlerClient.selectorSelectedHoriPrev = Morph.proxy.tickHandlerClient.selectorSelectedHori;
					if(k > 0)
					{
						Morph.proxy.tickHandlerClient.selectorSelectedHori--;
					}
					else
					{
						Morph.proxy.tickHandlerClient.selectorSelectedHori++;
					}
				}
				else
				{
					Morph.proxy.tickHandlerClient.selectorSelectedPrev = Morph.proxy.tickHandlerClient.selectorSelected;
					if(k > 0)
					{
						Morph.proxy.tickHandlerClient.selectorSelected--;
						if(Morph.proxy.tickHandlerClient.selectorSelected < 0)
						{
							Morph.proxy.tickHandlerClient.selectorSelected = Morph.proxy.tickHandlerClient.playerMorphCatMap.size() - 1;
						}
					}
					else
					{
						Morph.proxy.tickHandlerClient.selectorSelected++;
						if(Morph.proxy.tickHandlerClient.selectorSelected > Morph.proxy.tickHandlerClient.playerMorphCatMap.size() - 1)
						{
							Morph.proxy.tickHandlerClient.selectorSelected = 0;
						}
					}
				}
				event.setCanceled(true);
			}
		}
	}
	
	@ForgeSubscribe
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event)
	{
		if(Morph.hostileAbilityMode > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			ArrayList<Ability> mobAbilities = AbilityHandler.getEntityAbilities(event.entityLiving.getClass());
			boolean hostile = false;
			for(Ability ab : mobAbilities)
			{
				if(ab.getType().equalsIgnoreCase("hostile"))
				{
					hostile = true;
					break;
				}
			}
			if(hostile && event.target instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)event.target;
				if(Morph.proxy.tickHandlerServer.playerMorphInfo.containsKey(player.username))
				{
					MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
					if(!info.getMorphing() && info.morphProgress >= 80)
					{
						boolean playerHostile = false;
						for(Ability ab: info.morphAbilities)
						{
							if(ab.getType().equalsIgnoreCase("hostile"))
							{
								playerHostile = true;
								break;
							}
						}
						if(hostile && playerHostile)
						{
							if(info.nextState.entInstance.getClass() == event.entityLiving.getClass() && Morph.hostileAbilityMode == 2 || info.nextState.entInstance.getClass() != event.entityLiving.getClass() && Morph.hostileAbilityMode == 3)
							{
								return;
							}
							if(Morph.hostileAbilityMode == 4)
							{
								double dist = event.entityLiving.getDistanceToEntity(player);
								if(dist < Morph.hostileAbilityDistanceCheck)
								{
									return;
								}
							}
							event.entityLiving.setRevengeTarget(null);
							if(event.entityLiving instanceof EntityLiving)
							{
								((EntityLiving)event.entityLiving).setAttackTarget(null);
							}
						}
					}
				}
			}
		}
	}
	
	@ForgeSubscribe
	public void onPlayerSleep(PlayerSleepInBedEvent event)
	{
		EntityPlayer player = (EntityPlayer)event.entityPlayer;
		EnumStatus stats = EnumStatus.OTHER_PROBLEM;
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && Morph.proxy.tickHandlerServer.playerMorphInfo.containsKey(player.username))
		{
			event.result = stats;
			player.addChatMessage("You may not rest now, you are in morph");
		}
		else if(FMLCommonHandler.instance().getEffectiveSide().isClient() && Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(player.username))
		{
			event.result = stats;
		}
	}
	
	@ForgeSubscribe
	public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event)
	{
		if(event.entity instanceof EntityPlayer && event.name.equalsIgnoreCase("damage.hit"))
		{
			EntityPlayer player = (EntityPlayer)event.entity;
			if(FMLCommonHandler.instance().getEffectiveSide().isServer() && Morph.proxy.tickHandlerServer.playerMorphInfo.containsKey(player.username))
			{
				MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
				event.name = ObfHelper.invokeGetHurtSound(info.nextState.entInstance.getClass(), info.nextState.entInstance);
			}
			else if(FMLCommonHandler.instance().getEffectiveSide().isClient() && Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(player.username))
			{
				MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.username);
				event.name = ObfHelper.invokeGetHurtSound(info.nextState.entInstance.getClass(), info.nextState.entInstance);
			}
		}
	}
	
	@ForgeSubscribe
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(Morph.loseMorphsOnDeath == 1 && event.entityLiving instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)event.entityLiving;
				
				Morph.proxy.tickHandlerServer.playerMorphs.remove(player.username);
				MorphState state = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.username);
				
				MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);
				
				MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
				if(info != null && state != null)
				{
					MorphInfo info2 = new MorphInfo(player.username, info.nextState, state);
					info2.setMorphing(true);
					
					Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);
					
					PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
					
					player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
				}
			}
			if(event.source.getEntity() instanceof EntityPlayerMP && event.entityLiving != event.source.getEntity())
			{
				EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();
				
				if(EntityHelper.morphPlayer(player, event.entityLiving, true))
				{
					event.entityLiving.setDead();
				}
			}
		}
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.target instanceof EntityLivingBase)
		{
			System.out.println(event.target);
			Morph.proxy.tickHandlerServer.trackingEntities.add(new EntTracker((EntityLivingBase)event.target, "water", true));
		}
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
			
            //write data
            
			NBTTagCompound tag = Morph.proxy.tickHandlerServer.saveData;

            for(Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet())
            {
            	NBTTagCompound tag1 = new NBTTagCompound();
            	e.getValue().writeNBT(tag1);
            	tag.setCompoundTag(e.getKey() + "_morphData", tag1);
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
	
	public boolean retoggleHandRender;
	
}
