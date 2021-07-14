package me.ichun.mods.morph.client.gui.scene;

import me.ichun.mods.morph.client.gui.WorkspaceMorph;

public abstract class Scene
{
    public Scene(WorkspaceMorph workspace){}
    public abstract void init();
    public abstract void addWindows(WorkspaceMorph workspace);
    public abstract void removeWindows(WorkspaceMorph workspace);
}
