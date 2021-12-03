package me.ichun.mods.morph.client.gui.mob.window;

import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.mob.TraitHandler;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class WindowAddTrait extends Window<Workspace>
{
    public WindowAddTrait(Workspace parent, Consumer<Trait<?>> callback)
    {
        super(parent);

        setView(new ViewAddTrait(this, callback));
        disableDockingEntirely();
    }

    public static class ViewAddTrait extends View<WindowAddTrait>
    {
        public final Consumer<Trait<?>> callback;
        public ElementTextWrapper description;
        public ElementList<?> list;

        public ViewAddTrait(@Nonnull WindowAddTrait parent, Consumer<Trait<?>> callback)
        {
            super(parent, "morph.gui.workspace.mobData.addTrait");
            this.callback = callback;

            ElementScrollBar<?> sv = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            sv.setConstraint(new Constraint(sv).top(this, Constraint.Property.Type.TOP, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, 64) // 10 + 20 + 10, bottom + button height + padding
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(sv);

            list = new ElementList<>(this).setScrollVertical(sv);
            list.setConstraint(new Constraint(list).bottom(this, Constraint.Property.Type.BOTTOM, 64)
                    .left(this, Constraint.Property.Type.LEFT, 0).right(sv, Constraint.Property.Type.LEFT, 0)
                    .top(this, Constraint.Property.Type.TOP, 0));
            elements.add(list);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), btn ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 14));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), btn ->
            {
                for(ElementList.Item<?> item : list.items)
                {
                    if(item.selected)
                    {
                        returnTraitInstance((Trait<?>)item.getObject());
                        return;
                    }
                }
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);

            //Add the description box
            ElementScrollBar<?> scroll = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            scroll.setConstraint(new Constraint(scroll).top(list, Constraint.Property.Type.BOTTOM, 0)
                    .bottom(this, Constraint.Property.Type.BOTTOM, 0)
                    .right(this, Constraint.Property.Type.RIGHT, 0)
            );
            elements.add(scroll);

            ElementScrollView list1 = new ElementScrollView(this).setScrollVertical(scroll);
            list1.setConstraint(new Constraint(list1).bottom(this, Constraint.Property.Type.BOTTOM, 0).left(this, Constraint.Property.Type.LEFT, 0).right(scroll, Constraint.Property.Type.LEFT, 0).top(scroll, Constraint.Property.Type.TOP, 0));
            elements.add(list1);

            description = new ElementTextWrapper(list1);
            description.setConstraint(Constraint.sizeOnly(description));
            list1.addElement(description);

            TreeMap<Trait, Class<? extends Trait>> clzList = new TreeMap<>(Comparator.naturalOrder());
            TraitHandler.TRAITS.forEach((k, clz) -> {
                try
                {
                    clzList.put(clz.newInstance(), clz);
                }
                catch(InstantiationException | IllegalAccessException e)
                {
                    Morph.LOGGER.error("Error creating trait instance!", e);
                }
            });
            clzList.forEach((k, v) -> {
                        final Trait<?> instance = k;
                        ElementList.Item<? extends Trait<?>> traitItem = list.addItem(instance).addTextWrapper(I18n.format(instance.getTranslationKeyRoot() + ".name")).setDoubleClickHandler(item -> {
                            if(item.selected)
                            {
                                returnTraitInstance(instance);
                            }
                        }).setEnterResponder(item -> {
                            if(item.selected)
                            {
                                returnTraitInstance(instance);
                                return true;
                            }
                            return false;
                        }).setSelectionHandler(item -> {
                            String desc = I18n.format(instance.getTranslationKeyRoot() + ".desc");
                            if(item.selected && !desc.equals(instance.getTranslationKeyRoot() + ".desc"))
                            {
                                description.setText(desc);
                            }
                            else
                            {
                                description.setText("");
                            }
                            description.init();
                        });

                        ElementTextWrapper text = (ElementTextWrapper)traitItem.elements.get(0);
                        if(!instance.isAbility())
                        {
                            text.setColor(Theme.getAsHex(getTheme().fontChat));
                        }
                    }
            );
        }

        public void returnTraitInstance(Trait<?> obj)
        {
            parentFragment.parent.removeWindow(parentFragment);

            callback.accept(obj);
        }
    }
}
