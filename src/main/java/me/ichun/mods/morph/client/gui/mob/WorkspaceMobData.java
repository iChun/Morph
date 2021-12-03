package me.ichun.mods.morph.client.gui.mob;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.morph.client.gui.mob.window.WindowMobData;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkspaceMobData extends Workspace
{
    public static final int PADDING_VERTICAL = 15;

    public final WindowMobData windowMobData;

    public WorkspaceMobData(Screen lastScreen)
    {
        super(lastScreen, new TranslationTextComponent("morph.gui.workspace.mobData.title"), Morph.configClient.guiMinecraftStyle);

        windowMobData = new WindowMobData(this);
        windowMobData.size(0, 20);
        windowMobData.constraints().top(this, Constraint.Property.Type.TOP, PADDING_VERTICAL).bottom(this, Constraint.Property.Type.BOTTOM, PADDING_VERTICAL).width(this, Constraint.Property.Type.WIDTH, 85);
        windows.add(windowMobData); //add to end of list
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
