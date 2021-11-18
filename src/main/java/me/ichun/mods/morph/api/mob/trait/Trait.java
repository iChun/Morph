package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class Trait<T extends Trait>
{
    @Nonnull
    public String type;

    public transient PlayerEntity player;

    public void addHooks(){} //also used to set default values if not set in the JSON
    public void removeHooks() {}

    public abstract void tick(float strength); //Strength ranges 0 - 1F

    public void transitionalTick(T prevTrait, float transitionProgress)
    {
        this.tick(1F); // by default, no transitional state, just tick at max strength
    }

    public void doTick(float strength)
    {
        this.tick(strength);
    }

    public void doTransitionalTick(T prevTrait, float transitionProgress)
    {
        this.transitionalTick(prevTrait, transitionProgress);
    }

    public boolean canTransitionTo(Trait<?> trait)
    {
        return this.getClass().equals(trait.getClass());
    }

    public abstract T copy();

    //IN-GAME INFO
    public String keyName() //A null name = hidden trait
    {
        return null;
    }

    public String keyDescription()
    {
        return null;
    }

    public ResourceLocation texIcon()
    {
        return null;
    }
    //END IN-GAME INFO

    public boolean isAbility()
    {
        return false;
    }

}
