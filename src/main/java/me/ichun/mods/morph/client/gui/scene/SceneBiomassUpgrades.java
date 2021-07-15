package me.ichun.mods.morph.client.gui.scene;

import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.morph.client.gui.WorkspaceMorph;
import me.ichun.mods.morph.client.gui.window.WindowBiomassUpgrades;

public class SceneBiomassUpgrades extends Scene
{
    public WindowBiomassUpgrades windowBiomassUpgrades;

    public SceneBiomassUpgrades(WorkspaceMorph workspace)
    {
        super(workspace);
        windowBiomassUpgrades = new WindowBiomassUpgrades(workspace);
        windowBiomassUpgrades.constraints().top(workspace.windowHeader, Constraint.Property.Type.BOTTOM, WorkspaceMorph.PADDING_WINDOW).left(workspace.windowSidebar, Constraint.Property.Type.RIGHT, WorkspaceMorph.PADDING_WINDOW).right(workspace.windowHeader, Constraint.Property.Type.RIGHT, 0).bottom(workspace, Constraint.Property.Type.BOTTOM, WorkspaceMorph.PADDING_VERTICAL);
    }

    @Override
    public void init()
    {
        windowBiomassUpgrades.init();
    }

    @Override
    public void addWindows(WorkspaceMorph workspace)
    {
        workspace.windows.add(windowBiomassUpgrades);
    }

    @Override
    public void removeWindows(WorkspaceMorph workspace)
    {
        workspace.removeWindow(windowBiomassUpgrades);
    }
}
