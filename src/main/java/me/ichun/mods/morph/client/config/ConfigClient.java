package me.ichun.mods.morph.client.config;

import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.morph.client.render.hand.HandHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = 0)
    public int selectorDistanceFromTop = 30;

    @Prop(min = 0, max = 30)
    public double selectorScale = 1D;

    public boolean selectorAllowMouseControl = true;

    @Prop(min = 0D, max = 1D)
    public double radialScale = 0.75D;

    @Prop(min = 0, max = 3)
    public int acquisitionPlayAnimation = 3; //0 = no, 1 = morph, 2 = biomass, 3 = all

    @Prop(min = 3, max = 100)
    public int acquisitionTendrilMaxChild = 10; //also estimated ticks to get to entity at max

    @Prop(min = 0, max = 100)
    public int acquisitionTendrilPartOpacity = 5;

    public boolean morphAllowHandOverride = true;

    @Prop(min = 0, max = 2)
    public int biomassBarMode = 1;

    @Prop(min = 0, max = 2, guiElementOverride = "Morph:reloadResources")//we're mounting this to add a button underneath
    public int guiMinecraftStyle = 2;

    public ConfigClient()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-client.toml");
    }

    @Override
    public <T extends ConfigBase> T init()
    {
        GUI_ELEMENT_OVERRIDES.put("Morph:reloadResources", (value, itemOri) -> {

            ElementList.Item<?> item = itemOri.parentFragment.addItem(value).setBorderSize(0);
            item.setSelectionHandler(itemObj -> {
                if(itemObj.selected)
                {
                    for(Element<?> element : itemObj.elements)
                    {
                        if(element instanceof ElementTextWrapper || element instanceof ElementPadding)
                        {
                            continue;
                        }
                        element.parentFragment.setListener(element);
                        element.mouseClicked(element.getLeft() + element.getWidth() / 2D, element.getTop() + element.getHeight() / 2D, 0);
                        element.mouseReleased(element.getLeft() + element.getWidth() / 2D, element.getTop() + element.getHeight() / 2D, 0);
                        break;
                    }
                }
            });
            ElementTextWrapper wrapper = new ElementTextWrapper(item).setText(I18n.format("config.morph.resources.reload.desc"));
            wrapper.setConstraint(new Constraint(wrapper).left(item, Constraint.Property.Type.LEFT, 3).right(item, Constraint.Property.Type.RIGHT, 90));
            wrapper.setTooltip(value.desc);
            item.addElement(wrapper);
            ElementPadding padding = new ElementPadding(item, 0, 20);
            padding.setConstraint(new Constraint(padding).right(item, Constraint.Property.Type.RIGHT, 0));
            item.addElement(padding);

            ElementButton<?> button = new ElementButton<>(item, "config.morph.resources.reload.btn", btn ->
            {
                ResourceHandler.reloadAllResources();
                WindowPopup.popup(item.getWorkspace(), 0.6D, 0.6D, null, I18n.format("config.morph.resources.reload.success"));
            });
            button.setTooltip(I18n.format("config.morph.resources.reload.reextract.desc"));
            button.setSize(80, 14);
            button.setConstraint(new Constraint(button).top(item, Constraint.Property.Type.TOP, 3).bottom(item, Constraint.Property.Type.BOTTOM, 3).right(item, Constraint.Property.Type.RIGHT, 8));
            item.addElement(button);

            ElementButton<?> button1 = new ElementButton<>(item, "config.morph.resources.reload.reextract", btn ->
            {
                try
                {
                    ResourceHandler.extractFiles(ResourceHandler.getMorphDir().resolve(ResourceHandler.MOB_SUPPORT_VERSION + ".extracted"));
                    ResourceHandler.reloadAllResources();
                    WindowPopup.popup(item.getWorkspace(), 0.6D, 0.6D, null, I18n.format("config.morph.resources.reload.success"));
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            });
            button1.setTooltip(I18n.format("config.morph.resources.reload.reextract.desc"));
            button1.setSize(80, 14);
            button1.setConstraint(new Constraint(button1).top(item, Constraint.Property.Type.TOP, 3).bottom(item, Constraint.Property.Type.BOTTOM, 3).right(button, Constraint.Property.Type.LEFT, 4));
            item.addElement(button1);

            return false; //we still want the button to generate, this is a hook in.
        });

        return super.init();
    }

    @Override
    public void onConfigLoaded()
    {
        HandHandler.setState(morphAllowHandOverride);
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Morph.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Morph.MOD_NAME;
    }

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }
}
