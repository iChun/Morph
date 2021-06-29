package me.ichun.mods.morph.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.common.module.tabula.project.Identifiable;
import me.ichun.mods.ichunutil.common.module.tabula.project.Project;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class MorphRenderHandler
{
    private static float playerShadowSize = -1F;
    private static boolean changedShadowSize = false;

    public static ModelRendererCapture currentCapture = null; //Are we capturing ModelRenderer renders?

    public static boolean isRenderingMorph = false;

    public static void renderMorphInfo(PlayerEntity player, MorphInfo info, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick)
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
                skinProg = MorphInfo.sineifyProgress(morphProgress / 0.125F);
            }
            else if(transitionProgress >= 1F)
            {
                MorphState.syncEntityWithPlayer(info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), player);
                renderLiving(info.nextState, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, buffer, light, partialTick);
                skinProg = 1F - MorphInfo.sineifyProgress((morphProgress - 0.875F) / 0.125F);
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

    private static void renderLiving(MorphState state, LivingEntity livingInstance, MatrixStack stack, IRenderTypeBuffer buffer, int light, float partialTick)
    {
        EntityRenderer livingRenderer = Minecraft.getInstance().getRenderManager().getRenderer(livingInstance);

        if(livingRenderer != null)
        {
            float yaw = MathHelper.lerp(partialTick, livingInstance.prevRotationYaw, livingInstance.rotationYaw);
            stack.push();
            livingRenderer.render(livingInstance, yaw, partialTick, stack, buffer, light);
            state.renderedShadowSize = livingRenderer.shadowSize;
            stack.pop();
        }
    }

    //TODO fix the high memory use of this call
    public static void renderTransitionState(PlayerEntity player, MorphInfo info, MatrixStack stack, IRenderTypeBuffer buffer, int light, int overlay, float partialTick, float transitionProgress, float skinAlpha)
    {
        if(transitionProgress <= 0F)
        {
            currentCapture = new ModelRendererCapture();
            renderLiving(info.prevState, info.prevState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, RenderHelper.getDummyBuffer(), light, partialTick);
            ModelRendererCapture prevModel = currentCapture;

            currentCapture = null; //reset before we do anything else accidentally.
            RenderHelper.getDummyBuffer().finish();

            prevModel.render(buffer, light, overlay, skinAlpha);
        }
        else if(transitionProgress >= 1F)
        {
            currentCapture = new ModelRendererCapture();
            renderLiving(info.nextState, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, RenderHelper.getDummyBuffer(), light, partialTick);
            ModelRendererCapture nextModel = currentCapture;

            currentCapture = null; //reset before we do anything else accidentally.
            RenderHelper.getDummyBuffer().finish();

            nextModel.render(buffer, light, overlay, skinAlpha);
        }
        else
        {
            currentCapture = new ModelRendererCapture();
            renderLiving(info.prevState, info.prevState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, RenderHelper.getDummyBuffer(), light, partialTick);
            ModelRendererCapture prevModel = currentCapture;

            currentCapture = new ModelRendererCapture();
            renderLiving(info.nextState, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()), stack, RenderHelper.getDummyBuffer(), light, partialTick);
            ModelRendererCapture nextModel = currentCapture;

            currentCapture = null; //reset before we do anything else accidentally.
            RenderHelper.getDummyBuffer().finish();

            stack.push();
            stack.translate(0F, info.prevState.getEntityInstance(player.world, player.getGameProfile().getId()).getHeight() / 2F, 0F);
            MatrixStack.Entry prevMid = stack.getLast();
            stack.pop();

            stack.push();
            stack.translate(0F, info.nextState.getEntityInstance(player.world, player.getGameProfile().getId()).getHeight() / 2F, 0F);
            MatrixStack.Entry nextMid = stack.getLast();
            stack.pop();

            prevModel.combineTowards(prevMid, nextMid, nextModel, transitionProgress);

            prevModel.render(buffer, light, overlay, skinAlpha);
        }
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

    public static class ModelRendererCapture
    {
        public ArrayList<CaptureInfo> infos = new ArrayList<>();

        public void capture(ModelRenderer renderer, MatrixStack stack)
        {
            HashMap<ModelRenderer, Identifiable<?>> store = new HashMap<>();
            ModelHelper.createPartFor("", renderer, store, null, false);
            Project.Part part = (Project.Part)store.get(renderer);
            part.rotPX = part.rotPY = part.rotPZ = part.rotAX = part.rotAY = part.rotAZ = 0F;
            part.children.clear();
            infos.add(new CaptureInfo(stack.getLast(), part));
        }

        public void combineTowards(MatrixStack.Entry prevMid, MatrixStack.Entry nextMid, ModelRendererCapture other, float transitionProgress)
        {
            ArrayList<CaptureInfo> prevInfo = new ArrayList<>(infos);
            ArrayList<CaptureInfo> nextInfo = new ArrayList<>(other.infos);

            //Fill with empty parts first
            while(prevInfo.size() < nextInfo.size())
            {
                Project.Part part = new Project.Part(null, 0);
                part.boxes.clear();
                prevInfo.add(new CaptureInfo(prevMid, part));
            }

            while(nextInfo.size() < prevInfo.size())
            {
                Project.Part part = new Project.Part(null, 0);
                part.boxes.clear();
                nextInfo.add(new CaptureInfo(nextMid, part));
            }

            infos = new ArrayList<>();

            //sync up the box count
            for(int i = 0; i < prevInfo.size(); i++)
            {
                Project.Part oldPart = prevInfo.get(i).part;
                Project.Part newPart = nextInfo.get(i).part;

                while(oldPart.boxes.size() < newPart.boxes.size())
                {
                    Project.Part.Box box = new Project.Part.Box(oldPart);
                    Project.Part.Box otherBox = newPart.boxes.get(oldPart.boxes.size());
                    box.dimX = box.dimY = box.dimZ = 0F;
                    box.texOffX = otherBox.texOffX;
                    box.texOffY = otherBox.texOffY;
                    oldPart.boxes.add(box);
                }

                while(newPart.boxes.size() < oldPart.boxes.size())
                {
                    Project.Part.Box box = new Project.Part.Box(newPart);
                    Project.Part.Box otherBox = oldPart.boxes.get(newPart.boxes.size());
                    box.dimX = box.dimY = box.dimZ = 0F;
                    box.texOffX = otherBox.texOffX;
                    box.texOffY = otherBox.texOffY;
                    newPart.boxes.add(box);
                }

                infos.add(new CaptureInfo(createInterimStackEntry(prevInfo.get(i).e, nextInfo.get(i).e, transitionProgress), createInterimPart(oldPart, newPart, transitionProgress)));
            }
        }

        private MatrixStack.Entry createInterimStackEntry(MatrixStack.Entry prevEntry, MatrixStack.Entry nextEntry, float prog)
        {
            //create a copy of prevEntry
            MatrixStack stack = new MatrixStack();
            MatrixStack.Entry last = stack.getLast();

            last.getMatrix().mul(prevEntry.getMatrix());
            last.getNormal().mul(prevEntry.getNormal());

            //get the difference
            //matrix
            Matrix4f subtractMatrix = prevEntry.getMatrix().copy();
            subtractMatrix.mul(-1F);
            Matrix4f diffMatrix = nextEntry.getMatrix().copy();
            diffMatrix.add(subtractMatrix);
            diffMatrix.mul(prog);
            last.getMatrix().add(diffMatrix);

            //normal... no add function
            Matrix3f lastNormal = last.getNormal();
            Matrix3f prevNormal = prevEntry.getNormal().copy();
            Matrix3f nextNormal = nextEntry.getNormal().copy();
            lastNormal.m00 = prevNormal.m00 + (nextNormal.m00 - prevNormal.m00) * prog;
            lastNormal.m01 = prevNormal.m01 + (nextNormal.m01 - prevNormal.m01) * prog;
            lastNormal.m02 = prevNormal.m02 + (nextNormal.m02 - prevNormal.m02) * prog;
            lastNormal.m10 = prevNormal.m10 + (nextNormal.m10 - prevNormal.m10) * prog;
            lastNormal.m11 = prevNormal.m11 + (nextNormal.m11 - prevNormal.m11) * prog;
            lastNormal.m12 = prevNormal.m12 + (nextNormal.m12 - prevNormal.m12) * prog;
            lastNormal.m20 = prevNormal.m20 + (nextNormal.m20 - prevNormal.m20) * prog;
            lastNormal.m21 = prevNormal.m21 + (nextNormal.m21 - prevNormal.m21) * prog;
            lastNormal.m22 = prevNormal.m22 + (nextNormal.m22 - prevNormal.m22) * prog;

            return last;
        }

        private Project.Part createInterimPart(Project.Part prevPart, Project.Part nextPart, float prog)
        {
            Project.Part part = new Project.Part(null, 0);
            part.boxes.clear();

            part.texWidth = Math.round(prevPart.texWidth + (nextPart.texWidth - prevPart.texWidth) * prog);
            part.texHeight = Math.round(prevPart.texHeight + (nextPart.texHeight - prevPart.texHeight) * prog);

            part.texOffX = Math.round(prevPart.texOffX + (nextPart.texOffX - prevPart.texOffX) * prog);
            part.texOffY = Math.round(prevPart.texOffY + (nextPart.texOffY - prevPart.texOffY) * prog);

            part.mirror = nextPart.mirror;

            for(int i = 0; i < prevPart.boxes.size(); i++)
            {
                Project.Part.Box box = new Project.Part.Box(part);

                Project.Part.Box prevBox = prevPart.boxes.get(i);
                Project.Part.Box nextBox = nextPart.boxes.get(i);

                box.posX = prevBox.posX + (nextBox.posX - prevBox.posX) * prog;
                box.posY = prevBox.posY + (nextBox.posY - prevBox.posY) * prog;
                box.posZ = prevBox.posZ + (nextBox.posZ - prevBox.posZ) * prog;

                box.dimX = prevBox.dimX + (nextBox.dimX - prevBox.dimX) * prog;
                box.dimY = prevBox.dimY + (nextBox.dimY - prevBox.dimY) * prog;
                box.dimZ = prevBox.dimZ + (nextBox.dimZ - prevBox.dimZ) * prog;

                box.expandX = prevBox.expandX + (nextBox.expandX - prevBox.expandX) * prog;
                box.expandY = prevBox.expandY + (nextBox.expandY - prevBox.expandY) * prog;
                box.expandZ = prevBox.expandZ + (nextBox.expandZ - prevBox.expandZ) * prog;

                box.texOffX = Math.round(prevBox.texOffX + (nextBox.texOffX - prevBox.texOffX) * prog);
                box.texOffY = Math.round(prevBox.texOffY + (nextBox.texOffY - prevBox.texOffY) * prog);

                part.boxes.add(box);
            }

            return part;
        }

        public void render(IRenderTypeBuffer buffer, int light, int overlay, float skinAlpha)
        {
            for(CaptureInfo info : infos)
            {
                MatrixStack newStack = new MatrixStack();
                MatrixStack.Entry entLast = newStack.getLast();
                MatrixStack.Entry correctorLast = info.e;

                entLast.getMatrix().mul(correctorLast.getMatrix());
                entLast.getNormal().mul(correctorLast.getNormal());

                Project.Part part = info.part;
                ModelRenderer modelPart = new ModelRenderer(part.texWidth, part.texHeight, part.texOffX, part.texOffY);

                modelPart.mirror = part.mirror;
                modelPart.showModel = part.showModel;

                part.boxes.forEach(box -> {
                    int texOffX = modelPart.textureOffsetX;
                    int texOffY = modelPart.textureOffsetY;
                    modelPart.setTextureOffset(modelPart.textureOffsetX + box.texOffX, modelPart.textureOffsetY + box.texOffY);
                    modelPart.addBox(box.posX, box.posY, box.posZ, box.dimX, box.dimY, box.dimZ, box.expandX, box.expandY, box.expandZ);
                    modelPart.setTextureOffset(texOffX, texOffY);
                });

                modelPart.render(newStack, buffer.getBuffer(RenderType.getEntityTranslucent(MorphHandler.INSTANCE.getMorphSkinTexture())), light, overlay, 1F, 1F, 1F, skinAlpha);
            }
        }

        public static class CaptureInfo
        {
            public final MatrixStack.Entry e;
            public final Project.Part part;

            public CaptureInfo(MatrixStack.Entry e, Project.Part part) {
                this.e = e;
                this.part = part;
            }
        }
    }
}
