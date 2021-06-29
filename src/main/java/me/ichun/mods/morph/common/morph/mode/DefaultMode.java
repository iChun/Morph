package me.ichun.mods.morph.common.morph.mode;

import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

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

                boolean morphTo = true;
                if(morphTo) //TODO make a config
                {
                    MorphHandler.INSTANCE.morphTo(player, variant);
                }
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
