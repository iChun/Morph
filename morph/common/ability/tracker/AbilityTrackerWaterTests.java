package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.ability.AbilitySwim;
import morph.common.ability.AbilityWaterAllergy;
import morph.common.entity.EntTracker;
import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;

public class AbilityTrackerWaterTests extends AbilityTracker 
{
	
	public int posXUsed;
	public int posZUsed;
	
	public float entHealth;
	public boolean waterAllergy;
	public boolean landBreather;
	public boolean waterBreather;
	
	public AbilityTrackerWaterTests(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
	}

	@SuppressWarnings("unused")
	@Override
	public void initialize() 
	{
		if(entTracker.simulated)
		{
			ChunkCoordinates chunk = entTracker.trackedEnt.worldObj.getSpawnPoint();
			
			boolean success = false;
			
			int posX = chunk.posX;
			int posZ = chunk.posZ;

			for(int tries = 0; tries < 3; tries++)
			{
				posX = chunk.posX;
				posZ = chunk.posZ;
				
				posX += entTracker.trackedEnt.worldObj.rand.nextInt(200) - 100;
				posZ += entTracker.trackedEnt.worldObj.rand.nextInt(200) - 100;
				
				for(int i = -3; i <= 3; i++)
				{
					for(int k = -3; k <= 3; k++)
					{
						for(int j = 0; j <= 6; j++)
						{
							if(!entTracker.trackedEnt.worldObj.isAirBlock(posX + i, 245 + j, posZ + k))
							{
								continue;
							}
						}
					}
				}
				
				for(int i = -3; i <= 3; i++)
				{
					for(int k = -3; k <= 3; k++)
					{
						for(int j = 0; j <= 6; j++)
						{
							if(Math.abs(i) == 3 || Math.abs(k) == 3 || j == 0 || j == 6)
							{
								entTracker.trackedEnt.worldObj.setBlock(posX + i, 245 + j, posZ + k, j == 6 ? Block.leaves.blockID : Block.glass.blockID);
							}
							else
							{
								entTracker.trackedEnt.worldObj.setBlock(posX + i, 245 + j, posZ + k, Block.waterMoving.blockID);
							}
						}
					}
				}
				success = true;
				break;
			}
			
			if(success)
			{
				posXUsed = posX;
				posZUsed = posZ;
				entTracker.trackedEnt.setPosition(posX + 0.5D, 246.1D, posZ + 0.5D);
				entHealth = entTracker.trackedEnt.getHealth();
			}
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
