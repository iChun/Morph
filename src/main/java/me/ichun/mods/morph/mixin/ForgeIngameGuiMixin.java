package me.ichun.mods.morph.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.morph.common.Morph;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeIngameGui.class) //I'm sorry Forge!!
public abstract class ForgeIngameGuiMixin
{
    @Inject(method = "renderIngameGui", at = @At(value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"))
    public void renderIngameGuiPre(MatrixStack mStack, float partialTicks, CallbackInfo ci)
    {
        if(Morph.eventHandlerClient.hudHandler != null)
        {
            Morph.eventHandlerClient.hudHandler.preDrawBiomassBar(mStack, partialTicks);
        }
    }

    @Inject(method = "renderIngameGui", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/gui/ForgeIngameGui;renderSleepFade(IILcom/mojang/blaze3d/matrix/MatrixStack;)V"))
    public void renderIngameGuiPost(MatrixStack mStack, float partialTicks, CallbackInfo ci)
    {
        if(Morph.eventHandlerClient.hudHandler != null)
        {
            Morph.eventHandlerClient.hudHandler.postDrawBiomassBar(mStack, partialTicks);
        }
    }
}
