package me.ichun.mods.morph.api.ability.types.active;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.ResourceLocation;

public class AbilityClimb extends Ability
{
	public AbilityClimb()
	{
		iconResource = new ResourceLocation("morph", "textures/icon/climb.png");
	}

	@Override
	public String getType()
	{
		return "climb";
	}

	@Override
	public void tick()
	{
		if(getParent().isCollidedHorizontally)
		{
            isActive = true;
			getParent().fallDistance = 0.0F;
			if(getParent().isSneaking())
			{
				getParent().motionY = 0.0D;
			}
			else
			{
				getParent().motionY = 0.1176D; //(0.2D - 0.08D) * 0.98D
			}
		}
		if(!getParent().worldObj.isRemote)
		{
            //TODO test this
			double motionX = getParent().posX - getParent().lastTickPosX;
			double motionZ = getParent().posZ - getParent().lastTickPosZ;
			double motionY = getParent().posY - getParent().lastTickPosY - 0.765D; //serverside motion is weird.
			if(motionY > 0.0D && (motionX == 0D || motionZ == 0D))
			{
				//most likely climbing.
				getParent().fallDistance = 0.0F;
                isActive = true;
            }
		}
	}

    @Override
    public boolean isActive()
    {
        return isActive;
    }

    @Override
    public float activeCost()
    {
        return 0.005F;
    }

	@Override
	public Ability clone()
	{
		return new AbilityClimb();
	}
}
