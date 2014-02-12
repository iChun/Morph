package morph.common.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import morph.api.Ability;
import morph.common.Morph;
import morph.common.ability.AbilityFly;
import morph.common.ability.AbilityHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class TickHandlerServer 
	implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.WORLD)))
		{
			preWorldTick((WorldServer)tickData[0]);
		}
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			prePlayerTick((WorldServer)((EntityPlayerMP)tickData[0]).worldObj, (EntityPlayerMP)tickData[0]);
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.WORLD)))
		{
			worldTick((WorldServer)tickData[0]);
		}
		else if (type.equals(EnumSet.of(TickType.SERVER)))
		{
			serverTick();
		}
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			playerTick((WorldServer)((EntityPlayerMP)tickData[0]).worldObj, (EntityPlayerMP)tickData[0]);
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD, TickType.PLAYER, TickType.SERVER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerServerMorph";
	}
	
	public void serverTick()
	{
		Iterator<Entry<String, MorphInfo>> ite = playerMorphInfo.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry<String, MorphInfo> e = ite.next();
			MorphInfo info = e.getValue();

			EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(info.playerName);

			if(info.getMorphing())
			{
				info.morphProgress++;
				if(info.morphProgress > 80)
				{
					info.morphProgress = 80;
					info.setMorphing(false);

					if(player != null)
					{
						ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
						player.setPosition(player.posX, player.posY, player.posZ);
						player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;

						ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
						ArrayList<Ability> oldAbilities = info.morphAbilities;
						info.morphAbilities = new ArrayList<Ability>();
						for(Ability ability : newAbilities)
						{
							try
							{
								Ability clone = ability.clone();
								clone.setParent(player);
								info.morphAbilities.add(clone);
							}
							catch(Exception e1)
							{
							}
						}
						for(Ability ability : oldAbilities)
						{
							boolean isRemoved = true;
							for(Ability newAbility : info.morphAbilities)
							{
								if(newAbility.getType().equalsIgnoreCase(ability.getType()))
								{
									isRemoved = false;
									break;
								}
							}
							if(isRemoved && ability.getParent() != null)
							{
								ability.kill();
							}
						}
					}

					if(info.nextState.playerMorph.equalsIgnoreCase(e.getKey()))
					{
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);
						try
						{
							stream.writeUTF(e.getKey());

							PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short)Morph.getNetId(), (short)1, bytes.toByteArray()));
						}
						catch(IOException e1)
						{

						}
						catch(Exception e1)
						{
							ObfHelper.obfWarning();
							e1.printStackTrace();
						}

						for(Ability ability : info.morphAbilities)
						{
							if(ability.getParent() != null)
							{
								ability.kill();
							}
						}

						saveData.removeTag(e.getKey() + "_morphData");

						ite.remove();
					}
				}
			}
			
			if(player != null)
			{
				 if(player.isPlayerSleeping() && player.sleepTimer > 0)
				 {
					 info.sleeping = true;
				 }
				 else if(info.sleeping)
				 {
					 info.sleeping = false;
					 ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
					 player.setPosition(player.posX, player.posY, player.posZ);
					 player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
				 }
			}

			for(Ability ability : info.morphAbilities)
			{
				if(player != null && ability.getParent() == player || player == null && ability.getParent() != null)
				{
					ability.tick();
					if(!info.firstUpdate && ability instanceof AbilityFly && player != null)
					{
						info.flying = player.capabilities.isFlying;
					}
				}
				else
				{
					ability.setParent(player);
				}
			}

			info.firstUpdate = false;

			//					if(info.morphProgress > 70)
			//					{
			//						info.nextState.entInstance.isDead = false;
			//						info.nextState.entInstance.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
			//						info.nextState.entInstance.onUpdate();
			//					}
		}

		//				ArrayList<MorphState> states = getPlayerMorphs(world, "ohaiiChun");
		//				for(MorphState state : states)
		//				{
		//					System.out.println(state.identifier);
		//				}
	}

	public void preWorldTick(WorldServer world)
	{
	}

	public void worldTick(WorldServer world)
	{
		if(clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
		{
			clock = world.getWorldTime();

//						for(int i = 0 ; i < world.loadedEntityList.size(); i++)
//						{
//							if(world.loadedEntityList.get(i) instanceof EntityCow)
//							{
//								((EntityCow)world.loadedEntityList.get(i)).setDead();
//							}
//						}
		}
	}

	public void prePlayerTick(WorldServer world, EntityPlayerMP player)
	{
		//		MorphInfo info = playerMorphInfo.get(player.username);
		//		if(info != null)
		//		{
		//		}

	}

	public void playerTick(WorldServer world, EntityPlayerMP player)
	{
		MorphInfo info = playerMorphInfo.get(player.username);
		if(info != null)
		{
			float prog = info.morphProgress > 10 ? (((float)info.morphProgress) / 60F) : 0.0F;
			if(prog > 1.0F)
			{
				prog = 1.0F;
			}

			prog = (float)Math.pow(prog, 2);

			float prev = info.prevState != null && !(info.prevState.entInstance instanceof EntityPlayer) ? info.prevState.entInstance.getEyeHeight() : player.yOffset;
			float next = info.nextState != null && !(info.nextState.entInstance instanceof EntityPlayer) ? info.nextState.entInstance.getEyeHeight() : player.yOffset;
			double ySize = player.yOffset - (prev + (next - prev) * prog);
			player.lastTickPosY += ySize;
			player.prevPosY += ySize;
			player.posY += ySize;
		}

	}

	public MorphState getSelfState(World world, String name)
	{
		ArrayList<MorphState> list = getPlayerMorphs(world, name);
		for(MorphState state : list)
		{
			if(state.playerName.equalsIgnoreCase(state.playerMorph))
			{
				return state;
			}
		}
		return new MorphState(world, name, name, null, world.isRemote);
	}

	public ArrayList<MorphState> getPlayerMorphs(World world, String name)
	{
		ArrayList<MorphState> list = playerMorphs.get(name);
		if(list == null)
		{
			list = new ArrayList<MorphState>();
			playerMorphs.put(name, list);
			list.add(0, new MorphState(world, name, name, null, world.isRemote));
		}
		boolean found = false;
		for(MorphState state : list)
		{
			if(state.playerMorph.equals(name))
			{
				found = true;
				break;
			}
		}
		if(!found)
		{
			list.add(0, new MorphState(world, name, name, null, world.isRemote));
		}
		return list;
	}

	public boolean hasMorphState(EntityPlayer player, MorphState state)
	{
		ArrayList<MorphState> states = getPlayerMorphs(player.worldObj, player.username);
		if(!state.playerMorph.equalsIgnoreCase(""))
		{
			for(MorphState mState : states)
			{
				if(mState.playerMorph.equalsIgnoreCase(state.playerMorph))
				{
					return true;
				}
			}
		}
		else
		{
			for(MorphState mState : states)
			{
				if(mState.identifier.equalsIgnoreCase(state.identifier))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void updateSession(EntityPlayer player) 
	{
		try
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);

			stream.writeBoolean(SessionState.abilities);
			stream.writeBoolean(SessionState.canSleepMorphed);
			stream.writeBoolean(SessionState.allowMorphSelection);
			stream.writeBoolean(SessionState.allowFlight);
			
			if(player != null)
			{
				PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Morph.getNetId(), (short)2, bytes.toByteArray()), (Player)player);
			}
			else
			{
				PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short)Morph.getNetId(), (short)2, bytes.toByteArray()));
			}
		}
		catch(IOException e)
		{
			
		}
	}

	public long clock;

	public int lastIndex;
	
	public NBTTagCompound saveData;

	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
	public HashMap<String, ArrayList<MorphState>> playerMorphs = new HashMap<String, ArrayList<MorphState>>();
}