package morph.common.morph;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Cancelable
public class MorphEvent extends PlayerEvent
{
	public final MorphState ms;
	
	public MorphEvent(EntityPlayer player,MorphState ms) 
	{
		super(player);
		
		this.ms = ms;
	}

}
