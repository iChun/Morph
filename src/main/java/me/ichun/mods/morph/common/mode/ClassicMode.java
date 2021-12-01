package me.ichun.mods.morph.common.mode;

import me.ichun.mods.morph.api.event.MorphEvent;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.Ability;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ClassicMode implements MorphMode
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

                MorphHandler.INSTANCE.spawnAnimation(player, living, true);
            }
        }
    }

    @Override
    public boolean canShowMorphSelector(PlayerEntity player)
    {
        return true;
    }

    @Override
    public boolean canMorph(PlayerEntity player)
    {
        MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);
        if(info.isMorphed())
        {
            if(info.getMorphProgress(1F) < 1F) //mid morphing
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living, @Nullable MorphVariant variant) //variant should be the MorphVariant of the EntityLiving we're trying to acquire
    {
        if(variant == null || MinecraftForge.EVENT_BUS.post(new MorphEvent.CanAcquire(player, variant)))
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
        ArrayList<Trait<?>> traits = new ArrayList<>();

        MobData mobData = MobDataHandler.getMobData(variant.id);

        if(mobData != null && mobData.traits != null)
        {
            for(Trait<?> trait : mobData.traits)
            {
                if(trait != null && !Morph.configServer.disabledTraits.contains(trait.type) && trait.upgradeFor == null) //no trait upgrades in classic
                {
                    traits.add(trait.copy());
                }
            }

            for(Trait<?> trait : traits)
            {
                trait.player = player;
                trait.stateTraits = traits;
            }
        }

        return traits;
    }

    @Override
    public boolean canUseAbility(PlayerEntity player, Ability<?> ability)
    {
        return true;
    }

    @Override
    public boolean hasUnlockedBiomass(PlayerEntity player)
    {
        return false;
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

    @Override
    public String getModeName()
    {
        return "classic";
    }
}
