package me.ichun.mods.morph.common.morph.ability.types.passive;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class AbilityFallNegate extends Ability
{
	public AbilityFallNegate()
	{
		iconResource = new ResourceLocation("morph", "textures/icon/fall_negate.png");
	}

	@Override
	public String getType() 
	{
		return "fallNegate";
	}

	@Override
	public void tick() 
	{
		getParent().fallDistance = -0.5F;
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFallNegate();
	}

    @Override
    public boolean isCharacteristic(EntityLivingBase living)
    {
        return true;
    }
}
