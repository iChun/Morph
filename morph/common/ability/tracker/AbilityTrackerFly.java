package morph.common.ability.tracker;

import morph.api.Ability;
import morph.common.ability.AbilityFly;
import morph.common.entity.EntTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.FakePlayer;

public class AbilityTrackerFly extends AbilityTracker 
{
	public int maxTimeAirborne;
	public int timeAirborne;
	public double airborneHeight;
	public boolean airborne;
	
	public AbilityTrackerFly(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
		maxTimeAirborne = 0;
		timeAirborne = 0;
	}

	@Override
	public void initialize() 
	{
		if(entTracker.simulated)
		{
			entTracker.trackedEnt.setPosition(entTracker.trackedEnt.posX, entTracker.trackedEnt.posY + 2D, entTracker.trackedEnt.posZ);
		}
	}

	@Override
	public void trackAbility() 
	{
		if(entTracker.trackTimer == 200 && entTracker.simulated)
		{
			EntityPlayer player = new FakePlayer(entTracker.trackedEnt.worldObj, "MorphEntTracker");
			player.setLocationAndAngles(entTracker.trackedEnt.posX + entTracker.trackedEnt.getRNG().nextDouble() * 10D - 5D, entTracker.trackedEnt.boundingBox.maxY + entTracker.trackedEnt.getRNG().nextDouble() * 4D, entTracker.trackedEnt.posZ + entTracker.trackedEnt.getRNG().nextDouble() * 10D - 5D, 0.0F, 0.0F);
			entTracker.trackedEnt.attackEntityFrom(DamageSource.causePlayerDamage(player), 1);
		}
		if(!airborne && !entTracker.trackedEnt.onGround && !entTracker.trackedEnt.isCollidedHorizontally)
		{
			timeAirborne = 0;
			airborneHeight = entTracker.trackedEnt.posY;
		}
		if(airborne)
		{
			timeAirborne++;
			if(timeAirborne > maxTimeAirborne && entTracker.trackedEnt.motionY > -0.15D)
			{
				maxTimeAirborne = timeAirborne;
			}
		}
		if(maxTimeAirborne > 60 && airborneHeight < entTracker.trackedEnt.posY + 1.5D && timeAirborne > 3)
		{
			setHasAbility(true);
			if(entTracker.trackTimer > 205)
			{
				entTracker.trackTimer = 205;
			}
			else
			{
				entTracker.trackTimer = 5;
			}
		}
		airborne = !entTracker.trackedEnt.onGround && !entTracker.trackedEnt.isCollidedHorizontally;
	}

	@Override
	public Ability createAbility() 
	{
		return new AbilityFly();
	}
	
	@Override
	public int triesRequired()
	{
		return 10;
	}

	@Override
	public int trackingTime() 
	{
		return entTracker.simulated ? 400 : 200; // 20s
	}

}
