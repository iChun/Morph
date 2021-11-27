package me.ichun.mods.morph.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.morph.client.entity.EntityBiomassAbility;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfoImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
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
    public void render(EntityBiomassAbility ability, float entityYaw, float partialTick, MatrixStack stack, IRenderTypeBuffer buffer, int light)
    {
        if(ability.player.removed)
        {
            return; //no render
        }

        MorphInfoImpl info = (MorphInfoImpl)MorphHandler.INSTANCE.getMorphInfo(ability.player);
        boolean isFirstPerson = ability.player == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON;
        if(isFirstPerson)
        {
            if(info.entityBiomassAbility == null || info.entityBiomassAbility.getSkinAlpha(partialTick) < ability.getSkinAlpha(partialTick))
            {
                info.entityBiomassAbility = ability; //will be removed by MorphInfo when the entity is invalid/removed
            }
            return; //no render
        }

        LivingEntity activeLiving = info.getActiveAppearanceEntity(partialTick);
        if(activeLiving != null)
        {
            EntityRenderer<? super LivingEntity> renderer = renderManager.getRenderer(activeLiving);
            if(renderer != null)
            {
                float alpha = ability.getSkinAlpha(partialTick);

                MorphRenderHandler.denyRenderNameplate = true;
                stack.push();
                MorphRenderHandler.renderLiving(renderer, activeLiving, stack, buffer, renderManager.getPackedLight(activeLiving, partialTick), partialTick);
                stack.pop();

                MorphRenderHandler.currentCapture = ability.capture;
                MorphRenderHandler.currentCapture.infos.clear();

                MorphRenderHandler.renderLiving(renderer, activeLiving, new MatrixStack(), buffer, renderManager.getPackedLight(activeLiving, partialTick), partialTick, Morph.configServer.biomassSkinWhilstInvisible);

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
