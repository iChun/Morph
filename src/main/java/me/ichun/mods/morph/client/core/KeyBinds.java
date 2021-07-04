package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public final class KeyBinds
{
    public static KeyBind keySelectorUp;
    public static KeyBind keySelectorDown;
    public static KeyBind keySelectorLeft;
    public static KeyBind keySelectorRight;
    public static KeyBind keyFavourite;
    public static KeyBind keyAbility;
    public static KeyBind keyBiomass;

    public static void init()
    {
        keySelectorUp = new KeyBind(new KeyBinding("morph.key.selectorUp", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_LEFT_BRACKET), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), null);
        keySelectorDown = new KeyBind(new KeyBinding("morph.key.selectorDown", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_RIGHT_BRACKET), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), null);
        keySelectorLeft = new KeyBind(new KeyBinding("morph.key.selectorLeft", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, KeyModifier.SHIFT, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_LEFT_BRACKET), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), null);
        keySelectorRight = new KeyBind(new KeyBinding("morph.key.selectorRight", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, KeyModifier.SHIFT, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_RIGHT_BRACKET), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), null);
        keyFavourite = new KeyBind(new KeyBinding("morph.key.favourite", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_GRAVE_ACCENT), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, true));
        keyAbility = new KeyBind(new KeyBinding("morph.key.ability", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_B), "key.categories.morph"), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, false), keyBind -> Morph.eventHandlerClient.handleInput(keyBind, true));
        keyBiomass = new KeyBind(new KeyBinding("morph.key.biomass", KeyConflictContextMorph.IN_GAME_MODIFIER_SENSITIVE, KeyModifier.SHIFT, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_B), "key.categories.morph"), null, null);
    }

    //Sorry, Forge.
    public enum KeyConflictContextMorph implements IKeyConflictContext
    {
        //Allows in-game modifiers (or lack thereof) to conflict
        IN_GAME_MODIFIER_SENSITIVE {
            @Override
            public boolean isActive()
            {
                return !KeyConflictContext.GUI.isActive();
            }

            @Override
            public boolean conflicts(IKeyConflictContext other)
            {
                return this == other;
            }
        }
    }

}