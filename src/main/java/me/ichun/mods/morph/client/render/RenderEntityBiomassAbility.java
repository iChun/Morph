package me.ichun.mods.morph.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.client.entity.EntityBiomassAbility;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class RenderEntityBiomassAbility extends EntityRenderer<EntityBiomassAbility>
{
    protected RenderEntityBiomassAbility(EntityRendererManager renderManager)
    {
        super(renderManager);
        shadowSize = 0.0F;
    }

    @Override
    public void render(EntityBiomassAbility ability, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light)
    {
        boolean isFirstPerson = ability.player == Minecraft.getInstance().player && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON;
        if(isFirstPerson || ability.player.removed)
        {
            return; //no render
        }

        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(ability.player);
        LivingEntity activeLiving = info.getActiveAppearanceEntity(partialTicks);
        if(activeLiving != null)
        {
            EntityRenderer<? super LivingEntity> renderer = renderManager.getRenderer(activeLiving);
            if(renderer != null)
            {
                float alpha;
                if(ability.age < ability.fadeTime)
                {
                    alpha = EntityHelper.sineifyProgress(MathHelper.clamp((ability.age + partialTicks) / ability.fadeTime, 0F, 1F));
                }
                else if(ability.age >= ability.fadeTime + ability.solidTime)
                {
                    alpha = EntityHelper.sineifyProgress(1F - MathHelper.clamp((ability.age - (ability.fadeTime + ability.solidTime) + partialTicks) / ability.fadeTime, 0F, 1F));
                }
                else
                {
                    alpha = 1F;
                }

                MorphRenderHandler.denyRenderNameplate = true;
                stack.push();
                MorphRenderHandler.renderLiving(renderer, activeLiving, stack, buffer, renderManager.getPackedLight(activeLiving, partialTicks), partialTicks);
                stack.pop();

                MorphRenderHandler.currentCapture = ability.capture;
                MorphRenderHandler.currentCapture.infos.clear();

                boolean isInvisible = activeLiving.isInvisible();
                if(Morph.configServer.biomassSkinWhilstInvisible && isInvisible)
                {
                    activeLiving.setInvisible(false);
                }
                MorphRenderHandler.renderLiving(renderer, activeLiving, new MatrixStack(), buffer, renderManager.getPackedLight(activeLiving, partialTicks), partialTicks);
                if(Morph.configServer.biomassSkinWhilstInvisible && isInvisible)
                {
                    activeLiving.setInvisible(true);
                }

                MorphRenderHandler.currentCapture = null;
                MorphRenderHandler.denyRenderNameplate = false;

                ability.capture.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, alpha);
            }
        }
    }

    @Override
    public boolean shouldRender(EntityBiomassAbility ability, ClippingHelper camera, double camX, double camY, double camZ)
    {
        ability.syncWithOriginPosition();
        return super.shouldRender(ability, camera, camX, camY, camZ);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityBiomassAbility ability)
    {
        return MorphHandler.INSTANCE.getMorphSkinTexture();
    }

    public static class RenderFactory implements IRenderFactory<EntityBiomassAbility>
    {
        @Override
        public EntityRenderer<? super EntityBiomassAbility> createRenderFor(EntityRendererManager manager)
        {
            return new RenderEntityBiomassAbility(manager);
        }
    }
}
