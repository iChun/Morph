package morph.common.morph;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MorphState 
{
	public String playerName;
	public String playerMorph;
	public boolean isRemote;
	
	public EntityLivingBase entInstance;
	
	public String identifier;
	
	public MorphState(World world, String name, String player, NBTTagCompound tag, boolean remote)
	{
		playerName = name;
		playerMorph = player;
		isRemote = remote;
		
		if(!player.equalsIgnoreCase(""))
		{
			entInstance = !isRemote ? new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), world, player, new ItemInWorldManager(world)) : new EntityOtherPlayerMP(world, player);
		}
		else if(tag != null)
		{
			entInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag, world);
		}
		
		if(entInstance != null)
		{
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + entInstance.getDataWatcher().toString();
		}
	}

	public NBTTagCompound getTag() 
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setString("playerName", playerName);
		tag.setString("playerMorph", playerMorph);
		
		NBTTagCompound tag1 = new NBTTagCompound();
		entInstance.addEntityID(tag1);
		
		tag.setCompoundTag("entInstanceTag", tag1);
		
		tag.setString("identifier", identifier);
		
		return tag;
	}
	
	public void readTag(World world, NBTTagCompound tag)
	{
		playerName = tag.getString("playerName");
		
		boolean invalid = false;
		if(playerName.equalsIgnoreCase(""))
		{
			invalid = true;
		}
		
		playerMorph = tag.getString("playerMorph");
		
		if(invalid)
		{
			entInstance = (EntityLivingBase)EntityList.createEntityByName("Pig", world);
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + entInstance.getDataWatcher().toString();
		}
		else
		{
			NBTTagCompound tag1 = tag.getCompoundTag("entInstanceTag");
			
			if(!playerMorph.equalsIgnoreCase(""))
			{
				entInstance = !isRemote ? new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), world, playerMorph, new ItemInWorldManager(world)) : new EntityOtherPlayerMP(world, playerMorph);
			}
			else
			{
				entInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag1, world);
			}
			identifier = tag.getString("identifier");
		}
	}
	
}
