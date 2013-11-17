package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.entity.EntTracker;

public class AbilityTrackerDummy extends AbilityTracker 
{

	public AbilityTrackerDummy(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
	}

	@Override
	public void initialize() {}

	@Override
	public void trackAbility() {}

	@Override
	public Ability createAbility() { return null; }

	@Override
	public int trackingTime() 
	{
		return 5;
	}

}
