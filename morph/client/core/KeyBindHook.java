package morph.client.core;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindHook extends KeyHandler 
{

	public static KeyBinding keyBindHook = new KeyBinding("morph.keybinds", Keyboard.KEY_NEXT);
	
	public KeyBindHook() {
		super(new KeyBinding[] { keyBindHook });
	}

	@Override
	public String getLabel() {
		return "MorphKeybind";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return null;
	}

}
