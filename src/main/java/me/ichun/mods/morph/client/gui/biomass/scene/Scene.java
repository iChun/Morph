package me.ichun.mods.morph.client.gui.biomass.scene;

import me.ichun.mods.morph.client.gui.biomass.WorkspaceMorph;

public abstract class Scene
{
    public Scene(WorkspaceMorph workspace){}
    public abstract void init();
    public abstract void addWindows(WorkspaceMorph workspace);
    public abstract void removeWindows(WorkspaceMorph workspace);
}
