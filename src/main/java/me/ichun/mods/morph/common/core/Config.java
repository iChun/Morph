package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
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
    @IntMinMax(min = 1)
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
    public String disabledAbilities = ""; //TODO this

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
