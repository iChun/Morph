package morph.common.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandler 
{

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onSoundLoad(SoundLoadEvent event)
	{
		for(int i = 1; i <= 6; i++)
		{
			event.manager.soundPoolSounds.addSound("morph:morph" + i + ".ogg");
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderEntity(RenderLivingEvent.Pre event)
	{
		if(event.entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.entity;
			
			if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(player.username))
			{
				
			}
		}
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.target instanceof EntityLivingBase)
		{
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "morph:morph", 1.0F, 1.0F);
			//event.entityPlayer event.target
			
			MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(event.entityPlayer.username);
			
			if(info == null)
			{
				info = new MorphInfo((EntityLivingBase)event.target, event.entityPlayer);
			}
			else if(info.getMorphing())
			{
				return;
			}
			
			MorphInfo info2 = new MorphInfo(info.nextEntInstance, (EntityLivingBase)event.target);
			info2.setMorphing(true);
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(event.entityPlayer.username, info2);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);
			try
			{
				stream.writeUTF(event.entityPlayer.username);
				
				stream.writeUTF(info.nextEntClass.getName());
				stream.writeUTF(event.target.getClass().getName());
				
				PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()));
			}
			catch(IOException e)
			{
				
			}
		}
	}
	
}
