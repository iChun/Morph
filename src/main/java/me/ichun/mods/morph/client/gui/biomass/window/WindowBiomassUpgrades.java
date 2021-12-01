package me.ichun.mods.morph.client.gui.biomass.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.morph.client.gui.biomass.WorkspaceMorph;
import me.ichun.mods.morph.client.gui.biomass.window.element.ElementBiomassUpgrades;

import javax.annotation.Nonnull;

public class WindowBiomassUpgrades extends Window<WorkspaceMorph>
{
    public WindowBiomassUpgrades(WorkspaceMorph parent)
    {
        super(parent);

        disableBringToFront();
        disableDockingEntirely();
        disableDrag();
        disableDragResize();
        disableTitle();

        setId("windowBiomassUpgrades");

        setView(new ViewBiomassUpgrades(this));
    }

    public static class ViewBiomassUpgrades extends View<WindowBiomassUpgrades>
    {
        public ElementButton buttonUpgrade;
        public ElementBiomassUpgrades biomassUpgrades;

        public ViewBiomassUpgrades(@Nonnull WindowBiomassUpgrades parent)
        {
            super(parent, "morph.gui.scene.biomassUpgrades.title");

            int padding = 3;

            buttonUpgrade = new ElementButton(this, "morph.gui.button.upgrade", btn -> {

            });
            buttonUpgrade.disabled = true;
            buttonUpgrade.setSize(70, 20);
            buttonUpgrade.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(buttonUpgrade);

            biomassUpgrades = new ElementBiomassUpgrades(this);
            biomassUpgrades.constraints().top(this, Constraint.Property.Type.TOP, padding + 1).left(this, Constraint.Property.Type.LEFT, padding + 1).right(this, Constraint.Property.Type.RIGHT, padding + 1).bottom(buttonUpgrade, Constraint.Property.Type.TOP, padding + 1);
            elements.add(biomassUpgrades);
        }
    }
}
