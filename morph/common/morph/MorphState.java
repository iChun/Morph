package morph.common.morph;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MorphState 
{
	public String playerName;
	public String playerMorph;
	public boolean isFavourite;
	public boolean isRemote;
	
	public EntityLivingBase entInstance;
	
	public String identifier;
	
	public MorphState(World world, String name, String player, NBTTagCompound tag, boolean remote)
	{
		playerName = name;
		playerMorph = player;
		isFavourite = name.equalsIgnoreCase(player);
		isRemote = remote;
		
		if(!player.equalsIgnoreCase(""))
		{
			entInstance = isRemote ? createPlayer(world, player) : new FakePlayer(world, player);
		}
		else if(tag != null)
		{
			entInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag, world);
		}
		
		if(entInstance != null)
		{
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			writeFakeTags(entInstance, fakeTag);
			if(playerMorph.equalsIgnoreCase(""))
			{
				identifier = entInstance.getClass().toString() + entInstance.getEntityName() + fakeTag.toString();
			}
			else
			{
				identifier = entInstance.getClass().toString() + "player_" + playerMorph;
			}
		}
	}

	public NBTTagCompound getTag() 
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setString("playerName", playerName);
		tag.setString("playerMorph", playerMorph);
		tag.setBoolean("isFavourite", isFavourite);
		
		NBTTagCompound tag1 = new NBTTagCompound();
		if(entInstance != null)
		{
			entInstance.writeToNBTOptional(tag1);
			writeFakeTags(entInstance, tag1);
		}
		
		tag.setCompoundTag("entInstanceTag", tag1);
		
		tag.setString("identifier", identifier);
		
		return tag;
	}
	
	public void readTag(World world, NBTTagCompound tag)
	{
		playerName = tag.getString("playerName");
		playerMorph = tag.getString("playerMorph");
		isFavourite = tag.getBoolean("isFavourite");
		
		NBTTagCompound tag1 = tag.getCompoundTag("entInstanceTag");
		
		boolean invalid = false;
		if(playerName.equalsIgnoreCase("") || playerMorph.equalsIgnoreCase("") && tag1.getString("id").equalsIgnoreCase(""))
		{
			invalid = true;
		}
		
		if(!invalid)
		{
			if(!playerMorph.equalsIgnoreCase(""))
			{
				entInstance = isRemote ? createPlayer(world, playerMorph) : new FakePlayer(world, playerMorph);
			}
			else
			{
				entInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag1, world);
			}
			identifier = tag.getString("identifier");
			if(entInstance == null)
			{
				invalid = true;
			}
		}
		if(invalid)
		{
			entInstance = (EntityLivingBase)EntityList.createEntityByName("Pig", world);
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			writeFakeTags(entInstance, fakeTag);
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + fakeTag.toString();
		}
	}
	
	@SideOnly(Side.CLIENT)
	private EntityPlayer createPlayer(World world, String player) 
	{
		return new EntityOtherPlayerMP(world, player);
	}

	public void writeFakeTags(EntityLivingBase living, NBTTagCompound tag)
	{
		tag.setFloat("HealF", Short.MAX_VALUE);
		tag.setShort("Health", (short)Short.MAX_VALUE);
		tag.setShort("HurtTime", (short)0);
		tag.setShort("DeathTime", (short)0);
		tag.setShort("AttackTime", (short)0);
		tag.setTag("ActiveEffects", new NBTTagList());
		tag.setShort("Fire", (short)0);
		tag.setShort("Anger", (short)0);
		tag.setInteger("Age", living.isChild() ? -24000 : 0);
		
		if(living instanceof EntityLiving)
		{
			EntityLiving living1 = (EntityLiving)living;
			
			NBTTagList tagList = new NBTTagList();
			
	        for (int i = 0; i < living1.getLastActiveItems().length; ++i)
	        {
	            tagList.appendTag(new NBTTagCompound());
	        }
	        
			tag.setBoolean("CanPickUpLoot", true);
			tag.setTag("Equipment", tagList);
			tag.setBoolean("Leashed", false);
			tag.setBoolean("PersistenceRequired", true);
		}
	}
	
}
