package morph.client.core;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Keys extends KeyHandler{

	public static KeyBinding 
			keyNext = new KeyBinding("[Morph] Navigate Next", Keyboard.KEY_LBRACKET),
			keyPrev = new KeyBinding("[Morph] Navigate Previous", Keyboard.KEY_RBRACKET),
			keyHoldVertical = new KeyBinding("[Morph] Navigate Vertically", 0),
			keyHoldHorizontal = new KeyBinding("[Morph] Navigate Horizontally", Keyboard.KEY_LSHIFT),
			keySelect = new KeyBinding("[Morph] Select", Keyboard.KEY_RETURN),
			keyCancel = new KeyBinding("[Morph] Cancel", Keyboard.KEY_ESCAPE),
			keyRemove = new KeyBinding("[Morph] Remove", Keyboard.KEY_BACK),
			keyFavourite = new KeyBinding("[Morph] Favourite", Keyboard.KEY_EQUALS);
	
	public Keys() {
		super(new KeyBinding[]{keyNext, keyPrev,
				keyHoldVertical, keyHoldHorizontal,
				keySelect, keyCancel, keyRemove, keyFavourite}, 
				 new boolean[]{false,false,false,false,
								false,false,false,false});
	}

	@Override
	public String getLabel() {
		return "Morph Key Bindings";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		// We don't use this stuff!
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		// We don't use this stuff!
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}
	

}
