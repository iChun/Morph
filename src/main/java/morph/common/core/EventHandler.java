package morph.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.client.keybind.KeyEvent;
import ichun.common.core.network.PacketHandler;
import ichun.common.core.util.ObfHelper;
import morph.api.Ability;
import morph.client.model.ModelHelper;
import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import morph.common.Morph;
import morph.common.ability.AbilityFear;
import morph.common.ability.AbilityHandler;
import morph.common.ability.AbilityPotionEffect;
import morph.common.ability.AbilitySwim;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import morph.common.packet.PacketGuiInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class EventHandler
{

    public boolean forcedSpecialRenderCall;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event)
    {
        if(event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            if(Morph.proxy.tickHandlerClient.radialShow)
            {
                if(Morph.config.getInt("renderCrosshairInRadialMenu") == 1)
                {
                    double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
                    double magAcceptance = 0.8D;

                    double radialAngle = -720F;
                    double aSin = Math.toDegrees(Math.asin(Morph.proxy.tickHandlerClient.radialDeltaX));

                    if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
                    {
                        radialAngle = aSin;
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
                    {
                        radialAngle = 90D + (90D - aSin);
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
                    {
                        radialAngle = 180D - aSin;
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
                    {
                        radialAngle = 270D + (90D + aSin);
                    }

                    ScaledResolution reso = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

                    GL11.glTranslated(reso.getScaledWidth_double() / 2D, reso.getScaledHeight_double() / 2D, 0D);
                    GL11.glRotatef((float)radialAngle, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslated(-reso.getScaledWidth_double() / 2D, -reso.getScaledHeight_double() / 2D, 0D);
                    GL11.glTranslatef(0.0F, -((float)reso.getScaledHeight_double() / 2.85F * 0.675F * MathHelper.clamp_float((float)(mag / magAcceptance), 0.0F, 1.0F) + (MathHelper.clamp_float((float)((mag - magAcceptance) / (1.0D - magAcceptance)), 0.0F, 1.0F) * (float)reso.getScaledHeight_double() / 2.85F * (1F - 0.675F))), 0.0F);
                }
                else
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if(event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            if(Morph.proxy.tickHandlerClient.radialShow)
            {
                if(Morph.config.getInt("renderCrosshairInRadialMenu") == 1)
                {
                    double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
                    double magAcceptance = 0.8D;

                    double radialAngle = -720F;
                    double aSin = Math.toDegrees(Math.asin(Morph.proxy.tickHandlerClient.radialDeltaX));

                    if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
                    {
                        radialAngle = aSin;
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
                    {
                        radialAngle = 90D + (90D - aSin);
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
                    {
                        radialAngle = 180D - aSin;
                    }
                    else if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
                    {
                        radialAngle = 270D + (90D + aSin);
                    }

                    ScaledResolution reso = new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

                    GL11.glTranslatef(0.0F, ((float)reso.getScaledHeight_double() / 2.85F * 0.675F * MathHelper.clamp_float((float)(mag / magAcceptance), 0.0F, 1.0F) + (MathHelper.clamp_float((float)((mag - magAcceptance) / (1.0D - magAcceptance)), 0.0F, 1.0F) * (float)reso.getScaledHeight_double() / 2.85F * (1F - 0.675F))), 0.0F);
                    GL11.glTranslated(reso.getScaledWidth_double() / 2D, reso.getScaledHeight_double() / 2D, 0D);
                    GL11.glRotatef(-(float)radialAngle, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslated(-reso.getScaledWidth_double() / 2D, -reso.getScaledHeight_double() / 2D, 0D);


                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderHand(RenderHandEvent event)
    {
        if(Morph.config.getInt("handRenderOverride") == 1)
        {
            GL11.glPushMatrix();
            Minecraft mc = Minecraft.getMinecraft();
            if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(mc.thePlayer.getCommandSenderName()))
            {
                event.setCanceled(true);

                MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.getCommandSenderName());

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

                        EntityHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

                        if(info.getMorphing())
                        {
                            float progress = ((float)info.morphProgress + Morph.proxy.tickHandlerClient.renderTick) / 10F;
                            Morph.proxy.tickHandlerClient.renderHandInstance.progress = progress;

                            String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
                            String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);

                            ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
                            ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);

                            EntityHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

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

                        EntityHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

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

                            EntityHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

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

                    EntityHelper.invokeRenderHand(mc.entityRenderer, Morph.proxy.tickHandlerClient.renderTick);

                    RenderManager.instance.entityRenderMap.put(mc.thePlayer.getClass(), rend);
                }
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

        if(Morph.proxy.tickHandlerClient.allowRender)
        {
            Morph.proxy.tickHandlerClient.allowRender = false;
            ObfuscationReflectionHelper.setPrivateValue(Render.class, event.renderer, Morph.proxy.tickHandlerClient.playerRenderShadowSize, ObfHelper.shadowSize);
            return;
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

        Morph.proxy.tickHandlerClient.renderingPlayer++;

        if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.getCommandSenderName()))
        {
            MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(event.entityPlayer.getCommandSenderName());
            if(!event.entityPlayer.isPlayerSleeping())
            {
                info.player = event.entityPlayer;
            }

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
                shadowProg /= 0.8F;
                if(shadowProg < 1.0F)
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

                float prevEntSize = info.prevState.entInstance != null ? info.prevState.entInstance.width > info.prevState.entInstance.height ? info.prevState.entInstance.width : info.prevState.entInstance.height : 1.0F;
                float nextEntSize = info.nextState.entInstance != null ? info.nextState.entInstance.width > info.nextState.entInstance.height ? info.nextState.entInstance.width : info.nextState.entInstance.height : 1.0F;

                float prevScaleMag = prevEntSize > 2.5F ? (2.5F / prevEntSize) : 1.0F;
                float nextScaleMag = nextEntSize > 2.5F ? (2.5F / nextEntSize) : 1.0F;

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
                            GL11.glScalef(prevScaleMag, prevScaleMag, prevScaleMag);

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
                            GL11.glScalef(nextScaleMag, nextScaleMag, nextScaleMag);

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
                    float progress = ((float)info.morphProgress - 10F + Morph.proxy.tickHandlerClient.renderTick) / 60F;

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
                        GL11.glScalef(prevScaleMag + (nextScaleMag - prevScaleMag) * progress, prevScaleMag + (nextScaleMag - prevScaleMag) * progress, prevScaleMag + (nextScaleMag - prevScaleMag) * progress);

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
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSpecials(RenderLivingEvent.Specials.Pre event)
    {
        Iterator<Entry<String, MorphInfoClient>> ite = Morph.proxy.tickHandlerClient.playerMorphInfo.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry<String, MorphInfoClient> e = ite.next();
            if(e.getValue().nextState.entInstance == event.entity || e.getValue().prevState != null && e.getValue().prevState.entInstance == event.entity)
            {
                if(e.getValue().prevState != null && e.getValue().prevState.entInstance instanceof EntityPlayer && !((EntityPlayer)e.getValue().prevState.entInstance).getCommandSenderName().equals(e.getKey()))
                {
                    event.setCanceled(true);
                }
                EntityPlayer player = event.entity.worldObj.getPlayerEntityByName(e.getKey());
                if(player != null && !(e.getValue().nextState.entInstance instanceof EntityPlayer && ((EntityPlayer)e.getValue().nextState.entInstance).getCommandSenderName().equals(e.getKey())))
                {
                    if(Morph.config.getSessionInt("showPlayerLabel") == 1)
                    {
                        if(e.getValue().nextState.entInstance instanceof EntityPlayer && !((EntityPlayer)e.getValue().nextState.entInstance).getCommandSenderName().equals(e.getKey()))
                        {
                            event.setCanceled(true);
                        }
                        RenderPlayer rend = (RenderPlayer)RenderManager.instance.getEntityRenderObject(player);

                        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

                        if(Minecraft.isGuiEnabled() && player != Minecraft.getMinecraft().thePlayer && !player.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && player.riddenByEntity == null)
                        {
                            float f = 1.6F;
                            float f1 = 0.016666668F * f;
                            double d3 = player.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer);
                            float f2 = player.isSneaking() ? RendererLivingEntity.NAME_TAG_RANGE_SNEAK : RendererLivingEntity.NAME_TAG_RANGE;

                            if(d3 < (double)(f2 * f2))
                            {
                                String s = player.func_145748_c_().getFormattedText();
                                ObfHelper.invokeRenderLivingLabel(rend, player, event.x, event.y, event.z, s, f1, d3);
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    //	@SideOnly(Side.CLIENT)
    //	@ForgeSubscribe
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
    @SubscribeEvent
    public void onKeyBindEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed())
        {
            if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorUp")) || event.keyBind.equals(Morph.config.getKeyBind("keySelectorDown")))
            {
                Morph.proxy.tickHandlerClient.abilityScroll = 0;
                if(!Morph.proxy.tickHandlerClient.selectorShow && mc.currentScreen == null)
                {
                    Morph.proxy.tickHandlerClient.selectorShow = true;
                    Morph.proxy.tickHandlerClient.selectorTimer = Morph.proxy.tickHandlerClient.selectorShowTime - Morph.proxy.tickHandlerClient.selectorTimer;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTime;

                    Morph.proxy.tickHandlerClient.selectorSelected = 0;
                    Morph.proxy.tickHandlerClient.selectorSelectedHori = 0;

                    MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.getCommandSenderName());
                    if(info != null)
                    {
                        MorphState state = info.nextState;

                        String entName = state.entInstance.getCommandSenderName();

                        int i = 0;

                        Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                        while(ite.hasNext())
                        {
                            Entry<String, ArrayList<MorphState>> e = ite.next();
                            if(e.getKey().equalsIgnoreCase(entName))
                            {
                                Morph.proxy.tickHandlerClient.selectorSelected = i;
                                ArrayList<MorphState> states = e.getValue();

                                for(int j = 0; j < states.size(); j++)
                                {
                                    if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
                                    {
                                        Morph.proxy.tickHandlerClient.selectorSelectedHori = j;
                                        break;
                                    }
                                }

                                break;
                            }
                            i++;
                        }
                    }
                }
                else
                {
                    Morph.proxy.tickHandlerClient.selectorSelectedHori = 0;
                    Morph.proxy.tickHandlerClient.selectorSelectedPrev = Morph.proxy.tickHandlerClient.selectorSelected;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTimer = Morph.proxy.tickHandlerClient.scrollTime;

                    if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorUp")))
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
            }
            else if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorLeft")) || event.keyBind.equals(Morph.config.getKeyBind("keySelectorRight")))
            {
                Morph.proxy.tickHandlerClient.abilityScroll = 0;
                if(!Morph.proxy.tickHandlerClient.selectorShow && mc.currentScreen == null)
                {
                    Morph.proxy.tickHandlerClient.selectorShow = true;
                    Morph.proxy.tickHandlerClient.selectorTimer = Morph.proxy.tickHandlerClient.selectorShowTime - Morph.proxy.tickHandlerClient.selectorTimer;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTime;

                    Morph.proxy.tickHandlerClient.selectorSelected = 0;
                    Morph.proxy.tickHandlerClient.selectorSelectedHori = 0;

                    MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.getCommandSenderName());
                    if(info != null)
                    {
                        MorphState state = info.nextState;

                        String entName = state.entInstance.getCommandSenderName();

                        int i = 0;

                        Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                        while(ite.hasNext())
                        {
                            Entry<String, ArrayList<MorphState>> e = ite.next();
                            if(e.getKey().equalsIgnoreCase(entName))
                            {
                                Morph.proxy.tickHandlerClient.selectorSelected = i;
                                ArrayList<MorphState> states = e.getValue();

                                for(int j = 0; j < states.size(); j++)
                                {
                                    if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
                                    {
                                        Morph.proxy.tickHandlerClient.selectorSelectedHori = j;
                                        break;
                                    }
                                }

                                break;
                            }
                            i++;
                        }
                    }
                }
                else
                {
                    Morph.proxy.tickHandlerClient.selectorSelectedHoriPrev = Morph.proxy.tickHandlerClient.selectorSelectedHori;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTime;

                    if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorLeft")))
                    {
                        Morph.proxy.tickHandlerClient.selectorSelectedHori--;
                    }
                    else
                    {
                        Morph.proxy.tickHandlerClient.selectorSelectedHori++;
                    }
                }
            }
            else if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorSelect")) || event.keyBind.keyIndex == mc.gameSettings.keyBindAttack.getKeyCode())
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorTimer = Morph.proxy.tickHandlerClient.selectorShowTime - Morph.proxy.tickHandlerClient.selectorTimer;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTime;

                    MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

                    MorphState selectedState = null;

                    int i = 0;

                    Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                    while(ite.hasNext())
                    {
                        Entry<String, ArrayList<MorphState>> e = ite.next();
                        if(i == Morph.proxy.tickHandlerClient.selectorSelected)
                        {
                            ArrayList<MorphState> states = e.getValue();

                            for(int j = 0; j < states.size(); j++)
                            {
                                if(j == Morph.proxy.tickHandlerClient.selectorSelectedHori)
                                {
                                    selectedState = states.get(j);
                                    break;
                                }
                            }

                            break;
                        }
                        i++;
                    }

                    if(selectedState != null && (info != null && !info.nextState.identifier.equalsIgnoreCase(selectedState.identifier) || info == null && !selectedState.playerMorph.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        PacketHandler.sendToServer(Morph.channels, new PacketGuiInput(0, selectedState.identifier, false));
                    }

                }
                else if(Morph.proxy.tickHandlerClient.radialShow)
                {
                    Morph.proxy.tickHandlerClient.selectRadialMenu();
                    Morph.proxy.tickHandlerClient.radialShow = false;
                }
            }
            else if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorCancel")) || event.keyBind.keyIndex == mc.gameSettings.keyBindUseItem.getKeyCode())
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    if(mc.currentScreen instanceof GuiIngameMenu)
                    {
                        mc.displayGuiScreen(null);
                    }
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorTimer = Morph.proxy.tickHandlerClient.selectorShowTime - Morph.proxy.tickHandlerClient.selectorTimer;
                    Morph.proxy.tickHandlerClient.scrollTimerHori = Morph.proxy.tickHandlerClient.scrollTime;
                }
                if(Morph.proxy.tickHandlerClient.radialShow)
                {
                    Morph.proxy.tickHandlerClient.radialShow = false;
                }
            }
            else if(event.keyBind.equals(Morph.config.getKeyBind("keySelectorRemoveMorph")) || event.keyBind.keyIndex == Keyboard.KEY_DELETE)
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

                    MorphState selectedState = null;

                    int i = 0;

                    Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                    boolean multiple = false;
                    boolean decrease = false;

                    while(ite.hasNext())
                    {
                        Entry<String, ArrayList<MorphState>> e = ite.next();
                        if(i == Morph.proxy.tickHandlerClient.selectorSelected)
                        {
                            ArrayList<MorphState> states = e.getValue();

                            for(int j = 0; j < states.size(); j++)
                            {
                                if(j == Morph.proxy.tickHandlerClient.selectorSelectedHori)
                                {
                                    selectedState = states.get(j);
                                    if(j == states.size() - 1)
                                    {
                                        decrease = true;
                                    }
                                    break;
                                }
                            }

                            if(states.size() > 1)
                            {
                                multiple = true;
                            }

                            break;
                        }
                        i++;
                    }

                    if(selectedState != null && !selectedState.isFavourite && ((info == null || info != null && !info.nextState.identifier.equalsIgnoreCase(selectedState.identifier)) && !selectedState.playerMorph.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        PacketHandler.sendToServer(Morph.channels, new PacketGuiInput(1, selectedState.identifier, false));

                        if(!multiple)
                        {
                            Morph.proxy.tickHandlerClient.selectorSelected--;
                            if(Morph.proxy.tickHandlerClient.selectorSelected < 0)
                            {
                                Morph.proxy.tickHandlerClient.selectorSelected = Morph.proxy.tickHandlerClient.playerMorphCatMap.size() - 1;
                            }
                        }
                        else if(decrease)
                        {
                            Morph.proxy.tickHandlerClient.selectorSelectedHori--;
                            if(Morph.proxy.tickHandlerClient.selectorSelected < 0)
                            {
                                Morph.proxy.tickHandlerClient.selectorSelected = 0;
                            }
                        }
                    }
                }
            }
            else if(event.keyBind.equals(Morph.config.getKeyBind("keyFavourite")))
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    MorphState selectedState = null;

                    int i = 0;

                    Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                    while(ite.hasNext())
                    {
                        Entry<String, ArrayList<MorphState>> e = ite.next();
                        if(i == Morph.proxy.tickHandlerClient.selectorSelected)
                        {
                            ArrayList<MorphState> states = e.getValue();

                            for(int j = 0; j < states.size(); j++)
                            {
                                if(j == Morph.proxy.tickHandlerClient.selectorSelectedHori)
                                {
                                    selectedState = states.get(j);
                                    break;
                                }
                            }

                            break;
                        }
                        i++;
                    }

                    if(selectedState != null && !selectedState.playerMorph.equalsIgnoreCase(selectedState.playerName))
                    {
                        selectedState.isFavourite = !selectedState.isFavourite;

                        PacketHandler.sendToServer(Morph.channels, new PacketGuiInput(2, selectedState.identifier, selectedState.isFavourite));
                    }
                }
                else if(mc.currentScreen == null)
                {
                    Morph.proxy.tickHandlerClient.favouriteStates.clear();

                    Iterator<Entry<String, ArrayList<MorphState>>> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.entrySet().iterator();

                    while(ite.hasNext())
                    {
                        Entry<String, ArrayList<MorphState>> e = ite.next();
                        ArrayList<MorphState> states = e.getValue();

                        for(int j = 0; j < states.size(); j++)
                        {
                            if(states.get(j).isFavourite)
                            {
                                Morph.proxy.tickHandlerClient.favouriteStates.add(states.get(j));
                            }
                        }
                    }

                    Morph.proxy.tickHandlerClient.radialPlayerYaw = mc.renderViewEntity.rotationYaw;
                    Morph.proxy.tickHandlerClient.radialPlayerPitch = mc.renderViewEntity.rotationPitch;

                    Morph.proxy.tickHandlerClient.radialDeltaX = Morph.proxy.tickHandlerClient.radialDeltaY = 0;

                    Morph.proxy.tickHandlerClient.radialShow = true;
                    Morph.proxy.tickHandlerClient.radialTime = 3;
                }
            }
        }
        else if(event.keyBind.equals(Morph.config.getKeyBind("keyFavourite")))
        {
            if(Morph.proxy.tickHandlerClient.radialShow)
            {
                Morph.proxy.tickHandlerClient.selectRadialMenu();
                Morph.proxy.tickHandlerClient.radialShow = false;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
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
        else if(Morph.proxy.tickHandlerClient.radialShow)
        {
            Morph.proxy.tickHandlerClient.radialDeltaX += event.dx / 100D;
            Morph.proxy.tickHandlerClient.radialDeltaY += event.dy / 100D;

            double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
            if(mag > 1.0D)
            {
                Morph.proxy.tickHandlerClient.radialDeltaX /= mag;
                Morph.proxy.tickHandlerClient.radialDeltaY /= mag;
            }
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event)
    {
        if(!event.entityLiving.worldObj.isRemote)
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
            if(event.target instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer)event.target;
                MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);

                if(info != null)
                {
                    for(Ability ab : info.morphAbilities)
                    {
                        if(ab instanceof AbilityFear)
                        {
                            AbilityFear abFear = (AbilityFear)ab;
                            for(Class clz : abFear.classList)
                            {
                                if(clz.isInstance(event.entityLiving))
                                {
                                    event.entityLiving.setRevengeTarget(null);
                                    if(event.entityLiving instanceof EntityLiving)
                                    {
                                        ((EntityLiving)event.entityLiving).setAttackTarget(null);
                                    }
                                }
                            }
                            break;
                        }
                    }

                    if(Morph.config.getInt("hostileAbilityMode") > 0 && hostile)
                    {
                        if(!info.getMorphing() && info.morphProgress >= 80)
                        {
                            boolean playerHostile = false;
                            for(Ability ab : info.morphAbilities)
                            {
                                if(ab.getType().equalsIgnoreCase("hostile"))
                                {
                                    playerHostile = true;
                                    break;
                                }
                            }
                            if(hostile && playerHostile)
                            {
                                if(info.nextState.entInstance.getClass() == event.entityLiving.getClass() && Morph.config.getInt("hostileAbilityMode") == 2 || info.nextState.entInstance.getClass() != event.entityLiving.getClass() && Morph.config.getInt("hostileAbilityMode") == 3)
                                {
                                    return;
                                }
                                if(Morph.config.getInt("hostileAbilityMode") == 4)
                                {
                                    double dist = event.entityLiving.getDistanceToEntity(player);
                                    if(dist < Morph.config.getInt("hostileAbilityDistanceCheck"))
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
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSetupFog(EntityViewRenderEvent.FogColors event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if(player != null && Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.getCommandSenderName()) != null)
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.getCommandSenderName());
            if(!info.getMorphing() && info.morphProgress >= 80 || info.getMorphing() && info.morphProgress <= 80)
            {
                for(Ability ab: info.morphAbilities)
                {
                    if(ab.getType().equalsIgnoreCase("swim"))
                    {
                        AbilitySwim abilitySwim = (AbilitySwim)ab;
                        if(!abilitySwim.canSurviveOutOfWater)
                        {
                            if(player.isInWater())
                            {
                                float multi = 7.5F;

                                boolean hasSwim = false;
                                ArrayList<Ability> mobAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        hasSwim = true;
                                        break;
                                    }
                                }

                                boolean alsoHasSwim = false;
                                mobAbilities = AbilityHandler.getEntityAbilities(info.prevState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        alsoHasSwim = true;
                                        break;
                                    }
                                }

                                if(info.getMorphing())
                                {
                                    if(!hasSwim)
                                    {
                                        multi -= 6.5F * MathHelper.clamp_float((float)info.morphProgress / 80F, 0.0F, 1.0F);
                                    }
                                    else if(!alsoHasSwim)
                                    {
                                        multi -= 6.5F * MathHelper.clamp_float((80F - (float)info.morphProgress) / 80F, 0.0F, 1.0F);
                                    }
                                }

                                event.red *= multi;
                                event.blue *= multi;
                                event.green *= multi;
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSetupFog(EntityViewRenderEvent.FogDensity event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if(player != null && Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.getCommandSenderName()) != null)
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.getCommandSenderName());
            if(!info.getMorphing() && info.morphProgress >= 80 || info.getMorphing() && info.morphProgress <= 80)
            {
                for(Ability ab: info.morphAbilities)
                {
                    if(ab.getType().equalsIgnoreCase("swim"))
                    {
                        AbilitySwim abilitySwim = (AbilitySwim)ab;
                        if(!abilitySwim.canSurviveOutOfWater)
                        {
                            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                            if(player.isInWater())
                            {
                                event.density = 0.025F;

                                boolean hasSwim = false;
                                ArrayList<Ability> mobAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        hasSwim = true;
                                        break;
                                    }
                                }

                                boolean alsoHasSwim = false;
                                mobAbilities = AbilityHandler.getEntityAbilities(info.prevState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        alsoHasSwim = true;
                                        break;
                                    }
                                }

                                if(info.getMorphing())
                                {
                                    if(!hasSwim)
                                    {
                                        event.density += 0.05F * MathHelper.clamp_float((float)info.morphProgress / 80F, 0.0F, 1.0F);
                                    }
                                    else if(!alsoHasSwim)
                                    {
                                        event.density += 0.05F * MathHelper.clamp_float((80F - (float)info.morphProgress) / 80F, 0.0F, 1.0F);
                                    }
                                }
                            }
                            else
                            {
                                event.density = 0.075F;

                                boolean hasSwim = false;
                                ArrayList<Ability> mobAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        hasSwim = true;
                                        break;
                                    }
                                }

                                boolean alsoHasSwim = false;
                                mobAbilities = AbilityHandler.getEntityAbilities(info.prevState.entInstance.getClass());
                                for(Ability ab1 : mobAbilities)
                                {
                                    if(ab1.getType().equalsIgnoreCase("swim"))
                                    {
                                        alsoHasSwim = true;
                                        break;
                                    }
                                }

                                if(info.getMorphing())
                                {
                                    if(!hasSwim)
                                    {
                                        event.density -= 0.073F * MathHelper.clamp_float((float)info.morphProgress / 80F, 0.0F, 1.0F);
                                    }
                                    else if(!alsoHasSwim)
                                    {
                                        event.density -= 0.073F * MathHelper.clamp_float((80F - (float)info.morphProgress) / 80F, 0.0F, 1.0F);
                                    }
                                }
                            }
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event)
    {
        EntityPlayer player = (EntityPlayer)event.entityPlayer;
        EnumStatus stats = EnumStatus.OTHER_PROBLEM;
        if(Morph.config.getSessionInt("canSleepMorphed") == 0)
        {
            if(FMLCommonHandler.instance().getEffectiveSide().isServer() && Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player) != null)
            {
                event.result = stats;
                player.addChatMessage(new ChatComponentTranslation("morph.denySleep"));
            }
            else if(FMLCommonHandler.instance().getEffectiveSide().isClient() && Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(player.getCommandSenderName()))
            {
                event.result = stats;
            }
        }
    }

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event)
    {
        if(event.entity instanceof EntityPlayer && event.name.equalsIgnoreCase("damage.hit"))
        {
            EntityPlayer player = (EntityPlayer)event.entity;
            if(FMLCommonHandler.instance().getEffectiveSide().isServer() && Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player) != null)
            {
                MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
                event.name = EntityHelper.getHurtSound(info.nextState.entInstance.getClass(), info.nextState.entInstance);
            }
            else if(FMLCommonHandler.instance().getEffectiveSide().isClient() && Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(player.getCommandSenderName()))
            {
                MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.getCommandSenderName());
                event.name = EntityHelper.getHurtSound(info.nextState.entInstance.getClass(), info.nextState.entInstance);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event)
    {
        if(event.source.getEntity() instanceof EntityPlayerMP && !(event.source instanceof EntityDamageSourceIndirect))
        {
            EntityPlayer player = (EntityPlayer)event.source.getEntity();
            MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);

            if(info != null && player.getCurrentEquippedItem() == null)
            {
                for(Ability ab : info.morphAbilities)
                {
                    if(ab instanceof AbilityPotionEffect)
                    {
                        AbilityPotionEffect abPot = (AbilityPotionEffect)ab;
                        event.entityLiving.addPotionEffect(new PotionEffect(abPot.potionId, abPot.duration, abPot.amplifier, abPot.ambient));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entityLiving.worldObj.isRemote)
        {
            if(Morph.config.getInt("loseMorphsOnDeath") >= 1 && event.entityLiving instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP)event.entityLiving;

                MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);

                MorphState state = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player);

                if(Morph.config.getInt("loseMorphsOnDeath") == 1)
                {
                    Morph.proxy.tickHandlerServer.removeAllPlayerMorphsExcludingCurrentMorph(player);
                }
                else if(info != null && info.nextState != state)
                {
                    ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player);
                    states.remove(info.nextState);
                }

                MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);

                if(info != null && state != null)
                {
                    MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), info.nextState, state);
                    info2.setMorphing(true);
                    info2.healthOffset = info.healthOffset;

                    Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player, info2);

                    PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

                    player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
                }
            }
            if(event.source.getEntity() instanceof EntityPlayerMP && event.entityLiving != event.source.getEntity())
            {
                EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();

                EntityLivingBase living = event.entityLiving;

                if(event.entityLiving instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player1 = (EntityPlayerMP)event.entityLiving;

                    MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player1);
                    if(info != null)
                    {
                        if(info.getMorphing())
                        {
                            living = info.prevState.entInstance;
                        }
                        else
                        {
                            living = info.nextState.entInstance;
                        }
                    }
                }

                if(EntityHelper.morphPlayer(player, living, true) && !(event.entityLiving instanceof EntityPlayerMP) && !(event.entityLiving instanceof IBossDisplayData))
                {
                    living.setDead();
                }
            }
            if(Morph.classToKillForFlight != null && Morph.classToKillForFlight.isInstance(event.entityLiving)|| Morph.classToKillForFlight == null &&  event.entityLiving instanceof EntityWither)
            {
                if(event.source.getEntity() instanceof EntityPlayerMP)
                {
                    EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();

                    boolean firstKill = !Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player).getBoolean("hasKilledWither");
                    Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player).setBoolean("hasKilledWither", true);
                    if(Morph.config.getInt("disableEarlyGameFlight") == 2 && firstKill)
                    {
                        Morph.proxy.tickHandlerServer.updateSession(player);
                    }
                }

                if(!Morph.proxy.tickHandlerServer.saveData.hasKilledWither)
                {
                    Morph.proxy.tickHandlerServer.saveData.hasKilledWither = true;
                    Morph.proxy.tickHandlerServer.saveData.markDirty();
                    if(Morph.config.getInt("disableEarlyGameFlight") == 2)
                    {
                        Morph.config.updateSession("allowFlight", 1);
                        Morph.proxy.tickHandlerServer.updateSession(null);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteract(EntityInteractEvent event)
    {
        //		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.target instanceof EntityLivingBase)
        //		{
        //			System.out.println(event.target);
        //			Morph.proxy.tickHandlerServer.trackingEntities.add();
        //		}
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

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.world.provider.dimensionId == 0)
        {
            WorldServer world = (WorldServer)event.world;
            MorphSaveData saveData = (MorphSaveData)world.perWorldStorage.loadData(MorphSaveData.class, "MorphSaveData");

            if(saveData == null)
            {
                saveData = new MorphSaveData("MorphSaveData");
                world.perWorldStorage.setData("MorphSaveData", saveData);
            }

            Morph.proxy.tickHandlerServer.saveData = saveData;

            if(Morph.config.getInt("disableEarlyGameFlight") == 1 && !Morph.proxy.tickHandlerServer.saveData.hasTravelledToNether || Morph.config.getInt("disableEarlyGameFlight") == 2 && !Morph.proxy.tickHandlerServer.saveData.hasKilledWither)
            {
                Morph.config.updateSession("allowFlight", 0);
            }
        }
    }

    //ConnectionHandler stuff

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        onClientConnection();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        onClientConnection();
    }

    public void onClientConnection()
    {
        Morph.config.resetSession();
        Morph.proxy.tickHandlerClient.playerMorphInfo.clear();
        Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
    }

    //IPlayerTracker stuff

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Morph.proxy.tickHandlerServer.updateSession(event.player);

        ArrayList list = Morph.proxy.tickHandlerServer.getPlayerMorphs(event.player.worldObj, event.player);

        NBTTagCompound tag = Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player);

        MorphHandler.addOrGetMorphState(list, new MorphState(event.player.worldObj, event.player.getCommandSenderName(), event.player.getCommandSenderName(), null, event.player.worldObj.isRemote));

        int count = tag.getInteger("morphStatesCount");
        for(int i = 0; i < count; i++)
        {
            MorphState state = new MorphState(event.player.worldObj, event.player.getCommandSenderName(), event.player.getCommandSenderName(), null, false);
            state.readTag(event.player.worldObj, tag.getCompoundTag("morphState" + i));
            if(!state.identifier.equalsIgnoreCase(""))
            {
                MorphHandler.addOrGetMorphState(list, state);
            }
        }

        NBTTagCompound tag1 = tag.getCompoundTag("morphData");
        if(tag1.hasKey("playerName"))
        {
            MorphInfo info = new MorphInfo();
            info.readNBT(tag1);
            if(!info.nextState.playerName.equals(info.nextState.playerMorph))
            {
                Morph.proxy.tickHandlerServer.setPlayerMorphInfo(event.player, info);
                MorphHandler.addOrGetMorphState(list, info.nextState);

                PacketHandler.sendToAll(Morph.channels, info.getMorphInfoAsPacket());
            }
        }

        MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)event.player, null, true);
        for(Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet())
        {
            if(e.getKey().equalsIgnoreCase(event.player.getCommandSenderName()))
            {
                continue;
            }
            PacketHandler.sendToPlayer(Morph.channels, e.getValue().getMorphInfoAsPacket(), event.player);
        }

        MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(event.player);

        if(info != null)
        {
            ObfHelper.forceSetSize(event.player.getClass(), event.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            event.player.setPosition(event.player.posX, event.player.posY, event.player.posZ);
            event.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).getCommandSenderName().equalsIgnoreCase(event.player.getCommandSenderName()) ? event.player.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - event.player.yOffset;

            double nextMaxHealth = MathHelper.clamp_double(info.nextState.entInstance.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue(), 0D, 20D) + info.healthOffset;

            if(nextMaxHealth < 1D)
            {
                nextMaxHealth = 1D;
            }

            if(nextMaxHealth != event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue())
            {
                event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(nextMaxHealth);
                event.player.setHealth((float)nextMaxHealth);
            }
        }

    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(event.player.getCommandSenderName());
        if(info != null)
        {
            NBTTagCompound tag1 = new NBTTagCompound();
            info.writeNBT(tag1);
            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player).setTag("morphData", tag1);
        }

        ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.playerMorphs.get(event.player.getCommandSenderName());
        if(states != null)
        {
            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player).setInteger("morphStatesCount", states.size());
            for(int i = 0; i < states.size(); i++)
            {
                Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player).setTag("morphState" + i, states.get(i).getTag());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(event.player);

        if(info != null)
        {
            ObfHelper.forceSetSize(event.player.getClass(), event.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            event.player.setPosition(event.player.posX, event.player.posY, event.player.posZ);
            event.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).getCommandSenderName().equalsIgnoreCase(event.player.getCommandSenderName()) ? event.player.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - event.player.yOffset;

            double nextMaxHealth = MathHelper.clamp_double(info.nextState.entInstance.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue(), 0D, 20D) + info.healthOffset;

            if(nextMaxHealth < 1D)
            {
                nextMaxHealth = 1D;
            }

            if(nextMaxHealth != event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue())
            {
                event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(nextMaxHealth);
                event.player.setHealth((float)nextMaxHealth);
            }
        }
        if(event.player.dimension == -1 && Morph.proxy.tickHandlerServer.saveData != null)
        {
            boolean firstVisit = !Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player).getBoolean("hasTravelledToNether");
            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(event.player).setBoolean("hasTravelledToNether", true);
            if(Morph.config.getInt("disableEarlyGameFlight") == 1 && firstVisit)
            {
                Morph.proxy.tickHandlerServer.updateSession(event.player);
            }

            if(!Morph.proxy.tickHandlerServer.saveData.hasTravelledToNether)
            {
                Morph.proxy.tickHandlerServer.saveData.hasTravelledToNether = true;
                Morph.proxy.tickHandlerServer.saveData.markDirty();
                if(Morph.config.getInt("disableEarlyGameFlight") == 1)
                {
                    Morph.config.updateSession("allowFlight", 1);
                    Morph.proxy.tickHandlerServer.updateSession(null);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(event.player);

        if(info != null)
        {
            ObfHelper.forceSetSize(event.player.getClass(), event.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            event.player.setPosition(event.player.posX, event.player.posY, event.player.posZ);
            event.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).getCommandSenderName().equalsIgnoreCase(event.player.getCommandSenderName()) ? event.player.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - event.player.yOffset;

            double nextMaxHealth = MathHelper.clamp_double(info.nextState.entInstance.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue(), 0D, 20D) + info.healthOffset;

            if(nextMaxHealth < 1D)
            {
                nextMaxHealth = 1D;
            }

            if(nextMaxHealth != event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue())
            {
                event.player.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(nextMaxHealth);
                event.player.setHealth((float)nextMaxHealth);
            }
        }
    }
}
