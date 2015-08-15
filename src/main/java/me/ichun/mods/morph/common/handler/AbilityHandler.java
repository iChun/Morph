package me.ichun.mods.morph.common.handler;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.api.ability.IAbilityHandler;
import net.minecraft.entity.EntityLivingBase;

public class AbilityHandler implements IAbilityHandler
{
    private static final AbilityHandler INSTANCE = new AbilityHandler();

    public static AbilityHandler getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        Ability.setAbilityHandlerImpl(INSTANCE);
    }

    @Override
    public void registerAbility(String name, Class<? extends Ability> clz)
    {

    }

    @Override
    public void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability... abilities)
    {

    }

    @Override
    public void removeAbility(Class<? extends EntityLivingBase> entClass, String type)
    {

    }

    @Override
    public boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type)
    {
        return false;
    }

    @Override
    public Ability createNewAbilityByType(String type, String[] arguments)
    {
        return null;
    }
}
