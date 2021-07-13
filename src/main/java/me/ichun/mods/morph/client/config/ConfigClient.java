package me.ichun.mods.morph.client.config;

import me.ichun.mods.ichunutil.common.config.ConfigBase;
import me.ichun.mods.ichunutil.common.config.annotations.CategoryDivider;
import me.ichun.mods.ichunutil.common.config.annotations.Prop;
import me.ichun.mods.morph.client.render.hand.HandHandler;
import me.ichun.mods.morph.common.Morph;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;

public class ConfigClient extends ConfigBase
{
    @CategoryDivider(name = "clientOnly")
    @Prop(min = 0)
    public int selectorDistanceFromTop = 30;

    @Prop(min = 0, max = 30)
    public double selectorScale = 1D;

    public boolean selectorAllowMouseControl = true;

    @Prop(min = 0D, max = 1D)
    public double radialScale = 0.75D;

    @Prop(min = 0, max = 3)
    public int acquisitionPlayAnimation = 3; //0 = no, 1 = morph, 2 = biomass, 3 = all

    @Prop(min = 3, max = 100)
    public int acquisitionTendrilMaxChild = 10; //also estimated ticks to get to entity at max

    @Prop(min = 0, max = 100)
    public int acquisitionTendrilPartOpacity = 5;

    public boolean morphAllowHandOverride = true;

    @Prop(min = 0, max = 2)
    public int biomassBarMode = 1;

    public ConfigClient()
    {
        super(ModLoadingContext.get().getActiveContainer().getModId() + "-client.toml");
    }

    @Override
    public void onConfigLoaded()
    {
        HandHandler.setState(morphAllowHandOverride);
    }

    @Nonnull
    @Override
    public String getModId()
    {
        return Morph.MOD_ID;
    }

    @Nonnull
    @Override
    public String getConfigName()
    {
        return Morph.MOD_NAME;
    }

    @Nonnull
    @Override
    public ModConfig.Type getConfigType()
    {
        return ModConfig.Type.CLIENT;
    }
}
