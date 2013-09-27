package morph.common.ability.tracker;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import morph.api.Ability;
import morph.common.ability.AbilityPoisonResistance;
import morph.common.core.EntityHelper;
import morph.common.entity.EntTracker;

public class AbilityTrackerPoisonResistance extends AbilityTracker{

    public AbilityTrackerPoisonResistance(EntTracker tracker, String ability) {
        super(tracker, ability);
    }

    @Override
    public void initialize() {
        if(entTracker.simulated){
            entTracker.trackedEnt.addPotionEffect(new PotionEffect(Potion.poison.id, 1));
        }
    }

    @Override
    public void trackAbility() {
        if(EntityHelper.hasPoisonEffect(entTracker.trackedEnt)){
            setHasAbility(true);
            if(entTracker.trackTimer > 5){
                entTracker.trackTimer = 5;
            }
        }
    }

    @Override
    public Ability createAbility() {
        return new AbilityPoisonResistance();
    }

    @Override
    public int trackingTime() {
        return entTracker.simulated ? 400 : 200; 
    }

    @Override
    public boolean shouldTrack(World worldObj, EntityLivingBase living){
        return EntityHelper.hasPoisonEffect(living);
    }

}
