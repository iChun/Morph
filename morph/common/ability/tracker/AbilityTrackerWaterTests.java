package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.ability.AbilitySwim;
import morph.common.ability.AbilityWaterAllergy;
import morph.common.entity.EntTracker;
import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;

public class AbilityTrackerWaterTests extends AbilityTracker 
{
	
	public float entHealth;
	public boolean waterAllergy;
	public boolean landBreather;
	public boolean waterBreather;
	
	public AbilityTrackerWaterTests(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
	}

	@Override
	public void initialize() 
	{
		if(entTracker.simulated)
		{
			for(int i = -3; i <= 3; i++)
			{
				for(int k = -3; k <= 3; k++)
				{
					for(int j = 0; j <= 6; j++)
					{
						if(Math.abs(i) == 3 || Math.abs(k) == 3 || j == 0 || j == 6)
						{
							entTracker.trackedEnt.worldObj.setBlock(posXUsed + i, 245 + j, posZUsed + k, j == 6 ? Block.leaves.blockID : Block.glass.blockID);
						}
						else
						{
							entTracker.trackedEnt.worldObj.setBlock(posXUsed + i, 245 + j, posZUsed + k, Block.waterMoving.blockID);
						}
					}
				}
			}
			
			entHealth = entTracker.trackedEnt.getHealth();
		}
	}

	@Override
	public void trackAbility() 
	{
		if(entTracker.simulated)
		{
			entTracker.trackedEnt.setPosition(posXUsed + 0.5D, waterBreather ? 252.1D : 246.1D, posZUsed + 0.5D);
		}
		
		if(entTracker.trackedEnt.isWet())
		{
			if(entTracker.trackedEnt.getHealth() < entHealth)
			{
				setHasAbility(true);
				waterAllergy = true;
			}
			else if(trackingTime() - entTracker.trackTimer > 10 && entTracker.trackedEnt.getAir() == 300)
			{
				setHasAbility(true);
				waterBreather = true;
			}
		}
		else if(trackingTime() - entTracker.trackTimer > 10)
		{
			if(entTracker.trackedEnt.getAir() < 300)
			{
				landBreather = false;
			}
			else
			{
				landBreather = true;
			}
		}
	}
	
	@Override
	public void kill()
	{
		if(trackingTime() - entTracker.trackTimer > (trackingTime() - 10) && entTracker.simulated)
		{
			for(int i = -3; i <= 3; i++)
			{
				for(int k = -3; k <= 3; k++)
				{
					for(int j = 0; j <= 6; j++)
					{
						entTracker.trackedEnt.worldObj.setBlock(posXUsed + i, 245 + j, posZUsed + k, 0);
					}
				}
			}
		}
	}

	@Override
	public Ability createAbility() 
	{
		if(waterAllergy)
		{
			return new AbilityWaterAllergy();
		}
		else
		{
			return new AbilitySwim(landBreather);
		}
	}

	@Override
	public int trackingTime() 
	{
		return entTracker.simulated ? 60 : 200;
	}

}
