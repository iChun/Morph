package me.ichun.mods.morph.api.biomass;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class BiomassUpgradeInfo
{
    public String id;
    public String parentId; //For morph traits, having a parent id makes this a direct upgrade over the parent, eliminating the parent.

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
    private String textureLocationOverride;
    private String keyNameOverride;
    private String keyDescOverride;

    public transient ResourceLocation textureLocation;

    public ResourceLocation getTextureLocation()
    {
        if(textureLocation == null)
        {
            if(textureLocationOverride != null)
            {
                textureLocation = new ResourceLocation(textureLocationOverride);
            }
            else
            {
                textureLocation = new ResourceLocation("morph", "textures/biomass/upgrade/" + id + ".png");
            }
        }
        return textureLocation;
    }

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
                case 4: return baseValue + (incValue * Math.log10(level * multiplier)); //log10 gain
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
