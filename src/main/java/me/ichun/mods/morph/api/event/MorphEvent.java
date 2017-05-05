package me.ichun.mods.morph.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class MorphEvent extends PlayerEvent
{
    //Can be null
    public final EntityLivingBase prevMorph;

    //Will never be null
    public final EntityLivingBase morph;

    public MorphEvent(EntityPlayer player, EntityLivingBase prevMorph, EntityLivingBase morph)
    {
        super(player);
        this.prevMorph = prevMorph;
        this.morph = morph;
    }
}
