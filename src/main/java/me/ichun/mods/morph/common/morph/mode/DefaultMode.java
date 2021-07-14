package me.ichun.mods.morph.common.morph.mode;

import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.packet.PacketAcquisition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.PacketDistributor;

public class DefaultMode implements MorphMode
{
    @Override
    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        if(canMorph(player) && canAcquireMorph(player, living))
        {
            MorphVariant variant = MorphHandler.INSTANCE.createVariant(living);
            if(variant != null) // we can morph to it
            {
                MorphHandler.INSTANCE.acquireMorph(player, variant);

                //TODO if player is invisible?

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
        return true; //TODO bind to upgrades
    }

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
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

        //Calculate the volume of the entity
        double volume = living.getWidth() * living.getWidth() * living.getHeight();

        double weight = 1000D * volume;

        //TODO biomass efficiency upgrades
        double finalBiomass = weight * Morph.configServer.biomassValue;

        return finalBiomass;
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living)
    {
        return true;
    }

    @Override
    public int getMorphingDuration(PlayerEntity player)
    {
        return Morph.configServer.morphTime;
    }
}
