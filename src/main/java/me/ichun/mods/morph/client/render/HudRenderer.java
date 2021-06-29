package me.ichun.mods.morph.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.core.KeyBinds;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketMorphInput;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class HudRenderer
{
    public static final ResourceLocation TEX_QS_SELECTED = new ResourceLocation("morph", "textures/gui/gui_selected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED = new ResourceLocation("morph", "textures/gui/gui_unselected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED_SIDE = new ResourceLocation("morph", "textures/gui/gui_unselected_side.png");

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
                    PlayerMorphData morphData = Morph.eventHandlerClient.morphData;
                    if(keyBind == KeyBinds.keySelectorDown)
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
                        }
                        indexChangeTime = 0;
                    }
                    else //selector up
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
                        }
                        indexChangeTime = 0;
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
                showSelector = false;

                Morph.channel.sendToServer(new PacketMorphInput(Morph.eventHandlerClient.morphData.morphs.get(indexVert).variants.get(indexHori).identifier, false, false));
            }
            keyEnterDown = isEnterDown;

            boolean isEscDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE);
            if(mc.currentScreen != null || !keyEscDown && isEscDown)
            {
                showSelector = false;
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

    private void drawSelector(MatrixStack stack, float partialTicks, MainWindow window)
    {
        double zLevel = 0D;

        double size = 50 * Morph.configClient.selectorScale;

        float outProg = EntityHelper.sineifyProgress(MathHelper.clamp((showSelector ? ((showTime + partialTicks) / SHOW_SELECTOR_TIME) : (showTime - partialTicks) / SHOW_SELECTOR_TIME), 0F, 1F));

        double posX = -size * (1F - outProg);

        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;

        //Draw the vertical stack
        double unSelY = (lastIndexVert + (indexVert - lastIndexVert) * (EntityHelper.sineifyProgress(MathHelper.clamp((indexChangeTime + partialTicks) / INDEX_TIME, 0F, 1F)))) * size;
        double height = size * morphData.morphs.size();

        Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX, Morph.configClient.selectorDistanceFromTop - unSelY, size, height, zLevel, 0D, 1D, 0D, morphData.morphs.size());

        //Draw the horizontal stack
        double unSelX = (lastIndexHori + (indexHori - lastIndexHori) * (EntityHelper.sineifyProgress(MathHelper.clamp((indexChangeTime + partialTicks) / INDEX_TIME, 0F, 1F)))) * size;
        double width = size * morphData.morphs.get(indexVert).variants.size() - 1;

        if(width > 0)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED_SIDE);
            RenderHelper.draw(stack, posX - unSelX, Morph.configClient.selectorDistanceFromTop, width, size, zLevel, 0D, morphData.morphs.get(indexVert).variants.size() - 1, 0D, 1D);
        }

        //Draw the end of the horizontal stack
        Minecraft.getInstance().getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX - unSelX * width, Morph.configClient.selectorDistanceFromTop, size, size, zLevel, 0D, 1D, 0D, 1D);

        //Draw the selected marker
        RenderHelper.drawTexture(stack, TEX_QS_SELECTED, posX, Morph.configClient.selectorDistanceFromTop, size, size, zLevel);

        //TODO horizontal index;
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
        //TODO clean
    }
}
