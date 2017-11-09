package me.ichun.mods.morph.client.model;

import com.google.common.collect.Lists;
import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityRabbit;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.*;

public class ModelMorph extends ModelBase
{
    public static final long RAND_SEED = "MorphModelRandSeed".hashCode();

    public static final Random RAND = new Random(RAND_SEED);

    public final ModelInfo prevModelInfo;
    public final ModelInfo nextModelInfo;

    public final ArrayList<ModelRenderer> modelList; //Model list to manipulate with progression.

    public final HashMap<ModelRenderer, ModelRenderer> prevCloneToOriMap;
    public final HashMap<ModelRenderer, ModelRenderer> nextCloneToOriMap;

    public final ArrayList<ModelRenderer> prevModels; //Copy of the arraylist of the prev models. Can modify this list but do not modify the objects in the list!
    public final ArrayList<ModelRenderer> nextModels; //Copy of the arraylist of the next models. Can modify this list but do not modify the objects in the list!

    public ModelMorph()
    {
        this(null, null, null, null);
    }

    public ModelMorph(ModelInfo prev, ModelInfo next, Entity oldRef, Entity newRef) //reference ent is for prev model selection.
    {
        prevModelInfo = prev;
        nextModelInfo = next;

        prevCloneToOriMap = new HashMap<>();
        nextCloneToOriMap = new HashMap<>();
        if(prev != null)
        {
            prevModels = ModelHelper.getModelCubesCopy(prev.modelList, this, oldRef);
            int i = -1;
            for(ModelRenderer model : prevModels)
            {
                while(true)
                {
                    i++;
                    if(prev.modelList.get(i).compiled)
                    {
                        prevCloneToOriMap.put(model, prev.modelList.get(i));
                        break;
                    }
                }
            }
        }
        else
        {
            prevModels = new ArrayList<>();
        }
        if(next != null)
        {
            nextModels = ModelHelper.getModelCubesCopy(next.modelList, this, newRef); //put all the next models in.
            int i = -1;
            for(ModelRenderer model : nextModels)
            {
                while(true)
                {
                    i++;
                    if(next.modelList.get(i).compiled)
                    {
                        nextCloneToOriMap.put(model, next.modelList.get(i));
                        break;
                    }
                }
            }
        }
        else
        {
            nextModels = new ArrayList<>();
        }

        //Now that we have our reference models, fill up the reference list and create the modelList.
        prepareReferences();
        //By this point, both the prevModel and nextModels should be the same size and number of children and are therefore proper references.

        modelList = ModelHelper.getModelCubesCopy(prevModels, this, null);
    }

    public void prepareReferences()
    {
        int prevAdjust = -1;
        while(prevModels.size() < nextModels.size()) //if the prev reference has less models than the next reference, create empty ones.
        {
            if(prevAdjust == -1)
            {
                prevAdjust = prevModels.size();
            }
            prevModels.add(ModelHelper.buildCopy(nextModels.get(prevModels.size()), this, 0, false, true));
        }
        int nextAdjust = -1;
        while(nextModels.size() < prevModels.size())
        {
            if(nextAdjust == -1)
            {
                nextAdjust = nextModels.size();
            }
            nextModels.add(ModelHelper.buildCopy(prevModels.get(nextModels.size()), this, 0, false, true));
        }

        //If new boxes are created, anchor them to previously created boxes.
        RAND.setSeed(RAND_SEED);
        if(prevAdjust > 0)
        {
            for(int i = prevAdjust; i < prevModels.size(); i++)
            {
                ModelRenderer renderer = prevModels.get(i);
                ModelRenderer anchor = prevModels.get(RAND.nextInt(prevAdjust));
                renderer.setRotationPoint(anchor.rotationPointX, anchor.rotationPointY, anchor.rotationPointZ);
            }
        }
        if(nextAdjust > 0)
        {
            for(int i = nextAdjust; i < nextModels.size(); i++)
            {
                ModelRenderer renderer = nextModels.get(i);
                ModelRenderer anchor = nextModels.get(RAND.nextInt(nextAdjust));
                renderer.setRotationPoint(anchor.rotationPointX, anchor.rotationPointY, anchor.rotationPointZ);
            }
        }
        fillWithChildren(prevModels, nextModels, 0);
    }

    public void render(float renderTick, float progress, EntityLivingBase prevRef, EntityLivingBase nextRef)
    {
        GlStateManager.pushMatrix();

        FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
        FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);

        float scaleX = 1.0F;
        float scaleY = 1.0F;
        float scaleZ = 1.0F;

