package morph.common.morph;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import morph.common.Morph;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class MorphInfo 
{
	public String playerName;
	public MorphState prevState;
	
	public MorphState nextState;
	
	private boolean morphing; //if true, increase progress
	public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
	
	public MorphInfo(){playerName="";}
	
	public MorphInfo(String name, MorphState prev, MorphState next)
	{
		playerName = name;
		
		prevState = prev;
		
		nextState = next;
		
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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try
		{
			stream.writeByte(0); //id
			stream.writeUTF(playerName);
			
			stream.writeBoolean(morphing);
			stream.writeInt(morphProgress);

			stream.writeBoolean(prevState != null);
			if(prevState != null)
			{
				Morph.writeNBTTagCompound(prevState.getTag(), stream);
			}
			stream.writeBoolean(nextState != null);
			if(nextState != null)
			{
				Morph.writeNBTTagCompound(nextState.getTag(), stream);
			}
		}
		catch(IOException e)
		{
			
		}
		return new Packet250CustomPayload("Morph", bytes.toByteArray());
	}
	
	public void writeNBT(NBTTagCompound tag)
	{
		tag.setString("playerName", playerName);
		
		tag.setInteger("dimension", nextState.entInstance.dimension);
		
		tag.setCompoundTag("nextState", nextState.getTag());
	}
	
	public void readNBT(NBTTagCompound tag)
	{
		playerName = tag.getString("playerName");
		
		World dimension = DimensionManager.getWorld(tag.getInteger("dimension"));
		
		nextState = new MorphState(dimension, playerName, playerName, null, false);
		
		nextState.readTag(dimension, tag.getCompoundTag("nextState"));
		
		morphing = true;
		morphProgress = 80;
	}
}

