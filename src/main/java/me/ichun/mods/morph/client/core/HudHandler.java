package me.ichun.mods.morph.client.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.core.KeyBinds;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketMorphInput;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class HudHandler
{
    public static final ResourceLocation TEX_QS_SELECTED = new ResourceLocation("morph", "textures/gui/gui_selected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED = new ResourceLocation("morph", "textures/gui/gui_unselected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED_SIDE = new ResourceLocation("morph", "textures/gui/gui_unselected_side.png");

    private static final MatrixStack LIGHT_STACK = Util.make(new MatrixStack(), stack -> stack.translate(1D, -1D, 0D));

    private static int SHOW_SELECTOR_TIME = 8;
    private static int INDEX_TIME = 5;

    public boolean showSelector = false;
    public int showTime = 0;

    public int indexChangeTime = 0;
    public double lastIndexVert = 0D;
    public double lastIndexHori = 0D;

    public int indexVert = 0;
    public int indexHori = 0;

    public boolean keyEscDown;
    public boolean keyEnterDown;

    public HashMap<MorphVariant, MorphState> morphStates = new HashMap<>();

    public void handleInput(KeyBind keyBind, boolean isReleased)
    {
        if(Minecraft.getInstance().player == null) // ???
        {
            return;
        }

        if(keyBind == KeyBinds.keySelectorUp || keyBind == KeyBinds.keySelectorDown) //TODO favourites?
        {
            handleMorphInput(keyBind, isReleased);
        }
    }

    private void handleMorphInput(KeyBind keyBind, boolean isReleased)
    {
        if(MorphHandler.INSTANCE.canMorph(Minecraft.getInstance().player))
        {
            if(keyBind == KeyBinds.keySelectorDown || keyBind == KeyBinds.keySelectorUp)
            {
                if(showSelector)
                {
                    if(keyBind == KeyBinds.keySelectorDown)
                    {
                        shiftIndexSelector(true);
                    }
                    else //selector up
                    {
                        shiftIndexSelector(false);
                    }
                }
                else
                {
                    showSelector = true;

                    setIndicesToCurrentMorph();

                    //reset the keydowns
                    keyEscDown = false;
                    keyEnterDown = false;
                }
            }
        }
        else
        {
            //TODO flash the biomass bar
        }
    }

    private void setIndicesToCurrentMorph()
    {
        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;

        indexVert = indexHori = 0; //the player default morph should always be first.

        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(Minecraft.getInstance().player);
        if(info.isMorphed())
        {
            MorphVariant currentMorph = info.nextState.variant;
            for(int i = 0; i < morphData.morphs.size(); i++)
            {
                MorphVariant variant = morphData.morphs.get(i);
                if(variant.id.equals(currentMorph.id))
                {
                    indexVert = i;

                    for(int i1 = 0; i1 < variant.variants.size(); i1++)
                    {
                        MorphVariant.Variant morphVariant = variant.variants.get(i1);
                        if(morphVariant.identifier.equals(currentMorph.thisVariant.identifier))
                        {
                            indexHori = i1;
                            break;
                        }
                    }

                    break;
                }
            }
        }

        lastIndexVert = indexVert;
        lastIndexHori = indexHori;
    }

    private void tick()
    {
        if(showSelector)
        {
            showTime++;
            if(showTime > SHOW_SELECTOR_TIME)
            {
                showTime = SHOW_SELECTOR_TIME;
            }

            indexChangeTime++;
            if(indexChangeTime > INDEX_TIME)
            {
                indexChangeTime = INDEX_TIME;
                lastIndexVert = indexVert;
                lastIndexHori = indexHori;
            }

            Minecraft mc = Minecraft.getInstance();

            boolean isEnterDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_ENTER) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_KP_ENTER);
            if(!keyEnterDown && isEnterDown)
            {
                confirmSelector();
            }
            keyEnterDown = isEnterDown;

            boolean isEscDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE);
            if(mc.currentScreen != null || !keyEscDown && isEscDown)
            {
                closeSelector();
            }
            keyEscDown = isEscDown;
        }
        else
        {
            showTime--;
            if(showTime < 0)
            {
                showTime = 0;
            }
        }
    }

    private void confirmSelector()
    {
        Morph.channel.sendToServer(new PacketMorphInput(Morph.eventHandlerClient.morphData.morphs.get(indexVert).variants.get(indexHori).identifier, false, false));

        closeSelector(); //TODO do not morph if you're already morphed as it
    }

    private void closeSelector()
    {
        showSelector = false;
    }

    private void shiftIndexSelector(boolean isDown)
    {
        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;
        if(isDown)
        {
            if(Screen.hasShiftDown()) //adjust horizontally
            {
                lastIndexHori = (lastIndexHori + (indexHori - lastIndexHori) * (EntityHelper.sineifyProgress(MathHelper.clamp((float)indexChangeTime / INDEX_TIME, 0F, 1F))));

                indexHori++;
                if(indexHori >= morphData.morphs.get(indexVert).variants.size())
                {
                    indexHori = 0;
                }
            }
            else
            {
                lastIndexVert = (lastIndexVert + (indexVert - lastIndexVert) * (EntityHelper.sineifyProgress(MathHelper.clamp((float)indexChangeTime / INDEX_TIME, 0F, 1F))));

                indexVert++;
                if(indexVert >= morphData.morphs.size())
                {
                    indexVert = 0;
                }

                lastIndexHori = morphData.morphs.get(indexVert).variants.size() - 1;
                indexHori = 0;//reset the hori index
            }
        }
        else
        {
            if(Screen.hasShiftDown()) //adjust horizontally
            {
                lastIndexHori = (lastIndexHori + (indexHori - lastIndexHori) * (EntityHelper.sineifyProgress(MathHelper.clamp((float)indexChangeTime / INDEX_TIME, 0F, 1F))));

                indexHori--;
                if(indexHori < 0)
                {
                    indexHori = morphData.morphs.get(indexVert).variants.size() - 1;
                }
            }
            else
            {
                lastIndexVert = (lastIndexVert + (indexVert - lastIndexVert) * (EntityHelper.sineifyProgress(MathHelper.clamp((float)indexChangeTime / INDEX_TIME, 0F, 1F))));

                indexVert--;
                if(indexVert < 0)
                {
                    indexVert = morphData.morphs.size() - 1;
                }

                lastIndexHori = morphData.morphs.get(indexVert).variants.size() - 1;
                indexHori = 0;//reset the hori index
            }
        }
        indexChangeTime = 0;
    }

    private void drawSelector(MatrixStack stack, float partialTicks, MainWindow window)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        double zLevel = 0D;

        double size = 50 * Morph.configClient.selectorScale;

        float outProg = EntityHelper.sineifyProgress(MathHelper.clamp((showSelector ? ((showTime + partialTicks) / SHOW_SELECTOR_TIME) : (showTime - partialTicks) / SHOW_SELECTOR_TIME), 0F, 1F));

        int top = Morph.configClient.selectorDistanceFromTop;

        double posX = -size * (1F - outProg);

        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;

        //Draw the vertical stack
        double indexVertProg = (lastIndexVert + (indexVert - lastIndexVert) * (EntityHelper.sineifyProgress(MathHelper.clamp((indexChangeTime + partialTicks) / INDEX_TIME, 0F, 1F))));
        double unSelY = indexVertProg * size;
        double height = size * morphData.morphs.size();

        Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX, top - unSelY, size, height, zLevel, 0D, 1D, 0D, morphData.morphs.size());

        //Draw the horizontal stack
        double indexHoriProg = (lastIndexHori + (indexHori - lastIndexHori) * (EntityHelper.sineifyProgress(MathHelper.clamp((indexChangeTime + partialTicks) / INDEX_TIME, 0F, 1F))));
        double unSelX = indexHoriProg * size;
        double width = size * (morphData.morphs.get(indexVert).variants.size() - 1);

        if(width > 0)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED_SIDE);
            RenderHelper.draw(stack, posX - unSelX, top, width, size, zLevel, 0D, morphData.morphs.get(indexVert).variants.size() - 1, 0D, 1D);
        }

        //Draw the end of the horizontal stack
        Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX - unSelX + width, top, size, size, zLevel, 0D, 1D, 0D, 1D);

        //Draw the selected marker
        RenderHelper.drawTexture(stack, TEX_QS_SELECTED, posX, top, size, size, zLevel);

        //Draw the entities
        int screenHeight = window.getScaledHeight();

        int firstMorphIndex = Math.max(0, indexVert - ((int)Math.ceil(top / size) + 1)); //first index to render, +1 because of the scrolling
        int lastMorphIndex = Math.min(morphData.morphs.size(), indexVert + ((int)Math.ceil((screenHeight - top) / size) + 1));

        PlayerEntity player = Minecraft.getInstance().player;

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        RenderSystem.enableRescaleNormal();

        //TODO cache this
        net.minecraft.client.renderer.RenderHelper.setupLevelDiffuseLighting(LIGHT_STACK.getLast().getMatrix());

        for(int i = firstMorphIndex; i < lastMorphIndex; i++)
        {
            MorphVariant morph = morphData.morphs.get(i);
            double v1 = (top + size * 0.775D) + ((i - indexVertProg) * size);
            if(i == indexVert)
            {
                for(int j = Math.max(0, indexHori - 1); j < morph.variants.size(); j++)
                {
                    MorphVariant variant = morph.getAsVariant(morph.variants.get(j));
                    MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));

                    LivingEntity livingEntity = state.getEntityInstance(player.world, player.getGameProfile().getId());

                    RenderSystem.pushMatrix();
                    RenderSystem.translated((int)(posX + (size / 2D) - 2) + ((j - indexHoriProg) * size), (int)v1, 0D);
                    RenderSystem.rotatef(-10F, 1F, 0F, 0F);
                    RenderSystem.scalef(0.5F, 0.5F, 0.5F);
                    InventoryScreen.drawEntityOnScreen(0, 0, 35, -60, 0, livingEntity);
                    RenderSystem.popMatrix();
                }
            }
            else
            {
                MorphVariant variant = morph.getAsVariant(morph.variants.get(0));
                MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));

                LivingEntity livingEntity = state.getEntityInstance(player.world, player.getGameProfile().getId());

                RenderSystem.pushMatrix();
                RenderSystem.translated((int)(posX + (size / 2D) - 2), (int)v1, 0D);
                RenderSystem.rotatef(-10F, 1F, 0F, 0F);
                RenderSystem.scalef(0.5F, 0.5F, 0.5F);
                InventoryScreen.drawEntityOnScreen(0, 0, 35, -60, 0, livingEntity);
                RenderSystem.popMatrix();
            }
        }

        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();

        RenderSystem.disableRescaleNormal();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    private boolean shouldRenderSelector()
    {
        return showSelector || showTime > 0;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null)
        {
            tick();
        }
    }

    @SubscribeEvent
    public void onIngameGuiPost(RenderGameOverlayEvent.Post event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL && shouldRenderSelector()) //we render our selector here
        {
            drawSelector(event.getMatrixStack(), event.getPartialTicks(), event.getWindow());
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        clean();
    }

    @SubscribeEvent
    public void onRawMouseInput(InputEvent.RawMouseEvent event)
    {
        if(Morph.configClient.selectorAllowMouseControl && showSelector && event.getAction() == GLFW.GLFW_PRESS)
        {
            event.setCanceled(true);

            if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                confirmSelector();
            }
            else if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                closeSelector();
            }
            else if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            {
                //TODO favourite
            }
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollEvent event)
    {
        if(Morph.configClient.selectorAllowMouseControl && showSelector && event.getScrollDelta() != 0)
        {
            event.setCanceled(true);

            shiftIndexSelector(event.getScrollDelta() < 0);
        }
    }

    public void clean()
    {
        morphStates.clear();
    }
}
