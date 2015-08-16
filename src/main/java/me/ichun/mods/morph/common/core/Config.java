package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp(category = "gameplay")
    @IntBool
    public int childMorphs = 0; //TODO this

    @ConfigProp(category = "gameplay")
    @IntBool
    public int playerMorphs = 1; //TODO this

    @ConfigProp(category = "gameplay")
    @IntBool
    public int bossMorphs = 0; //TODO this

    @ConfigProp(category = "gameplay")
    @IntBool
    public int classicMode = 0;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntMinMax(min = 30)
    public int morphTime = 80; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int canSleepMorphed = 1; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int showPlayerLabel = 0; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int useLocalResources = 0; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int loseMorphsOnDeath = 0; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int instaMorph = 0; //Also known as Morph On Kill  //TODO this

    @ConfigProp(category = "abilities", useSession = true)
    @IntBool
    public int abilities = 1; //TODO this

    @ConfigProp(category = "abilities", useSession = true)
    public String[] disabledAbilities = new String[0]; //TODO this

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int showAbilitiesInGui = 1;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorUp = new KeyBind(Keyboard.KEY_LBRACKET);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorDown = new KeyBind(Keyboard.KEY_RBRACKET);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorLeft = new KeyBind(Keyboard.KEY_LBRACKET, true, false, false, false);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorRight = new KeyBind(Keyboard.KEY_LBRACKET, true, false, false, false);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorSelect = new KeyBind(Keyboard.KEY_RETURN);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorCancel = new KeyBind(Keyboard.KEY_ESCAPE);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorRemoveMorph = new KeyBind(Keyboard.KEY_BACK);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keyFavourite = new KeyBind(Keyboard.KEY_GRAVE);


    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return Morph.MOD_NAME.toLowerCase();
    }

    @Override
    public String getModName()
    {
        return Morph.MOD_NAME;
    }
}
