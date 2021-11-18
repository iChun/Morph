package me.ichun.mods.morph.api.mob;

import me.ichun.mods.morph.api.mob.trait.Trait;

import java.util.ArrayList;

public class MobData
{
    public String forEntity;

    public Double biomassValueOverride;
    public Double biomassMultiplier;

    public ArrayList<Trait<?>> traits = new ArrayList<>();
}
