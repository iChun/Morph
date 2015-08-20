package me.ichun.mods.morph.client.core;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.client.render.RenderMorph;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.ability.AbilityPotionEffect;
import me.ichun.mods.morph.common.handler.AbilityHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.client.render.RendererHelper;
import us.ichun.mods.ichunutil.common.core.util.ResourceHelper;

import java.util.*;

public class TickHandlerClient
{
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.theWorld != null)
        {
            if(event.phase == TickEvent.Phase.START)
            {
            }
            else
            {
                renderMorphDepth++; //hacky fix to make the entity render on the selector
                drawSelector(mc, event.renderTickTime);
                renderMorphDepth--;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.theWorld != null)
            {
                if(!mc.isGamePaused())
                {
                    for(MorphInfoClient info : Morph.proxy.tickHandlerClient.morphsActive.values())
                    {
                        info.tick();
                    }
                    for(Map.Entry<String, ArrayList<MorphState>> e : Morph.proxy.tickHandlerClient.playerMorphs.entrySet())
                    {
                        for(MorphState state : e.getValue())
                        {
                            state.getEntInstance(mc.theWorld).ticksExisted++;
                        }
                    }
                }

                if(mc.currentScreen != null)
                {
                    if(selectorShow)
                    {
                        if(mc.currentScreen instanceof GuiIngameMenu)
                        {
                            mc.displayGuiScreen(null);
                        }
                        selectorShow = false;
                        selectorShowTimer = SELECTOR_SHOW_TIME - selectorShowTimer;
                        selectorScrollHoriTimer = SELECTOR_SCROLL_TIME;
                    }
                }
                abilityScroll++;
                if(selectorShowTimer > 0)
                {
                    selectorShowTimer--;
                }
                selectorScrollVertTimer--;
                selectorScrollHoriTimer--;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isClient() && event.phase == TickEvent.Phase.START)
        {
            MorphInfo info = morphsActive.get(event.player.getCommandSenderName());
            if(info != null)
            {
                info.player = event.player;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        //world is null, not connected to any worlds, get rid of objects for GC.
        morphsActive.clear();
        playerMorphs.clear();
    }

    public void drawSelector(Minecraft mc, float renderTick)
    {
        if((selectorShowTimer > 0 || selectorShow) && !mc.gameSettings.hideGUI)
        {
            if(selectorSelectedVert < 0)
            {
                selectorSelectedVert = 0;
            }
            while(selectorSelectedVert > playerMorphs.size() - 1)
            {
                selectorSelectedVert--;
            }

            GlStateManager.pushMatrix();

            float progress = MathHelper.clamp_float((SELECTOR_SHOW_TIME - (selectorShowTimer - renderTick)) / (float)SELECTOR_SHOW_TIME, 0F, 1F);

            if(selectorShow)
            {
                progress = 1.0F - progress;
            }

            if(selectorShow && selectorShowTimer < 0)
            {
                progress = 0.0F;
            }

            progress = (float)Math.pow(progress, 2D);

            GlStateManager.translate(-52F * progress, 0.0F, 0.0F);

            ScaledResolution reso = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

            int gap = (reso.getScaledHeight() - (42 * 5)) / 2;

            double size = 42D;
            double width1 = 0.0D;

            GlStateManager.pushMatrix();

            int maxShowable = (int)Math.ceil((double)reso.getScaledHeight() / size) + 2;

            if(selectorSelectedVert == 0 && selectorSelectedPrevVert > 0 || selectorSelectedPrevVert == 0 && selectorSelectedVert > 0)
            {
                maxShowable = 150;
            }

            float progressV = (SELECTOR_SCROLL_TIME - (selectorScrollVertTimer - renderTick)) / (float)SELECTOR_SCROLL_TIME;

            progressV = (float)Math.pow(progressV, 2D);

            if(progressV > 1.0F)
            {
                progressV = 1.0F;
                selectorSelectedPrevVert = selectorSelectedVert;
            }

            float progressH = (SELECTOR_SCROLL_TIME - (selectorScrollHoriTimer - renderTick)) / (float)SELECTOR_SCROLL_TIME;

            progressH = (float)Math.pow(progressH, 2D);

            if(progressH > 1.0F)
            {
                progressH = 1.0F;
                selectorSelectedPrevHori = selectorSelectedHori;
            }

            GlStateManager.translate(0.0F, ((selectorSelectedVert - selectorSelectedPrevVert) * 42F) * (1.0F - progressV), 0.0F);

            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableAlpha();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            int i = 0;

            Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();

            while(ite.hasNext())
            {
                Map.Entry<String, ArrayList<MorphState>> e = ite.next();

                if(i > selectorSelectedVert + maxShowable || i < selectorSelectedVert - maxShowable)
                {
                    i++;
                    continue;
                }

                double height1 = gap + size * (i - selectorSelectedVert);

                ArrayList<MorphState> states = e.getValue();
                if(states == null || states.isEmpty())
                {
                    ite.remove();
                    i++;
                    break;
                }

                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();

                if(i == selectorSelectedVert)
                {
                    if(selectorSelectedHori < 0)
                    {
                        selectorSelectedHori = states.size() - 1;
                    }
                    if(selectorSelectedHori >= states.size())
                    {
                        selectorSelectedHori = 0;
                    }

                    boolean newSlide = false;

                    if(progressV < 1.0F && selectorSelectedPrevVert != selectorSelectedVert)
                    {
                        selectorSelectedPrevHori = states.size() - 1;
                        selectorSelectedHori = 0;
                        newSlide = true;
                    }
                    if(!selectorShow)
                    {
                        selectorSelectedHori = states.size() - 1;
                        newSlide = true;
                    }
                    else if(progress > 0.0F)
                    {
                        selectorSelectedPrevHori = states.size() - 1;
                        newSlide = true;
                    }

                    for(int j = 0; j < states.size(); j++)
                    {
                        GlStateManager.pushMatrix();

                        GlStateManager.translate(newSlide && j == 0 ? 0.0D : ((selectorSelectedHori - selectorSelectedPrevHori) * 42F) * (1.0F - progressH), 0.0D, 0.0D);

                        mc.getTextureManager().bindTexture(states.size() == 1 || j == states.size() - 1 ? rlUnselected : rlUnselectedSide);

                        double dist = size * (j - selectorSelectedHori);

                        worldRenderer.startDrawingQuads();
                        worldRenderer.setColorOpaque_F(1F, 1F, 1F);
                        worldRenderer.addVertexWithUV(width1 + dist, height1 + size, -90.0D + j, 0.0D, 1.0D);
                        worldRenderer.addVertexWithUV(width1 + dist + size, height1 + size, -90.0D + j, 1.0D, 1.0D);
                        worldRenderer.addVertexWithUV(width1 + dist + size, height1, -90.0D + j, 1.0D, 0.0D);
                        worldRenderer.addVertexWithUV(width1 + dist, height1, -90.0D + j, 0.0D, 0.0D);
                        tessellator.draw();

                        GlStateManager.popMatrix();
                    }
                }
                else
                {
                    mc.getTextureManager().bindTexture(rlUnselected);
                    worldRenderer.startDrawingQuads();
                    worldRenderer.setColorOpaque_F(1F, 1F, 1F);
                    worldRenderer.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
                    worldRenderer.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
                    worldRenderer.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
                    worldRenderer.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
                    tessellator.draw();
                }
                i++;
            }

            GlStateManager.disableBlend();

            int height1;

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();

            gap += 36;

            i = 0;

            ite = playerMorphs.entrySet().iterator();

            while(ite.hasNext())
            {
                Map.Entry<String, ArrayList<MorphState>> e = ite.next();

                if(i > selectorSelectedVert + maxShowable || i < selectorSelectedVert - maxShowable)
                {
                    i++;
                    continue;
                }

                height1 = gap + (int)size * (i - selectorSelectedVert);

                ArrayList<MorphState> states = e.getValue();

                if(i == selectorSelectedVert)
                {
                    boolean newSlide = false;

                    if(progressV < 1.0F && selectorSelectedPrevVert != selectorSelectedVert)
                    {
                        selectorSelectedPrevHori = states.size() - 1;
                        selectorSelectedHori = 0;
                        newSlide = true;
                    }
                    if(!selectorShow)
                    {
                        selectorSelectedHori = states.size() - 1;
                        newSlide = true;
                    }

                    for(int j = 0; j < states.size(); j++)
                    {
                        MorphState state = states.get(j);
                        GlStateManager.pushMatrix();

                        double dist = size * (j - selectorSelectedHori);
                        GlStateManager.translate((newSlide && j == 0 ? 0.0D : ((selectorSelectedHori - selectorSelectedPrevHori) * 42F) * (1.0F - progressH)) + dist, 0.0D, 0.0D);

                        EntityLivingBase entInstance = state.getEntInstance(mc.theWorld);
                        float entSize = Math.max(entInstance.width, entInstance.height);
                        float prog = MathHelper.clamp_float(j - selectorSelectedHori == 0 ? (!selectorShow ? selectorScrollHoriTimer - renderTick : (3F - selectorScrollHoriTimer + renderTick)) / 3F : 0.0F, 0.0F, 1.0F);
                        float scaleMag = ((2.5F + (entSize - 2.5F) * prog) / entSize) ;

                        drawEntityOnScreen(state, entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, true, j == states.size() - 1);

                        GlStateManager.popMatrix();
                    }
                }
                else
                {
                    MorphState state = states.get(0);
                    EntityLivingBase entInstance = state.getEntInstance(mc.theWorld);
                    float entSize = Math.max(entInstance.width, entInstance.height);
                    float scaleMag = (2.5F / entSize);
                    drawEntityOnScreen(state, entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, selectorSelectedVert == i, true);
                }
                GlStateManager.translate(0.0F, 0.0F, 20F);
                i++;
            }
            GlStateManager.popMatrix();

            if(selectorShow)
            {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                gap -= 36;
                height1 = gap;

                mc.getTextureManager().bindTexture(rlSelected);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.startDrawingQuads();
                worldRenderer.setColorOpaque_F(1F, 1F, 1F);
                worldRenderer.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
                worldRenderer.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
                worldRenderer.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
                worldRenderer.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
                tessellator.draw();

                GlStateManager.disableBlend();
            }
            GlStateManager.popMatrix();
        }
    }

    public void handleSelectorNavigation(KeyBind bind)
    {
        Minecraft mc = Minecraft.getMinecraft();
        abilityScroll = 0;
        if(!selectorShow && mc.currentScreen == null) //show the selector.
        {
            selectorShow = true;
            selectorShowTimer = SELECTOR_SHOW_TIME - selectorShowTimer;
            selectorScrollVertTimer = selectorScrollHoriTimer = SELECTOR_SCROLL_TIME;
            selectorSelectedVert = selectorSelectedHori = 0; //Reset the selected selector position //TODO set to the lowest and outmost selector possible so that it scrolls to the selected?
        }
        else if(bind.equals(Morph.config.keySelectorUp) || bind.equals(Morph.config.keySelectorDown)) //Vertical scrolling
        {
            selectorSelectedHori = 0;
            selectorSelectedPrevVert = selectorSelectedVert;
            selectorScrollHoriTimer = selectorScrollVertTimer = SELECTOR_SCROLL_TIME;

            if(bind.equals(Morph.config.keySelectorUp))
            {
                selectorSelectedVert--;
                if(selectorSelectedVert < 0)
                {
                    selectorSelectedVert = playerMorphs.size() - 1;
                }
            }
            else
            {
                selectorSelectedVert++;
                if(selectorSelectedVert > playerMorphs.size() - 1)
                {
                    selectorSelectedVert = 0;
                }
            }
        }
        else //Horizontal scrolling
        {
            selectorSelectedPrevHori = selectorSelectedHori;
            selectorScrollHoriTimer = SELECTOR_SCROLL_TIME;

            if(bind.equals(Morph.config.keySelectorLeft))
            {
                selectorSelectedHori--;
            }
            else
            {
                selectorSelectedHori++;
            }

            int i = 0;
            Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<String, ArrayList<MorphState>> e = ite.next();

                if(i == selectorSelectedVert)
                {
                    ArrayList<MorphState> states = e.getValue();
                    if(selectorSelectedHori < 0)
                    {
                        selectorSelectedHori = states.size() - 1;
                    }
                    if(selectorSelectedHori >= states.size())
                    {
                        selectorSelectedHori = 0;
                    }
                    break;
                }
                i++;
            }
        }
    }

    public void drawEntityOnScreen(MorphState state, EntityLivingBase ent, int posX, int posY, float scale, float par4, float par5, float renderTick, boolean selected, boolean drawText)
    {
        forcePlayerRender = true;
        if(ent != null)
        {
            Minecraft mc = Minecraft.getMinecraft();
            boolean hideGui = mc.gameSettings.hideGUI;

            mc.gameSettings.hideGUI = true;

            GlStateManager.pushMatrix();

            GlStateManager.disableAlpha();

            GlStateManager.translate((float)posX, (float)posY, 50.0F);

            GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            float f2 = ent.renderYawOffset;
            float f3 = ent.rotationYaw;
            float f4 = ent.rotationPitch;
            float f5 = ent.rotationYawHead;

            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(25.0F, 0.0F, 1.0F, 0.0F);

            ent.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
            ent.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
            ent.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
            ent.rotationYawHead = ent.renderYawOffset;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if(ent instanceof EntityDragon)
            {
                GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
            }

            float viewY = mc.getRenderManager().playerViewY;
            mc.getRenderManager().setPlayerViewY(180.0F);
            mc.getRenderManager().setRenderShadow(false);
            mc.getRenderManager().renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
            mc.getRenderManager().setRenderShadow(true);

            if(ent instanceof EntityDragon)
            {
                GlStateManager.rotate(180F, 0.0F, -1.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -0.22F, 0.0F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
            Tessellator.getInstance().getWorldRenderer().setBrightness(240);

            mc.getRenderManager().setPlayerViewY(viewY);
            ent.renderYawOffset = f2;
            ent.rotationYaw = f3;
            ent.rotationPitch = f4;
            ent.rotationYawHead = f5;

            GlStateManager.popMatrix();

            GlStateManager.disableLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)posX, (float)posY, 50.0F);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            MorphInfoClient info = morphsActive.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

            GlStateManager.translate(0.0F, 0.0F, 100F);
            if(drawText)
            {
                //TODO radial changes
                //                if(radialShow)
                //                {
                //                    GlStateManager.pushMatrix();
                //                    float scaleee = 0.75F;
                //                    GlStateManager.scale(scaleee, scaleee, scaleee);
                //                    String name = (selected ? EnumChatFormatting.YELLOW : (info != null && info.nextState.currentVariant.thisVariant.identifier.equalsIgnoreCase(state.currentVariant.thisVariant.identifier) || info == null && state.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())) ? EnumChatFormatting.GOLD : "") + ent.getCommandSenderName();
                //                    Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(name, (int)(-3 - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(name) / 2) * scaleee), 5, 16777215);
                //                    GlStateManager.popMatrix();
                //                }
                //                else
                {
                    Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow((selected ? EnumChatFormatting.YELLOW : (info != null && info.nextState.getName().equalsIgnoreCase(state.getName()) || info == null && ent.getCommandSenderName().equalsIgnoreCase(mc.thePlayer.getCommandSenderName())) ? EnumChatFormatting.GOLD : "") + ent.getCommandSenderName(), 26, -32, 16777215);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            if(state != null && !state.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName()) && state.currentVariant.thisVariant.isFavourite)
            {
                double pX = 9.5D;
                double pY = -33.5D;
                double size = 9D;

                Minecraft.getMinecraft().getTextureManager().bindTexture(rlFavourite);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.setColorRGBA(255, 255, 255, 255);

                worldRenderer.startDrawingQuads();
                double iconX = pX;
                double iconY = pY;

                worldRenderer.addVertexWithUV(iconX, iconY + size, 0.0D, 0.0D, 1.0D);
                worldRenderer.addVertexWithUV(iconX + size, iconY + size, 0.0D, 1.0D, 1.0D);
                worldRenderer.addVertexWithUV(iconX + size, iconY, 0.0D, 1.0D, 0.0D);
                worldRenderer.addVertexWithUV(iconX, iconY, 0.0D, 0.0D, 0.0D);
                tessellator.draw();

                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.6F);

                worldRenderer.startDrawingQuads();
                iconX = pX + 1D;
                iconY = pY + 1D;

                worldRenderer.addVertexWithUV(iconX, iconY + size, -1.0D, 0.0D, 1.0D);
                worldRenderer.addVertexWithUV(iconX + size, iconY + size, -1.0D, 1.0D, 1.0D);
                worldRenderer.addVertexWithUV(iconX + size, iconY, -1.0D, 1.0D, 0.0D);
                worldRenderer.addVertexWithUV(iconX, iconY, -1.0D, 0.0D, 0.0D);
                tessellator.draw();
            }

            if(Morph.config.showAbilitiesInGui == 1)
            {
                ArrayList<Ability> abilities = AbilityHandler.getInstance().getEntityAbilities(ent.getClass());

                int abilitiesSize = abilities.size();
                for(int i = abilities.size() - 1; i >= 0; i--)
                {
                    if(!abilities.get(i).entityHasAbility(ent) || (abilities.get(i).getIcon() == null && !(abilities.get(i) instanceof AbilityPotionEffect)) || abilities.get(i) instanceof AbilityPotionEffect && Potion.potionTypes[((AbilityPotionEffect)abilities.get(i)).potionId] != null && !Potion.potionTypes[((AbilityPotionEffect)abilities.get(i)).potionId].hasStatusIcon())
                    {
                        abilitiesSize--;
                    }
                }

                boolean shouldScroll = false;

                final int stencilBit = MinecraftForgeClient.reserveStencilBit();

                if(stencilBit >= 0 && abilitiesSize > 3)
                {
                    MorphState selectedState = null;

                    int i = 0;

                    Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();

                    while(ite.hasNext())
                    {
                        Map.Entry<String, ArrayList<MorphState>> e = ite.next();
                        if(i == selectorSelectedVert)
                        {
                            ArrayList<MorphState> states = e.getValue();

                            for(int j = 0; j < states.size(); j++)
                            {
                                if(j == selectorSelectedHori)
                                {
                                    selectedState = states.get(j);
                                    break;
                                }
                            }

                            break;
                        }
                        i++;
                    }

                    if(state != null && selectedState == state)
                    {
                        shouldScroll = true;
                    }

                    if(shouldScroll)
                    {
                        final int stencilMask = 1 << stencilBit;

                        GL11.glEnable(GL11.GL_STENCIL_TEST);
                        GlStateManager.depthMask(false);
                        GlStateManager.colorMask(false, false, false, false);

                        GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
                        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
                        GL11.glStencilMask(stencilMask);
                        GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);

                        RendererHelper.drawColourOnScreen(255, 255, 255, 255, -20.5D, -32.5D, 40D, 35D, -10D);

                        GL11.glStencilMask(0x00);
                        GL11.glStencilFunc(GL11.GL_EQUAL, stencilMask, stencilMask);

                        GlStateManager.depthMask(true);
                        GlStateManager.colorMask(true, true, true, true);
                    }
                }

                int offsetX = 0;
                int offsetY = 0;
                int renders = 0;
                for(int i = 0; i < (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3 ? abilities.size() * 2 : abilities.size()); i++)
                {
                    Ability ability = abilities.get(i >= abilities.size() ? i - abilities.size() : i);

                    if(!ability.entityHasAbility(ent) || (ability.getIcon() == null && !(ability instanceof AbilityPotionEffect)) || ability instanceof AbilityPotionEffect && Potion.potionTypes[((AbilityPotionEffect)ability).potionId] != null && !Potion.potionTypes[((AbilityPotionEffect)ability).potionId].hasStatusIcon() || (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3) && !shouldScroll && renders >= 3)
                    {
                        continue;
                    }

                    ResourceLocation loc = ability.getIcon();
                    if(loc != null || ability instanceof AbilityPotionEffect)
                    {
                        double pX = -20.5D;
                        double pY = -33.5D;
                        double size = 12D;

                        if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
                        {
                            int round = abilityScroll % (30 * abilities.size());

                            pY -= (size + 1) * (double)(round + (double)renderTick) / 30D;
                        }

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                        worldRenderer.setColorRGBA(255, 255, 255, 255);

                        double iconX = pX + (offsetX * (size + 1));
                        double iconY = pY + (offsetY * (size + 1));

                        if(loc != null)
                        {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(loc);

                            worldRenderer.startDrawingQuads();
                            worldRenderer.addVertexWithUV(iconX, iconY + size, 0.0D, 0.0D, 1.0D);
                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, 0.0D, 1.0D, 1.0D);
                            worldRenderer.addVertexWithUV(iconX + size, iconY, 0.0D, 1.0D, 0.0D);
                            worldRenderer.addVertexWithUV(iconX, iconY, 0.0D, 0.0D, 0.0D);
                            tessellator.draw();
                        }
                        else
                        {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceHelper.texGuiInventory);
                            int l = Potion.potionTypes[((AbilityPotionEffect)ability).potionId].getStatusIconIndex();

                            float f = 0.00390625F;
                            float f1 = 0.00390625F;

                            int xStart = l % 8 * 18;
                            int yStart = 198 + l / 8 * 18;

                            worldRenderer.startDrawingQuads();
                            worldRenderer.addVertexWithUV(iconX, iconY + size, 0.0D, xStart * f, (yStart + 18) * f1);
                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, 0.0D, (xStart + 18) * f, (yStart + 18) * f1);
                            worldRenderer.addVertexWithUV(iconX + size, iconY, 0.0D, (xStart + 18) * f, yStart * f1);
                            worldRenderer.addVertexWithUV(iconX, iconY, 0.0D, xStart * f, yStart * f1);
                            tessellator.draw();

                        }

                        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.6F);

                        size = 12D;
                        iconX = pX + 1D + (offsetX * (size + 1));
                        iconY = pY + 1D + (offsetY * (size + 1));

                        if(loc != null)
                        {
                            worldRenderer.startDrawingQuads();
                            worldRenderer.addVertexWithUV(iconX, iconY + size, -1.0D, 0.0D, 1.0D);
                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, -1.0D, 1.0D, 1.0D);
                            worldRenderer.addVertexWithUV(iconX + size, iconY, -1.0D, 1.0D, 0.0D);
                            worldRenderer.addVertexWithUV(iconX, iconY, -1.0D, 0.0D, 0.0D);
                            tessellator.draw();
                        }
                        else
                        {
                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceHelper.texGuiInventory);
                            int l = Potion.potionTypes[((AbilityPotionEffect)ability).potionId].getStatusIconIndex();

                            float f = 0.00390625F;
                            float f1 = 0.00390625F;

                            int xStart = l % 8 * 18;
                            int yStart = 198 + l / 8 * 18;

                            worldRenderer.startDrawingQuads();
                            worldRenderer.addVertexWithUV(iconX, iconY + size, -1.0D, xStart * f, (yStart + 18) * f1);
                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, -1.0D, (xStart + 18) * f, (yStart + 18) * f1);
                            worldRenderer.addVertexWithUV(iconX + size, iconY, -1.0D, (xStart + 18) * f, yStart * f1);
                            worldRenderer.addVertexWithUV(iconX, iconY, -1.0D, xStart * f, yStart * f1);
                            tessellator.draw();
                        }

                        offsetY++;
                        if(offsetY == 3 && stencilBit < 0)
                        {
                            offsetY = 0;
                            offsetX++;
                        }
                    }
                    renders++;
                }

                if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
                {
                    GL11.glDisable(GL11.GL_STENCIL_TEST);
                }

                MinecraftForgeClient.releaseStencilBit(stencilBit);
            }
            GlStateManager.translate(0.0F, 0.0F, -100F);

            GlStateManager.disableBlend();

            GlStateManager.popMatrix();

            GlStateManager.enableAlpha();

            Minecraft.getMinecraft().gameSettings.hideGUI = hideGui;
        }
        forcePlayerRender = false;
    }

    public MorphState getCurrentlySelectedMorphState()
    {
        int i = 0;
        Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry<String, ArrayList<MorphState>> e = ite.next();

            if(i == selectorSelectedVert)
            {
                ArrayList<MorphState> states = e.getValue();
                if(selectorSelectedHori > states.size())
                {
                    return null;
                }
                else
                {
                    return states.get(selectorSelectedHori);
                }
            }
            i++;
        }
        return null;
    }

    public boolean selectorShow = false;
    public int selectorShowTimer = 0;
    public int selectorSelectedPrevVert = 0; //which morph category was selected
    public int selectorSelectedPrevHori = 0; //which morph in category was selected
    public int selectorSelectedVert = 0; //which morph category is selected
    public int selectorSelectedHori = 0; //which morph in category is selected
    public int selectorScrollVertTimer = 0;
    public int selectorScrollHoriTimer = 0;

    public int abilityScroll;

    public RenderMorph renderMorphInstance;
    public boolean forcePlayerRender;

    public int renderMorphDepth;

    public HashMap<String, MorphInfoClient> morphsActive = new HashMap<String, MorphInfoClient>(); //Current morphs per-player
    public LinkedHashMap<String, ArrayList<MorphState>> playerMorphs = new LinkedHashMap<String, ArrayList<MorphState>>(); //Minecraft Player's available morphs. LinkedHashMap maintains insertion order.

    public static final int SELECTOR_SHOW_TIME = 10;
    public static final int SELECTOR_SCROLL_TIME = 3;

    public static final ResourceLocation rlFavourite = new ResourceLocation("morph", "textures/gui/fav.png");
    public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/guiSelected.png");
    public static final ResourceLocation rlUnselected = new ResourceLocation("morph", "textures/gui/guiUnselected.png");
    public static final ResourceLocation rlUnselectedSide = new ResourceLocation("morph", "textures/gui/guiUnselectedSide.png");
}
