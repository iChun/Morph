package me.ichun.mods.morph.api.mob.trait;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import javax.annotation.Nonnull;
import java.util.HashMap;

public abstract class Trait
{
    private static final HashMap<String, Class<? extends Trait>> TRAITS = Util.make(new HashMap<>(), m -> {
       //Register the default traits
    });

    @Nonnull
    public String type;

    public transient PlayerEntity player;

    public void addHooks(){}
    public void removeHooks() {}

    public abstract void tick(float strength);

    public abstract <T extends Trait> T copy();

    //IN-GAME INFO
    public abstract String keyName();

    public abstract String keyDescription();

    public abstract ResourceLocation texIcon();
    //END IN-GAME INFO

    public boolean isAbility()
    {
        return false;
    }

    public static void registerTrait(String type, Class<? extends Trait> clz)
    {
        if(TRAITS.containsKey(type))
        {
            Morph.LOGGER.warn("We already have another Trait of type {} with class {}. This is a mod-level override so we shall acknowledge it. Overriding with class {}", type, TRAITS.get(type).getName(), clz.getName());
        }

        TRAITS.put(type, clz);
    }
}
