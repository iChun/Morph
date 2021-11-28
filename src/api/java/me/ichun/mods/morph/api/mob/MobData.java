package me.ichun.mods.morph.api.mob;

import me.ichun.mods.morph.api.mob.trait.Trait;

import java.util.ArrayList;

public class MobData
{
    public String author; //For Credit
    public String forEntity;

    public Double biomassValueOverride;
    public Double biomassMultiplier;

    public Boolean disableAcquiringMorph;

    public ArrayList<Trait<?>> traits = new ArrayList<>();
}
