package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public abstract class Trait<T extends Trait>
{
    @Nonnull
    public String type;
    public String upgradeFor; //TODO make upgrades
    public Double purchaseCost;

    public transient PlayerEntity player;
    public transient ArrayList<Trait<?>> stateTraits;

    //also used to set default values if not set in the JSON
    public void addHooks()
    {
        if(this instanceof IEventBusRequired)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void removeHooks()
    {
        if(this instanceof IEventBusRequired)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

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
