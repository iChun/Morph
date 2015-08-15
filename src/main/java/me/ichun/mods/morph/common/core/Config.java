package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp(category = "gameplay", useSession = true)
    @IntMinMax(min = 1)
    public int morphTime = 80;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int canSleepMorphed = 1;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int showPlayerLabel = 0;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int useLocalResources = 0;

    @ConfigProp(category = "gameplay", useSession = true)
    @IntBool
    public int instaMorph = 0; //Also known as Morph On Kill

    @ConfigProp(category = "abilities", useSession = true)
    @IntBool
    public int abilities = 1;

    @ConfigProp(category = "abilities", useSession = true)
    public String disabledAbilities = "";

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
