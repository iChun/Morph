package morph.common.ability.tracker;

import java.util.HashMap;

import morph.api.Ability;
import morph.common.entity.EntTracker;

public abstract class AbilityTracker 
{
	public final String abilityTracked;
	public final EntTracker entTracker;
	
	private boolean hasAbility;
	
	public AbilityTracker(EntTracker tracker, String ability)
	{
		entTracker = tracker;
		abilityTracked = ability;
		hasAbility = false;
	}

	public abstract void initialize();
	public abstract void trackAbility();
	public abstract Ability createAbility();
	public abstract int trackingTime();
	
	public void kill() {}
	
	public void setHasAbility(boolean flag)
	{
		hasAbility = flag;
	}
	
	public boolean hasAbility()
	{
		return hasAbility;
	}
	
	public static AbilityTracker createTracker(EntTracker tracker, String type)
	{
		Class clz = trackerClasses.get(type);
		if(clz != null)
		{
			try
			{
				AbilityTracker abilityTracker = (AbilityTracker)clz.getConstructor(EntTracker.class, String.class).newInstance(tracker, type);
				return abilityTracker;
			}
			catch(Exception e)
			{
				
			}
		}
		return new AbilityTrackerDummy(tracker, type);
	}
	
	public static HashMap<String, Class> trackerClasses = new HashMap<String, Class>();
	
	static
	{
		//TODO complete maps
		trackerClasses.put("fly", AbilityTrackerFly.class);
		trackerClasses.put("float", AbilityTrackerFloat.class);
		trackerClasses.put("fireImmunity", AbilityTrackerFireImmunity.class);
		trackerClasses.put("water", AbilityTrackerWaterTests.class);
	}
}
