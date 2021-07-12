package me.ichun.mods.morph.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.morph.client.entity.EntityAcquisition;
import me.ichun.mods.morph.client.model.ModelAcquisition;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class RenderEntityAcquisition extends EntityRenderer<EntityAcquisition>
{
    private final ModelAcquisition model;

    protected RenderEntityAcquisition(EntityRendererManager renderManager)
    {
        super(renderManager);
        model = new ModelAcquisition();
        shadowSize = 0.0F;
    }

    @Override
    public void render(EntityAcquisition acquisition, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light)
    {
        if(acquisition.acquiredCapture != null)
        {
            EntityRenderer<? super LivingEntity> renderer = renderManager.getRenderer(acquisition.livingAcquired);
            Vector3d vector3d = renderer.getRenderOffset(acquisition.livingAcquired, partialTicks);

            //calculate the difference of that entity from ours
            double d0 = MathHelper.lerp(partialTicks, acquisition.livingAcquired.lastTickPosX - acquisition.lastTickPosX, acquisition.livingAcquired.getPosX() - acquisition.getPosX()) + vector3d.getX();
            double d1 = MathHelper.lerp(partialTicks, acquisition.livingAcquired.lastTickPosY - acquisition.lastTickPosY, acquisition.livingAcquired.getPosY() - acquisition.getPosY()) + vector3d.getY();
            double d2 = MathHelper.lerp(partialTicks, acquisition.livingAcquired.lastTickPosZ - acquisition.lastTickPosZ, acquisition.livingAcquired.getPosZ() - acquisition.getPosZ()) + vector3d.getZ();

            if(acquisition.age <= 10)
            {
                stack.push();
                stack.translate(d0, d1, d2);
                MorphRenderHandler.renderLiving(renderer, acquisition.livingAcquired, stack, buffer, renderManager.getPackedLight(acquisition.livingAcquired, partialTicks), partialTicks);
                stack.pop();

                MorphRenderHandler.currentCapture = acquisition.acquiredCapture;
                MorphRenderHandler.currentCapture.infos.clear();

                MorphRenderHandler.renderLiving(renderer, acquisition.livingAcquired, new MatrixStack(), buffer, renderManager.getPackedLight(acquisition.livingAcquired, partialTicks), partialTicks, Morph.configServer.biomassSkinWhilstInvisible);

                MorphRenderHandler.currentCapture = null;

                acquisition.maxRequiredTendrils = acquisition.acquiredCapture.infos.size();
            }

            float skinAlpha = MathHelper.clamp((acquisition.age + partialTicks) / 10, 0F, 1F);

            stack.push();
            stack.translate(d0, d1, d2);
            acquisition.acquiredCapture.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, skinAlpha);
            stack.pop();
        }
        model.render(acquisition, partialTicks, stack, buffer.getBuffer(RenderType.getEntityTranslucent(getEntityTexture(acquisition))), light, LivingRenderer.getPackedOverlay(acquisition.livingOrigin, 0F));
    }

    @Override
    public boolean shouldRender(EntityAcquisition livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ)
    {
        livingEntityIn.syncWithOriginPosition();
        return super.shouldRender(livingEntityIn, camera, camX, camY, camZ);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityAcquisition entity)
    {
        return MorphHandler.INSTANCE.getMorphSkinTexture();
    }

    public static class RenderFactory implements IRenderFactory<EntityAcquisition>
    {
        @Override
        public EntityRenderer<? super EntityAcquisition> createRenderFor(EntityRendererManager manager)
        {
            return new RenderEntityAcquisition(manager);
        }
    }
}
