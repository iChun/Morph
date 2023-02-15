package me.ichun.mods.morph.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.morph.client.render.hand.HandHandler;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
{
    //This is more the "render arm" method, not so much "render item"
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void renderItem(MatrixStack stack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, ModelRenderer arm, ModelRenderer armwear, CallbackInfo ci)
    {
        if(HandHandler.instance != null && HandHandler.instance.renderHand(((PlayerRenderer)(Object)this), stack, buffer, light, player, arm, armwear))
        {
            ci.cancel(); //cancel the call
        }
    }
}
