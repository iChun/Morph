package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.core.event.RendererSafeCompatibilityEvent;
import me.ichun.mods.ichunutil.client.keybind.KeyBind;
import me.ichun.mods.ichunutil.client.keybind.KeyEvent;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.client.render.RenderPlayerHand;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import me.ichun.mods.morph.common.packet.PacketGuiInput;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class EventHandlerClient
{
    public static final ResourceLocation rlFavourite = new ResourceLocation("morph", "textures/gui/fav.png");
    public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/gui_selected.png");
    public static final ResourceLocation rlUnselected = new ResourceLocation("morph", "textures/gui/gui_unselected.png");
    public static final ResourceLocation rlUnselectedSide = new ResourceLocation("morph", "textures/gui/gui_unselected_side.png");

    public static final int SELECTOR_SHOW_TIME = 10;
    public static final int SELECTOR_SCROLL_TIME = 3;

    public static final int RADIAL_SHOW_TIME = 3;

    public HashMap<String, MorphInfoClient> morphsActive = new HashMap<>(); //Current morphs per-player
    public LinkedHashMap<String, ArrayList<MorphState>> playerMorphs = new LinkedHashMap<>(); //Minecraft Player's available morphs. LinkedHashMap maintains insertion order.
    public ArrayList<MorphState> favouriteStates = new ArrayList<>();

    public int renderMorphDepth;

    public RenderPlayerHand renderHandInstance;
    public boolean allowSpecialRender;
    public boolean forcePlayerRender;
    public float playerShadowSize = -1F;

    public int abilityScroll;

    public boolean selectorShow = false;
    public int selectorShowTimer = 0;
    public int selectorSelectedPrevVert = 0; //which morph category was selected
    public int selectorSelectedPrevHori = 0; //which morph in category was selected
    public int selectorSelectedVert = 0; //which morph category is selected
    public int selectorSelectedHori = 0; //which morph in category is selected
    public int selectorScrollVertTimer = 0;
    public int selectorScrollHoriTimer = 0;

    public boolean showFavourites = false;
    public int radialShowTimer = 0;
    public double radialDeltaX = 0D;
    public double radialDeltaY = 0D;
    public float radialPlayerYaw;
    public float radialPlayerPitch;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        for(Map.Entry<Class<? extends Entity>, Render<? extends Entity >> e : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet())
        {
            Class clz = e.getKey();
            if(EntityLivingBase.class.isAssignableFrom(clz))
            {
                ModelHandler.dissectForModels(clz, e.getValue());
            }
            ModelHandler.mapPlayerModels();
        }
    }

    @SubscribeEvent
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed())
        {
            if(event.keyBind.equals(Morph.config.keySelectorUp) || event.keyBind.equals(Morph.config.keySelectorDown) || event.keyBind.equals(Morph.config.keySelectorLeft) || event.keyBind.equals(Morph.config.keySelectorRight))
            {
                Morph.eventHandlerClient.handleSelectorNavigation(event.keyBind);
            }
            else if(event.keyBind.equals(Morph.config.keySelectorSelect) || (event.keyBind.keyIndex == mc.gameSettings.keyBindAttack.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.eventHandlerClient.selectorShow)
                {
                    Morph.eventHandlerClient.selectorShow = false;
                    Morph.eventHandlerClient.selectorShowTimer = EventHandlerClient.SELECTOR_SHOW_TIME - Morph.eventHandlerClient.selectorShowTimer;
                    Morph.eventHandlerClient.selectorScrollHoriTimer = EventHandlerClient.SELECTOR_SCROLL_TIME;

                    MorphState selectedState = Morph.eventHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.eventHandlerClient.morphsActive.get(mc.player.getName());

                    if(selectedState != null && (info != null && !info.nextState.currentVariant.equals(selectedState.currentVariant) || info == null && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.player.getName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 0, false));
                    }
                }
                else if(showFavourites)
                {
                    showFavourites = false;
                    selectRadialMenu();
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorCancel) || (event.keyBind.keyIndex == mc.gameSettings.keyBindUseItem.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.eventHandlerClient.selectorShow)
                {
                    if(mc.currentScreen instanceof GuiIngameMenu)
                    {
                        mc.displayGuiScreen(null);
                    }
                    Morph.eventHandlerClient.selectorShow = false;
                    Morph.eventHandlerClient.selectorShowTimer = EventHandlerClient.SELECTOR_SHOW_TIME - Morph.eventHandlerClient.selectorShowTimer;
                    Morph.eventHandlerClient.selectorScrollHoriTimer = EventHandlerClient.SELECTOR_SCROLL_TIME;
                }
                else if(showFavourites)
                {
                    showFavourites = false;
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorRemoveMorph) || event.keyBind.keyIndex == Keyboard.KEY_DELETE)
            {
                if(Morph.eventHandlerClient.selectorShow)
                {
                    MorphState selectedState = Morph.eventHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.eventHandlerClient.morphsActive.get(mc.player.getName());

                    if(selectedState != null && !selectedState.currentVariant.thisVariant.isFavourite && ((info == null || !info.nextState.currentVariant.thisVariant.identifier.equalsIgnoreCase(selectedState.currentVariant.thisVariant.identifier)) && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.player.getName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 2, false));
                    }
                }
            }
            else if(event.keyBind.equals(Morph.config.keyFavourite))
            {
                if(Morph.eventHandlerClient.selectorShow)
                {
                    MorphState selectedState = Morph.eventHandlerClient.getCurrentlySelectedMorphState();
                    if(selectedState != null && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.player.getName()))
                    {
                        selectedState.currentVariant.thisVariant.isFavourite = !selectedState.currentVariant.thisVariant.isFavourite;
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 1, selectedState.currentVariant.thisVariant.isFavourite));
                        MorphState playerState = favouriteStates.get(0);
                        favouriteStates.remove(0);
                        if(selectedState.currentVariant.thisVariant.isFavourite)
                        {
                            if(!favouriteStates.contains(selectedState))
                            {
                                favouriteStates.add(selectedState);
                                Collections.sort(favouriteStates);
                            }
                        }
                        else
                        {
                            favouriteStates.remove(selectedState);
                        }
                        favouriteStates.add(0, playerState);
                    }
                }
                else if(mc.currentScreen == null)
                {
                    showFavourites = true;
                    radialShowTimer = RADIAL_SHOW_TIME;
                    radialDeltaX = 0D;
                    radialDeltaY = 0D;
                    radialPlayerYaw = mc.player.rotationYaw;
                    radialPlayerPitch = mc.player.rotationPitch;
                }
            }
        }
        else if(event.keyBind.equals(Morph.config.keyFavourite) && showFavourites)
        {
            //RADIAL MENU
            showFavourites = false;
            selectRadialMenu();
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event)
    {
        if(Morph.eventHandlerClient.selectorShow)
        {
            int k = event.getDwheel();
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
                Morph.eventHandlerClient.handleSelectorNavigation(bind);
                event.setCanceled(true);
            }
        }
        else if(Morph.eventHandlerClient.showFavourites)
        {
            Morph.eventHandlerClient.radialDeltaX += event.getDx() / 100D;
            Morph.eventHandlerClient.radialDeltaY += event.getDy() / 100D;

            double mag = Math.sqrt(Morph.eventHandlerClient.radialDeltaX * Morph.eventHandlerClient.radialDeltaX + Morph.eventHandlerClient.radialDeltaY * Morph.eventHandlerClient.radialDeltaY);
            if(mag > 1.0D)
            {
                Morph.eventHandlerClient.radialDeltaX /= mag;
                Morph.eventHandlerClient.radialDeltaY /= mag;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        if(Morph.eventHandlerClient.playerShadowSize < 0F)
        {
            Morph.eventHandlerClient.playerShadowSize = event.getRenderer().shadowSize;
        }
        if(Morph.eventHandlerClient.forcePlayerRender)
        {
            event.getRenderer().shadowSize = Morph.eventHandlerClient.playerShadowSize;
            return;
        }
        if(Morph.eventHandlerClient.renderMorphDepth > 0) //It's trying to render a player while rendering a morph, allow it.
        {
            return;
        }

        MorphInfoClient info = Morph.eventHandlerClient.morphsActive.get(event.getEntityPlayer().getName());
        if(info != null)
        {
            event.setCanceled(true);

            if(event.getEntityPlayer().getEntityWorld().playerEntities.contains(event.getEntityPlayer()) && info.getPlayer() != event.getEntityPlayer())
            {
                info.setPlayer(event.getEntityPlayer());
            }

            Minecraft mc = Minecraft.getMinecraft();

            Morph.eventHandlerClient.renderMorphDepth++;

            float renderTick = event.getPartialRenderTick();

            float f1 = EntityHelper.interpolateRotation(event.getEntityPlayer().prevRotationYaw, event.getEntityPlayer().rotationYaw, renderTick);
            if(info.isMorphing() && !(info.morphTime > Morph.config.morphTime - 10))
            {
                if(info.morphTime < 10)
                {
                    EntityLivingBase entInstance = info.prevState.getEntInstance(event.getEntityPlayer().getEntityWorld());
                    if(info.firstUpdate)
                    {
                        GameType oriGameType = null;
                        NetworkPlayerInfo npi = null;
                        if(entInstance instanceof EntityOtherPlayerMP)
                        {
                            npi = Minecraft.getMinecraft().getConnection().getPlayerInfo(((EntityOtherPlayerMP)entInstance).getGameProfile().getId());
                            if(npi != null)
                            {
                                oriGameType = npi.getGameType();
                                npi.setGameType(GameType.SPECTATOR);
                            }
                        }
                        info.syncEntityWithPlayer(entInstance);
                        entInstance.onUpdate();
                        info.syncEntityWithPlayer(entInstance);
                        if(npi != null)
                        {
                            npi.setGameType(oriGameType);
                        }
                    }

                    float prevEntSize = entInstance.width > entInstance.height ? entInstance.width : entInstance.height;
                    float prevScaleMag = prevEntSize > 2.5F ? (2.5F / prevEntSize) : 1.0F;

                    float ff2 = entInstance.renderYawOffset;
                    float ff3 = entInstance.rotationYaw;
                    float ff4 = entInstance.rotationPitch;
                    float ff5 = entInstance.prevRotationYawHead;
                    float ff6 = entInstance.rotationYawHead;

                    if((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative) && mc.getRenderManager().playerViewY == 180.0F)
                    {
                        GL11.glScalef(prevScaleMag, prevScaleMag, prevScaleMag);

                        EntityLivingBase renderView = mc.player;

                        entInstance.renderYawOffset = renderView.renderYawOffset;
                        entInstance.rotationYaw = renderView.rotationYaw;
                        entInstance.rotationPitch = renderView.rotationPitch;
                        entInstance.prevRotationYawHead = renderView.prevRotationYawHead;
                        entInstance.rotationYawHead = renderView.rotationYawHead;
                        renderTick = 1.0F;
                    }

                    float prog = info.getMorphSkinAlpha(renderTick);

                    event.getRenderer().shadowSize = info.getPrevStateModel(mc.world).entRenderer.shadowSize;

                    ModelInfo modelInfo = info.getPrevStateModel(event.getEntityPlayer().getEntityWorld());
                    modelInfo.forceRender(entInstance, event.getX(), event.getY(), event.getZ(), f1, renderTick);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, prog);

                    ResourceLocation resourceLoc = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), entInstance);
                    String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
                    String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);

                    modelInfo.forceRender(entInstance, event.getX(), event.getY(), event.getZ(), f1, renderTick);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    GlStateManager.disableAlpha();

                    entInstance.renderYawOffset = ff2;
                    entInstance.rotationYaw = ff3;
                    entInstance.rotationPitch = ff4;
                    entInstance.prevRotationYawHead = ff5;
                    entInstance.rotationYawHead = ff6;
                }
                else
                {
                    EntityLivingBase prevEntInstance = info.prevState.getEntInstance(event.getEntityPlayer().getEntityWorld());
                    EntityLivingBase nextEntInstance = info.nextState.getEntInstance(event.getEntityPlayer().getEntityWorld());

                    float prevEntSize = prevEntInstance.width > prevEntInstance.height ? prevEntInstance.width : prevEntInstance.height;
                    float prevScaleMag = prevEntSize > 2.5F ? (2.5F / prevEntSize) : 1.0F;

                    float nextEntSize = nextEntInstance.width > nextEntInstance.height ? nextEntInstance.width : nextEntInstance.height;
                    float nextScaleMag = nextEntSize > 2.5F ? (2.5F / nextEntSize) : 1.0F;

                    float ff2 = prevEntInstance.renderYawOffset;
                    float ff3 = prevEntInstance.rotationYaw;
                    float ff4 = prevEntInstance.rotationPitch;
                    float ff5 = prevEntInstance.prevRotationYawHead;
                    float ff6 = prevEntInstance.rotationYawHead;

                    float fff2 = nextEntInstance.renderYawOffset;
                    float fff3 = nextEntInstance.rotationYaw;
                    float fff4 = nextEntInstance.rotationPitch;
                    float fff5 = nextEntInstance.prevRotationYawHead;
                    float fff6 = nextEntInstance.rotationYawHead;

                    float morphProgress = info.getMorphTransitionProgress(renderTick);

                    if((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative) && mc.getRenderManager().playerViewY == 180.0F)
                    {
                        renderTick = 1.0F;

                        morphProgress = info.getMorphTransitionProgress(renderTick);

                        EntityLivingBase renderView = mc.player;

                        prevEntInstance.renderYawOffset = nextEntInstance.renderYawOffset = renderView.renderYawOffset;
                        prevEntInstance.rotationYaw = nextEntInstance.rotationYaw = renderView.rotationYaw;
                        prevEntInstance.rotationPitch = nextEntInstance.rotationPitch = renderView.rotationPitch;
                        prevEntInstance.prevRotationYawHead = nextEntInstance.prevRotationYawHead = renderView.prevRotationYawHead;
                        prevEntInstance.rotationYawHead = nextEntInstance.rotationYawHead = renderView.rotationYawHead;

                        float scale = EntityHelper.interpolateValues(prevScaleMag, nextScaleMag, morphProgress);
                        GL11.glScalef(scale, scale, scale);
                    }

                    event.getRenderer().shadowSize = EntityHelper.interpolateValues(info.getPrevStateModel(mc.world).entRenderer.shadowSize, info.getNextStateModel(mc.world).entRenderer.shadowSize, morphProgress);

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(event.getX(), event.getY(), event.getZ());
                    GlStateManager.rotate(180F - EntityHelper.interpolateRotation(event.getEntityPlayer().prevRenderYawOffset, event.getEntityPlayer().renderYawOffset, renderTick), 0F, 1F, 0F);
                    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                    ModelMorph model = info.getModelMorph(event.getEntityPlayer().getEntityWorld());
                    model.render(renderTick, morphProgress, info.prevState.getEntInstance(event.getEntityPlayer().getEntityWorld()), info.nextState.getEntInstance(event.getEntityPlayer().getEntityWorld()));
                    GlStateManager.popMatrix();

                    prevEntInstance.renderYawOffset = fff2;
                    prevEntInstance.rotationYaw = fff3;
                    prevEntInstance.rotationPitch = fff4;
                    prevEntInstance.prevRotationYawHead = fff5;
                    prevEntInstance.rotationYawHead = fff6;

                    nextEntInstance.renderYawOffset = ff2;
                    nextEntInstance.rotationYaw = ff3;
                    nextEntInstance.rotationPitch = ff4;
                    nextEntInstance.prevRotationYawHead = ff5;
                    nextEntInstance.rotationYawHead = ff6;

                    if(Morph.config.showPlayerLabel == 1)//Morph wants to show the player label.
                    {
                        Morph.eventHandlerClient.allowSpecialRender = true;
                        event.getRenderer().renderName((AbstractClientPlayer)event.getEntityPlayer(), event.getX(), event.getY(), event.getZ());
                        Morph.eventHandlerClient.allowSpecialRender = false;
                    }
                }
            }
            else
            {
                EntityLivingBase entInstance = info.nextState.getEntInstance(event.getEntityPlayer().getEntityWorld());

                float nextEntSize = entInstance.width > entInstance.height ? entInstance.width : entInstance.height;
                float nextScaleMag = nextEntSize > 2.5F ? (2.5F / nextEntSize) : 1.0F;

                float ff2 = entInstance.renderYawOffset;
                float ff3 = entInstance.rotationYaw;
                float ff4 = entInstance.rotationPitch;
                float ff5 = entInstance.prevRotationYawHead;
                float ff6 = entInstance.rotationYawHead;

                if((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative) && mc.getRenderManager().playerViewY == 180.0F)
                {
                    GL11.glScalef(nextScaleMag, nextScaleMag, nextScaleMag);

                    EntityLivingBase renderView = mc.player;

                    entInstance.renderYawOffset = renderView.renderYawOffset;
                    entInstance.rotationYaw = renderView.rotationYaw;
                    entInstance.rotationPitch = renderView.rotationPitch;
                    entInstance.prevRotationYawHead = renderView.prevRotationYawHead;
                    entInstance.rotationYawHead = renderView.rotationYawHead;
                    renderTick = 1.0F;
                }

                event.getRenderer().shadowSize = info.getNextStateModel(mc.world).entRenderer.shadowSize;

                ModelInfo modelInfo = info.getNextStateModel(event.getEntityPlayer().getEntityWorld());
                modelInfo.forceRender(entInstance, event.getX(), event.getY(), event.getZ(), f1, renderTick);

                if(info.isMorphing())
                {
                    float prog = info.getMorphSkinAlpha(renderTick);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, prog);

                    ResourceLocation resourceLoc = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), entInstance);
                    String resourceDomain = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourceDomain);
                    String resourcePath = ReflectionHelper.getPrivateValue(ResourceLocation.class, resourceLoc, ObfHelper.resourcePath);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "morph", ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, "textures/skin/morphskin.png", ObfHelper.resourcePath);

                    modelInfo.forceRender(entInstance, event.getX(), event.getY(), event.getZ(), f1, renderTick);

                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourceDomain, ObfHelper.resourceDomain);
                    ReflectionHelper.setPrivateValue(ResourceLocation.class, resourceLoc, resourcePath, ObfHelper.resourcePath);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    GlStateManager.disableAlpha();
                }

                entInstance.renderYawOffset = ff2;
                entInstance.rotationYaw = ff3;
                entInstance.rotationPitch = ff4;
                entInstance.prevRotationYawHead = ff5;
                entInstance.rotationYawHead = ff6;
            }

            Morph.eventHandlerClient.renderMorphDepth--;
        }
        else
        {
            event.getRenderer().shadowSize = Morph.eventHandlerClient.playerShadowSize;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderHand(RenderHandEvent event)
    {
        if(Morph.config.handRenderOverride == 1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            MorphInfoClient info = Morph.eventHandlerClient.morphsActive.get(mc.player.getName());
            if(info != null)
            {
                event.setCanceled(true);

                String s = mc.player.getSkinType();
                RenderPlayer rend = mc.getRenderManager().skinMap.get(s);

                Morph.eventHandlerClient.renderHandInstance.renderTick = event.getPartialTicks();
                Morph.eventHandlerClient.renderHandInstance.parent = rend;
                Morph.eventHandlerClient.renderHandInstance.clientInfo = info;

                mc.getRenderManager().skinMap.put(s, Morph.eventHandlerClient.renderHandInstance);

                boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)mc.getRenderViewEntity()).isPlayerSleeping();
                if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator())
                {
                    mc.entityRenderer.enableLightmap();
                    mc.getItemRenderer().renderItemInFirstPerson(event.getPartialTicks());
                    mc.entityRenderer.disableLightmap();
                }

                mc.getRenderManager().skinMap.put(s, rend);

                Morph.eventHandlerClient.renderHandInstance.clientInfo = null;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSpecials(RenderLivingEvent.Specials.Pre event)
    {
        if(Morph.eventHandlerClient.allowSpecialRender)
        {
            return;
        }
        Iterator<Map.Entry<String, MorphInfoClient>> ite = Morph.eventHandlerClient.morphsActive.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry<String, MorphInfoClient> e = ite.next();
            if(e.getValue().nextState.getEntInstance(event.getEntity().getEntityWorld()) == event.getEntity() || e.getValue().prevState != null && e.getValue().prevState.getEntInstance(event.getEntity().getEntityWorld()) == event.getEntity())
            {
                if(e.getValue().prevState != null && e.getValue().prevState.getEntInstance(event.getEntity().getEntityWorld()) instanceof EntityPlayer && e.getValue().prevState.getEntInstance(event.getEntity().getEntityWorld()).getName().equals(e.getKey()) || e.getValue().nextState.getEntInstance(event.getEntity().getEntityWorld()) instanceof EntityPlayer && e.getValue().nextState.getEntInstance(event.getEntity().getEntityWorld()).getName().equals(e.getKey()))
                {
                    //if the player is just morphing from/to itself don't render the label, we're doing that anyways.
                    event.setCanceled(true); //render layer safe, layers aren't special renders.
                }
                AbstractClientPlayer player = (AbstractClientPlayer)event.getEntity().getEntityWorld().getPlayerEntityByName(e.getKey());
                if(player == Minecraft.getMinecraft().player)
                {
                    //If the entity is the mc player morph, no need to render the label at all, since, well, it's the player.
                    event.setCanceled(true);
                    return;
                }

                if(player != null && Morph.config.showPlayerLabel == 1)//if the player is in the world and Morph wants to show the player label.
                {
                    event.setCanceled(true); //Don't render the entity label, render the actual player's label instead.

                    RenderPlayer rend = (RenderPlayer)(Render)Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
                    Morph.eventHandlerClient.allowSpecialRender = true;
                    rend.renderName(player, event.getX(), event.getY(), event.getZ());
                    Morph.eventHandlerClient.allowSpecialRender = false;
                }
                return; //no need to continue the loop, we're done here.
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(event.getWorld().isRemote && event.getWorld() instanceof WorldClient)
        {
            //Clean up the Morph States and stuff like that to prevent mem leaks.
            Morph.eventHandlerClient.morphsActive.values().forEach(MorphInfoClient::clean);
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
        MorphInfo info = morphsActive.get(player.getName());
        if(info != null && info.nextState.getEntInstance(player.getEntityWorld()).width < 0.45F && player.isEntityInsideOpaqueBlock())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPushPlayerSPOutOfBlock(PlayerSPPushOutOfBlocksEvent event)
    {
        MorphInfo info = morphsActive.get(event.getEntityPlayer().getName());
        if(info != null) //player is morphed
        {
            event.setCanceled(true);

            PlayerMorphHandler.setPlayerSize(event.getEntityPlayer(), info, false);
            AxisAlignedBB axisalignedbb = event.getEntityPlayer().getEntityBoundingBox();
            event.setEntityBoundingBox(axisalignedbb);

            float playerWidth = event.getEntityPlayer().width;
            float playerHeight = event.getEntityPlayer().height;
            event.getEntityPlayer().width = info.nextState.getEntInstance(event.getEntityPlayer().getEntityWorld()).width;
            event.getEntityPlayer().height = info.nextState.getEntInstance(event.getEntityPlayer().getEntityWorld()).height;

            pushOutOfBlocksSP(event.getEntityPlayer(), event.getEntityPlayer().posX - (double)event.getEntityPlayer().width * 0.35D, axisalignedbb.minY + 0.5D, event.getEntityPlayer().posZ + (double)event.getEntityPlayer().width * 0.35D);
            pushOutOfBlocksSP(event.getEntityPlayer(), event.getEntityPlayer().posX - (double)event.getEntityPlayer().width * 0.35D, axisalignedbb.minY + 0.5D, event.getEntityPlayer().posZ - (double)event.getEntityPlayer().width * 0.35D);
            pushOutOfBlocksSP(event.getEntityPlayer(), event.getEntityPlayer().posX + (double)event.getEntityPlayer().width * 0.35D, axisalignedbb.minY + 0.5D, event.getEntityPlayer().posZ - (double)event.getEntityPlayer().width * 0.35D);
            pushOutOfBlocksSP(event.getEntityPlayer(), event.getEntityPlayer().posX + (double)event.getEntityPlayer().width * 0.35D, axisalignedbb.minY + 0.5D, event.getEntityPlayer().posZ + (double)event.getEntityPlayer().width * 0.35D);

            event.getEntityPlayer().width = playerWidth;
            event.getEntityPlayer().height = playerHeight;
        }
    }

    public boolean pushOutOfBlocksSP(EntityPlayer player, double x, double y, double z)
    {
        if (player.noClip)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double)blockpos.getX();
            double d1 = z - (double)blockpos.getZ();

            int entHeight = Math.max((int)Math.ceil(player.height), 1);

            boolean inTranslucentBlock = !isHeadspaceFree(player.getEntityWorld(), blockpos, entHeight);

            if (inTranslucentBlock)
            {
                int i = -1;
                double d2 = 9999.0D;

                if (isHeadspaceFree(player.getEntityWorld(), blockpos.west(), entHeight) && d0 < d2)
                {
                    d2 = d0;
                    i = 0;
                }

                if (isHeadspaceFree(player.getEntityWorld(), blockpos.east(), entHeight) && 1.0D - d0 < d2)
                {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (isHeadspaceFree(player.getEntityWorld(), blockpos.north(), entHeight) && d1 < d2)
                {
                    d2 = d1;
                    i = 4;
                }

                if (isHeadspaceFree(player.getEntityWorld(), blockpos.south(), entHeight) && 1.0D - d1 < d2)
                {
                    d2 = 1.0D - d1;
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0)
                {
                    player.motionX = -0.10000000149011612D;
                }

                if (i == 1)
                {
                    player.motionX = 0.10000000149011612D;
                }

                if (i == 4)
                {
                    player.motionZ = -0.10000000149011612D;
                }

                if (i == 5)
                {
                    player.motionZ = 0.10000000149011612D;
                }
            }

            return false;
        }
    }

    private boolean isHeadspaceFree(World world, BlockPos pos, int height)
    {
        for (int y = 0; y < height; y++)
        {
            if (!isOpenBlockSpace(world, pos.add(0, y, 0))) return false;
        }
        return true;
    }

    private boolean isOpenBlockSpace(World world, BlockPos pos)
    {
        IBlockState iblockstate = world.getBlockState(pos);
        return !iblockstate.getBlock().isNormalCube(iblockstate, world, pos);
    }
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.world != null)
        {
            if(event.phase == TickEvent.Phase.START)
            {
                MorphInfo info = morphsActive.get(mc.player.getName());
                if(info != null && info.isMorphing())
                {
                    float morphTransition = info.getMorphTransitionProgress(event.renderTickTime);

                    EntityLivingBase prevEnt = info.prevState.getEntInstance(mc.world);
                    EntityLivingBase nextEnt = info.nextState.getEntInstance(mc.world);

                    mc.player.eyeHeight = EntityHelper.interpolateValues(prevEnt.getEyeHeight(), nextEnt.getEyeHeight(), morphTransition);
                }

                if(showFavourites)
                {
                    Mouse.getDX();
                    Mouse.getDY();
                    mc.mouseHelper.deltaX = mc.mouseHelper.deltaY = 0;
//                    mc.renderViewEntity.prevRotationYawHead = mc.renderViewEntity.rotationYawHead = radialPlayerYaw;
//                    mc.renderViewEntity.prevRotationYaw = mc.renderViewEntity.rotationYaw = radialPlayerYaw;
//                    mc.renderViewEntity.prevRotationPitch = mc.renderViewEntity.rotationPitch = radialPlayerPitch;
                }
            }
            else
            {
                ScaledResolution reso = new ScaledResolution(mc);
                drawSelector(mc, reso, event.renderTickTime);
                drawRadialMenu(mc, reso, event.renderTickTime);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null)
            {
                if(!mc.isGamePaused())
                {
                    Morph.eventHandlerClient.morphsActive.values().forEach(MorphInfo::tick);
                    for(Map.Entry<String, ArrayList<MorphState>> e : Morph.eventHandlerClient.playerMorphs.entrySet())
                    {
                        for(MorphState state : e.getValue())
                        {
                            state.getEntInstance(mc.world).ticksExisted++;
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
                if(radialShowTimer > 0)
                {
                    radialShowTimer--;
                }
                selectorScrollVertTimer--;
                selectorScrollHoriTimer--;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isClient())
        {
            if(event.player.getEntityWorld().playerEntities.contains(event.player))
            {
                MorphInfo info = morphsActive.get(event.player.getName());
                if(info != null)
                {
                    if(event.phase == TickEvent.Phase.START)
                    {
                        if(info.getPlayer() != event.player)
                        {
                            info.setPlayer(event.player);
                        }

                        float f;
                        float f1;

                        if (event.player.isElytraFlying())
                        {
                            f = 0.6F;
                            f1 = 0.6F;
                        }
                        else if (event.player.isPlayerSleeping())
                        {
                            f = 0.2F;
                            f1 = 0.2F;
                        }
                        else if (event.player.isSneaking())
                        {
                            f = 0.6F;
                            f1 = 1.65F;
                        }
                        else
                        {
                            f = 0.6F;
                            f1 = 1.8F;
                        }

                        event.player.width = f;
                        event.player.height = f1;

                        PlayerMorphHandler.setPlayerSize(event.player, info, false);
                    }
                    else
                    {
                        PlayerMorphHandler.setPlayerSize(event.player, info, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Minecraft.getMinecraft().addScheduledTask(this::disconnectFromServer);
    }

    public void selectRadialMenu()
    {
        double mag = Math.sqrt(radialDeltaX * radialDeltaX + radialDeltaY * radialDeltaY);
        double magAcceptance = 0.8D;

        double radialAngle = -720F;
        if(mag > magAcceptance)
        {
            //is on radial menu
            //TODO atan2?
            double aSin = Math.toDegrees(Math.asin(radialDeltaX));
            if(radialDeltaY >= 0 && radialDeltaX >= 0)
            {
                radialAngle = aSin;
            }
            else if(radialDeltaY < 0 && radialDeltaX >= 0)
            {
                radialAngle = 90D + (90D - aSin);
            }
            else if(radialDeltaY < 0 && radialDeltaX < 0)
            {
                radialAngle = 180D - aSin;
            }
            else if(radialDeltaY >= 0 && radialDeltaX < 0)
            {
                radialAngle = 270D + (90D + aSin);
            }
        }
        else
        {
            return;
        }

        if(mag > 0.9999999D)
        {
            mag = Math.round(mag);
        }

        for(int i = 0; i < favouriteStates.size(); i++)
        {
            float leeway = 360F / favouriteStates.size();
            if(mag > magAcceptance * 0.75D && (i == 0 && (radialAngle < (leeway / 2) && radialAngle >= 0F || radialAngle > (360F) - (leeway / 2)) || i != 0 && radialAngle < (leeway * i) + (leeway / 2) && radialAngle > (leeway * i ) - (leeway / 2)))
            {
                MorphState selectedState = favouriteStates.get(i);
                MorphInfoClient info = morphsActive.get(Minecraft.getMinecraft().player.getName());

                if(selectedState != null && (info != null && !info.nextState.currentVariant.equals(selectedState.currentVariant) || info == null && !selectedState.currentVariant.playerName.equalsIgnoreCase(Minecraft.getMinecraft().player.getName())))
                {
                    Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 0, false));
                    break;
                }
            }
        }

    }

    public void drawRadialMenu(Minecraft mc, ScaledResolution reso, float renderTick)
    {
        if((radialShowTimer > 0 || showFavourites) && !mc.gameSettings.hideGUI)
        {
            double mag = Math.sqrt(radialDeltaX * radialDeltaX + radialDeltaY * radialDeltaY);
            double magAcceptance = 0.8D;

            float prog = 1.0F - (radialShowTimer - renderTick) / (float)RADIAL_SHOW_TIME;
            if(prog > 1.0F)
            {
                prog = 1.0F;
            }

            int radius = 80;
            radius *= Math.pow(prog, 0.5D);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();

            int NUM_PIZZA_SLICES = 100;

            float zLev = -0.05F;

            GlStateManager.disableTexture2D();

            float rad;

            int stencilBit = -1;
            if(RendererHelper.canUseStencils())
            {
                stencilBit = MinecraftForgeClient.reserveStencilBit();
                if(stencilBit >= 0)
                {
                    GL11.glEnable(GL11.GL_STENCIL_TEST);
                    GlStateManager.depthMask(false);
                    GlStateManager.colorMask(false, false, false, false);

                    final int stencilMask = 1 << stencilBit;

                    GL11.glStencilMask(stencilMask);
                    GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
                    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                    GL11.glClearStencil(0);
                    GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);

                    rad = (mag > magAcceptance ? 0.85F : 0.82F) * prog * (257F / (float)reso.getScaledHeight());

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    GlStateManager.glBegin(GL11.GL_TRIANGLE_FAN);
                    GlStateManager.glVertex3f(0, 0, zLev);
                    for(int i = 0; i <= NUM_PIZZA_SLICES; i++)
                    { //NUM_PIZZA_SLICES decides how round the circle looks.
                        double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
                        GlStateManager.glVertex3f((float)(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad), (float)(Math.sin(angle) * rad), zLev);
                    }
                    GlStateManager.glEnd();

                    GL11.glStencilFunc(GL11.GL_ALWAYS, 0, stencilMask);

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    rad = 0.44F * prog * (257F / (float)reso.getScaledHeight());

                    GlStateManager.glBegin(GL11.GL_TRIANGLE_FAN);
                    GlStateManager.glVertex3f(0, 0, zLev);
                    for(int i = 0; i <= NUM_PIZZA_SLICES; i++)
                    { //NUM_PIZZA_SLICES decides how round the circle looks.
                        double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
                        GlStateManager.glVertex3f((float)(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad), (float)(Math.sin(angle) * rad), zLev);
                    }
                    GlStateManager.glEnd();

                    GL11.glStencilMask(0x00);
                    GL11.glStencilFunc(GL11.GL_EQUAL, stencilMask, stencilMask);

                    GlStateManager.depthMask(true);
                    GlStateManager.colorMask(true, true, true, true);
                }
            }

            rad = (mag > magAcceptance ? 0.85F : 0.82F) * prog * (257F / (float)reso.getScaledHeight());

            GlStateManager.color(0.0F, 0.0F, 0.0F, mag > magAcceptance ? 0.6F : 0.4F);

            GlStateManager.glBegin(GL11.GL_TRIANGLE_FAN);
            GlStateManager.glVertex3f(0, 0, zLev);
            for(int i = 0; i <= NUM_PIZZA_SLICES; i++){ //NUM_PIZZA_SLICES decides how round the circle looks.
                double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
                GlStateManager.glVertex3f((float)(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad), (float)(Math.sin(angle) * rad), zLev);
            }
            GlStateManager.glEnd();

            if(RendererHelper.canUseStencils() && stencilBit >= 0)
            {
                GL11.glDisable(GL11.GL_STENCIL_TEST);

                MinecraftForgeClient.releaseStencilBit(stencilBit);
            }

            GlStateManager.enableTexture2D();

            GlStateManager.popMatrix();

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();

            double radialAngle = -720F;
            if(mag > magAcceptance)
            {
                //is on radial menu
                double aSin = Math.toDegrees(Math.asin(radialDeltaX));

                if(radialDeltaY >= 0 && radialDeltaX >= 0)
                {
                    radialAngle = aSin;
                }
                else if(radialDeltaY < 0 && radialDeltaX >= 0)
                {
                    radialAngle = 90D + (90D - aSin);
                }
                else if(radialDeltaY < 0 && radialDeltaX < 0)
                {
                    radialAngle = 180D - aSin;
                }
                else if(radialDeltaY >= 0 && radialDeltaX < 0)
                {
                    radialAngle = 270D + (90D + aSin);
                }
            }

            if(mag > 0.9999999D)
            {
                mag = Math.round(mag);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();

            for(int i = 0; i < favouriteStates.size(); i++)
            {
                double angle = Math.PI * 2 * i / favouriteStates.size();

                angle -= Math.toRadians(90D);

                float leeway = 360F / favouriteStates.size();

                boolean selected = false;

                if(mag > magAcceptance * 0.75D && (i == 0 && (radialAngle < (leeway / 2) && radialAngle >= 0F || radialAngle > (360F) - (leeway / 2)) || i != 0 && radialAngle < (leeway * i) + (leeway / 2) && radialAngle > (leeway * i ) - (leeway / 2)))
                {
                    selected = true;
                }

                float entSize = favouriteStates.get(i).getEntInstance(mc.world).width > favouriteStates.get(i).getEntInstance(mc.world).height ? favouriteStates.get(i).getEntInstance(mc.world).width : favouriteStates.get(i).getEntInstance(mc.world).height;

                float scaleMag = entSize > 2.5F ? (float)((2.5F + (entSize - 2.5F) * (mag > magAcceptance && selected ? ((mag - magAcceptance) / (1.0F - magAcceptance)) : 0.0F)) / entSize) : 1.0F;

                drawEntityOnScreen(favouriteStates.get(i), favouriteStates.get(i).getEntInstance(mc.world), reso.getScaledWidth() / 2 + (int)(radius * Math.cos(angle)), (reso.getScaledHeight() + 24) / 2 + (int)(radius * Math.sin(angle)), 16 * prog * scaleMag + (float)(selected ? 6 * mag : 0), 2, 2, renderTick, selected, true);
            }

            GlStateManager.popMatrix();
        }
    }

    public void drawSelector(Minecraft mc, ScaledResolution reso, float renderTick)
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

            float progress = MathHelper.clamp((SELECTOR_SHOW_TIME - (selectorShowTimer - renderTick)) / (float)SELECTOR_SHOW_TIME, 0F, 1F);

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
                BufferBuilder bufferbuilder = tessellator.getBuffer();

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

                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferbuilder.pos(width1 + dist, height1 + size, -90.0D + j).tex(0.0D, 1.0D).endVertex();
                        bufferbuilder.pos(width1 + dist + size, height1 + size, -90.0D + j).tex(1.0D, 1.0D).endVertex();
                        bufferbuilder.pos(width1 + dist + size, height1, -90.0D + j).tex(1.0D, 0.0D).endVertex();
                        bufferbuilder.pos(width1 + dist, height1, -90.0D + j).tex(0.0D, 0.0D).endVertex();
                        tessellator.draw();

                        GlStateManager.popMatrix();
                    }
                }
                else
                {
                    mc.getTextureManager().bindTexture(rlUnselected);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    bufferbuilder.pos(width1, height1 + size, -90.0D).tex(0.0D, 1.0D).endVertex();
                    bufferbuilder.pos(width1 + size, height1 + size, -90.0D).tex(1.0D, 1.0D).endVertex();
                    bufferbuilder.pos(width1 + size, height1, -90.0D).tex(1.0D, 0.0D).endVertex();
                    bufferbuilder.pos(width1, height1, -90.0D).tex(0.0D, 0.0D).endVertex();
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

                        EntityLivingBase entInstance = state.getEntInstance(mc.world);
                        float entSize = Math.max(entInstance.width, entInstance.height);
                        float prog = MathHelper.clamp(j - selectorSelectedHori == 0 ? (!selectorShow ? selectorScrollHoriTimer - renderTick : (3F - selectorScrollHoriTimer + renderTick)) / 3F : 0.0F, 0.0F, 1.0F);
                        float scaleMag = ((2.5F + (entSize - 2.5F) * prog) / entSize) ;

                        drawEntityOnScreen(state, entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, true, j == states.size() - 1);

                        GlStateManager.popMatrix();
                    }
                }
                else
                {
                    MorphState state = states.get(0);
                    EntityLivingBase entInstance = state.getEntInstance(mc.world);
                    float entSize = Math.max(entInstance.width, entInstance.height);
                    float scaleMag = (2.5F / entSize);
                    drawEntityOnScreen(state, entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, selectorSelectedVert == i, true);
                }
                GlStateManager.translate(0.0F, 0.0F, 20F);
                GlStateManager.color(1F, 1F, 1F, 1F);
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
                BufferBuilder bufferbuilder = tessellator.getBuffer();

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(width1, height1 + size, -90.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(width1 + size, height1 + size, -90.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(width1 + size, height1, -90.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(width1, height1, -90.0D).tex(0.0D, 0.0D).endVertex();
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
            selectorSelectedVert = selectorSelectedHori = 0; //Reset the selected selector position

            MorphInfoClient info = morphsActive.get(mc.player.getName());
            if(info != null)
            {
                String entName = info.nextState.getEntInstance(mc.world).getName();

                int i = 0;
                Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();
                while(ite.hasNext())
                {
                    Map.Entry<String, ArrayList<MorphState>> e = ite.next();
                    if(e.getKey().equalsIgnoreCase(entName))
                    {
                        selectorSelectedVert = i;
                        ArrayList<MorphState> states = e.getValue();

                        for(int j = 0; j < states.size(); j++)
                        {
                            if(states.get(j).currentVariant.equals(info.nextState.currentVariant))
                            {
                                selectorSelectedHori = j;
                                break;
                            }
                        }

                        break;
                    }
                    i++;
                }
            }
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

            GlStateManager.translate((float)posX, (float)posY, 75.0F);

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
            GameType oriGameType = null;
            NetworkPlayerInfo npi = null;
            if(ent instanceof EntityOtherPlayerMP)
            {
                npi = Minecraft.getMinecraft().getConnection().getPlayerInfo(((EntityOtherPlayerMP)ent).getGameProfile().getId());
                if(npi != null)
                {
                    oriGameType = npi.getGameType();
                    npi.setGameType(GameType.ADVENTURE);
                }
            }

            float viewY = mc.getRenderManager().playerViewY;
            mc.getRenderManager().setPlayerViewY(180.0F);
            mc.getRenderManager().setRenderShadow(false);
            mc.getRenderManager().renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            mc.getRenderManager().setRenderShadow(true);

            if(npi != null)
            {
                npi.setGameType(oriGameType);
            }
            if(ent instanceof EntityDragon)
            {
                GlStateManager.rotate(180F, 0.0F, -1.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -0.22F, 0.0F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
            //            Tessellator.getInstance().getWorldRenderer().setBrightness(240); //What is this for...?

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

            MorphInfoClient info = morphsActive.get(Minecraft.getMinecraft().player.getName());

            GlStateManager.translate(0.0F, 0.0F, 100F);
            if(drawText)
            {
                if(showFavourites)
                {
                    GlStateManager.pushMatrix();
                    float scaleee = 0.75F;
                    GlStateManager.scale(scaleee, scaleee, scaleee);
                    String name = (selected ? TextFormatting.YELLOW : (info != null && info.nextState.currentVariant.thisVariant.identifier.equalsIgnoreCase(state.currentVariant.thisVariant.identifier) || info == null && state.currentVariant.playerName.equalsIgnoreCase(mc.player.getName())) ? TextFormatting.GOLD : "") + ent.getName();
                    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, (int)(-3 - (Minecraft.getMinecraft().fontRenderer.getStringWidth(name) / 2) * scaleee), 5, 16777215);
                    GlStateManager.popMatrix();
                }
                else
                {
                    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow((selected ? TextFormatting.YELLOW : (info != null && info.nextState.getName().equalsIgnoreCase(state.getName()) || info == null && ent.getName().equalsIgnoreCase(mc.player.getName())) ? TextFormatting.GOLD : "") + ent.getName(), 26, -32, 16777215);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            if(state != null && !state.currentVariant.playerName.equalsIgnoreCase(mc.player.getName()) && state.currentVariant.thisVariant.isFavourite && !showFavourites)
            {
                double pX = 9.5D;
                double pY = -33.5D;
                double size = 9D;

                Minecraft.getMinecraft().getTextureManager().bindTexture(rlFavourite);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                double iconX = pX;
                double iconY = pY;

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(iconX, iconY + size, 0.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(iconX + size, iconY + size, 0.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(iconX + size, iconY, 0.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(iconX, iconY, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();

                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.6F);

                iconX = pX + 1D;
                iconY = pY + 1D;


                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(iconX, iconY + size, -1.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(iconX + size, iconY + size, -1.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(iconX + size, iconY, -1.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(iconX, iconY, -1.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
            }

            //            if(Morph.config.showAbilitiesInGui == 1)
            //            {
            //                ArrayList<Ability> abilities = AbilityHandler.getInstance().getEntityAbilities(ent.getClass());
            //
            //                int abilitiesSize = abilities.size();
            //                for(int i = abilities.size() - 1; i >= 0; i--)
            //                {
            //                    if(!abilities.get(i).entityHasAbility(ent) || (abilities.get(i).getIcon() == null && !(abilities.get(i) instanceof AbilityPotionEffect)) || abilities.get(i) instanceof AbilityPotionEffect && Potion.potionTypes[((AbilityPotionEffect)abilities.get(i)).potionId] != null && !Potion.potionTypes[((AbilityPotionEffect)abilities.get(i)).potionId].hasStatusIcon())
            //                    {
            //                        abilitiesSize--;
            //                    }
            //                }
            //
            //                boolean shouldScroll = false;
            //
            //                final int stencilBit = MinecraftForgeClient.reserveStencilBit();
            //
            //                if(stencilBit >= 0 && abilitiesSize > 3)
            //                {
            //                    MorphState selectedState = null;
            //
            //                    int i = 0;
            //
            //                    Iterator<Map.Entry<String, ArrayList<MorphState>>> ite = playerMorphs.entrySet().iterator();
            //
            //                    while(ite.hasNext())
            //                    {
            //                        Map.Entry<String, ArrayList<MorphState>> e = ite.next();
            //                        if(i == selectorSelectedVert)
            //                        {
            //                            ArrayList<MorphState> states = e.getValue();
            //
            //                            for(int j = 0; j < states.size(); j++)
            //                            {
            //                                if(j == selectorSelectedHori)
            //                                {
            //                                    selectedState = states.get(j);
            //                                    break;
            //                                }
            //                            }
            //
            //                            break;
            //                        }
            //                        i++;
            //                    }
            //
            //                    if(state != null && selectedState == state)
            //                    {
            //                        shouldScroll = true;
            //                    }
            //
            //                    if(shouldScroll)
            //                    {
            //                        final int stencilMask = 1 << stencilBit;
            //
            //                        GL11.glEnable(GL11.GL_STENCIL_TEST);
            //                        GlStateManager.depthMask(false);
            //                        GlStateManager.colorMask(false, false, false, false);
            //
            //                        GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
            //                        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
            //                        GL11.glStencilMask(stencilMask);
            //                        GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
            //
            //                        RendererHelper.drawColourOnScreen(255, 255, 255, 255, -20.5D, -32.5D, 40D, 35D, -10D);
            //
            //                        GL11.glStencilMask(0x00);
            //                        GL11.glStencilFunc(GL11.GL_EQUAL, stencilMask, stencilMask);
            //
            //                        GlStateManager.depthMask(true);
            //                        GlStateManager.colorMask(true, true, true, true);
            //                    }
            //                }
            //
            //                int offsetX = 0;
            //                int offsetY = 0;
            //                int renders = 0;
            //                for(int i = 0; i < (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3 ? abilities.size() * 2 : abilities.size()); i++)
            //                {
            //                    Ability ability = abilities.get(i >= abilities.size() ? i - abilities.size() : i);
            //
            //                    if(!ability.entityHasAbility(ent) || (ability.getIcon() == null && !(ability instanceof AbilityPotionEffect)) || ability instanceof AbilityPotionEffect && Potion.potionTypes[((AbilityPotionEffect)ability).potionId] != null && !Potion.potionTypes[((AbilityPotionEffect)ability).potionId].hasStatusIcon() || (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3) && !shouldScroll && renders >= 3)
            //                    {
            //                        continue;
            //                    }
            //
            //                    ResourceLocation loc = ability.getIcon();
            //                    if(loc != null || ability instanceof AbilityPotionEffect)
            //                    {
            //                        double pX = -20.5D;
            //                        double pY = -33.5D;
            //                        double size = 12D;
            //
            //                        if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
            //                        {
            //                            int round = abilityScroll % (30 * abilities.size());
            //
            //                            pY -= (size + 1) * (double)(round + (double)renderTick) / 30D;
            //                        }
            //
            //                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            //                        Tessellator tessellator = Tessellator.getInstance();
            //                        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            //                        worldRenderer.setColorRGBA(255, 255, 255, 255);
            //
            //                        double iconX = pX + (offsetX * (size + 1));
            //                        double iconY = pY + (offsetY * (size + 1));
            //
            //                        if(loc != null)
            //                        {
            //                            Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
            //
            //                            worldRenderer.startDrawingQuads();
            //                            worldRenderer.addVertexWithUV(iconX, iconY + size, 0.0D, 0.0D, 1.0D);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, 0.0D, 1.0D, 1.0D);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY, 0.0D, 1.0D, 0.0D);
            //                            worldRenderer.addVertexWithUV(iconX, iconY, 0.0D, 0.0D, 0.0D);
            //                            tessellator.draw();
            //                        }
            //                        else
            //                        {
            //                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceHelper.texGuiInventory);
            //                            int l = Potion.potionTypes[((AbilityPotionEffect)ability).potionId].getStatusIconIndex();
            //
            //                            float f = 0.00390625F;
            //                            float f1 = 0.00390625F;
            //
            //                            int xStart = l % 8 * 18;
            //                            int yStart = 198 + l / 8 * 18;
            //
            //                            worldRenderer.startDrawingQuads();
            //                            worldRenderer.addVertexWithUV(iconX, iconY + size, 0.0D, xStart * f, (yStart + 18) * f1);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, 0.0D, (xStart + 18) * f, (yStart + 18) * f1);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY, 0.0D, (xStart + 18) * f, yStart * f1);
            //                            worldRenderer.addVertexWithUV(iconX, iconY, 0.0D, xStart * f, yStart * f1);
            //                            tessellator.draw();
            //
            //                        }
            //
            //                        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.6F);
            //
            //                        size = 12D;
            //                        iconX = pX + 1D + (offsetX * (size + 1));
            //                        iconY = pY + 1D + (offsetY * (size + 1));
            //
            //                        if(loc != null)
            //                        {
            //                            worldRenderer.startDrawingQuads();
            //                            worldRenderer.addVertexWithUV(iconX, iconY + size, -1.0D, 0.0D, 1.0D);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, -1.0D, 1.0D, 1.0D);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY, -1.0D, 1.0D, 0.0D);
            //                            worldRenderer.addVertexWithUV(iconX, iconY, -1.0D, 0.0D, 0.0D);
            //                            tessellator.draw();
            //                        }
            //                        else
            //                        {
            //                            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceHelper.texGuiInventory);
            //                            int l = Potion.potionTypes[((AbilityPotionEffect)ability).potionId].getStatusIconIndex();
            //
            //                            float f = 0.00390625F;
            //                            float f1 = 0.00390625F;
            //
            //                            int xStart = l % 8 * 18;
            //                            int yStart = 198 + l / 8 * 18;
            //
            //                            worldRenderer.startDrawingQuads();
            //                            worldRenderer.addVertexWithUV(iconX, iconY + size, -1.0D, xStart * f, (yStart + 18) * f1);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY + size, -1.0D, (xStart + 18) * f, (yStart + 18) * f1);
            //                            worldRenderer.addVertexWithUV(iconX + size, iconY, -1.0D, (xStart + 18) * f, yStart * f1);
            //                            worldRenderer.addVertexWithUV(iconX, iconY, -1.0D, xStart * f, yStart * f1);
            //                            tessellator.draw();
            //                        }
            //
            //                        offsetY++;
            //                        if(offsetY == 3 && stencilBit < 0)
            //                        {
            //                            offsetY = 0;
            //                            offsetX++;
            //                        }
            //                    }
            //                    renders++;
            //                }
            //
            //                if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
            //                {
            //                    GL11.glDisable(GL11.GL_STENCIL_TEST);
            //                }
            //
            //                MinecraftForgeClient.releaseStencilBit(stencilBit);
            //            }
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

    public void disconnectFromServer()
    {
        //world is null, not connected to any worlds, get rid of objects for GC.
        morphsActive.clear();
        playerMorphs.clear();
        favouriteStates.clear();
    }
}
