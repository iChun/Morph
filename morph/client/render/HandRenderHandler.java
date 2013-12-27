package morph.client.render;

import morph.client.model.ModelHelper;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.core.EntityHelper;
import morph.common.core.ObfHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * This class is here for late registration of an event handler, due to the custom hand renderer clearing the depth buffer, meaning it needs to be one of the last to render.
 * Hacky workaround, yes, but it works 95% of the time.
 * @author iChun
 *
 */

public class HandRenderHandler 
{

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(Morph.handRenderOverride == 1)
		{
			GL11.glPushMatrix();
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
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public boolean retoggleHandRender;
}
