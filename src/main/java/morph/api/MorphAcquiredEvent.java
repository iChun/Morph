package morph.api;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
