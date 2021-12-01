package me.ichun.mods.morph.client.gui.biomass.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggle;
import me.ichun.mods.morph.client.gui.biomass.WorkspaceMorph;
import me.ichun.mods.morph.client.gui.biomass.window.element.ElementBiomassBar;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class WindowHeader extends Window<WorkspaceMorph>
{
    public WindowHeader(WorkspaceMorph parent)
    {
        super(parent);

        disableBringToFront();
        disableDockingEntirely();
        disableDrag();
        disableDragResize();
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

            //Render the biomass bar
            ElementBiomassBar bar = new ElementBiomassBar(this);
            bar.setSize(182, 5);
            bar.constraints().right(this, Constraint.Property.Type.RIGHT, padding).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(bar);

            ElementTextWrapper text = new ElementTextWrapper(this);
            text.setNoWrap().setId("text");
            text.constraints().left(this, Constraint.Property.Type.LEFT, padding).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding).right(bar, Constraint.Property.Type.LEFT, padding);
            elements.add(text);
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            ElementTextWrapper text = getById("text");
            String weightKg = WorkspaceMorph.FORMATTER.format(Morph.eventHandlerClient.morphData.biomass);
            String weightLb = WorkspaceMorph.FORMATTER.format(Morph.eventHandlerClient.morphData.biomass * 2.20462D);
            text.setText(weightKg + " kg");
            text.setTooltip(I18n.format("morph.gui.text.weight.tooltip", weightKg, weightLb));
            text.init();

            super.render(stack, mouseX, mouseY, partialTick);
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
