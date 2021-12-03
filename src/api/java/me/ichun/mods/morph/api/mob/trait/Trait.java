package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class Trait<T extends Trait> implements Comparable<T>
{
    @Nonnull
    public String type;
    public String upgradeFor; //TODO make upgrades
    public Double purchaseCost;
    public Boolean hidden;

    public transient PlayerEntity player;
    public transient ArrayList<Trait<?>> stateTraits;
    @Nullable
    public transient LivingEntity livingInstance;

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
    public String getTranslationKeyRoot()
    {
        return translationKeyRootOverride() == null ? "morph.trait." + type : translationKeyRootOverride();
    }

    private String translationKeyRootOverride() //we use <key>.name/desc/<field>.name/<field>.desc. If left null, defaults to Morph's prefix "morph.trait.<type>"
    {
        return null;
    }

    public boolean isHidden()
    {
        return hidden != null && hidden;
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

    @Override
    public int compareTo(T o)
    {
        if(!this.isAbility() && o.isAbility())
        {
            //we're before
            return -1;
        }
        else if(this.isAbility() && !o.isAbility())
        {
            //we're after
            return 1;
        }
        else if(FMLEnvironment.dist.isClient())
        {
            return compareTranslatedName(o);
        }
        return type.compareTo(o.type);
    }

    @OnlyIn(Dist.CLIENT)
    private int compareTranslatedName(T o)
    {
        return I18n.format(getTranslationKeyRoot() + ".name").compareTo(I18n.format(o.getTranslationKeyRoot() + ".name"));
    }
}
