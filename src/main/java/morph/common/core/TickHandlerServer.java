package morph.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ichun.common.core.EntityHelperBase;
import ichun.common.core.network.PacketHandler;
import ichun.common.core.util.ObfHelper;
import ichun.common.core.util.PlayerHelper;
import morph.api.Ability;
import morph.common.Morph;
import morph.common.ability.AbilityFly;
import morph.common.ability.AbilityHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import morph.common.packet.PacketCompleteDemorph;
import morph.common.packet.PacketSession;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class TickHandlerServer {

	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
			// Post world tick
			WorldServer world = (WorldServer) event.world;
			if (clock != world.getWorldTime()
					|| !world.getGameRules().getGameRuleBooleanValue(
							"doDaylightCycle")) {
				clock = world.getWorldTime();
				// for(int i = 0 ; i < world.loadedEntityList.size(); i++)
				// {
				// if(world.loadedEntityList.get(i) instanceof EntityCow)
				// {
				// ((EntityCow)world.loadedEntityList.get(i)).setDead();
				// }
				// }
			}
		}
	}

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
			EntityPlayer player = event.player;
			MorphInfo info = getPlayerMorphInfo(player);
			if (info != null) {
				float prog = info.morphProgress > 10 ? (((float) info.morphProgress) / 60F)
						: 0.0F;
				if (prog > 1.0F) {
					prog = 1.0F;
				}

				prog = (float) Math.pow(prog, 2);

				float prev = info.prevState != null
						&& !(info.prevState.entInstance instanceof EntityPlayer) ? info.prevState.entInstance
						.getEyeHeight() : player.yOffset;
				float next = info.nextState != null
						&& !(info.nextState.entInstance instanceof EntityPlayer) ? info.nextState.entInstance
						.getEyeHeight() : player.yOffset;
				double ySize = player.yOffset - (prev + (next - prev) * prog);
				player.lastTickPosY += ySize;
				player.prevPosY += ySize;
				player.posY += ySize;
			}
			// ArrayList<MorphState> states =
			// getPlayerMorphs(event.player.worldObj, "ohaiiChun");
			// for(MorphState state : states)
			// {
			// System.out.println(state.identifier);
			// }
		}
	}

	@SubscribeEvent
	public void serverTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Iterator<Entry<String, MorphInfo>> ite = playerMorphInfo.entrySet()
					.iterator();
			while (ite.hasNext()) {
				Entry<String, MorphInfo> e = ite.next();
				MorphInfo info = e.getValue();

				EntityPlayer player = PlayerHelper.getPlayerFromUsername(info.playerName);

				if (info.getMorphing()) {
					info.morphProgress++;
					if (info.morphProgress > 80) {
						info.morphProgress = 80;
						info.setMorphing(false);

						if (player != null) {
							ObfHelper.forceSetSize(player.getClass(), player,
									info.nextState.entInstance.width,
									info.nextState.entInstance.height);
							player.setPosition(player.posX, player.posY,
									player.posZ);
							player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance)
									.getDefaultEyeHeight()
									: info.nextState.entInstance.getEyeHeight()
											- player.yOffset;

							double nextMaxHealth = MathHelper
									.clamp_double(
											info.nextState.entInstance
													.getEntityAttribute(
															SharedMonsterAttributes.maxHealth)
													.getBaseValue(), 1D, 20D)
									+ info.healthOffset;

							if (nextMaxHealth < 1D) {
								nextMaxHealth = 1D;
							}

							if (nextMaxHealth != player.getEntityAttribute(
									SharedMonsterAttributes.maxHealth)
									.getBaseValue()) {
								player.getEntityAttribute(
										SharedMonsterAttributes.maxHealth)
										.setBaseValue(nextMaxHealth);
							}

							ArrayList<Ability> newAbilities = AbilityHandler
									.getEntityAbilities(info.nextState.entInstance
											.getClass());
							ArrayList<Ability> oldAbilities = info.morphAbilities;
							info.morphAbilities = new ArrayList<Ability>();
							for (Ability ability : newAbilities) {
								try {
									Ability clone = ability.clone();
									clone.setParent(player);
									info.morphAbilities.add(clone);
								} catch (Exception e1) {
								}
							}
							for (Ability ability : oldAbilities) {
								if (ability.inactive) {
									continue;
								}
								boolean isRemoved = true;
								for (Ability newAbility : info.morphAbilities) {
									if (newAbility.getType().equalsIgnoreCase(
											ability.getType())) {
										isRemoved = false;
										break;
									}
								}
								if (isRemoved && ability.getParent() != null) {
									ability.kill();
								}
							}
						}

						if (info.nextState.playerMorph.equalsIgnoreCase(e
								.getKey())) {
							// Demorphed
							PacketHandler.sendToAll(Morph.channels,
									new PacketCompleteDemorph(e.getKey()));

							for (Ability ability : info.morphAbilities) {
								if (ability.inactive) {
									continue;
								}
								if (ability.getParent() != null) {
									ability.kill();
								}
							}

							if (player != null) {
								getMorphDataFromPlayer(player).removeTag(
										"morphData");
							}

							ite.remove();
						}
					} else if (info.prevState != null && player != null) {
						ObfHelper
								.forceSetSize(
										player.getClass(),
										player,
										info.prevState.entInstance.width
												+ (info.nextState.entInstance.width - info.prevState.entInstance.width)
												* ((float) info.morphProgress / 80F),
										info.prevState.entInstance.height
												+ (info.nextState.entInstance.height - info.prevState.entInstance.height)
												* ((float) info.morphProgress / 80F));
						player.setPosition(player.posX, player.posY,
								player.posZ);
						float prevEyeHeight = info.prevState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.prevState.entInstance)
								.getDefaultEyeHeight()
								: info.prevState.entInstance.getEyeHeight()
										- player.yOffset;
						float nextEyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance)
								.getDefaultEyeHeight()
								: info.nextState.entInstance.getEyeHeight()
										- player.yOffset;
						player.eyeHeight = prevEyeHeight
								+ (nextEyeHeight - prevEyeHeight)
								* ((float) info.morphProgress / 80F);

						double prevMaxHealth = MathHelper.clamp_double(
								info.prevState.entInstance.getEntityAttribute(
										SharedMonsterAttributes.maxHealth)
										.getBaseValue(), 1D, 20D);
						double nextMaxHealth = MathHelper.clamp_double(
								info.nextState.entInstance.getEntityAttribute(
										SharedMonsterAttributes.maxHealth)
										.getBaseValue(), 1D, 20D);
						if (prevMaxHealth != nextMaxHealth) {
							double healthScale = info.preMorphHealth
									/ prevMaxHealth;
							double prevHealth = info.preMorphHealth;
							double nextHealth = nextMaxHealth * healthScale;
							if (healthScale <= 1.0D) {
								float targetHealth = (float) (prevHealth + (nextHealth - prevHealth)
										* ((float) info.morphProgress / 80F));
								if (targetHealth < 1.0F) {
									targetHealth = 1.0F;
								}
								if (nextMaxHealth > prevMaxHealth
										&& player.getHealth() + 0.5F < (float) Math
												.floor(targetHealth)
										|| prevMaxHealth > nextMaxHealth) {
									player.setHealth(targetHealth);
								}
							}

							double curMaxHealth = player.getEntityAttribute(
									SharedMonsterAttributes.maxHealth)
									.getBaseValue();
							double morphMaxHealth = Math.round(prevMaxHealth
									+ (nextMaxHealth - prevMaxHealth)
									* ((float) info.morphProgress / 80F))
									+ info.healthOffset;
							if (morphMaxHealth < 1D) {
								morphMaxHealth = 1D;
							}

							if (morphMaxHealth != curMaxHealth) {
								player.getEntityAttribute(
										SharedMonsterAttributes.maxHealth)
										.setBaseValue(morphMaxHealth);
							}
						}
					}
				}

				if (player != null) {
					// TODO check that the sleep timer doesn't affect the
					// bounding box
					// if(player.isPlayerSleeping() && player.sleepTimer > 0)
					if (player.isPlayerSleeping()) {
						info.sleeping = true;
					} else if (info.sleeping) {
						info.sleeping = false;
						ObfHelper.forceSetSize(player.getClass(), player,
								info.nextState.entInstance.width,
								info.nextState.entInstance.height);
						player.setPosition(player.posX, player.posY,
								player.posZ);
						player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance)
								.getDefaultEyeHeight()
								: info.nextState.entInstance.getEyeHeight()
										- player.yOffset;
					}

					if (info.prevState != null) {
						info.prevState.entInstance.setEntityId(player
								.getEntityId());
					}
					if (info.nextState != null) {
						info.nextState.entInstance.setEntityId(player
								.getEntityId());
					}
				}

				for (Ability ability : info.morphAbilities) {
					if (player != null && ability.getParent() == player
							|| player == null && ability.getParent() != null) {
						if (ability.inactive) {
							continue;
						}
						ability.tick();
						if (!info.firstUpdate && ability instanceof AbilityFly
								&& player != null) {
							info.flying = player.capabilities.isFlying;
						}
					} else {
						ability.setParent(player);
					}
				}

				info.firstUpdate = false;

				// if(info.morphProgress > 70)
				// {
				// info.nextState.entInstance.isDead = false;
				// info.nextState.entInstance.setLocationAndAngles(player.posX,
				// player.posY, player.posZ, player.rotationYaw,
				// player.rotationPitch);
				// info.nextState.entInstance.onUpdate();
				// }
			}

			Iterator<Entry<EntityPlayer, ArrayList<MorphState>>> ite1 = saveList
					.entrySet().iterator();
			while (ite1.hasNext()) {
				Entry<EntityPlayer, ArrayList<MorphState>> e = ite1.next();

				NBTTagCompound tag = getMorphDataFromPlayer(e.getKey());

				tag.setInteger("morphStatesCount", e.getValue().size());

				for (int i = 0; i < e.getValue().size(); i++) {
					MorphState state = e.getValue().get(i);
					tag.setTag("morphState" + i, state.getTag());
				}

				ite1.remove();
			}
			if (purgeSession) {
				purgeSession = false;
				updateSession(null);
			}
		}
	}

	public MorphState getSelfState(World world, EntityPlayer player) {
		ArrayList<MorphState> list = getPlayerMorphs(world, player);
		for (MorphState state : list) {
			if (state.playerName.equalsIgnoreCase(state.playerMorph)) {
				return state;
			}
		}
		return new MorphState(world, player.getCommandSenderName(),
				player.getCommandSenderName(), null, world.isRemote);
	}

	public ArrayList<MorphState> getPlayerMorphs(World world,
			EntityPlayer player) {
		String name = player.getCommandSenderName();
		ArrayList<MorphState> list = playerMorphs.get(name);
		if (list == null) {
			list = new ArrayList<MorphState>();
			playerMorphs.put(name, list);
			list.add(0, new MorphState(world, name, name, null, world.isRemote));
		}
		boolean found = false;
		for (MorphState state : list) {
			if (state.playerMorph.equals(name)) {
				found = true;
				break;
			}
		}
		if (!found) {
			list.add(0, new MorphState(world, name, name, null, world.isRemote));
		}

		saveList.put(player, list);

		return list;
	}

	public void removeAllPlayerMorphsExcludingCurrentMorph(EntityPlayer player) {
		getMorphDataFromPlayer(player).removeTag("morphStatesCount");
	}

	public boolean hasMorphState(EntityPlayer player, MorphState state) {
		ArrayList<MorphState> states = getPlayerMorphs(player.worldObj, player);
		if (!state.playerMorph.equalsIgnoreCase("")) {
			for (MorphState mState : states) {
				if (mState.playerMorph.equalsIgnoreCase(state.playerMorph)) {
					return true;
				}
			}
		} else {
			for (MorphState mState : states) {
				if (mState.identifier.equalsIgnoreCase(state.identifier)) {
					return true;
				}
			}
		}
		return false;
	}

	public void updateSession(EntityPlayer player) {
		if (player != null) {
			PacketHandler.sendToPlayer(Morph.channels,
					new PacketSession(player), player);
		} else {
			PacketHandler.sendToAll(Morph.channels, new PacketSession(player));
		}
	}

	public NBTTagCompound getMorphDataFromPlayer(EntityPlayer player) {
		NBTTagCompound tag = EntityHelperBase.getPlayerPersistentData(player)
				.getCompoundTag("MorphSave");
		EntityHelperBase.getPlayerPersistentData(player).setTag("MorphSave",
				tag);
		return tag;
	}

	public void setPlayerMorphInfo(EntityPlayer player, MorphInfo info) {
		if (info != null) {
			NBTTagCompound tag1 = new NBTTagCompound();
			info.writeNBT(tag1);
			getMorphDataFromPlayer(player).setTag("morphData", tag1);
			playerMorphInfo.put(player.getCommandSenderName(), info);
		} else {
			getMorphDataFromPlayer(player).removeTag("morphData");
			playerMorphInfo.remove(player.getCommandSenderName());
		}
	}

	public MorphInfo getPlayerMorphInfo(EntityPlayer player) {
		return playerMorphInfo.get(player.getCommandSenderName());
	}

	public MorphInfo getPlayerMorphInfo(String playerName) {
		return playerMorphInfo.get(playerName);
	}

	public long clock;

	public boolean purgeSession;

	public MorphSaveData saveData = null;
	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
	public HashMap<String, ArrayList<MorphState>> playerMorphs = new HashMap<String, ArrayList<MorphState>>();

	public WeakHashMap<EntityPlayer, ArrayList<MorphState>> saveList = new WeakHashMap<EntityPlayer, ArrayList<MorphState>>();
}