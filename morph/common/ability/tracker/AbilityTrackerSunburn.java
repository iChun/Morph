package morph.common.ability.tracker;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import morph.api.Ability;
import morph.common.ability.AbilitySunburn;
import morph.common.entity.EntTracker;

public class AbilityTrackerSunburn extends AbilityTracker 
{
	public AbilityTrackerSunburn(EntTracker tracker, String ability) 
	{
		super(tracker, ability);
	}

	@Override
	public void initialize() 
	{
		if(entTracker.simulated)
		{
			entTracker.trackedEnt.setCurrentItemOrArmor(4, null);
		}
	}

	@Override
	public void trackAbility() 
	{
        if (entTracker.trackedEnt.worldObj.isDaytime() && !entTracker.trackedEnt.worldObj.isRemote && !entTracker.trackedEnt.isChild())
        {
            float f = entTracker.trackedEnt.getBrightness(1.0F);

            if (f > 0.5F && entTracker.trackedEnt.worldObj.canBlockSeeTheSky(MathHelper.floor_double(entTracker.trackedEnt.posX), MathHelper.floor_double(entTracker.trackedEnt.posY), MathHelper.floor_double(entTracker.trackedEnt.posZ)))
            {
                boolean flag = true;
                ItemStack itemstack = entTracker.trackedEnt.getCurrentItemOrArmor(4);

                if (itemstack == null && flag && entTracker.trackedEnt.isBurning())
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
	public Ability createAbility() 
	{
		return new AbilitySunburn();
	}

	@Override
	public int trackingTime() 
	{
		return entTracker.simulated ? 100 : 200; // 20s
	}

	@Override
	public boolean shouldTrack(World worldObj, EntityLivingBase living)
	{
		return worldObj.isDaytime() && !living.isChild();
	}
}
