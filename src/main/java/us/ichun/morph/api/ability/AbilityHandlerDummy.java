package us.ichun.morph.api.ability;

import net.minecraft.entity.EntityLivingBase;

public class AbilityHandlerDummy implements IAbilityHandler
{
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
    public Ability createNewAbilityByType(String type, String...arguments)
    {
        return null;
    }
}
