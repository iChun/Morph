package me.ichun.mods.morph.api.biomass;

import java.util.ArrayList;

public class BiomassUpgradeInfo
{
    public String id;
    public String parentId;

    public Integer maxLevel; //null = infinite

    public double baseCost;
    public Multiplier costMultiplier; //TODO start unlocked level?
    
    public double baseValue;
    public Multiplier valueMultiplier;

    public ArrayList<Requirement> requirements;

    //Display
    public String nameKey; //TODO Change the to overrides
    public String descriptionKey;

    public static class Multiplier
    {
        public double multiplier = 1D;
        public boolean isExponential;

        public double apply(double amount, int currentLevel)
        {
            if(isExponential)
            {
                return amount * Math.pow(multiplier, currentLevel);
            }
            else
            {
                return amount + (amount * multiplier * currentLevel); //TODO how do I head towards a value eg from 0.4 to gradually 0.8? maybe a base existing value?
            }
        }
    }

    public static class Requirement
    {
        public String id;
        public Integer level;
    }
}
