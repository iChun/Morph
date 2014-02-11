package morph.common.morph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.common.Morph;
import morph.common.core.ObfHelper;
import morph.common.morph.mod.NBTStripper;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MorphState 
	implements Comparable
{
	public final int NBT_PROTOCOL = 1;
	
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
				identifier = entInstance.getClass().toString() + entInstance.getEntityName() + parseTag(fakeTag);
			}
			else
			{
				identifier = "playerMorphState::player_" + playerMorph;
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
		isFavourite = tag.getBoolean("isFavourite") || playerName.equals(playerMorph);
		
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
				identifier = "playerMorphState::player_" + playerMorph;
			}
			else
			{
				entInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag1, world);
				identifier = tag.getString("identifier");
				if(!tag1.hasKey("MorphNBTProtocolNumber") && entInstance != null)
				{
					//Assume updating from pre 0.7.0
					tag1.setInteger("MorphNBTProtocolNumber", NBT_PROTOCOL);//changed everytime the identifier may change or requires a change.
					identifier = entInstance.getClass().toString() + entInstance.getEntityName() + parseTag(tag1);
					
					tag1.setString("identifier", identifier);
				}
				if(tag1.getInteger("MorphNBTProtocolNumber") < NBT_PROTOCOL)
				{
					identifier = "";
					invalid = true;
				}
			}
			if(entInstance == null)
			{
				invalid = true;
			}
			else if(identifier.equalsIgnoreCase(""))
			{
				NBTTagCompound fakeTag = new NBTTagCompound();
				entInstance.writeEntityToNBT(fakeTag);
				writeFakeTags(entInstance, fakeTag);
				identifier = entInstance.getClass().toString() + entInstance.getEntityName() + parseTag(fakeTag);
			}
		}
		if(invalid)
		{
			entInstance = (EntityLivingBase)EntityList.createEntityByName("Pig", world);
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			writeFakeTags(entInstance, fakeTag);
			identifier = entInstance.getClass().toString() + entInstance.getEntityName() + parseTag(fakeTag);
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
		
		ArrayList<String> stripList = NBTStripper.getNBTTagsToStrip(living);
		for(String s : stripList)
		{
			tag.removeTag(s);
		}
		tag.removeTag("bukkit");
		tag.removeTag("InLove");
		tag.setInteger("MorphNBTProtocolNumber", NBT_PROTOCOL);//changed everytime the identifier may change or requires a change.
	}
	
	public static String parseTag(NBTTagCompound tag)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<String> tags = new ArrayList<String>();
		
		HashMap tagMap;
		try
		{
			tagMap = ObfuscationReflectionHelper.getPrivateValue(NBTTagCompound.class, tag, ObfHelper.tagMap);
		}
		catch(Exception e)
		{
			ObfHelper.obfWarning();
			e.printStackTrace();
			tagMap = new HashMap();
		}
		
		for(Object obj : tagMap.entrySet())
		{
			Entry e = (Entry)obj;
			tags.add(e.getKey().toString() + ":" + tagMap.get(e.getKey()));
		}
		
		Collections.sort(tags);
		
		sb.append(tag.getName() + ":[");
		
		for(int i = 0; i < tags.size(); i++)
		{
			sb.append(tags.get(i));
			
			if(i != tags.size() - 1)
			{
				sb.append(",");
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}

	@Override
	public int compareTo(Object arg0) 
	{
		if(arg0 instanceof MorphState)
		{
			MorphState state = (MorphState)arg0;
			return identifier.compareTo(state.identifier);
		}
		return 0;
	}
}
