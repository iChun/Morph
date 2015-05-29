package us.ichun.morph.api.ability;

import net.minecraft.entity.EntityLivingBase;

public interface IAbilityHandler
{
    public void registerAbility(String name, Class<? extends Ability> clz);

    public void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities);

    public void removeAbility(Class<? extends EntityLivingBase> entClass, String type);

    public boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type);

    public Ability createNewAbilityByType(String type, String[] arguments);
}
