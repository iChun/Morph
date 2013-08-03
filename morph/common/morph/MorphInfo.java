package morph.common.morph;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import morph.common.Morph;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class MorphInfo 
{
	public String playerName;
	public EntityLivingBase prevEntInstance;
	
	public EntityLivingBase nextEntInstance;
	
	private boolean morphing; //if true, increase progress
	public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
	
	public MorphInfo(){playerName="";}
	
	public MorphInfo(String name, EntityLivingBase prev, EntityLivingBase next)
	{
		playerName = name;
		
		prevEntInstance = prev;
		
		nextEntInstance = next;
		
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

		if(prevEntInstance != null)
		{
			prevEntInstance.addEntityID(prevTag);
		}
		if(nextEntInstance != null)
		{
			nextEntInstance.addEntityID(nextTag);
		}
		
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
	
	public void writeNBT(NBTTagCompound tag)
	{
		tag.setString("playerName", playerName);
		
		tag.setInteger("dimension", nextEntInstance.dimension);
		
		NBTTagCompound prevTag = new NBTTagCompound();
		NBTTagCompound nextTag = new NBTTagCompound();

		if(prevEntInstance != null)
		{
			prevEntInstance.addEntityID(prevTag);
		}
		if(nextEntInstance != null)
		{
			nextEntInstance.addEntityID(nextTag);
		}
		
		tag.setCompoundTag("prevEntInstance", prevTag);
		tag.setCompoundTag("nextEntInstance", nextTag);
	}
	
	public void readNBT(NBTTagCompound tag)
	{
		playerName = tag.getString("playerName");
		
		World dimension = DimensionManager.getWorld(tag.getInteger("dimension"));
		
		prevEntInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag.getCompoundTag("prevEntInstance"), dimension);
		nextEntInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag.getCompoundTag("nextEntInstance"), dimension);
		
		if(nextEntInstance == null)
		{
			Morph.console("Invalid morph form! Creating generic pig as replacement.", true);
			nextEntInstance = (EntityLivingBase)EntityList.createEntityByName("Pig", dimension);
		}
		
		morphing = true;
		morphProgress = 80;
	}
}
