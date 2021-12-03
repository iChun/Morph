package me.ichun.mods.morph.client.gui.mob.window;

import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextField;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class WindowEditString extends Window<Workspace>
{
    public WindowEditString(Workspace parent, String s, String title, Consumer<String> callback)
    {
        super(parent);

        setView(new ViewEditString(this, s, title, callback));
        size(400, 300);

        disableDockingEntirely();

        setListener(currentView);
    }

    public static class ViewEditString extends View<WindowEditString>
    {
        public Consumer<String> callback;

        public ViewEditString(@Nonnull WindowEditString parent, String input, String title, Consumer<String> callback)
        {
            super(parent, title);
            this.callback = callback;

            ElementTextField textField = new ElementTextField(this);
            textField.setDefaultText(input).setEnterResponder(s -> submit()).setId("input");
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
                submit();
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
            textField.mouseClicked(textField.getRight() - 10, textField.getTop() + 5, 0);
        }

        public void submit()
        {
            parentFragment.parent.removeWindow(parentFragment);

            ElementTextField textField = getById("input");
            String text = textField.getText();

            callback.accept(text);
        }
    }
}
