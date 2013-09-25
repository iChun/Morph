package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.ability.AbilityClimb;
import morph.common.entity.EntTracker;
import net.minecraft.block.Block;

public class AbilityTrackerClimb extends AbilityTracker 
{
	
	public int timeClimbing;
	
	public AbilityTrackerClimb(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
		timeClimbing = 0;
	}

	@Override
	public void initialize() 
	{
		if(entTracker.simulated)
		{
			for(int i = -3; i <= 3; i++)
			{
				for(int j = 0; j < 5; j++)
				{
					entTracker.trackedEnt.worldObj.setBlock(posXUsed + i, 246 + j, posZUsed - 3, Block.glass.blockID);
				}
			}
		}
	}

	@Override
	public void trackAbility() 
	{
		if(entTracker.simulated)
		{
			entTracker.trackedEnt.rotationYaw = 180F;
			entTracker.trackedEnt.moveEntityWithHeading(0.0F, 1.0F);
			if(entTracker.trackedEnt.isCollidedHorizontally && entTracker.trackedEnt.motionY > 0.1D)
			{
				timeClimbing++;
				if(timeClimbing >= 10)
				{
					setHasAbility(true);
			    	if(entTracker.trackTimer > 5)
			    	{
			    		entTracker.trackTimer = 5;
			    	}
				}
			}
		}
	}
	
	@Override
	public void kill()
	{
		if(entTracker.simulated)
		{
			for(int i = -3; i <= 3; i++)
			{
				for(int j = 0; j < 5; j++)
				{
					entTracker.trackedEnt.worldObj.setBlockToAir(posXUsed + i, 246 + j, posZUsed - 3);
				}
			}
		}
	}

	@Override
	public Ability createAbility() 
	{
		return new AbilityClimb();
	}

	@Override
	public int trackingTime() 
	{
		return entTracker.simulated ? 160 : 200; // 20s
	}
}
