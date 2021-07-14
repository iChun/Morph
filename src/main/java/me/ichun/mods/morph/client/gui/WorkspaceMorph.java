package me.ichun.mods.morph.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.morph.client.gui.scene.Scene;
import me.ichun.mods.morph.client.gui.scene.SceneBiomassAbilities;
import me.ichun.mods.morph.client.gui.scene.SceneBiomassUpgrades;
import me.ichun.mods.morph.client.gui.scene.SceneMorphs;
import me.ichun.mods.morph.client.gui.window.WindowHeader;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkspaceMorph extends Workspace
{
    public static final int PADDING_VERTICAL = 30;
    public static final int PADDING_HEADER = 7;

    public Scene sceneBiomassUpgrades;
    public Scene sceneBiomassAbilities;
    public Scene sceneMorphs;

    private Scene currentScene;

    private WindowHeader windowHeader;

    public WorkspaceMorph(Screen lastScreen)
    {
        super(lastScreen, new TranslationTextComponent("morph.gui.workspace.title"), Morph.configClient.guiMinecraftStyle);

        addWindow(windowHeader = new WindowHeader(this));
        windowHeader.constraints().top(this, Constraint.Property.Type.TOP, PADDING_VERTICAL).width(this, Constraint.Property.Type.WIDTH, 60);

        sceneBiomassUpgrades = new SceneBiomassUpgrades(this);
        sceneBiomassAbilities = new SceneBiomassAbilities(this);
        sceneMorphs = new SceneMorphs(this);

        currentScene = sceneBiomassUpgrades;
        currentScene.addWindows(this);
        //no need to call init, we haven't even inited yet
    }

    public void setScene(Scene scene)
    {
        if(currentScene != scene)
        {
            currentScene.removeWindows(this);

            scene.addWindows(this); //call before assigning so the scene has reference to the previous scene.
            scene.init();
            currentScene = scene;
        }
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
