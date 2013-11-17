package morph.common.ability.tracker;

import net.minecraft.entity.Entity;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import morph.api.Ability;
import morph.common.ability.AbilityFireImmunity;
import morph.common.core.ObfHelper;
import morph.common.entity.EntTracker;

public class AbilityTrackerFireImmunity extends AbilityTracker 
{

	private boolean immune;
	
	public AbilityTrackerFireImmunity(EntTracker tracker, String ability) 
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
		try
		{
			if((Boolean)ObfuscationReflectionHelper.getPrivateValue(Entity.class, entTracker.trackedEnt, ObfHelper.isImmuneToFire))
			{
				setHasAbility(true);
			}
		}
		catch(Exception e)
		{
			
		}
	}

	@Override
	public int triesRequired()
	{
		return 1;
	}

	@Override
	public Ability createAbility() 
	{
		return new AbilityFireImmunity();
	}

	@Override
	public int trackingTime() 
	{
		return 2;
	}

}
