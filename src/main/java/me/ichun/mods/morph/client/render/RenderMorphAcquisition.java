package me.ichun.mods.morph.client.render;

import me.ichun.mods.morph.client.entity.EntityMorphAcquisition;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import us.ichun.mods.ichunutil.common.core.util.ObfHelper;
import us.ichun.mods.ichunutil.common.iChunUtil;

import java.nio.FloatBuffer;

public class RenderMorphAcquisition extends Render
{
    public RenderMorphAcquisition()
    {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        EntityMorphAcquisition ent = (EntityMorphAcquisition)entity;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180F - ent.acquired.renderYawOffset, 0F, 1F, 0F);
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float skinProg = MathHelper.clamp_float((ent.progress + partialTicks) / 5F, 0F, 1F);
        float morphProg = MathHelper.clamp_float(((ent.progress - 5F) + partialTicks) / 35F, 0F, 1F);

        if(ent.prevScaleX == -1) //seting up
        {
            FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
            FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);

            GlStateManager.pushMatrix();
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
            Render rend = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(ent.acquired);
            ObfHelper.invokePreRenderCallback(rend, rend.getClass(), ent.acquired, iChunUtil.proxy.tickHandlerClient.renderTick);
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
            GlStateManager.popMatrix();

            ent.prevScaleX = buffer1.get(0) / buffer.get(0);
            ent.prevScaleY = buffer1.get(5) / buffer.get(5);
            ent.prevScaleZ = buffer1.get(8) / buffer.get(8);
        }

        float scaleX = ent.prevScaleX + (1.0F - ent.prevScaleX) * morphProg;
        float scaleY = ent.prevScaleY + (1.0F - ent.prevScaleY) * morphProg;
        float scaleZ = ent.prevScaleZ + (1.0F - ent.prevScaleZ) * morphProg;

        GlStateManager.scale(scaleX, scaleY, scaleZ);
        GlStateManager.translate(0F, -1.5F, 0F);
        GlStateManager.color(1F, 1F, 1F, 1F);

        for(ModelRenderer renderer : ent.model.nextModels)
        {
            ent.getRNG().setSeed(Math.abs(renderer.hashCode()));
            float spinSpeed = MathHelper.clamp_float(morphProg, 0F, 0.45F);
            float spinValue = (float)Math.pow(3D, 4F * spinSpeed);
            float offsetX = (ent.getRNG().nextFloat() - ent.getRNG().nextFloat()) * spinValue;
            float offsetY = (ent.getRNG().nextFloat() - ent.getRNG().nextFloat()) * spinValue;
            float offsetZ = (ent.getRNG().nextFloat() - ent.getRNG().nextFloat()) * spinValue;
            renderer.rotateAngleX = offsetX;
            renderer.rotateAngleY = offsetY;
            renderer.rotateAngleZ = offsetZ;
        }

        ent.model.updateModelList(morphProg, ent.model.modelList, ent.model.prevModels, ent.model.nextModels, 0);
        if(skinProg < 1F)
        {
            bindTexture(ent.acquiredTexture);
            for(ModelRenderer cube : ent.model.modelList)
            {
                cube.render(0.0625F);
            }
            GlStateManager.color(1F, 1F, 1F, skinProg);
        }

        bindTexture(PlayerMorphHandler.morphSkin);
        for(ModelRenderer cube : ent.model.modelList)
        {
            cube.render(0.0625F);
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return PlayerMorphHandler.morphSkin;
    }
}
