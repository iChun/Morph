package me.ichun.mods.morph.api.event;

import net.minecraftforge.eventbus.api.Event;

public class MorphLoadResourceEvent extends Event
{
    public enum Type
    {
        BIOMASS,
        HAND,
        MOB,
        NBT
    }

    private final Type type;

    public MorphLoadResourceEvent(Type type)
    {
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }
}
