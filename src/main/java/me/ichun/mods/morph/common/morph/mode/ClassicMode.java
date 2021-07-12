package me.ichun.mods.morph.common.morph.mode;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ClassicMode implements MorphMode
{
    @Override
    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living)
    {
        //TODO
    }

    @Override
    public boolean canMorph(PlayerEntity player)
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

    @Override
    public boolean canAcquireBiomass(PlayerEntity player, LivingEntity living)
    {
        return false; // no biomass capabilities in classic.
    }

    @Override
    public double getBiomassAmount(PlayerEntity player, LivingEntity living)
    {
        return 0D; // no biomass capabilities in classic.
    }
}
