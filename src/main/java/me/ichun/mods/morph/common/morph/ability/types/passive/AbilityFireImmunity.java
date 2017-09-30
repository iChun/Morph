package me.ichun.mods.morph.common.morph.ability.types.passive;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class AbilityFireImmunity extends Ability
{
	public AbilityFireImmunity()
	{
		iconResource = new ResourceLocation("morph", "textures/icon/fire_immunity.png");
	}

	@Override
	public String getType()
	{
		return "fireImmunity";
	}

	@Override
	public void tick()
	{
		if(!getParent().isImmuneToFire())
		{
			getParent().isImmuneToFire = true;
		}
		getParent().extinguish();
	}

	@Override
	public void kill(ArrayList<Ability> nextAbilities)
	{
		getParent().isImmuneToFire = false;
	}

	@Override
	public Ability clone()
	{
		return new AbilityFireImmunity();
	}

	@Override
	public boolean entityHasAbility(EntityLivingBase living)
	{
		return true;
	}

	@Override
	public boolean isCharacteristic(EntityLivingBase living)
	{
		return true;
	}
}
