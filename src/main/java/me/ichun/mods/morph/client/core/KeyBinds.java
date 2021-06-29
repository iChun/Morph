package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.key.KeyBind;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public final class KeyBinds
{
    public static KeyBind keySelectorUp;
    public static KeyBind keySelectorDown;
    public static KeyBind keyFavourite;
    public static KeyBind keyAbility;
    public static KeyBind keyBiomass;

    public static void init()
    {
        keySelectorUp = new KeyBind(new KeyBinding("morph.key.selectorUp", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_LEFT_BRACKET), "key.categories.morph"), null, null);
        keySelectorDown = new KeyBind(new KeyBinding("morph.key.selectorDown", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_LEFT_BRACKET), "key.categories.morph"), null, null);
        keyFavourite = new KeyBind(new KeyBinding("morph.key.favourite", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_GRAVE_ACCENT), "key.categories.morph"), null, null);
        keyAbility = new KeyBind(new KeyBinding("morph.key.ability", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_X), "key.categories.morph"), null, null);
        keyBiomass = new KeyBind(new KeyBinding("morph.key.biomass", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_B), "key.categories.morph"), null, null);
    }
}
