package morph.common.morph;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			fakeTag.setFloat("HealF", entInstance.func_110138_aP());
			fakeTag.setShort("Health", (short)entInstance.func_110138_aP());
			fakeTag.setShort("HurtTime", (short)0);
			fakeTag.setShort("DeathTime", (short)0);
			fakeTag.setShort("AttackTime", (short)0);
			fakeTag.setTag("ActiveEffects", new NBTTagList());
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + fakeTag.toString();
		}
	}

	public NBTTagCompound getTag() 
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setString("playerName", playerName);
		tag.setString("playerMorph", playerMorph);
		
		NBTTagCompound tag1 = new NBTTagCompound();
		entInstance.addEntityID(tag1);
		tag1.setFloat("HealF", entInstance.func_110138_aP());
		tag1.setShort("Health", (short)entInstance.func_110138_aP());
		tag1.setShort("HurtTime", (short)0);
		tag1.setShort("DeathTime", (short)0);
		tag1.setShort("AttackTime", (short)0);
		tag1.setTag("ActiveEffects", new NBTTagList());
		
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
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			fakeTag.setFloat("HealF", entInstance.func_110138_aP());
			fakeTag.setShort("Health", (short)entInstance.func_110138_aP());
			fakeTag.setShort("HurtTime", (short)0);
			fakeTag.setShort("DeathTime", (short)0);
			fakeTag.setShort("AttackTime", (short)0);
			fakeTag.setTag("ActiveEffects", new NBTTagList());
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + fakeTag.toString();
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
