package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;

import java.io.File;
import java.lang.reflect.Field;

public class Config extends ConfigBase
{
    @ConfigProp(category = "gameplay")
    @IntBool
    public int childMorphs = 0;

    @ConfigProp(category = "gameplay")
    @IntBool
    public int playerMorphs = 1;

    @ConfigProp(category = "gameplay")
    @IntBool
    public int bossMorphs = 0;

    @ConfigProp(category = "gameplay")
    @IntBool
    public int classicMode = 0; //TODO check if this needs to be in the sexsseion

    @ConfigProp(category = "gameplay", useSession = true)
    @IntMinMax(min = 30)
    public int morphTime = 80;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int canSleepMorphed = 0; //TODO this

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int showPlayerLabel = 0; //TODO this

    @ConfigProp(category = "gameplay")
    @IntBool
    public int loseMorphsOnDeath = 0; //TODO this

    @ConfigProp(category = "gameplay")
    @IntBool
    public int instaMorph = 0; //Also known as Morph On Kill

    @ConfigProp(category = "gameplay")
    @IntBool
    public int useLocalResources = 0;

    @ConfigProp(category = "gameplay")
    public String customPatchLink = "";

    @ConfigProp(category = "gameplay")
    public String[] blackwhiteListedMobs = new String[0];

    @ConfigProp(category = "gameplay")
    @IntBool
    public int listIsBlacklistMobs = 1;

    @ConfigProp(category = "gameplay")
    public String[] blackwhiteListedPlayers = new String[0];

    @ConfigProp(category = "gameplay")
    @IntBool
    public int listIsBlacklistPlayers = 1;

    @ConfigProp(category = "abilities", useSession = true)
    @IntBool
    public int abilities = 1; //TODO this

    @ConfigProp(category = "abilities", useSession = true)
    public String[] disabledAbilities = new String[0]; //TODO this

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public String[] abilitiesToHideInGui = new String[0]; //TODO this

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorUp = new KeyBind(Keyboard.KEY_LBRACKET);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorDown = new KeyBind(Keyboard.KEY_RBRACKET);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorLeft = new KeyBind(Keyboard.KEY_LBRACKET, true, false, false, false);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind keySelectorRight = new KeyBind(Keyboard.KEY_RBRACKET, true, false, false, false);

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

    @Override
    public void onConfigChange(Field field, Object original) //Nested int array and keybind original is the new var, no ori cause lazy
    {
        readBlackWhitelists();
    }

    @Override
    public void setup()
    {
        super.setup();
        readBlackWhitelists();
    }

    public void readBlackWhitelists()
    {
        PlayerMorphHandler.blackwhiteEntityClasses.clear();
        for(String s : blackwhiteListedMobs)
        {
            try
            {
                Class clz = Class.forName(s);
                if(EntityLivingBase.class.isAssignableFrom(clz) && !PlayerMorphHandler.blackwhiteEntityClasses.contains(clz))
                {
                    PlayerMorphHandler.blackwhiteEntityClasses.add(clz);
                }
            }
            catch(ClassNotFoundException ignored){}
        }
    }
}
