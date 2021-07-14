package me.ichun.mods.morph.api.biomass;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class BiomassUpgradeInfo
{
    public String id;
    public String parentId;

    public Boolean unlockedWhenRequirementsMet; //when the requirements are met, the player is automatically given one level of this

    @Nullable
    public Integer maxLevel; //null = infinite

    @Nullable
    public Multiplier value; //null = 1 time upgrade

    @Nullable
    public Multiplier cost;

    public ArrayList<Requirement> showRequirements; //TODO handle requirements
    public ArrayList<Requirement> requirements; //TODO handle requirements

    //Display
    public String keyNameOverride;
    public String keyDescOverride;

    public static class Multiplier
    {
        private double baseValue;
        private Double incValue; //so that this can be omitted in the JSON
        private double multiplier;
        private int equation;

        public double get(int level)
        {
            if(level <= 0)
            {
                return 0D;
            }
            if(incValue == null)
            {
                incValue = baseValue;
            }

            switch(equation)
            {
                default:
                case 1: return baseValue + (incValue * multiplier * level); //linear gain
                case 2: return baseValue + (incValue * Math.pow(level - 1, multiplier)); //exponential gain
                case 3: return baseValue + (incValue * Math.pow(multiplier, level - 1)); //exponential gain ver 2
                case 4: return baseValue + (incValue * Math.log(level * multiplier)); //log gain
                case 5: return baseValue + (incValue * Math.sqrt((level - 1) * multiplier)); //sqrt
            }
        }
    }

    public static class Requirement
    {
        public String id;
        public Integer level;
    }
}
