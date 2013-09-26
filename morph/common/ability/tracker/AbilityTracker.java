package morph.common.ability.tracker;

import java.util.HashMap;

import morph.api.Ability;
import morph.common.entity.EntTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public abstract class AbilityTracker 
{
	public final String abilityTracked;
	public final EntTracker entTracker;
	
	private boolean hasAbility;
	
	public int posXUsed;
	public int posZUsed;
	
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
	
	public void kill() 
	{
		for(int i = -3; i <= 3; i++)
		{
			for(int k = -3; k <= 3; k++)
			{
				for(int j = 0; j <= 6; j++)
				{
					entTracker.trackedEnt.worldObj.setBlockToAir(posXUsed + i, 245 + j, posZUsed + k);
				}
			}
		}
	}
	
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
	
	public boolean shouldTrack(World worldObj, EntityLivingBase living)
	{
		return true;
	}
	
	public int triesRequired()
	{
		return 5;
	}
	
	public final static String[] trackableAbilities = new String[] { "fly", "float", "water", "fireImmunity", "sunburn", "hostile", "climb" };
	public static HashMap<String, Class> trackerClasses = new HashMap<String, Class>();
	
	static
	{
		trackerClasses.put("fly", AbilityTrackerFly.class);
		trackerClasses.put("float", AbilityTrackerFloat.class);
		trackerClasses.put("water", AbilityTrackerWaterTests.class);
		trackerClasses.put("fireImmunity", AbilityTrackerFireImmunity.class);
		trackerClasses.put("sunburn", AbilityTrackerSunburn.class);
		trackerClasses.put("hostile", AbilityTrackerHostile.class);
		trackerClasses.put("climb", AbilityTrackerClimb.class);
	}
}
