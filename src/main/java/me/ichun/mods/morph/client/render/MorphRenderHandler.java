package me.ichun.mods.morph.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MorphRenderHandler
{
    private static float playerShadowSize = -1F;
    private static boolean changedShadowSize = false;

    public static ModelRendererCapture currentCapture = null; //Are we capturing ModelRenderer renders?

    public static boolean isRenderingMorph = false;
    public static boolean denyRenderNameplate = false;

    //TODO add stats eg total morphs/total biomass/biomass lost etc
    //TODO config that forces nameplate rendering when morphed
    public static void renderMorphInfo(PlayerEntity player, MorphInfo info, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick) //TODO test the nameplate for named mods, maybe add a config to rename the mob
    {
        isRenderingMorph = true;

        float morphProgress = info.getMorphProgress(partialTick);

        if(morphProgress < 1F) //still morphing
        {
            float skinProg = 1F;
            float transitionProgress = info.getTransitionProgressSine(partialTick);
            if(transitionProgress <= 0F)
            {
                MorphState.syncEntityWithPlayer(info.prevState.getEntityInstance(player.world, player.getGameProfile().getId()), player);
                renderLiving(info.prevState, info.prevState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, buffer, light, partialTick);
                skinProg = EntityHelper.sineifyProgress(morphProgress / 0.125F);
            }
            else if(transitionProgress >= 1F)
            {
                MorphState.syncEntityWithPlayer(info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), player);
                renderLiving(info.nextState, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, buffer, light, partialTick);
                skinProg = 1F - EntityHelper.sineifyProgress((morphProgress - 0.875F) / 0.125F);
            }

            int overlay = LivingRenderer.getPackedOverlay(player, 0.0F); //player usually default to 0.0F;
            renderTransitionState(player, info, stack, buffer, light, overlay, partialTick, transitionProgress, skinProg);
        }
        else //has completed morph
        {
            MorphState.syncEntityWithPlayer(info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), player);
            renderLiving(info.nextState, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, buffer, light, partialTick);
        }

        isRenderingMorph = false;
    }

    private static void renderLiving(MorphState state, LivingEntity living, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick) //also captures the shadow size
    {
        renderLiving(state, living, stack, buffer, light, partialTick, false);
    }

    private static void renderLiving(MorphState state, LivingEntity living, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick, boolean forceDuringInvisibility) //also captures the shadow size
    {
        EntityRenderer<? super LivingEntity> livingRenderer = Minecraft.getInstance().getRenderManager().getRenderer(living);
        if(livingRenderer != null)
        {
            renderLiving(livingRenderer, living, stack, buffer, light, partialTick, forceDuringInvisibility);
            if (living instanceof MobEntity && living.isChild()) //Checked in EntityRendererManager
            {
                state.renderedShadowSize = livingRenderer.shadowSize * 0.5F;
            }
            else
            {
                state.renderedShadowSize = livingRenderer.shadowSize;
            }
        }
    }

    public static void renderLiving(EntityRenderer<? super LivingEntity> renderer, LivingEntity living, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick, boolean forceDuringInvisibility)
    {
        boolean isInvisible = living.isInvisible();
        if(forceDuringInvisibility && isInvisible)
        {
            living.setInvisible(false);
        }
        renderLiving(renderer, living, stack, buffer, light, partialTick);
        if(forceDuringInvisibility && isInvisible)
        {
            living.setInvisible(true);
        }
    }

    public static void renderLiving(EntityRenderer<? super LivingEntity> renderer, LivingEntity living, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick)
    {
        Minecraft mc = Minecraft.getInstance();
        if(living instanceof AbstractClientPlayerEntity)
        {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)living;
            if(mc.getConnection().getPlayerInfo(player.getGameProfile().getId()) == null) //we have to assign a NetworkPlayerInfo for the player skin to render.
            {
                //Silly Mojang and their privates
                SPlayerListItemPacket spoof = new SPlayerListItemPacket()
                {
                    @Override
                    public List<AddPlayerData> getEntries()
                    {
                        return Lists.newArrayList(new AddPlayerData(player.getGameProfile(), -100, GameType.ADVENTURE, new StringTextComponent(player.getGameProfile().getName())));
                    }
                };

                NetworkPlayerInfo info = new NetworkPlayerInfo(spoof.getEntries().get(0));

                mc.getConnection().playerInfoMap.put(player.getGameProfile().getId(), info);
            }

            //TODO do I have to handle different gamemodes?
        }

        float yaw = MathHelper.lerp(partialTick, living.prevRotationYaw, living.rotationYaw);
        stack.push();
        renderer.render(living, yaw, partialTick, stack, buffer, light);
        stack.pop();
    }

    public static void renderTransitionState(PlayerEntity player, MorphInfo info, MatrixStack stack, IRenderTypeBuffer buffer, int light, int overlay, float partialTick, float transitionProgress, float skinAlpha)
    {
        if(info.transitionState == null)
        {
            info.transitionState = new MorphTransitionState();
        }

        info.transitionState.renderTransitionState(player, info, stack, buffer, light, overlay, partialTick, transitionProgress, skinAlpha);
    }

    public static void restoreShadowSize(PlayerRenderer renderer)
    {
        if(playerShadowSize == -1F)
        {
            playerShadowSize = renderer.shadowSize;
        }

        if(changedShadowSize)
        {
            changedShadowSize = false;
            renderer.shadowSize = playerShadowSize;
        }
    }

    public static void setShadowSize(PlayerRenderer renderer, MorphInfo info, float partialTick)
    {
        float morphProgress = info.getMorphProgress(partialTick);
        if(morphProgress < 1F) //midmorph
        {
            float prevSize = info.prevState.renderedShadowSize;
            float nextSize = info.nextState.renderedShadowSize;

            renderer.shadowSize = prevSize + (nextSize - prevSize) * info.getTransitionProgressSine(partialTick);
        }
        else
        {
            renderer.shadowSize = info.nextState.renderedShadowSize;
        }

        changedShadowSize = true;
    }

    public static class MorphTransitionState
    {
        protected ModelRendererCapture prevModel;
        protected ModelRendererCapture nextModel;

        public void renderTransitionState(PlayerEntity player, MorphInfo info, MatrixStack stack, IRenderTypeBuffer buffer, int light, int overlay, float partialTick, float transitionProgress, float skinAlpha)
        {
            if(transitionProgress <= 0F)
            {
                if(prevModel == null)
                {
                    currentCapture = prevModel = new ModelRendererCapture();
                }
                else
                {
                    currentCapture = prevModel;
                    currentCapture.infos.clear();
                }

                LivingEntity livingInstance = info.prevState.getEntityInstance(player.world, player.getGameProfile().getId());

                renderLiving(info.prevState, livingInstance, stack, buffer, light, partialTick, Morph.configServer.biomassSkinWhilstInvisible);

                currentCapture = null; //reset before we do anything else

                prevModel.render(null, buffer, light, overlay, skinAlpha);
            }
            else if(transitionProgress >= 1F)
            {
                if(nextModel == null)
                {
                    currentCapture = nextModel = new ModelRendererCapture();
                }
                else
                {
                    currentCapture = nextModel;
                    currentCapture.infos.clear();
                }

                LivingEntity livingInstance = info.nextState.getEntityInstance(player.world, player.getGameProfile().getId());

                renderLiving(info.nextState, livingInstance, stack, buffer, light, partialTick, Morph.configServer.biomassSkinWhilstInvisible);

                currentCapture = null; //reset before we do anything else

                nextModel.render(null, buffer, light, overlay, skinAlpha);
            }
            else
            {
                denyRenderNameplate = true;
                if(prevModel == null)
                {
                    currentCapture = prevModel = new ModelRendererCapture();
                }
                else
                {
                    currentCapture = prevModel;
                    currentCapture.infos.clear();
                }

                LivingEntity prevLivingInstance = info.prevState.getEntityInstance(player.world, player.getGameProfile().getId());

                renderLiving(info.prevState, prevLivingInstance, stack, buffer, light, partialTick, Morph.configServer.biomassSkinWhilstInvisible);

                if(nextModel == null)
                {
                    currentCapture = nextModel = new ModelRendererCapture();
                }
                else
                {
                    currentCapture = nextModel;
                    currentCapture.infos.clear();
                }

                LivingEntity nextLivingInstance = info.nextState.getEntityInstance(player.world, player.getGameProfile().getId());

                renderLiving(info.nextState, nextLivingInstance, stack, buffer, light, partialTick, Morph.configServer.biomassSkinWhilstInvisible);

                currentCapture = null; //reset before we do anything else
                denyRenderNameplate = false;

                stack.push();
                stack.translate(0F, prevLivingInstance.getHeight() / 2F, 0F);
                MatrixStack.Entry prevMid = stack.getLast();
                stack.pop();

                stack.push();
                stack.translate(0F, nextLivingInstance.getHeight() / 2F, 0F);
                MatrixStack.Entry nextMid = stack.getLast();
                stack.pop();

                ModelRendererCapture transitionCapture = new ModelRendererCapture();
                transitionCapture.infos = prevModel.combineTowards(prevMid, nextMid, nextModel, transitionProgress);

                transitionCapture.render(null, buffer, light, overlay, skinAlpha);
            }
        }
    }

    public static class ModelRendererCapture
    {
        private final HashMap<ModelRenderer, CaptureInfo.ModelPart> modelToPart = new HashMap<>();

        public ArrayList<CaptureInfo> infos = new ArrayList<>();

        public void capture(ModelRenderer renderer, MatrixStack stack)
        {
            if(modelToPart.containsKey(renderer))
            {
                infos.add(new CaptureInfo(stack.getLast(), modelToPart.get(renderer)));
            }
            else
            {
                Project.Part part = ModelHelper.createPartFor(renderer, false);
                part.rotPX = part.rotPY = part.rotPZ = part.rotAX = part.rotAY = part.rotAZ = 0F;
                part.children.clear();
                CaptureInfo.ModelPart modelPart = new CaptureInfo.ModelPart(part);
                infos.add(new CaptureInfo(stack.getLast(), modelPart));
                modelToPart.put(renderer, modelPart);

                for(Project.Part.Box box : part.boxes) //to prevent z-fighting
                {
                    box.expandX += 0.0015F;
                    box.expandY += 0.0015F;
                    box.expandZ += 0.0015F;
                }
            }
        }

        public ArrayList<CaptureInfo> combineTowards(MatrixStack.Entry prevMid, MatrixStack.Entry nextMid, ModelRendererCapture other, float transitionProgress)
        {
            ArrayList<CaptureInfo> prevInfo = infos;
            ArrayList<CaptureInfo> nextInfo = other.infos;

            //Fill with empty parts first
            while(prevInfo.size() < nextInfo.size())
            {
                Project.Part part = new Project.Part(null, 0);
                part.boxes.clear();
                prevInfo.add(new CaptureInfo(prevMid, new CaptureInfo.ModelPart(part)));
            }

            while(nextInfo.size() < prevInfo.size())
            {
                Project.Part part = new Project.Part(null, 0);
                part.boxes.clear();
                nextInfo.add(new CaptureInfo(nextMid, new CaptureInfo.ModelPart(part)));
            }

            ArrayList<CaptureInfo> transitionInfos = new ArrayList<>();

            //sync up the box count
            for(int i = 0; i < prevInfo.size(); i++)
            {
                Project.Part oldPart = prevInfo.get(i).modelPart.part;
                Project.Part newPart = nextInfo.get(i).modelPart.part;

                ModelHelper.matchBoxesCount(oldPart, newPart);
                ModelHelper.matchBoxesCount(newPart, oldPart);

                transitionInfos.add(new CaptureInfo(RenderHelper.createInterimStackEntry(prevInfo.get(i).e, nextInfo.get(i).e, transitionProgress), new CaptureInfo.ModelPart(ModelHelper.createInterimPart(oldPart, newPart, transitionProgress))));
            }

            return transitionInfos;
        }

        public void render(MatrixStack stack, IRenderTypeBuffer buffer, int light, int overlay, float skinAlpha)
        {
            render(stack, buffer.getBuffer(RenderType.getEntityTranslucent(MorphHandler.INSTANCE.getMorphSkinTexture())), light, overlay, skinAlpha);
        }

        public void render(MatrixStack stack, IVertexBuilder vertexBuilder, int light, int overlay, float skinAlpha)
        {
            MatrixStack newStack = stack != null ? stack : new MatrixStack();
            for(CaptureInfo info : infos)
            {
                newStack.push();
                MatrixStack.Entry entLast = newStack.getLast();
                MatrixStack.Entry correctorLast = info.e;

                entLast.getMatrix().mul(correctorLast.getMatrix());
                entLast.getNormal().mul(correctorLast.getNormal());

                info.createAndRender(newStack, vertexBuilder, light, overlay, 1F, 1F, 1F, skinAlpha);
                newStack.pop();
            }
        }

        private static class CaptureInfo
        {
            public final MatrixStack.Entry e;
            public final ModelPart modelPart;

            private CaptureInfo(MatrixStack.Entry e, ModelPart modelPart) {
                this.e = e;
                this.modelPart = modelPart;
            }

            private void createAndRender(MatrixStack stack, IVertexBuilder buffer, int light, int overlay, float red, float green, float blue, float alpha)
            {
                if(this.modelPart.model == null)
                {
                    this.modelPart.model = ModelHelper.createModelRenderer(this.modelPart.part);
                }

                this.modelPart.model.render(stack, buffer, light, overlay, red, green, blue, alpha);
            }

            private static class ModelPart
            {
                public final Project.Part part;
                public ModelRenderer model;

                private ModelPart(Project.Part part)
                {
                    this.part = part;
                }
            }
        }
    }
}
