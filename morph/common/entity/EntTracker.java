package morph.common.entity;

import morph.common.ability.AbilityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class EntTracker 
{
	public final EntityLivingBase trackedEnt;
	public final boolean shouldKill;

	public AbilityTracker abilityTracker;
	public int trackTimer;
	public boolean ticking;
	
	public EntTracker(EntityLivingBase tracking, String type, boolean kill)
	{
		trackedEnt = tracking;
		shouldKill = kill;
		
		abilityTracker = AbilityTracker.createTracker(this, type);
		trackTimer = abilityTracker.trackingTime();
		
		ticking = true;
	}
	
	public void tick()
	{
		if(!trackedEnt.isEntityAlive() || trackTimer < 0)
		{
			ticking = false;
		}
		trackTimer--;
		abilityTracker.trackAbility();
	}
	
	public void kill()
	{
		if(shouldKill)
		{
			trackedEnt.setDead();
		}
		abilityTracker.kill();
	}
	
	public boolean shouldTick()
	{
		return ticking;
	}
}
