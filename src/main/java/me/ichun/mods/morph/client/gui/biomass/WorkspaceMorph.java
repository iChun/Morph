package me.ichun.mods.morph.client.gui.biomass;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.morph.client.gui.biomass.scene.Scene;
import me.ichun.mods.morph.client.gui.biomass.scene.SceneBiomassAbilities;
import me.ichun.mods.morph.client.gui.biomass.scene.SceneBiomassUpgrades;
import me.ichun.mods.morph.client.gui.biomass.scene.SceneMorphs;
import me.ichun.mods.morph.client.gui.biomass.window.WindowSidebar;
import me.ichun.mods.morph.client.gui.biomass.window.WindowHeader;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;

public class WorkspaceMorph extends Workspace
{
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
    public static final int PADDING_VERTICAL = 15;
    public static final int PADDING_WINDOW = 2;

    public Scene sceneBiomassUpgrades;
    public Scene sceneBiomassAbilities;
    public Scene sceneMorphs;

    private Scene currentScene;

    public WindowHeader windowHeader;
    public WindowSidebar windowSidebar;

    public WorkspaceMorph(Screen lastScreen)
    {
        super(lastScreen, new TranslationTextComponent("morph.gui.workspace.title"), Morph.configClient.guiMinecraftStyle);

        windowHeader = new WindowHeader(this);
        windowHeader.size(0, 20);
        windowHeader.constraints().top(this, Constraint.Property.Type.TOP, PADDING_VERTICAL).width(this, Constraint.Property.Type.WIDTH, 60);
        windows.add(windowHeader); //add to end of list

        windowSidebar = new WindowSidebar(this);
        windowSidebar.size(22, 0);
        windowSidebar.constraints().left(windowHeader, Constraint.Property.Type.LEFT, 0).top(windowHeader, Constraint.Property.Type.BOTTOM, PADDING_WINDOW).bottom(this, Constraint.Property.Type.BOTTOM, PADDING_VERTICAL);
        windows.add(windowSidebar); //add to end of list

        sceneBiomassUpgrades = new SceneBiomassUpgrades(this);
        sceneBiomassAbilities = new SceneBiomassAbilities(this);
        sceneMorphs = new SceneMorphs(this);

        currentScene = sceneBiomassUpgrades;
        currentScene.addWindows(this);
        //no need to call init, we haven't even inited yet
    }

    public void setScene(Scene scene) //TODO why not just swap views??
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
