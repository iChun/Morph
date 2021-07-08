package me.ichun.mods.morph.client.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketMorphInput;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;

public class HudHandler
{
    public static final ResourceLocation TEX_QS_FAVOURITE = new ResourceLocation("morph", "textures/gui/fav.png");
    public static final ResourceLocation TEX_QS_SELECTED = new ResourceLocation("morph", "textures/gui/gui_selected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED = new ResourceLocation("morph", "textures/gui/gui_unselected.png");
    public static final ResourceLocation TEX_QS_UNSELECTED_SIDE = new ResourceLocation("morph", "textures/gui/gui_unselected_side.png");

    private static final MatrixStack LIGHT_STACK = Util.make(new MatrixStack(), stack -> stack.translate(1D, -1D, 0D));

    //selector stuff
    private static final int SHOW_SELECTOR_TIME = 8;
    private static final int INDEX_TIME = 4;

    public boolean showSelector = false;
    public int showTime = 0;

    public int indexChangeTime = 0;
    public double lastIndexVert = 0D;
    public double lastIndexHori = 0D;

    public int indexVert = 0;
    public int indexHori = 0;

    //radial stuff
    private static final int RADIAL_TIME = 3;

    public boolean showRadial = false;
    public int radialTime = 0;

    public RadialMode radialMode = null;

    public ArrayList<MorphVariant> radialFavourites = null;

    //key listeners
    public boolean keyEscDown;
    public boolean keyEnterDown;

    public HashMap<MorphVariant, MorphState> morphStates = new HashMap<>();

    public void handleInput(KeyBind keyBind, boolean isReleased)
    {
        if(Minecraft.getInstance().player == null) // ???what
        {
            return;
        }

        if(keyBind == KeyBinds.keySelectorUp || keyBind == KeyBinds.keySelectorDown || keyBind == KeyBinds.keySelectorLeft || keyBind == KeyBinds.keySelectorRight || keyBind == KeyBinds.keyFavourite)
        {
            handleMorphInput(keyBind, isReleased);
        }
    }

    private void handleMorphInput(KeyBind keyBind, boolean isReleased)
    {
        if(MorphHandler.INSTANCE.canMorph(Minecraft.getInstance().player))
        {
            if(keyBind == KeyBinds.keySelectorDown || keyBind == KeyBinds.keySelectorUp || keyBind == KeyBinds.keySelectorLeft || keyBind == KeyBinds.keySelectorRight)
            {
                if(showSelector)
                {
                    shiftIndexSelector(keyBind == KeyBinds.keySelectorDown || keyBind == KeyBinds.keySelectorRight, keyBind == KeyBinds.keySelectorLeft || keyBind == KeyBinds.keySelectorRight);
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
            else if(keyBind == KeyBinds.keyFavourite)
            {
                if(showSelector)
                {
                    if(isReleased)
                    {
                        toggleFavourite();
                    }
                }
                else if(!isReleased)
                {
                    //open radial menu
                    if(!showRadial)
                    {
                        gatherFavourites();

                        showRadial = true;
                        radialTime = 0;
                        radialMode = RadialMode.FAVOURITE;

                        Minecraft.getInstance().mouseHelper.ungrabMouse();
                    }
                }
                else if(radialMode == RadialMode.FAVOURITE)
                {
                    //confirm radial menu selection
                    confirmRadial();
                }
            }
        }
        else
        {
            //TODO flash the biomass bar
        }
    }

    public void setIndicesToCurrentMorph()
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
        //selector stuff
        if(showSelector)
        {
            showTime++;
            if(showTime > SHOW_SELECTOR_TIME)
            {
                showTime = SHOW_SELECTOR_TIME;
            }
        }
        else
        {
            showTime--;
            if(showTime < 0)
            {
                showTime = 0;
            }
        }

        indexChangeTime++;
        if(indexChangeTime > INDEX_TIME)
        {
            indexChangeTime = INDEX_TIME;
            lastIndexVert = indexVert;
            lastIndexHori = indexHori;
        }

        //radial stuff
        if(showRadial)
        {
            radialTime++;
            if(radialTime > RADIAL_TIME)
            {
                radialTime = RADIAL_TIME;
            }

            //TODO can radial menus show during selector?
        }

        updateKeyListeners();
    }

    private void updateKeyListeners()
    {
        Minecraft mc = Minecraft.getInstance();
        boolean isEnterDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_ENTER) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_KP_ENTER);
        boolean isEscDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE);

