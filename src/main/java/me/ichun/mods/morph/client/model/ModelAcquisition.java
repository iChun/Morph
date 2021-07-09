package me.ichun.mods.morph.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.morph.client.entity.EntityAcquisition;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class ModelAcquisition extends EntityModel<EntityAcquisition>
{
    public void render(EntityAcquisition entity, float partialTick, MatrixStack stack, IVertexBuilder buffer, int light, int overlay)
    {
        boolean isFirstPerson = entity.livingOrigin == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON;

        if(!entity.livingOrigin.isInvisible() || Morph.configServer.biomassSkinWhilstInvisible)
        {
            for(EntityAcquisition.Tendril tendril : entity.tendrils)
            {
                if(!tendril.isDone())
                {
                    ArrayList<ModelRenderer> modelRenderers = new ArrayList<>();
                    tendril.createModelRenderer(modelRenderers, partialTick);
                    tendril.renderCapture(entity, stack, buffer, light, overlay, partialTick);
                    for(int i = 0; i < modelRenderers.size(); i++)
                    {
                        ModelRenderer modelRenderer = modelRenderers.get(i);
                        float alpha = 1F;
                        if(isFirstPerson && Morph.configClient.acquisitionTendrilPartOpacity > 0)
                        {
                            alpha = MathHelper.clamp((modelRenderers.size() - i) / (float)Morph.configClient.acquisitionTendrilPartOpacity, 0F, 1F);
                        }
                        modelRenderer.render(stack, buffer, light, overlay, 1F, 1F, 1F, alpha);
                    }
                }
            }
        }
        else
        {
            for(EntityAcquisition.Tendril tendril : entity.tendrils)
            {
                if(!tendril.isDone())
                {
                    tendril.renderCapture(entity, stack, buffer, light, overlay, partialTick);
                }
            }
        }
    }

    @Override
    public void setRotationAngles(EntityAcquisition entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
    }
}
