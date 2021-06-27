package me.ichun.mods.morph.api.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererMixin
{
    @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/model/ModelRenderer;doRender(Lcom/mojang/blaze3d/matrix/MatrixStack$Entry;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V"))
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, CallbackInfo ci)
    {
        if(MorphRenderHandler.currentCapture != null)
        {
            MorphRenderHandler.currentCapture.capture((ModelRenderer)(Object)this, matrixStackIn);
        }
    }
}