        if(showSelector || showRadial)
        {
            if(!keyEnterDown && isEnterDown)
            {
                if(showSelector)
                {
                    confirmSelector();
                }
                else
                {
                    confirmRadial();
                }
            }

            if(mc.currentScreen != null || !keyEscDown && isEscDown)
            {
                if(showSelector)
                {
                    closeSelector();
                }
                else
                {
                    closeRadial();
                }
            }
        }
        keyEnterDown = isEnterDown;
        keyEscDown = isEscDown;
    }

    private void confirmSelector()
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(Minecraft.getInstance().player);
        MorphVariant.Variant variant = Morph.eventHandlerClient.morphData.morphs.get(indexVert).variants.get(indexHori);

        if(!info.isCurrentlyThisVariant(variant)) //if we're already morphed to this, don't morph to this.
        {
            Morph.channel.sendToServer(new PacketMorphInput(variant.identifier, false, false));
        }

        closeSelector();
    }

    private void closeSelector()
    {
        showSelector = false;

        //makes the horizontal slider slide back in
        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;
        indexHori = morphData.morphs.get(indexVert).variants.size() - 1;
        indexChangeTime = 0;
    }

    private void confirmRadial()
    {
        if(isMouseOutsideRadialDeadZone(Minecraft.getInstance().getMainWindow()))
        {
            if(radialMode == RadialMode.FAVOURITE)
            {
                //morph to the selected Morph
                MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(Minecraft.getInstance().player);
                MorphVariant variant = radialFavourites.get(getSelectedIndex(radialFavourites.size()));

                if(!info.isCurrentlyThisVariant(variant.thisVariant)) //if we're already morphed to this, don't morph to this.
                {
                    Morph.channel.sendToServer(new PacketMorphInput(variant.thisVariant.identifier, false, false));
                }

                radialFavourites = null; //enjoy, GC.
            }
        }
        closeRadial();
    }

    private void closeRadial()
    {
        showRadial = false;
        radialMode = null;

        Minecraft.getInstance().mouseHelper.grabMouse();
    }

    private void toggleFavourite()
    {
        MorphVariant.Variant variant = Morph.eventHandlerClient.morphData.morphs.get(indexVert).variants.get(indexHori);
        if(!variant.identifier.equals(MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE)) //you can't favourite your personal variant
        {
            variant.isFavourite = !variant.isFavourite;

            Morph.channel.sendToServer(new PacketMorphInput(variant.identifier, true, variant.isFavourite));
        }
    }

    private void gatherFavourites()
    {
        radialFavourites = new ArrayList<>();
        radialFavourites.add(MorphVariant.createPlayerMorph(Minecraft.getInstance().player.getGameProfile().getId(), true));
        radialFavourites.get(0).thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;

        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;
        for(MorphVariant morph : morphData.morphs)
        {
            if(morph.hasFavourite())
            {
                for(MorphVariant.Variant variant : morph.variants)
                {
                    if(variant.isFavourite)
                    {
                        radialFavourites.add(morph.getAsVariant(variant));
                    }
                }
            }
        }
    }

    private void shiftIndexSelector(boolean isDown, boolean isHori)
    {
        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;
        if(isDown)
        {
            if(isHori) //adjust horizontally
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
            if(isHori) //adjust horizontally
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

    private void drawSelector(MatrixStack stack, float partialTick, MainWindow window)
    {
        Minecraft mc = Minecraft.getInstance();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        double zLevel = 0D;

        double size = 50 * Morph.configClient.selectorScale;

        float outProg = EntityHelper.sineifyProgress(MathHelper.clamp((showSelector ? ((showTime + partialTick) / SHOW_SELECTOR_TIME) : (showTime - partialTick) / SHOW_SELECTOR_TIME), 0F, 1F));

        int top = Morph.configClient.selectorDistanceFromTop;

        double posX = -size * (1F - outProg);

        PlayerMorphData morphData = Morph.eventHandlerClient.morphData;

        float indexChangeTimeProg = EntityHelper.sineifyProgress(MathHelper.clamp((indexChangeTime + partialTick) / INDEX_TIME, 0F, 1F));

        //Draw the vertical stack
        double indexVertProg = (lastIndexVert + (indexVert - lastIndexVert) * indexChangeTimeProg);
        double unSelY = indexVertProg * size;
        double height = size * morphData.morphs.size();

        mc.getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX, top - unSelY, size, height, zLevel, 0D, 1D, 0D, morphData.morphs.size());

        //Draw the horizontal stack
        double indexHoriProg = (lastIndexHori + (indexHori - lastIndexHori) * indexChangeTimeProg);
        double unSelX = indexHoriProg * size;
        double width = size * (morphData.morphs.get(indexVert).variants.size() - 1);

        if(width > 0)
        {
            mc.getTextureManager().bindTexture(TEX_QS_UNSELECTED_SIDE);
            RenderHelper.draw(stack, posX - unSelX, top, width, size, zLevel, 0D, morphData.morphs.get(indexVert).variants.size() - 1, 0D, 1D);
        }

        //Draw the end of the horizontal stack
        mc.getTextureManager().bindTexture(TEX_QS_UNSELECTED);
        RenderHelper.draw(stack, posX - unSelX + width, top, size, size, zLevel, 0D, 1D, 0D, 1D);

        //Draw the selected marker
        RenderHelper.drawTexture(stack, TEX_QS_SELECTED, posX, top, size, size, zLevel);

        //Draw the entities
        int screenHeight = window.getScaledHeight();

        int firstMorphIndex = Math.max(0, indexVert - ((int)Math.ceil(top / size) + 1)); //first index to render, +1 because of the scrolling
        int lastMorphIndex = Math.min(morphData.morphs.size(), indexVert + ((int)Math.ceil((screenHeight - top) / size) + 1));

        PlayerEntity player = mc.player;

        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);

        MorphVariant currentMorph;
        if(info.isMorphed())
        {
            currentMorph = info.nextState.variant;
        }
        else
        {
            currentMorph = MorphVariant.createPlayerMorph(player.getGameProfile().getId(), true);
            currentMorph.thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
        }

        for(int i = firstMorphIndex; i < lastMorphIndex; i++)
        {
            MorphVariant morph = morphData.morphs.get(i);
            double indexSizeHeight = (i - indexVertProg) * size;
            double morphHeight = (top + size * 0.775D) + indexSizeHeight;
            double textHeight = (top + (size - mc.fontRenderer.FONT_HEIGHT) / 2) + indexSizeHeight;
            double favHeight = top + (size * 0.13D) + indexSizeHeight;
            if(i == indexVert) //is selected
            {
                for(int j = Math.max(0, indexHori - 1); j < morph.variants.size(); j++)
                {
                    double indexHeightWidth = (j - indexHoriProg) * size;
                    MorphVariant.Variant theVariant = morph.variants.get(j);
                    MorphVariant variant = morph.getAsVariant(theVariant);
                    MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));
                    state.variant.thisVariant.isFavourite = theVariant.isFavourite;

                    LivingEntity living = state.getEntityInstance(player.world, player.getGameProfile().getId());

                    float entSize = Math.max(living.getWidth(), living.getHeight()) / 1.95F; //1.95F = zombie height

                    if(j == indexHori) //if it is selected, prevent the downscale.
                    {
                        if(showSelector)
                        {
                            entSize *= (1F - indexChangeTimeProg);
                        }
                        else if(j == Math.round(lastIndexHori) && indexChangeTimeProg < 1F || variant.equals(currentMorph))
                        {
                            entSize = 0F; //keep the morph big
                        }
                    }

                    float entScale = 0.5F * (1F / Math.max(1F, entSize));

                    renderMorphEntity(living, (posX + (size / 2D) - 2) + indexHeightWidth, morphHeight, zLevel + (j == indexHori ? 100F : 50F), entScale);

                    zLevel += 30F;

                    if(j == 0 && morph.hasFavourite() || state.variant.thisVariant.isFavourite)
                    {
                        stack.push();
                        stack.translate(0F, 0F, 300F);

                        if(!state.variant.thisVariant.isFavourite)
                        {
                            RenderHelper.colour(0x00ffff);
                        }

                        RenderHelper.drawTexture(stack, TEX_QS_FAVOURITE, posX + 1 + indexHeightWidth, favHeight, size * 0.15D, size * 0.15D, zLevel);
                        RenderHelper.colour(0xffffff); //reset the colour

                        stack.pop();
                    }

                    if(j == morph.variants.size() - 1)
                    {
                        MorphVariant selectedVariant = morph.getAsVariant(morph.variants.get(indexHori));
                        MorphState selectedState = morphStates.computeIfAbsent(selectedVariant, v -> new MorphState(selectedVariant));

                        LivingEntity selectedLiving = selectedState.getEntityInstance(player.world, player.getGameProfile().getId());

                        IFormattableTextComponent text;

                        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(variant.id);
                        if(value != null)
                        {
                            if(!selectedLiving.getName().equals(value.getName())) //has a custom name
                            {
                                text = living.getName().deepCopy();
                                text.setStyle(text.getStyle().setItalic(true));
                            }
                            else
                            {
                                text = new TranslationTextComponent(value.getTranslationKey());
                            }
                        }
                        else
                        {
                            text = new TranslationTextComponent("morph.morph.type.unknown");
                        }


                        if(selectedVariant.equals(currentMorph))
                        {
                            text.setStyle(text.getStyle().setFormatting(TextFormatting.GOLD));
                        }
                        else
                        {
                            text.setStyle(text.getStyle().setFormatting(TextFormatting.YELLOW));
                        }

                        stack.push();
                        stack.translate(0F, 0F, 300F);
                        mc.fontRenderer.drawTextWithShadow(stack, text, (float)((posX + size + 5) + indexHeightWidth), (float)textHeight, 0xFFFFFF);
                        stack.pop();
                    }
                }
            }
            else
            {
                MorphVariant.Variant theVariant = morph.variants.get(0);
                MorphVariant variant = morph.getAsVariant(theVariant);
                MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));
                state.variant.thisVariant.isFavourite = theVariant.isFavourite;

                LivingEntity living = state.getEntityInstance(player.world, player.getGameProfile().getId());

                float entSize = Math.max(living.getWidth(), living.getHeight()) / 1.95F; //1.95F = zombie height

                if(i == Math.round(lastIndexVert)) //last selected
                {
                    entSize *= indexChangeTimeProg;
                }

                float entScale = 0.5F * (1F / Math.max(1F, entSize));

                renderMorphEntity(living, (int)(posX + (size / 2D) - 2), morphHeight, zLevel, entScale);

                zLevel += 30F;

                IFormattableTextComponent text;

                EntityType<?> value = ForgeRegistries.ENTITIES.getValue(variant.id);
                if(value != null)
                {
                    text = new TranslationTextComponent(value.getTranslationKey());
                }
                else
                {
                    text = new TranslationTextComponent("morph.morph.type.unknown");
                }

                if(morph.containsVariant(currentMorph))
                {
                    text.setStyle(text.getStyle().setFormatting(TextFormatting.GOLD));
                }
                else
                {
                    text.setStyle(text.getStyle().setFormatting(TextFormatting.WHITE));
                }

                stack.push();
                stack.translate(0F, 0F, 300F);
                if(morph.hasFavourite())
                {
                    if(!state.variant.thisVariant.isFavourite)
                    {
                        RenderHelper.colour(0x00ffff);
                    }

                    RenderHelper.drawTexture(stack, TEX_QS_FAVOURITE, posX + 1, favHeight, size * 0.15D, size * 0.15D, zLevel);
                    RenderHelper.colour(0xffffff); //reset the colour
                }

                mc.fontRenderer.drawTextWithShadow(stack, text, (float)(posX + size + 5), (float)textHeight, 0xFFFFFF);
                stack.pop();
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
    }

    private void drawRadial(MatrixStack stack, float partialTick, MainWindow window)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        Minecraft mc = Minecraft.getInstance();

        double diameter = Math.min(window.getScaledWidth(), window.getScaledHeight()) * Morph.configClient.radialScale;
        double radius = diameter / 2D;

        double radialProg = EntityHelper.sineifyProgress(MathHelper.clamp((radialTime + partialTick) / RADIAL_TIME, 0F, 1F));
        double radialDist = radius * radialProg;
        float textScale = (float)radius / 96.375F;
        float deadzoneScale = 0.55F;
        float deadzoneSize = (float)radius * deadzoneScale;

        double distanceFromDeadzone = getMouseDistanceFromCenter(window) - deadzoneSize;

        float bonusScale = MathHelper.clamp((float)(distanceFromDeadzone / (radius * (1F - deadzoneScale) * 0.5F)), 0F, 1F);

        double centerX = window.getScaledWidth() / 2D;
        double centerY = window.getScaledHeight() / 2D;

        float zLevel = 0F;

        int slices = mc.gameSettings.graphicFanciness == GraphicsFanciness.FAST ? 30 : 100;

        stack.push();
        stack.translate(centerX, centerY, zLevel);
        Matrix4f matrix = stack.getLast().getMatrix();
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= slices; i++)
        {
            double angle = Math.PI * 2 * i / slices;
            bufferbuilder.pos(matrix, (float)(Math.cos(angle) * radialDist), (float)(Math.sin(angle) * radialDist), 0).color(0, 0, 0, 150).endVertex();
            bufferbuilder.pos(matrix, (float)(Math.cos(angle) * radialDist * deadzoneScale), (float)(Math.sin(angle) * radialDist * deadzoneScale), 0).color(0, 0, 0, 150).endVertex();
        }
        tessellator.draw();
        RenderSystem.enableTexture();

        TranslationTextComponent catText = new TranslationTextComponent(radialMode == RadialMode.FAVOURITE ? "morph.key.favourite" : "morph.key.ability");

        stack.push();
        stack.translate(0F, 0F, 300F);
        stack.scale(textScale, textScale, 1F);
        int textWidth = mc.fontRenderer.getStringPropertyWidth(catText);
        mc.fontRenderer.drawTextWithShadow(stack, catText, -textWidth / 2F, -mc.fontRenderer.FONT_HEIGHT / 2F, 0xFFFFFF);
        stack.pop();

        if(radialMode == RadialMode.FAVOURITE)
        {
            PlayerEntity player = mc.player;

            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);

            MorphVariant currentMorph;
            if(info.isMorphed())
            {
                currentMorph = info.nextState.variant;
            }
            else
            {
                currentMorph = MorphVariant.createPlayerMorph(player.getGameProfile().getId(), true);
                currentMorph.thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
            }

            for(int i = 0; i < radialFavourites.size(); i++)
            {
                MorphVariant variant = radialFavourites.get(i);
                MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));

                LivingEntity living = state.getEntityInstance(player.world, player.getGameProfile().getId());

                boolean isSelectedIndex = isMouseOutsideRadialDeadZone(window) && i == getSelectedIndex(radialFavourites.size());

                float entSize = Math.max(living.getWidth(), living.getHeight()) / 1.95F; //1.95F = zombie height

                float entScale = 0.4F * (1F / Math.max(1F, entSize)) * (float)(radialProg * textScale);

                if(isSelectedIndex)
                {
                    entScale += 0.1F * bonusScale;
                }

                double angle = Math.toRadians(90F + (360F * i / radialFavourites.size()));

                RenderSystem.pushMatrix();
                RenderSystem.translated(0F, radialDist * 0.1F, 0F);
                renderMorphEntity(living, centerX - (Math.cos(angle) * radialDist * 0.775F), centerY - (float)(Math.sin(angle) * radialDist * 0.775F), zLevel + 50F, entScale);
                RenderSystem.popMatrix();
            }

            for(int i = 0; i < radialFavourites.size(); i++)
            {
                MorphVariant variant = radialFavourites.get(i);
                MorphState state = morphStates.computeIfAbsent(variant, v -> new MorphState(variant));

                LivingEntity living = state.getEntityInstance(player.world, player.getGameProfile().getId());

                boolean isSelectedIndex = isMouseOutsideRadialDeadZone(window) && i == getSelectedIndex(radialFavourites.size());

                double angle = Math.toRadians(90F + (360F * i / radialFavourites.size()));

                IFormattableTextComponent text;

                EntityType<?> value = ForgeRegistries.ENTITIES.getValue(variant.id);
                if(value != null)
                {
                    if(!living.getName().equals(value.getName())) //has a custom name
                    {
                        text = living.getName().deepCopy();
                        text.setStyle(text.getStyle().setItalic(true));
                    }
                    else
                    {
                        text = new TranslationTextComponent(value.getTranslationKey());
                    }
                }
                else
                {
                    text = new TranslationTextComponent("morph.morph.type.unknown");
                }

                if(variant.thisVariant.identifier.equals(currentMorph.thisVariant.identifier))
                {
                    text.setStyle(text.getStyle().setFormatting(TextFormatting.GOLD));
                }
                else if(isSelectedIndex)
                {
                    text.setStyle(text.getStyle().setFormatting(TextFormatting.YELLOW));
                }
                else
                {
                    text.setStyle(text.getStyle().setFormatting(TextFormatting.WHITE));
                }

                float scale = 0.5F * (float)(radialProg * textScale);
                stack.push();
                stack.translate(0F, radialDist * 0.1F + mc.fontRenderer.FONT_HEIGHT / 1.75F * textScale, 300F);
                stack.scale(scale, scale, 1F);
                mc.fontRenderer.drawTextWithShadow(stack, text, (float)(-(Math.cos(angle) * radialDist * 0.775F) / scale - mc.fontRenderer.getStringPropertyWidth(text) / 2F), (float)(-Math.sin(angle) * radialDist * 0.775F / scale), 0xFFFFFF);
                stack.pop();
            }
        }

        stack.pop();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
    }

    private void renderMorphEntity(LivingEntity livingEntity, double x, double y, double z, float scale)
    {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        RenderSystem.enableRescaleNormal();

        net.minecraft.client.renderer.RenderHelper.setupLevelDiffuseLighting(LIGHT_STACK.getLast().getMatrix());

        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, z);
        RenderSystem.rotatef(-10F, 1F, 0F, 0F);
        RenderSystem.scalef(scale, scale, scale);
        InventoryScreen.drawEntityOnScreen(0, 0, 35, -60, 0, livingEntity);
        RenderSystem.popMatrix();

        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();

        RenderSystem.disableRescaleNormal();

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
    }

    private double getMouseDistanceFromCenter(MainWindow window)
    {
        double centerX = window.getScaledWidth() / 2D;
        double centerY = window.getScaledHeight() / 2D;

        Minecraft mc = Minecraft.getInstance();

        double posX = mc.mouseHelper.mouseX * window.getScaledWidth() / window.getWidth() - centerX;
        double posY = mc.mouseHelper.mouseY * window.getScaledHeight() / window.getHeight() - centerY;

        return Math.sqrt(posX * posX + posY * posY);
    }

    private boolean isMouseOutsideRadialDeadZone(MainWindow window)
    {
        double diameter = Math.min(window.getScaledWidth(), window.getScaledHeight()) * Morph.configClient.radialScale;
        double deadZoneBorder = diameter / 2D * 0.55F;

        return getMouseDistanceFromCenter(window) > deadZoneBorder;
    }

    private float getMouseAngleFromCenter(MainWindow window)
    {
        double centerX = window.getScaledWidth() / 2D;
        double centerY = window.getScaledHeight() / 2D;

        Minecraft mc = Minecraft.getInstance();

        double posX = mc.mouseHelper.mouseX * window.getScaledWidth() / window.getWidth() - centerX;
        double posY = mc.mouseHelper.mouseY * window.getScaledHeight() / window.getHeight() - centerY;

        return (float)(Math.toDegrees(Math.atan2(posY, posX)) + 90F + 360F) % 360F;
    }

    private int getSelectedIndex(int count)
    {
        float angle = getMouseAngleFromCenter(Minecraft.getInstance().getMainWindow());

        float segment = 360F / count;

        float startSeg = segment / 2F;

        for(int i = 0; i < count; i++)
        {
            if(angle < startSeg)
            {
                return i;
            }
            startSeg += segment;
        }

        return 0; //angle is larger than (360 - (segment / 2)), it's back to index of 0
    }

    private boolean shouldRenderSelector()
    {
        return showSelector || showTime > 0;
    }

    public void clean()
    {
        morphStates.clear();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            if((showSelector || showRadial) && Minecraft.getInstance().currentScreen instanceof IngameMenuScreen)
            {
                Minecraft.getInstance().displayGuiScreen(null);

                if(showSelector)
                {
                    closeSelector();
                }
                else
                {
                    closeRadial();
                }
            }
        }
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
    public void onIngameGuiPre(RenderGameOverlayEvent.Pre event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && showRadial)
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onIngameGuiPost(RenderGameOverlayEvent.Post event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL) //we render our selector here
        {
            if(shouldRenderSelector())
            {
                drawSelector(event.getMatrixStack(), event.getPartialTicks(), event.getWindow());
            }
            if(showRadial)
            {
                drawRadial(event.getMatrixStack(), event.getPartialTicks(), event.getWindow());
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if(event.getWorld().isRemote())
        {
            clean();
        }
    }

    @SubscribeEvent
    public void onRawMouseInput(InputEvent.RawMouseEvent event)
    {
        if(Morph.configClient.selectorAllowMouseControl && event.getAction() == GLFW.GLFW_PRESS)
        {
            if(showSelector)
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
                    toggleFavourite();
                }
            }
            else if(showRadial)
            {
                event.setCanceled(true);

                if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                {
                    confirmRadial();
                }
                else if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT || event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
                {
                    closeRadial();
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollEvent event)
    {
        if(Morph.configClient.selectorAllowMouseControl && showSelector && event.getScrollDelta() != 0)
        {
            event.setCanceled(true);

            shiftIndexSelector(event.getScrollDelta() < 0, Screen.hasShiftDown());
        }
    }

    public enum RadialMode
    {
        FAVOURITE,
        ABILITY
    }
}
