package me.ichun.mods.morph.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggleRotatable;
import me.ichun.mods.morph.client.gui.WorkspaceMorph;
import me.ichun.mods.morph.common.biomass.Upgrades;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class WindowSidebar extends Window<WorkspaceMorph>
{
    public WindowSidebar(WorkspaceMorph parent)
    {
        super(parent);

        disableBringToFront();
        disableDockingEntirely();
        disableDrag();
        disableDragResize();
        disableTitle();

        setId("windowSidebar");

        setView(new ViewSidebar(this));
    }

    public static class ViewSidebar extends View<WindowSidebar>
    {
        public ViewSidebar(@Nonnull WindowSidebar parent)
        {
            super(parent, "morph.gui.window.sidebar");

            int padding = 2;

            ElementToggleRotatable toggleBiomassUpgrades = new ElementToggleRotatable(this, "morph.gui.scene.biomassUpgrades.title", -1, btn -> {
                parent.parent.setScene(parent.parent.sceneBiomassUpgrades);
                deselectAllExcept((ElementToggleRotatable)btn);
            });
            toggleBiomassUpgrades.toggleState = true; //this scene is always available and selected by default.
            toggleBiomassUpgrades.setSize(60, 14);
            toggleBiomassUpgrades.constraints().top(this, Constraint.Property.Type.TOP, 0);
            elements.add(toggleBiomassUpgrades);

            //now, add the optional ones
            ElementToggleRotatable last = toggleBiomassUpgrades;

            if(MorphHandler.INSTANCE.getBiomassUpgrade(Minecraft.getInstance().player, Upgrades.ID_BIOMASS_ABILITIES) != null) //has unlocked biomass abilities
            {
                ElementToggleRotatable toggleBiomassAbilities = new ElementToggleRotatable(this, "morph.gui.scene.biomassAbilities.title", -1, btn -> {
                    parent.parent.setScene(parent.parent.sceneBiomassAbilities);
                    deselectAllExcept((ElementToggleRotatable)btn);
                });
                toggleBiomassAbilities.setSize(60, 14);
                toggleBiomassAbilities.constraints().top(last, Constraint.Property.Type.BOTTOM, 0);
                elements.add(toggleBiomassAbilities);
                last = toggleBiomassAbilities;
            }

            if(MorphHandler.INSTANCE.getBiomassUpgrade(Minecraft.getInstance().player, Upgrades.ID_MORPH_ABILITY) != null) //has unlocked morphing
            {
                ElementToggleRotatable toggleMorphs = new ElementToggleRotatable(this, "morph.gui.scene.morphs.title", -1, btn -> {
                    parent.parent.setScene(parent.parent.sceneMorphs);
                    deselectAllExcept((ElementToggleRotatable)btn);
                });
                toggleMorphs.setSize(60, 14);
                toggleMorphs.constraints().top(last, Constraint.Property.Type.BOTTOM, 0);
                elements.add(toggleMorphs);
                last = toggleMorphs;
            }
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
