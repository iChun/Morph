package me.ichun.mods.morph.client.gui.mob.window;

import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementNumberInput;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class WindowEditNumber extends Window<Workspace>
{
    public WindowEditNumber(Workspace parent, String title, Consumer<String> callback, Trait<?> trait, Field f)
    {
        super(parent);

        setView(new ViewEditString(this, title, callback, trait, f));
        size(400, 300);

        disableDockingEntirely();

        setListener(currentView);
    }

    public static class ViewEditString extends View<WindowEditNumber>
    {
        public Consumer<String> callback;

        public ViewEditString(@Nonnull WindowEditNumber parent, String title, Consumer<String> callback, Trait<?> trait, Field f)
        {
            super(parent, title);
            this.callback = callback;

            boolean canDecimal = f.getType() == Double.class || f.getType() == Float.class;

            String input = "0";
            try
            {
                Object o = f.get(trait);
                if(o != null)
                {
                    if(f.getType() == Double.class)
                    {
                        input = Double.toString((Double)o);
                    }
                    else if(f.getType() == Float.class)
                    {
                        input = Float.toString((Float)o);
                    }
                    else
                    {
                        input = Integer.toString((Integer)o);
                    }
                }
            }
            catch(IllegalAccessException ignored){}

            ElementNumberInput textField = new ElementNumberInput(this, canDecimal);
            if(canDecimal)
            {
                textField.setMaxDec(3);
            }
            textField.setDefaultText(input).setEnterResponder(s -> submit(trait, f)).setId("input");
            textField.setConstraint(new Constraint(textField).left(this, Constraint.Property.Type.LEFT, 10).right(this, Constraint.Property.Type.RIGHT, 10).top(this, Constraint.Property.Type.TOP, 20));
            elements.add(textField);

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.cancel"), elementClickable ->
            {
                getWorkspace().removeWindow(parent);
            });
            button.setSize(60, 20);
            button.setConstraint(new Constraint(button).bottom(this, Constraint.Property.Type.BOTTOM, 10).right(this, Constraint.Property.Type.RIGHT, 10));
            elements.add(button);

            ElementButton<?> button1 = new ElementButton<>(this, I18n.format("gui.ok"), elementClickable -> {
                submit(trait, f);
            });
            button1.setSize(60, 20);
            button1.setConstraint(new Constraint(button1).right(button, Constraint.Property.Type.LEFT, 10));
            elements.add(button1);
        }

        @Override
        public void init()
        {
            super.init();

            ElementTextField textField = getById("input");
            setListener(textField);
            textField.mouseClicked(textField.getRight() - 20, textField.getTop() + 5, 0);
        }

        public void submit(Trait<?> trait, Field f)
        {
            ElementNumberInput textField = getById("input");
            boolean isDouble = f.getType() == Double.class;
            try
            {
                if(f.getType() == Double.class)
                {
                    f.set(trait, (Double)textField.getDouble());
                }
                else if(f.getType() == Float.class)
                {
                    f.set(trait, (Float)(float)textField.getDouble());
                }
                else
                {
                    f.set(trait, (Integer)textField.getInt());
                }
            }
            catch(IllegalAccessException ignored) {}

            callback.accept(null);

            getWorkspace().removeWindow(parentFragment);
        }
    }
}
