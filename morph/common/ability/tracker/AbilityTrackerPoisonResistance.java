package morph.common.ability.tracker;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import morph.api.Ability;
import morph.common.ability.AbilityPoisonResistance;
import morph.common.entity.EntTracker;

public class AbilityTrackerPoisonResistance extends AbilityTracker{
	
	public float entHealth;

    public AbilityTrackerPoisonResistance(EntTracker tracker, String ability) {
        super(tracker, ability);
    }

    @Override
    public void initialize() {
    	entHealth = entTracker.trackedEnt.getHealth();
        if(entTracker.simulated){
            entTracker.trackedEnt.addPotionEffect(new PotionEffect(Potion.poison.id, 100)); // duration can't be 1. Not long enough.
        }
    }

    @Override
    public void trackAbility() {
        if(entTracker.trackTimer == 20 && (entTracker.trackedEnt.isPotionActive(Potion.poison) || entTracker.trackedEnt.getHealth() == entHealth)){
        	setHasAbility(true);
            entTracker.trackTimer = 5;
        }
    }

    @Override
    public Ability createAbility() {
        return new AbilityPoisonResistance();
    }

    @Override
    public int trackingTime() {
        return 80; 
    }

}
