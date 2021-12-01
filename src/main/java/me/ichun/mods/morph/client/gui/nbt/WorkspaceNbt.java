package me.ichun.mods.morph.client.gui.nbt;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.morph.client.gui.nbt.window.WindowNbt;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkspaceNbt extends Workspace
{
    public static final int PADDING_VERTICAL = 15;

    public final LivingEntity target;
    public final WindowNbt windowNbt;

    public WorkspaceNbt(Screen lastScreen, LivingEntity target)
    {
        super(lastScreen, new TranslationTextComponent("morph.gui.workspace.nbt.title"), Morph.configClient.guiMinecraftStyle);

        this.target = target;

        windowNbt = new WindowNbt(this);
        windowNbt.size(0, 20);
        windowNbt.constraints().top(this, Constraint.Property.Type.TOP, PADDING_VERTICAL).bottom(this, Constraint.Property.Type.BOTTOM, PADDING_VERTICAL).width(this, Constraint.Property.Type.WIDTH, 85);
        windows.add(windowNbt); //add to end of list
    }

    @Override
    public boolean canDockWindows()
    {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack stack)
    {
        this.renderBackground(stack, 0);

        RenderSystem.pushMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void resetBackground()
    {
        RenderSystem.popMatrix();
    }
}
