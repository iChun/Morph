package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.client.core.TickHandlerClient;
import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import me.ichun.mods.morph.common.packet.PacketGuiInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.client.keybind.KeyEvent;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;
import us.ichun.mods.ichunutil.common.core.event.RendererSafeCompatibilityEvent;
import us.ichun.mods.ichunutil.common.core.util.ObfHelper;

import java.util.Map;

public class EventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        for(Object obj : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet())
        {
            Map.Entry<Class, Render> e = (Map.Entry<Class, Render>)obj;
            Class clz = e.getKey();
            if(EntityLivingBase.class.isAssignableFrom(clz))
            {
                ModelHandler.dissectForModels(clz, e.getValue());
            }
            ModelHandler.mapPlayerModels();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed())
        {
            if(event.keyBind.equals(Morph.config.keySelectorUp) || event.keyBind.equals(Morph.config.keySelectorDown) || event.keyBind.equals(Morph.config.keySelectorLeft) || event.keyBind.equals(Morph.config.keySelectorRight))
            {
                Morph.proxy.tickHandlerClient.handleSelectorNavigation(event.keyBind);
            }
            else if(event.keyBind.equals(Morph.config.keySelectorSelect) || (event.keyBind.keyIndex == mc.gameSettings.keyBindAttack.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorShowTimer = TickHandlerClient.SELECTOR_SHOW_TIME - Morph.proxy.tickHandlerClient.selectorShowTimer;
                    Morph.proxy.tickHandlerClient.selectorScrollHoriTimer = TickHandlerClient.SELECTOR_SCROLL_TIME;

                    MorphState selectedState = Morph.proxy.tickHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.proxy.tickHandlerClient.morphsActive.get(mc.thePlayer.getCommandSenderName());

                    if(selectedState != null && (info != null && !info.nextState.currentVariant.equals(selectedState.currentVariant) || info == null && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 0, false));
                    }
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorCancel) || (event.keyBind.keyIndex == mc.gameSettings.keyBindUseItem.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    if(mc.currentScreen instanceof GuiIngameMenu)
                    {
                        mc.displayGuiScreen(null);
                    }
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorShowTimer = TickHandlerClient.SELECTOR_SHOW_TIME - Morph.proxy.tickHandlerClient.selectorShowTimer;
                    Morph.proxy.tickHandlerClient.selectorScrollHoriTimer = TickHandlerClient.SELECTOR_SCROLL_TIME;
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorRemoveMorph) || event.keyBind.keyIndex == Keyboard.KEY_DELETE)
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    MorphState selectedState = Morph.proxy.tickHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.proxy.tickHandlerClient.morphsActive.get(mc.thePlayer.getCommandSenderName());

                    if(selectedState != null && !selectedState.currentVariant.thisVariant.isFavourite && ((info == null || !info.nextState.currentVariant.thisVariant.identifier.equalsIgnoreCase(selectedState.currentVariant.thisVariant.identifier)) && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 2, false));
                    }
                }
            }
        }
        else
        {
            //RADIAL MENU
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseEvent(MouseEvent event)
    {
        if(Morph.proxy.tickHandlerClient.selectorShow)
        {
            int k = event.dwheel;
            if(k != 0)
            {
                KeyBind bind;
                if(GuiScreen.isShiftKeyDown())
                {
                    if(k > 0)
                    {
                        bind = Morph.config.keySelectorLeft;
                    }
                    else
                    {
                        bind = Morph.config.keySelectorRight;
                    }
                }
                else
                {
                    if(k > 0)
                    {
                        bind = Morph.config.keySelectorUp;
                    }
                    else
                    {
                        bind = Morph.config.keySelectorDown;
                    }
                }
                Morph.proxy.tickHandlerClient.handleSelectorNavigation(bind);
                event.setCanceled(true);
            }
        }
        //        else if(Morph.proxy.tickHandlerClient.radialShow)
        //        {
        //            Morph.proxy.tickHandlerClient.radialDeltaX += event.dx / 100D;
        //            Morph.proxy.tickHandlerClient.radialDeltaY += event.dy / 100D;
        //
        //            double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
        //            if(mag > 1.0D)
        //            {
        //                Morph.proxy.tickHandlerClient.radialDeltaX /= mag;
        //                Morph.proxy.tickHandlerClient.radialDeltaY /= mag;
        //            }
        //        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        if(Morph.proxy.tickHandlerClient.renderMorphDepth > 0) //It's trying to render a player while rendering a morph, allow it.
        {
            return;
        }

        MorphInfoClient info = Morph.proxy.tickHandlerClient.morphsActive.get(event.entityPlayer.getCommandSenderName());
        if(info != null)
        {
            event.setCanceled(true);

            if(event.entityPlayer.worldObj.playerEntities.contains(event.entityPlayer))
            {
                info.player = event.entityPlayer;
            }

            Minecraft mc = Minecraft.getMinecraft();

            Morph.proxy.tickHandlerClient.renderMorphDepth++;

            float f1 = EntityHelperBase.interpolateRotation(event.entityPlayer.prevRotationYaw, event.entityPlayer.rotationYaw, event.partialRenderTick);
            if(info.isMorphing())
            {
                if(info.morphTime < 10)
                {
                    float prog = (float)Math.pow((info.morphTime + event.partialRenderTick) / 10F, 2D);

                    EntityLivingBase entInstance = info.prevState.getEntInstance(event.entityPlayer.worldObj);
                    if(info.firstUpdate)
                    {
                        info.syncEntityWithPlayer(entInstance);
                    }
                    ModelInfo modelInfo = info.getPrevStateModel(event.entityPlayer.worldObj);
                    modelInfo.forceRender(entInstance, event.x, event.y, event.z, f1, event.partialRenderTick);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, prog);

                    ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), entInstance);
                    String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
                    String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);

                    modelInfo.forceRender(entInstance, event.x, event.y, event.z, f1, event.partialRenderTick);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    GlStateManager.disableAlpha();
                }
                else if(info.morphTime > Morph.config.morphTime - 10)
                {
                    float prog = (float)Math.pow(1F - ((info.morphTime + event.partialRenderTick) - (Morph.config.morphTime - 10)) / 10F, 2D);

                    EntityLivingBase entInstance = info.nextState.getEntInstance(event.entityPlayer.worldObj);
                    ModelInfo modelInfo = info.getNextStateModel(event.entityPlayer.worldObj);
                    modelInfo.forceRender(entInstance, event.x, event.y, event.z, f1, event.partialRenderTick);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, prog);

                    ResourceLocation resourceLoc = ObfHelper.invokeGetEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), entInstance);
                    String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
                    String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);

                    modelInfo.forceRender(entInstance, event.x, event.y, event.z, f1, event.partialRenderTick);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    GlStateManager.disableAlpha();
                }
                else
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(event.x, event.y + 1.5F, event.z);
                    GlStateManager.rotate(180F - EntityHelperBase.interpolateRotation(event.entityPlayer.prevRenderYawOffset, event.entityPlayer.renderYawOffset, event.partialRenderTick), 0F, 1F, 0F);
                    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                    ModelMorph model = info.getModelMorph(event.entityPlayer.worldObj);
                    float morphProgress = (float)Math.sin(Math.toRadians(MathHelper.clamp_float((info.morphTime - 10 + event.partialRenderTick) / (Morph.config.morphTime - 20F), 0.0F, 1.0F) * 90F));
                    model.render(event.partialRenderTick, morphProgress, info.prevState.getEntInstance(event.entityPlayer.worldObj), info.nextState.getEntInstance(event.entityPlayer.worldObj));
                    GlStateManager.popMatrix();
                }
            }
            else
            {
                ModelInfo modelInfo = info.getNextStateModel(event.entityPlayer.worldObj);
                modelInfo.forceRender(info.nextState.getEntInstance(event.entityPlayer.worldObj), event.x, event.y, event.z, f1, event.partialRenderTick);
            }

            Morph.proxy.tickHandlerClient.renderMorphDepth--;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(event.world.isRemote && event.world instanceof WorldClient)
        {
            //Clean up the Morph States and stuff like that to prevent mem leaks.
            for(MorphInfoClient info : Morph.proxy.tickHandlerClient.morphsActive.values())
            {
                info.clean();
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entityLiving.worldObj.isRemote)
        {
            if(event.source.getEntity() instanceof EntityPlayerMP && event.entityLiving != event.source.getEntity())
            {
                EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();

                if(PlayerMorphHandler.getInstance().canPlayerMorph(player))
                {
                    EntityLivingBase living = event.entityLiving; //entity to acquire

                    if(event.entityLiving instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player1 = (EntityPlayerMP)event.entityLiving;

                        MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(player1.getCommandSenderName());
                        if(info != null)
                        {
                            if(info.isMorphing() && info.prevState != null)
                            {
                                living = info.prevState.getEntInstance(player1.worldObj);
                            }
                            else
                            {
                                living = info.nextState.getEntInstance(player1.worldObj);
                            }
                        }
                    }

                    PlayerMorphHandler.getInstance().acquireMorph(player, living, Morph.config.instaMorph == 1, true);
                }
            }
        }
    }
}
