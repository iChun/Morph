package me.ichun.mods.morph.common.mode;

import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.event.MorphEvent;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.Ability;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.biomass.Upgrades;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class DefaultMode implements MorphMode
{
    @Override
    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        //TODO the actual method that isn't a debug function
        if(canMorph(player))
        {
            MorphVariant variant = MorphHandler.INSTANCE.createVariant(living);
            if(canAcquireMorph(player, living, variant)) // we can morph to it
            {
                MorphHandler.INSTANCE.addBiomassAmount(player, getBiomassAmount(player, living));

                MorphHandler.INSTANCE.acquireMorph(player, variant);

                MorphHandler.INSTANCE.spawnAnimation(player, living, false);

//                boolean morphTo = true;
//                if(morphTo) //TODO make a config
//                {
//                    MorphHandler.INSTANCE.morphTo(player, variant);
//                }
            }
        }

/*

        //TODO uncomment this actual method
        if(hasUnlockedBiomass(player) && canAcquireBiomass(player, living))
        {
            boolean alsoAcquiredMorph = false;

            MorphHandler.INSTANCE.addBiomassAmount(player, getBiomassAmount(player, living));

            if(canMorph(player))
            {
                MorphVariant variant = MorphHandler.INSTANCE.createVariant(living);
                if(canAcquireMorph(player, living, variant)) // we can morph to it
                {
                    MorphHandler.INSTANCE.acquireMorph(player, variant);

                    alsoAcquiredMorph = true;
                }
            }

            MorphHandler.INSTANCE.spawnAnimation(player, living, alsoAcquiredMorph);
        }
*/
    }

    @Override
    public boolean canShowMorphSelector(PlayerEntity player)
    {
        return MorphHandler.INSTANCE.getBiomassUpgrade(player, Upgrades.ID_MORPH_ABILITY) != null && MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.selectorFilterType, Morph.configServer.selectorFilterNames);
    }

    @Override
    public boolean canMorph(PlayerEntity player)
    {
        if(!MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.morphFilterType, Morph.configServer.morphFilterNames))
        {
            return false;
        }

        if(MorphHandler.INSTANCE.getBiomassUpgrade(player, Upgrades.ID_MORPH_ABILITY) != null)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);
            if(!info.isMorphed() || info.getMorphProgress(1F) == 1F)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living, @Nullable MorphVariant variant)
    {
        if(variant == null || MinecraftForge.EVENT_BUS.post(new MorphEvent.CanAcquire(player, variant)) || !MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.morphFilterType, Morph.configServer.morphFilterNames))
        {
            return false;
        }

        PlayerMorphData playerMorphData = MorphHandler.INSTANCE.getPlayerMorphData(player);

        return !playerMorphData.containsVariant(variant);
    }

    @Override
    public int getMorphingDuration(PlayerEntity player)
    {
        return Morph.configServer.morphTime;
    }

    @Override
    public ArrayList<Trait<?>> getTraitsForVariant(PlayerEntity player, MorphVariant variant)
    {
        //TODO this
        return new ArrayList<>();
    }

    @Override
    public boolean canUseAbility(PlayerEntity player, Ability<?> ability)
    {
        return true; //TODO calculate the biomass cost
    }

    //BIOMASS STUFF BELOW THIS LINE
    @Override
    public boolean hasUnlockedBiomass(PlayerEntity player)
    {
        return (Morph.configServer.biomassBypassAdvancement || EntityHelper.hasCompletedAdvancement(Morph.Advancements.UNLOCK_BIOMASS, player)) && MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.biomassFilterType, Morph.configServer.biomassFilterNames);
    }

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        if(Morph.configServer.disabledMobsRL.contains(living.getType().getRegistryName()))
        {
            return false;
        }

        if(!MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.biomassFilterType, Morph.configServer.biomassFilterNames))
        {
            return false;
        }

        double weight = getLivingWeight(living);

        double maxAbsorbable = 1D + MorphHandler.INSTANCE.getBiomassUpgradeValue(player, Upgrades.ID_BIOMASS_MAX_MASS_ABSORBABLE);

        return weight < maxAbsorbable;
    }

    @Override
    public double getBiomassAmount(PlayerEntity player, LivingEntity living)
    {
        //Get the player's efficiency level
        double playerEfficiency = MorphHandler.INSTANCE.getBiomassUpgradeValue(player, Upgrades.ID_BIOMASS_EFFICIENCY);

        return getLivingWeight(living) * playerEfficiency;
    }

    public double getLivingWeight(LivingEntity living)
    {
        //Values of densities of meat tend to average around 1000 kg/m^3. This also makes things easier for calculation, I guess
        //Yes, if you're reading this, I did look this up. https://twitter.com/ohaiiChun/status/1408516172228616195
        //For the player
        //Volume: 0.648 m^3
        //Weight: 648 kg
        //Biomass: 194.4 kg (* 0.3, default config)

        //Calculate the volume & weight of the entity
        double volume = living.getWidth() * living.getWidth() * living.getHeight();
        double weight = 1000D * volume;

        MobData data = MobDataHandler.getMobData(living);
        if(data != null)
        {
            if(data.biomassMultiplier != null)
            {
                weight *= data.biomassMultiplier;
            }

            if(data.biomassValueOverride != null)
            {
                weight = data.biomassValueOverride;
            }
        }

        return weight * Morph.configServer.biomassValue;
    }

    @Override
    public String getModeName()
    {
        return "default";
    }
}
