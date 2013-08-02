package morph.common.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
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
	public void onRenderEntity(RenderPlayerEvent.Pre event)
	{
		if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.username))
		{
			
		}
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.target instanceof EntityLivingBase)
		{
			MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(event.entityPlayer.username);
			
			
			if(!(event.target.addEntityID(new NBTTagCompound()) && !(event.target instanceof EntityPlayer) || event.target instanceof EntityPlayer))
			{
				System.out.println("stop");
				return;
			}
			
			if(info == null)
			{
				info = new MorphInfo((EntityLivingBase)event.target, event.entityPlayer);
			}
			else if(info.getMorphing() || info.nextEntInstance == event.target)
			{
				System.out.println("stop1");
				return;
			}
			
			byte isPlayer = (byte)((info.nextEntInstance instanceof EntityPlayer && event.target instanceof EntityPlayer) ? 3 : info.nextEntInstance instanceof EntityPlayer ? 1 : event.target instanceof EntityPlayer ? 2 : 0);
			
			if(!(info.nextEntInstance instanceof EntityPlayer) && !info.nextEntInstance.addEntityID(new NBTTagCompound()))
			{
				System.out.println("stop2");
				return;
			}
			
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "morph:morph", 1.0F, 1.0F);
			
			MorphInfo info2 = new MorphInfo(info.nextEntInstance, (EntityLivingBase)event.target);
			info2.setMorphing(true);
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(event.entityPlayer.username, info2);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);
			try
			{
				stream.writeByte(0); //id
				stream.writeUTF(event.entityPlayer.username);
				
				stream.writeByte(isPlayer);
				stream.writeUTF((isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)info.nextEntInstance).username : "");
				stream.writeUTF((isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)event.target).username : "");
				
				NBTTagCompound prevTag = new NBTTagCompound();
				NBTTagCompound nextTag = new NBTTagCompound();

				info.nextEntInstance.addEntityID(prevTag);
				event.target.addEntityID(nextTag);
				
				Morph.writeNBTTagCompound(prevTag, stream);
				Morph.writeNBTTagCompound(nextTag, stream);
				
				PacketDispatcher.sendPacketToAllPlayers(new Packet250CustomPayload("Morph", bytes.toByteArray()));
			}
			catch(IOException e)
			{
				
			}
		}
	}
	
}
