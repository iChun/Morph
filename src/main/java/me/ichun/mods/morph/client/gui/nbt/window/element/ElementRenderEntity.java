package me.ichun.mods.morph.client.gui.nbt.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.morph.client.gui.nbt.window.WindowNbt;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class ElementRenderEntity extends Element<WindowNbt.ViewNbt>
{
    private static final MatrixStack LIGHT_STACK = Util.make(new MatrixStack(), stack -> stack.translate(1D, -1D, 0D));

    @Nonnull
    public LivingEntity entToRender;

    public ElementRenderEntity(@Nonnull WindowNbt.ViewNbt parent)
    {
        super(parent);
    }

    public ElementRenderEntity setEntityToRender(LivingEntity ent)
    {
        entToRender = ent;
        return this;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        if(renderMinecraftStyle() > 0)
        {
            bindTexture(resourceHorse());
            cropAndStitch(stack, getLeft() - 1, getTop() - 1, width + 2, height + 2, 2, 79, 17, 90, 54, 256, 256);
        }
        else
        {
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, width + 2, 1, 0); //top
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, 1, height + 2, 0); //left
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getBottom(), width + 2, 1, 0); //bottom
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getRight(), getTop() - 1, 1, height + 2, 0); //right
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderHelper.drawColour(stack, 0, 0, 0, 255, getLeft(), getTop(), width, height, 0);

        float entSize = Math.max(entToRender.getWidth(), entToRender.getHeight()) / 1.95F; //1.95F = zombie height

        float entScale = 1.0F * (1F / Math.max(1F, entSize));

        renderEntity(getLeft() + (width / 2D), getBottom() - 15, 100, entScale);
    }

    private void renderEntity(double x, double y, double z, float scale)
    {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        RenderSystem.enableRescaleNormal();

        net.minecraft.client.renderer.RenderHelper.setupLevelDiffuseLighting(LIGHT_STACK.getLast().getMatrix());

        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, z);
        RenderSystem.rotatef(-10F, 1F, 0F, 0F);
        RenderSystem.scalef(scale, scale, scale);
        InventoryScreen.drawEntityOnScreen(0, 0, 35, -60, 0, entToRender);
        RenderSystem.popMatrix();

        net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();

        RenderSystem.disableRescaleNormal();

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
    }
}
