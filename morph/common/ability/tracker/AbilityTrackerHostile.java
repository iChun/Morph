package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.ability.AbilityHostile;
import morph.common.entity.EntTracker;
import net.minecraft.entity.monster.EntityMob;

public class AbilityTrackerHostile extends AbilityTracker 
{
	public AbilityTrackerHostile(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
	}

	@Override
	public void initialize() 
	{
	}

	@Override
	public void trackAbility() 
	{
		if(entTracker.trackedEnt instanceof EntityMob)
		{
	    	setHasAbility(true);
	    	if(entTracker.trackTimer > 5)
	    	{
	    		entTracker.trackTimer = 5;
	    	}
		}
	}
	
	@Override
	public int triesRequired()
	{
		return 1;
	}
	
	@Override
	public void kill()
	{
	}

	@Override
	public Ability createAbility() 
	{
		return new AbilityHostile();
	}

	@Override
	public int trackingTime() 
	{
		return 2; // 20s
	}
}
