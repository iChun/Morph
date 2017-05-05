package me.ichun.mods.morph.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class MorphAcquiredEvent extends PlayerEvent
{
    public final EntityLivingBase acquiredMorph;

    public MorphAcquiredEvent(EntityPlayer player, EntityLivingBase acquired)
    {
        super(player);
        acquiredMorph = acquired;
    }
}
