package me.ichun.mods.morph.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.morph.client.gui.WorkspaceMorph;
import me.ichun.mods.morph.client.gui.window.element.ElementBiomassBar;
import me.ichun.mods.morph.common.biomass.Upgrades;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class WindowHeader extends Window<WorkspaceMorph>
{
    public WindowHeader(WorkspaceMorph parent)
    {
        super(parent);

        disableBringToFront();
        disableDocking();
        disableDockStacking();
        disableUndocking();
        disableDrag();
        disableTitle();

        setId("windowHeader");

        setView(new ViewHeader(this));
    }

    public static class ViewHeader extends View<WindowHeader>
    {
        public ViewHeader(@Nonnull WindowHeader parent)
        {
            super(parent, "morph.gui.window.header");

            int padding = 2;

            ElementToggle toggleBiomassUpgrades = new ElementToggle(this, "morph.gui.scene.biomassUpgrades.title", btn -> {
                parent.parent.setScene(parent.parent.sceneBiomassUpgrades);
                deselectAllExcept((ElementToggle)btn);
            });
            toggleBiomassUpgrades.toggleState = true; //this scene is always available and selected by default.
            toggleBiomassUpgrades.constraints().left(this, Constraint.Property.Type.LEFT, padding).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(toggleBiomassUpgrades);

            //now, add the optional ones
            ElementToggle last = toggleBiomassUpgrades;

            if(MorphHandler.INSTANCE.getBiomassUpgrade(Minecraft.getInstance().player, Upgrades.ID_BIOMASS_ABILITIES) != null) //has unlocked biomass abilities
            {
                ElementToggle toggleBiomassAbilities = new ElementToggle(this, "morph.gui.scene.biomassAbilities.title", btn -> {
                    parent.parent.setScene(parent.parent.sceneBiomassAbilities);
                    deselectAllExcept((ElementToggle)btn);
                });
                toggleBiomassAbilities.constraints().left(last, Constraint.Property.Type.RIGHT, 0).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
                elements.add(toggleBiomassAbilities);
                last = toggleBiomassAbilities;
            }

            if(MorphHandler.INSTANCE.getBiomassUpgrade(Minecraft.getInstance().player, Upgrades.ID_MORPH_ABILITY) != null) //has unlocked morphing
            {
                ElementToggle toggleMorphs = new ElementToggle(this, "morph.gui.scene.morphs.title", btn -> {
                    parent.parent.setScene(parent.parent.sceneMorphs);
                    deselectAllExcept((ElementToggle)btn);
                });
                toggleMorphs.constraints().left(last, Constraint.Property.Type.RIGHT, 0).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
                elements.add(toggleMorphs);
                last = toggleMorphs;
            }

            //Render the biomass bar
            ElementBiomassBar bar = new ElementBiomassBar(this);
            bar.constraints().right(this, Constraint.Property.Type.RIGHT, padding).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(bar);
        }

        public void deselectAllExcept(ElementToggle toggle)
        {
            for(Element<?> element : elements)
            {
                if(element instanceof ElementToggle)
                {
                    ElementToggle<?> elementToggle = (ElementToggle<?>)element;
                    elementToggle.toggleState = elementToggle == toggle;
                }
            }
        }
    }
}
