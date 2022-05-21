package me.ichun.mods.morph.common.mode;

import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.Ability;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CommandMode implements MorphMode
{
    public final boolean allowSelection;

    public CommandMode(boolean allowSelection) {this.allowSelection = allowSelection;}

    @Override
    public void handleMurderEvent(ServerPlayerEntity player, LivingEntity living){}

    @Override
    public boolean canShowMorphSelector(PlayerEntity player)
    {
        return allowSelection && MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.selectorFilterType, Morph.configServer.selectorFilterNames);
    }

    @Override
    public boolean canMorph(PlayerEntity player)
    {
        if(!MorphHandler.INSTANCE.isPlayerAllowed(player, Morph.configServer.morphFilterType, Morph.configServer.morphFilterNames))
        {
            return false;
        }

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
    public boolean canAcquireMorph(PlayerEntity player, LivingEntity living, @Nullable MorphVariant variant)
    {
        return false;
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
        return false;
    }

    @Override
    public double getBiomassAmount(PlayerEntity player, LivingEntity living)
    {
        return 0;
    }

    @Override
    public String getModeName()
    {
        return allowSelection ? "command_allow_selection" : "command_only";
    }
}
