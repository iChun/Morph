package me.ichun.mods.morph.api.biomass;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class BiomassUpgradeInfo
{
    public String id;
    public String parentId;

    @Nullable
    public Integer maxLevel; //null = infinite

    @Nullable
    public Multiplier value; //null = 1 time upgrade

    @Nullable
    public Multiplier cost; //null = unlocked at start

    public ArrayList<Requirement> requirements;

    //Display
    public String nameKeyOverride; //TODO Change the to overrides
    public String descriptionKeyOverride;

    public static class Multiplier
    {
        public double baseValue;
        public double multiplier = 1D;
        public boolean isExponential;

        public double get(int level)
        {
            if(isExponential)
            {
                //level 0 starts with base value
                return baseValue * Math.pow(multiplier, level);
            }
            else
            {
                return baseValue + (baseValue * multiplier * level);
            }
        }
    }

    public static class Requirement
    {
        public String id;
        public Integer level;
    }
}