        if(prevRef != null && nextRef != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            Render rend = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(prevRef);
            ObfHelper.invokePreRenderCallback((RenderLivingBase)rend, rend.getClass(), prevRef, iChunUtil.eventHandlerClient.renderTick);
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
            GlStateManager.popMatrix();

            float prevScaleX = buffer1.get(0) / buffer.get(0);
            float prevScaleY = buffer1.get(5) / buffer.get(5);
            float prevScaleZ = buffer1.get(8) / buffer.get(8);

            if(prevRef instanceof EntityRabbit)
            {
                prevScaleX *= 0.6F;
                prevScaleY *= 0.6F;
                prevScaleZ *= 0.6F;
            }

            GlStateManager.pushMatrix();
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            rend = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(nextRef);
            ObfHelper.invokePreRenderCallback((RenderLivingBase)rend, rend.getClass(), nextRef, iChunUtil.eventHandlerClient.renderTick);
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
            GlStateManager.popMatrix();

            float nextScaleX = buffer1.get(0) / buffer.get(0);
            float nextScaleY = buffer1.get(5) / buffer.get(5);
            float nextScaleZ = buffer1.get(8) / buffer.get(8);

            if(nextRef instanceof EntityRabbit)
            {
                nextScaleX *= 0.6F;
                nextScaleY *= 0.6F;
                nextScaleZ *= 0.6F;
            }

            scaleX = prevScaleX + (nextScaleX - prevScaleX) * progress;
            scaleY = prevScaleY + (nextScaleY - prevScaleY) * progress;
            scaleZ = prevScaleZ + (nextScaleZ - prevScaleZ) * progress;

            if(prevModelInfo != null)
            {
                prevModelInfo.forceRender(prevRef, 0D, -500D, 0D, EntityHelper.interpolateRotation(prevRef.prevRotationYaw, prevRef.rotationYaw, renderTick), renderTick);
                for(Map.Entry<ModelRenderer, ModelRenderer> e : prevCloneToOriMap.entrySet())
                {
                    matchRotation(e.getKey(), e.getValue(), 0);
                }
            }
            if(nextModelInfo != null)
            {
                nextModelInfo.forceRender(nextRef, 0D, -500D, 0D, EntityHelper.interpolateRotation(nextRef.prevRotationYaw, nextRef.rotationYaw, renderTick), renderTick);
                for(Map.Entry<ModelRenderer, ModelRenderer> e : nextCloneToOriMap.entrySet())
                {
                    matchRotation(e.getKey(), e.getValue(), 0);
                }
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(PlayerMorphHandler.getInstance().getMorphSkinTexture());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        GlStateManager.translate(0F, -1.5F, 0F);

        updateModelList(progress, modelList, prevModels, nextModels, 0);

        for(ModelRenderer cube : modelList)
        {
            cube.render(0.0625F);
        }

        GlStateManager.popMatrix();
    }

    public void matchRotation(ModelRenderer clone, ModelRenderer ori, int depth)
    {
        if(depth > 20)
        {
            return;
        }
        clone.setRotationPoint(ori.rotationPointX, ori.rotationPointY, ori.rotationPointZ);
        clone.rotateAngleX = ori.rotateAngleX;
        clone.rotateAngleY = ori.rotateAngleY;
        clone.rotateAngleZ = ori.rotateAngleZ;

        if(ori.childModels != null)
        {
            for(int i = 0; i < ori.childModels.size(); i++)
            {
                ModelRenderer cloneChild = (ModelRenderer)clone.childModels.get(i);
                ModelRenderer child = (ModelRenderer)ori.childModels.get(i);

                matchRotation(cloneChild, child, depth + 1);
            }
        }
    }

    public void updateModelList(float progress, List modelList, List prevModelList, List nextModelList, int depth)
    {
        if(modelList == null || depth > 20)
        {
            return;
        }
        for(int i = 0; i < modelList.size(); i++)
        {
            ModelRenderer renderer = (ModelRenderer)modelList.get(i);
            ModelRenderer prevRend = (ModelRenderer)prevModelList.get(i);
            ModelRenderer nextRend = (ModelRenderer)nextModelList.get(i);

            for(int j = renderer.cubeList.size() - 1; j >= 0; j--)
            {
                ModelBox box = (ModelBox)renderer.cubeList.get(j);
                ModelBox prevBox = (ModelBox)prevRend.cubeList.get(j);
                ModelBox nextBox = (ModelBox)nextRend.cubeList.get(j);

                int x = (int)Math.abs(box.posX2 - box.posX1);
                int y = (int)Math.abs(box.posY2 - box.posY1);
                int z = (int)Math.abs(box.posZ2 - box.posZ1);

                int px = (int)Math.abs(prevBox.posX2 - prevBox.posX1);
                int py = (int)Math.abs(prevBox.posY2 - prevBox.posY1);
                int pz = (int)Math.abs(prevBox.posZ2 - prevBox.posZ1);

                int nx = (int)Math.abs(nextBox.posX2 - nextBox.posX1);
                int ny = (int)Math.abs(nextBox.posY2 - nextBox.posY1);
                int nz = (int)Math.abs(nextBox.posZ2 - nextBox.posZ1);

                int xx = Math.round(px + (nx - px) * progress);
                int yy = Math.round(py + (ny - py) * progress);
                int zz = Math.round(pz + (nz - pz) * progress);

                float offsetX = EntityHelper.interpolateValues(prevBox.posX1, nextBox.posX1, progress);
                float offsetY = EntityHelper.interpolateValues(prevBox.posY1, nextBox.posY1, progress);
                float offsetZ = EntityHelper.interpolateValues(prevBox.posZ1, nextBox.posZ1, progress);

                if(!(x == xx && y == yy && z == zz && offsetX == box.posX1 && offsetY == box.posY1 && offsetZ == box.posZ1))
                {
                    if(renderer.compiled)
                    {
                        GLAllocation.deleteDisplayLists(renderer.displayList);
                        renderer.compiled = false;
                    }
                    renderer.cubeList.remove(j);
                    renderer.cubeList.add(j, new ModelBox(renderer, renderer.textureOffsetX, renderer.textureOffsetY, offsetX, offsetY, offsetZ, xx, yy, zz, 0.0625F));
                }
            }

            renderer.setRotationPoint(EntityHelper.interpolateValues(prevRend.rotationPointX, nextRend.rotationPointX, progress), EntityHelper.interpolateValues(prevRend.rotationPointY, nextRend.rotationPointY, progress), EntityHelper.interpolateValues(prevRend.rotationPointZ, nextRend.rotationPointZ, progress));
            renderer.rotateAngleX = EntityHelper.interpolateValues(prevRend.rotateAngleX, nextRend.rotateAngleX, progress);
            renderer.rotateAngleY = EntityHelper.interpolateValues(prevRend.rotateAngleY, nextRend.rotateAngleY, progress);
            renderer.rotateAngleZ = EntityHelper.interpolateValues(prevRend.rotateAngleZ, nextRend.rotateAngleZ, progress);

            updateModelList(progress, renderer.childModels, prevRend.childModels, nextRend.childModels, depth + 1);
        }
    }

    public void fillWithChildren(List prevModels, List nextModels, int depth) //fills both lists with children from both sides...hopefully.
    {
        if(prevModels == null || nextModels == null || depth > 20)
        {
            return;
        }
        for(int i = 0; i < (prevModels.size() < nextModels.size() ? prevModels.size() : nextModels.size()); i++) //create empty child clones.
        {
            ModelRenderer prevModel = (ModelRenderer)prevModels.get(i);
            ModelRenderer nextModel = (ModelRenderer)nextModels.get(i);

            while(prevModel.cubeList.size() < nextModel.cubeList.size())
            {
                prevModel.addBox(0F, 0F, 0F, 0, 0, 0, 0.0625F);
            }
            while(nextModel.cubeList.size() < prevModel.cubeList.size())
            {
                nextModel.addBox(0F, 0F, 0F, 0, 0, 0, 0.0625F);
            }

            if(prevModel.childModels != null || nextModel.childModels != null)
            {
                ModelRenderer prevEmptyCopy = ModelHelper.buildCopy(prevModel, this, 0, true, true);
                ModelRenderer nextEmptyCopy = ModelHelper.buildCopy(nextModel, this, 0, true, true);
                if(prevModel.childModels == null)
                {
                    prevModel.childModels = Lists.newArrayList();
                    prevEmptyCopy.childModels = Lists.newArrayList();
                }
                if(nextModel.childModels == null)
                {
                    nextModel.childModels = Lists.newArrayList();
                    nextEmptyCopy.childModels = Lists.newArrayList();
                }
                for(int k = prevModel.childModels.size(); k < nextEmptyCopy.childModels.size(); k++)
                {
                    prevModel.addChild(nextEmptyCopy.childModels.get(k));
                }
                for(int k = nextModel.childModels.size(); k < prevEmptyCopy.childModels.size(); k++)
                {
                    nextModel.addChild(prevEmptyCopy.childModels.get(k));
                }
                //At this point the model should have the same children count
                fillWithChildren(prevModel.childModels, nextModel.childModels, depth + 1);
            }
        }
    }

    public void setRotationPointToZeroWithChildren(List<ModelRenderer> children, int depth)
    {
        if(children == null || depth > 20)
        {
            return;
        }
        for(ModelRenderer child : children)
        {
            child.setRotationPoint(0F, 0F, 0F);
            setRotationPointToZeroWithChildren(child.childModels, depth + 1);
        }
    }

    public void clean()
    {
        modelList.stream().filter(renderer -> renderer.compiled).forEach(renderer ->
        {
            GLAllocation.deleteDisplayLists(renderer.displayList);
            renderer.compiled = false;
        });
    }
}
