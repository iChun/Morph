package morph.common.ability.tracker;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

import morph.api.Ability;
import morph.common.ability.AbilityPoisonResistance;
import morph.common.entity.EntTracker;

public class AbilityTrackerPoisonResistance extends AbilityTracker{

    public AbilityTrackerPoisonResistance(EntTracker tracker, String ability) {
        super(tracker, ability);
    }

    @Override
    public void initialize() {
        //TODO Write proper initialize function
    }

    @Override
    public void trackAbility() {
        if(hasPoisonEffect(entTracker.trackedEnt)){
            setHasAbility(true);
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
        return hasPoisonEffect(living);
    }
    
    public boolean hasPoisonEffect(EntityLivingBase entity) {
        boolean hasEffect = false;
        if(entity != null) {
            if(entity.isPotionActive(Potion.poison) || entity.isPotionActive(Potion.wither))
                hasEffect = true;
        }
        return hasEffect;
    }

}
