package me.ichun.mods.morph.client.render.hand;

import com.google.gson.Gson;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.api.client.hand.HandInfo;
import me.ichun.mods.ichunutil.api.common.PlacementCorrector;
import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.event.MorphLoadResourceEvent;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public final class HandHandler
{
    private static final HashMap<Class<? extends EntityModel>, HandInfo> MODEL_HAND_INFO = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static HandHandler instance;

    private MorphInfo lastMorphInfo;
    private float lastPartialTick;

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) //if we're getting the event, the config has already assigned us;
    {
        Minecraft mc = Minecraft.getInstance();
        if(!mc.player.removed) //we need to cache this as the hand may be rendered even in the death screen.
        {
            lastMorphInfo = MorphHandler.INSTANCE.getMorphInfo(mc.player);
            lastPartialTick = event.getPartialTicks();
        }
    }

    //Returns true if we have to override and render the hand.
    public boolean renderHand(PlayerRenderer playerRenderer, MatrixStack stack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, ModelRenderer arm, ModelRenderer armwear)
    {
        //Check if this is the player, and we have the player's morph info.
        if(player == Minecraft.getInstance().getRenderViewEntity() && lastMorphInfo != null && !MorphRenderHandler.isRenderingMorph)
        {
            MorphInfo info = lastMorphInfo;
            float partialTick = lastPartialTick;
            float skinAlpha = info.getMorphSkinAlpha(partialTick);
            if(skinAlpha > 0F || info.isMorphed()) // if we're supposed to override the hand render
            {
                Minecraft mc = Minecraft.getInstance();

                ModelRenderer[] handParts = null;
                MatrixStack[] stacks = null;
                ResourceLocation texture = null;

                HandSide handSide = playerRenderer.entityModel.bipedRightArm != arm ? HandSide.LEFT : HandSide.RIGHT; //default to right arm instead any mods override the player model

                float morphProg = info.getMorphProgress(partialTick);
                float transitionProg = info.getTransitionProgressSine(partialTick);
                if(morphProg < 1F && transitionProg < 1F) //still morphing, transition may be required.
                {
                    if(transitionProg <= 0F)
                    {
                        LivingEntity livingInstance = info.prevState.getEntityInstance(mc.player.world, mc.player.getGameProfile().getId());
                        EntityRenderer entRenderer = playerRenderer.getRenderManager().getRenderer(livingInstance);
                        if(entRenderer instanceof LivingRenderer)
                        {
                            stack.push();
                            stack.translate(0D, -500D, 0D);
                            MorphRenderHandler.renderLiving(entRenderer, livingInstance, stack, buffer, light, partialTick);
                            stack.pop();

                            LivingRenderer livingRenderer = (LivingRenderer)entRenderer;
                            EntityModel entityModel = livingRenderer.getEntityModel();

                            HandInfo handInfo = HandHandler.getHandInfo(entityModel.getClass());
                            if(handInfo != null)
                            {
                                renderModelPreHandModelRendererCopy(entityModel, livingInstance);

                                handParts = handInfo.getHandParts(handSide, entityModel);
                                stacks = handInfo.getPlacementCorrectors(handSide);
                                texture = entRenderer.getEntityTexture(livingInstance);
                            }
                        }
                    }
                    else
                    {
                        ModelRenderer[] prevHandParts = null;
                        MatrixStack[] prevStacks = null;

                        ModelRenderer[] nextHandParts = null;
                        MatrixStack[] nextStacks = null;

                        LivingEntity prevInstance = info.prevState.getEntityInstance(mc.player.world, mc.player.getGameProfile().getId());
                        EntityRenderer prevRenderer = playerRenderer.getRenderManager().getRenderer(prevInstance);

                        LivingEntity nextInstance = info.nextState.getEntityInstance(mc.player.world, mc.player.getGameProfile().getId());
                        EntityRenderer nextRenderer = playerRenderer.getRenderManager().getRenderer(nextInstance);

                        stack.push();
                        stack.translate(0D, -500D, 0D); //maybe I should just set scale to 0?
                        if(prevRenderer instanceof LivingRenderer)
                        {
                            MorphRenderHandler.renderLiving(prevRenderer, prevInstance, stack, buffer, light, partialTick);

                            LivingRenderer livingRenderer = (LivingRenderer)prevRenderer;
                            EntityModel entityModel = livingRenderer.getEntityModel();

                            HandInfo handInfo = HandHandler.getHandInfo(entityModel.getClass());
                            if(handInfo != null)
                            {
                                renderModelPreHandModelRendererCopy(entityModel, prevInstance);

                                prevHandParts = handInfo.getHandParts(handSide, entityModel);
                                prevStacks = handInfo.getPlacementCorrectors(handSide);
                            }
                        }
                        if(nextRenderer instanceof LivingRenderer)
                        {
                            MorphRenderHandler.renderLiving(nextRenderer, nextInstance, stack, buffer, light, partialTick);

                            LivingRenderer livingRenderer = (LivingRenderer)nextRenderer;
                            EntityModel entityModel = livingRenderer.getEntityModel();

                            HandInfo handInfo = HandHandler.getHandInfo(entityModel.getClass());
                            if(handInfo != null)
                            {
                                renderModelPreHandModelRendererCopy(entityModel, nextInstance);

                                nextHandParts = handInfo.getHandParts(handSide, entityModel);
                                nextStacks = handInfo.getPlacementCorrectors(handSide);
                            }
                        }
                        stack.pop();

                        if(prevHandParts != null || nextHandParts != null)
                        {
                            if(prevHandParts == null)
                            {
                                prevHandParts = new ModelRenderer[nextHandParts.length];
                                prevStacks = new MatrixStack[nextHandParts.length];
                            }
                            if(nextHandParts == null)
                            {
                                nextHandParts = new ModelRenderer[prevHandParts.length];
                                nextStacks = new MatrixStack[prevHandParts.length];
                            }
                            if(prevHandParts.length < nextHandParts.length)
                            {
                                prevHandParts = Arrays.copyOf(prevHandParts, nextHandParts.length);
                                prevStacks = Arrays.copyOf(prevStacks, nextHandParts.length);
                            }
                            if(nextHandParts.length < prevHandParts.length)
                            {
                                nextHandParts = Arrays.copyOf(nextHandParts, prevHandParts.length);
                                nextStacks = Arrays.copyOf(nextStacks, prevHandParts.length);
                            }

                            //at this point the arrays have the same length
                            handParts = new ModelRenderer[prevHandParts.length];
                            stacks = new MatrixStack[prevHandParts.length];

                            for(int i = 0; i < handParts.length; i++)
                            {
                                Project.Part oldPart = ModelHelper.createPartFor(prevHandParts[i], true);
                                Project.Part newPart = ModelHelper.createPartFor(nextHandParts[i], true);

                                ModelHelper.matchBoxAndChildrenCount(oldPart, newPart);
                                ModelHelper.matchBoxAndChildrenCount(newPart, oldPart);

                                handParts[i] = ModelHelper.createModelRenderer(ModelHelper.createInterimPart(oldPart, newPart, transitionProg), true);

                                if(prevStacks[i] != null || nextStacks[i] != null)
                                {
                                    MatrixStack.Entry interimStackEntry = RenderHelper.createInterimStackEntry(prevStacks[i] != null ? prevStacks[i].getLast() : (new MatrixStack()).getLast(), nextStacks[i] != null ? nextStacks[i].getLast() : (new MatrixStack()).getLast(), transitionProg);
                                    MatrixStack interimStack = new MatrixStack();
                                    MatrixStack.Entry last = interimStack.getLast();
                                    last.getMatrix().mul(interimStackEntry.getMatrix());
                                    last.getNormal().mul(interimStackEntry.getNormal());
                                    stacks[i] = interimStack;
                                }
                                else
                                {
                                    stacks[i] = null;
                                }
                            }
                        }
                    }
                }
                else //morph completed, just use nextState's entity instance
                {
                    LivingEntity livingInstance = info.isMorphed() ? info.nextState.getEntityInstance(mc.player.world, mc.player.getGameProfile().getId()) : mc.player;
                    EntityRenderer entRenderer = playerRenderer.getRenderManager().getRenderer(livingInstance);
                    if(entRenderer instanceof LivingRenderer)
                    {
                        stack.push();
                        stack.translate(0D, -500D, 0D);
                        MorphRenderHandler.renderLiving(entRenderer, livingInstance, stack, buffer, light, partialTick);
                        stack.pop();

                        LivingRenderer livingRenderer = (LivingRenderer)entRenderer;
                        EntityModel entityModel = livingRenderer.getEntityModel();

                        HandInfo handInfo = HandHandler.getHandInfo(entityModel.getClass());
                        if(handInfo != null)
                        {
                            renderModelPreHandModelRendererCopy(entityModel, livingInstance);

                            handParts = handInfo.getHandParts(handSide, entityModel);
                            stacks = handInfo.getPlacementCorrectors(handSide);
                            texture = entRenderer.getEntityTexture(livingInstance);
                        }
                    }

                    if(entRenderer instanceof PlayerRenderer && livingInstance instanceof AbstractClientPlayerEntity)//this must be a player
                    {
                        MorphRenderHandler.isRenderingMorph = true;
                        PlayerRenderer morphPlayerRenderer = (PlayerRenderer)entRenderer;
                        if(handSide == HandSide.LEFT)
                        {
                            morphPlayerRenderer.renderLeftArm(stack, buffer, light, (AbstractClientPlayerEntity)livingInstance);
                        }
                        else
                        {
                            morphPlayerRenderer.renderRightArm(stack, buffer, light, (AbstractClientPlayerEntity)livingInstance);
                        }
                        MorphRenderHandler.isRenderingMorph = false;

                        if(handParts != null && skinAlpha > 0F) //let's check the handParts just in case BipedModel.json is missing
                        {
                            renderModelPartsWithTexture(handParts, stacks, stack, buffer.getBuffer(RenderType.getEntityTranslucent(MorphHandler.INSTANCE.getMorphSkinTexture())), light, skinAlpha);
                        }
                        return true; //we're done here, the player render does the work for us
                    }
                }

                if(handParts != null)
                {
                    if(texture != null)
                    {
                        renderModelPartsWithTexture(handParts, stacks, stack, buffer.getBuffer(RenderType.getEntityTranslucent(texture)), light, 1F);
                    }

                    if(skinAlpha > 0F)
                    {
                        renderModelPartsWithTexture(handParts, stacks, stack, buffer.getBuffer(RenderType.getEntityTranslucent(MorphHandler.INSTANCE.getMorphSkinTexture())), light, skinAlpha);
                    }
                }
                return true;
            }
        }

        return false;
    }

    private static void renderModelPreHandModelRendererCopy(EntityModel entityModel, LivingEntity livingInstance)
    {
        //these taken from PlayerRenderer
        entityModel.swingProgress = 0.0F;
        if(entityModel instanceof BipedModel)
        {
            ((BipedModel<?>)entityModel).isSneak = false;
            ((BipedModel<?>)entityModel).swimAnimation = 0.0F;
        }

        //Reset the limb swing because some mods calculate directly in their renderer :(
        float limbSwing = livingInstance.limbSwing;
        float prevLimbSwingAmount = livingInstance.prevLimbSwingAmount;
        float limbSwingAmount = livingInstance.limbSwingAmount;

        livingInstance.limbSwing = 0F;
        livingInstance.prevLimbSwingAmount = 0F;
        livingInstance.limbSwingAmount = 0F;

        entityModel.setRotationAngles(livingInstance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        livingInstance.limbSwing = limbSwing;
        livingInstance.prevLimbSwingAmount = prevLimbSwingAmount;
        livingInstance.limbSwingAmount = limbSwingAmount;

    }

    private static void renderModelPartsWithTexture(ModelRenderer[] parts, MatrixStack[] stacks, MatrixStack stack, IVertexBuilder buffer, int light, float alpha)
    {
        for(int i = 0; i < parts.length; i++)
        {
            ModelRenderer part = parts[i];
            if(part == null)
            {
                continue;
            }

            float prevX = part.rotateAngleX;
            part.rotateAngleX = 0F;

            //taken from ModelRenderer.render
            if(part.showModel && (!part.cubeList.isEmpty() || !part.childModels.isEmpty()))
            {
                stack.push();

                part.translateRotate(stack);

                if(stacks[i] != null) //inject our stack to reverse rotation and do the appropriate translates
                {
                    PlacementCorrector.multiplyStackWithStack(stack, stacks[i]);
                }

                part.doRender(stack.getLast(), buffer, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha);

                for(ModelRenderer modelrenderer : part.childModels) {
                    modelrenderer.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, alpha);
                }

                stack.pop();
            }

            part.rotateAngleX = prevX;
        }
    }

    public static void setState(boolean allowed)
    {
        if(allowed)
        {
            if(instance == null)
            {
                MinecraftForge.EVENT_BUS.register(instance = new HandHandler());
            }
        }
        else
        {
            if(instance != null)
            {
                MinecraftForge.EVENT_BUS.unregister(instance);
                instance = null;
            }
        }
    }

    @Nullable
    private static HandInfo getHandInfo(Class<? extends EntityModel> clz)
    {
        if(MODEL_HAND_INFO.containsKey(clz))
        {
            return MODEL_HAND_INFO.get(clz);
        }
        HandInfo helper = null;
        Class clzz = clz.getSuperclass();
        if(clzz != EntityModel.class)
        {
            helper = getHandInfo(clzz);
            //            if(helper != null)
            //            {
            //                helper = GSON.fromJson(GSON.toJson(helper), helper.getClass());
            //            }
        }
        MODEL_HAND_INFO.put(clz, helper);
        return helper;
    }

    public static void loadHandInfos()
    {
        MODEL_HAND_INFO.clear();

        ArrayList<HandInfo> infos = new ArrayList<>();
        try
        {
            IOUtil.scourDirectoryForFiles(ResourceHandler.getMorphDir().resolve("hand"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    try
                    {
                        HandInfo handInfo = GSON.fromJson(FileUtils.readFileToString(p.toFile(), "UTF-8"), HandInfo.class);
                        if(handInfo.setup())
                        {
                            infos.add(handInfo);
                            return true;
                        }
                    }
                    catch(IOException e)
                    {
                        Morph.LOGGER.error("Error reading file: {}", p);
                        e.printStackTrace();
                    }
                    return false;
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error reading Hand Infos");
            e.printStackTrace();
        }
        for(HandInfo info : infos)
        {
            if(MODEL_HAND_INFO.containsKey(info.modelClass))
            {
                Morph.LOGGER.warn("Hand Info for {} already exists!", info.modelClass);
            }
            MODEL_HAND_INFO.put(info.modelClass, info);
        }

        Morph.LOGGER.info("Loaded {} Hand Info(s)", MODEL_HAND_INFO.size());

        MinecraftForge.EVENT_BUS.post(new MorphLoadResourceEvent(MorphLoadResourceEvent.Type.HAND));
    }
}
