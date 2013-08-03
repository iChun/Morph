package morph.common.morph;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import morph.common.Morph;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;

public class MorphInfo 
{
	public String playerName;
	public Class prevEntClass;
	public EntityLivingBase prevEntInstance;
	public DataWatcher prevEntDataWatcher;
	
	public Class nextEntClass;
	public EntityLivingBase nextEntInstance;
	public DataWatcher nextEntDataWatcher;
	
	private boolean morphing; //if true, increase progress
	public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
	
	public MorphInfo(String name, EntityLivingBase prev, EntityLivingBase next)
	{
		playerName = name;
		
		prevEntClass = prev.getClass();
		prevEntInstance = prev;
		prevEntDataWatcher = prev.getDataWatcher();
		
		nextEntClass = next.getClass();
		nextEntInstance = next;
		nextEntDataWatcher = next.getDataWatcher();
		
		morphing = false;
		morphProgress = 0;
	}
	
	public void setMorphing(boolean flag)
	{
		morphing = flag;
	}
	
	public boolean getMorphing()
	{
		return morphing;
	}
	
	public Packet250CustomPayload getMorphInfoAsPacket()
	{
		byte isPlayer = (byte)((prevEntInstance instanceof EntityPlayer && nextEntInstance instanceof EntityPlayer) ? 3 : prevEntInstance instanceof EntityPlayer ? 1 : nextEntInstance instanceof EntityPlayer ? 2 : 0);
		
		String username1 = (isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)prevEntInstance).username : "";
		String username2 = (isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)nextEntInstance).username : "";
		
		NBTTagCompound prevTag = new NBTTagCompound();
		NBTTagCompound nextTag = new NBTTagCompound();

		prevEntInstance.addEntityID(prevTag);
		nextEntInstance.addEntityID(nextTag);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try
		{
			stream.writeByte(0); //id
			stream.writeUTF(playerName);
			
			stream.writeBoolean(morphing);
			stream.writeInt(morphProgress);
			
			stream.writeByte(isPlayer);
			stream.writeUTF(username1);
			stream.writeUTF(username2);
			
			Morph.writeNBTTagCompound(prevTag, stream);
			Morph.writeNBTTagCompound(nextTag, stream);
		}
		catch(IOException e)
		{
			
		}
		return new Packet250CustomPayload("Morph", bytes.toByteArray());
	}
	
}
