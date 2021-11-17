package me.ichun.mods.morph.common.morph.mode;

import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.biomass.Upgrades;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketAcquisition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class DefaultMode implements MorphMode
{
    @Override
    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        if(canMorph(player))
        {
            MorphVariant variant = MorphHandler.INSTANCE.createVariant(living);
            if(canAcquireMorph(player, living, variant)) // we can morph to it
            {
                MorphHandler.INSTANCE.acquireMorph(player, variant);

                MorphHandler.INSTANCE.addBiomassAmount(player, getBiomassAmount(player, living));
                Morph.channel.sendTo(new PacketAcquisition(player.getEntityId(), living.getEntityId(), false), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));

//                boolean morphTo = true;
//                if(morphTo) //TODO make a config
//                {
//                    MorphHandler.INSTANCE.morphTo(player, variant);
//                }
            }
        }
    }

    @Override
    public boolean canMorph(PlayerEntity player)
    {
        return MorphHandler.INSTANCE.getBiomassUpgrade(player, Upgrades.ID_MORPH_ABILITY) != null;
    }

    @Override
    public boolean hasUnlockedBiomass(PlayerEntity player)
    {
        return Morph.configServer.biomassBypassAdvancement || EntityHelper.hasCompletedAdvancement(Morph.Advancements.UNLOCK_BIOMASS, player);
    }

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        //TODO blacklist! (Should I just use the amount?? maybe that might cut down our functions/calls??
        return true;
    }

    @Override
    public double getBiomassAmount(PlayerEntity player, LivingEntity living)
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

        //TODO mob modifier
        //Get the player's efficiency level
        double playerEfficiency = MorphHandler.INSTANCE.getBiomassUpgradeValue(player, Upgrades.ID_BIOMASS_EFFICIENCY);

        double finalBiomass = weight * playerEfficiency * Morph.configServer.biomassValue;

        return finalBiomass;
    }

    @Override
    public boolean isClassicMode()
    {
        return false;
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living, @Nullable MorphVariant variant)
    {
        if(variant == null)
        {
            return false;
        }

        PlayerMorphData playerMorphData = MorphHandler.INSTANCE.getPlayerMorphData(player);

        return !playerMorphData.containsVariant(variant);
        //TODO an upgrade that allows you to acquire larger and larger mobs?? same with biomass
    }

    @Override
    public int getMorphingDuration(PlayerEntity player)
    {
        return Morph.configServer.morphTime;
    }
}
