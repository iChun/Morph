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
import morph.common.ability.AbilityHandler;
import morph.common.ability.tracker.AbilityTracker;
import morph.common.entity.EntTracker;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

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
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			playerTick((WorldServer)((EntityPlayerMP)tickData[0]).worldObj, (EntityPlayerMP)tickData[0]);
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD, TickType.PLAYER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerServerMorph";
	}

	public void preWorldTick(WorldServer world)
	{
	}

	public void worldTick(WorldServer world)
	{
		if(clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
		{
			clock = world.getWorldTime();

			//			for(int i = 0 ; i < world.loadedEntityList.size(); i++)
			//			{
			//				if(world.loadedEntityList.get(i) instanceof EntityCow)
			//				{
			//					((EntityCow)world.loadedEntityList.get(i)).setDead();
			//				}
			//			}

			if(Morph.abilityTracker == 1)
			{
				EntTracker activeEntTracker = activeEntTrackers.get(world.provider.dimensionId);

				if(activeEntTracker == null)
				{
					if(world.loadedEntityList.size() > 0)
					{
						Entity ent = (Entity)world.loadedEntityList.get(world.rand.nextInt(world.loadedEntityList.size()));
						if(ent instanceof EntityLivingBase && !(ent instanceof EntityPlayer))
						{
							EntityLivingBase living = (EntityLivingBase)ent;
							EntTracker entTracker = new EntTracker(living, AbilityTracker.trackableAbilities[world.rand.nextInt(AbilityTracker.trackableAbilities.length)], true);

							if(!AbilityHandler.hasAbility(living.getClass(), entTracker.abilityTracker.abilityTracked) && getTrackerResults(living.getClass()).size() < 10)
							{
								NBTTagCompound tag = new NBTTagCompound();
								if(living.writeToNBTOptional(tag))
								{
									Entity ent1 = EntityList.createEntityFromNBT(tag, world);
									if(ent1 instanceof EntityLivingBase)
									{
										EntityLivingBase living2 = (EntityLivingBase)ent1;
										if(entTracker.abilityTracker.shouldTrack(living.worldObj, living2))
										{
											ChunkCoordinates chunk = world.getSpawnPoint();

											int posX = chunk.posX;
											int posZ = chunk.posZ;

											boolean success = true;

											for(int x = 0; x < world.playerEntities.size(); x++)
											{
												EntityPlayer player = (EntityPlayer)world.playerEntities.get(x);
												double d = player.getDistance(posX + 0.5D, 246, posZ + 0.5D);
												if(d < 64D)
												{
													success = false;
													break;
												}
											}

											if(success)
											{
												for(int i = -3; i <= 3; i++)
												{
													for(int k = -3; k <= 3; k++)
													{
														world.setBlock(posX + i, 245, posZ + k, Block.glass.blockID);
													}
												}

												entTracker.abilityTracker.posXUsed = posX;
												entTracker.abilityTracker.posZUsed = posZ;
												entTracker.trackedEnt.setPosition(posX + 0.5D, 246.1D, posZ + 0.5D);

												world.spawnEntityInWorld(living2);

												activeEntTracker = entTracker;
											}
											entTracker.abilityTracker.initialize();
										}
									}
								}
							}
						}
					}
				}
				else
				{
					if(activeEntTracker.shouldTick())
					{
						activeEntTracker.tick();
					}
					else
					{
						activeEntTracker.kill();

						ArrayList<AbilityTracker> results = getTrackerResults(activeEntTracker.trackedEnt.getClass());
						results.add(activeEntTracker.abilityTracker);

						if(results.size() >= 10)
						{
							int successes = 0;
							for(int i = 0; i < results.size(); i++)
							{
								if(results.get(i).hasAbility())
								{
									successes++;
								}
							}
							float finalResult = (float)successes / (float)results.size();
							if(finalResult > 0.7F)
							{
								AbilityHandler.mapTrackedAbilities(activeEntTracker.trackedEnt.getClass(), results.get(results.size() - 1).createAbility());
							}
						}
						activeEntTracker = null;
					}
				}

				activeEntTrackers.put(world.provider.dimensionId, activeEntTracker);
			}

			if(world.provider.dimensionId == 0)
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

					for(Ability ability : info.morphAbilities)
					{
						if(player != null && ability.getParent() == player || player == null && ability.getParent() != null)
						{
							ability.tick();
						}
						else
						{
							ability.setParent(player);
						}
					}

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

	public ArrayList<AbilityTracker> getTrackerResults(Class clz)
	{
		ArrayList<AbilityTracker> results = entTrackerResults.get(clz);
		if(results == null)
		{
			results = new ArrayList<AbilityTracker>();
			entTrackerResults.put(clz, results);
		}
		return results;
	}

	public long clock;

	public NBTTagCompound saveData;

	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
	public HashMap<String, ArrayList<MorphState>> playerMorphs = new HashMap<String, ArrayList<MorphState>>();

	public HashMap<Integer, EntTracker> activeEntTrackers = new HashMap<Integer, EntTracker>();
	public HashMap<Class, ArrayList<AbilityTracker>> entTrackerResults = new HashMap<Class, ArrayList<AbilityTracker>>();
}