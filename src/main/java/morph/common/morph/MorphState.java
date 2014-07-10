package morph.common.morph;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.util.ObfHelper;
import morph.common.Morph;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

public class MorphState implements Comparable {
	public final int NBT_PROTOCOL = 2;

	public String playerName;
	public String playerMorph;
	public boolean isFavourite;
	public boolean isRemote;

	public EntityLivingBase entInstance;

	public String identifier;

	public MorphState(World world, String name, String player,
			NBTTagCompound tag, boolean remote) {
		playerName = name;
		playerMorph = player;
		isFavourite = name.equalsIgnoreCase(player);
		isRemote = remote;

		if (!player.equalsIgnoreCase("")) {
			entInstance = isRemote ? createPlayer(world, player)
					: new FakePlayer((WorldServer) world, new GameProfile(UUID.randomUUID(), player));
		} else if (tag != null) {
			entInstance = (EntityLivingBase) EntityList.createEntityFromNBT(
					tag, world);
		}

		if (entInstance != null) {
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			writeFakeTags(entInstance, fakeTag);
			if (playerMorph.equalsIgnoreCase("")) {
				identifier = entInstance.getClass().toString()
						+ parseTag(fakeTag);
			} else {
				identifier = "playerMorphState::player_" + playerMorph;
			}
		}
	}

	public NBTTagCompound getTag() {
		NBTTagCompound tag = new NBTTagCompound();

		tag.setString("playerName", playerName);
		tag.setString("playerMorph", playerMorph);
		tag.setBoolean("isFavourite", isFavourite);

		NBTTagCompound tag1 = new NBTTagCompound();
		if (entInstance != null) {
			try {
				entInstance.writeToNBTOptional(tag1);
			} catch (Exception e) {
				Morph.console(entInstance.toString()
						+ " threw an exception when trying to save!", true);
				e.printStackTrace();
			}
			writeFakeTags(entInstance, tag1);
		}

		tag.setTag("entInstanceTag", tag1);

		tag.setString("identifier", identifier);

		return tag;
	}

	public void readTag(World world, NBTTagCompound tag) {
		playerName = tag.getString("playerName");
		playerMorph = tag.getString("playerMorph");
		isFavourite = tag.getBoolean("isFavourite")
				|| playerName.equals(playerMorph);

		NBTTagCompound tag1 = tag.getCompoundTag("entInstanceTag");

		boolean invalid = false;
		if (playerName.equalsIgnoreCase("") || playerMorph.equalsIgnoreCase("")
				&& tag1.getString("id").equalsIgnoreCase("")) {
			invalid = true;
		}

		if (!invalid) {
			if (!playerMorph.equalsIgnoreCase("")) {
				entInstance = isRemote ? createPlayer(world, playerMorph)
						: new FakePlayer((WorldServer) world, new GameProfile(UUID.randomUUID(), playerMorph));
				identifier = "playerMorphState::player_" + playerMorph;
			} else {
				try {
					entInstance = (EntityLivingBase) EntityList
							.createEntityFromNBT(tag1, world);
					identifier = tag.getString("identifier");
					if (entInstance != null) {
						if ((identifier.contains(":[")
								&& identifier.endsWith("]") || tag1
								.getInteger("MorphNBTProtocolNumber") < NBT_PROTOCOL)
								&& !attemptRepairs(
										entInstance,
										tag1,
										tag1.getInteger("MorphNBTProtocolNumber"))) {
							identifier = "";
							invalid = true;
						}
					}
				} catch (Exception e) {
					Morph.console(
							"A mob (as a morph) is throwing an error when being read from NBT! You should report this to the mod author of the mob!",
							true);
					e.printStackTrace();
					invalid = true;
				}
			}
			if (entInstance == null) {
				invalid = true;
			} else if (identifier.equalsIgnoreCase("")) {
				NBTTagCompound fakeTag = new NBTTagCompound();
				entInstance.writeEntityToNBT(fakeTag);
				writeFakeTags(entInstance, fakeTag);
				identifier = entInstance.getClass().toString()
						+ parseTag(fakeTag);
			}
		}
		if (invalid) {
			entInstance = (EntityLivingBase) EntityList.createEntityByName(
					"Pig", world);
			NBTTagCompound fakeTag = new NBTTagCompound();
			entInstance.writeEntityToNBT(fakeTag);
			writeFakeTags(entInstance, fakeTag);
			identifier = entInstance.getClass().toString() + parseTag(fakeTag);
		}
	}

	@SideOnly(Side.CLIENT)
	private EntityPlayer createPlayer(World world, String player) {
		return new EntityOtherPlayerMP(world, new GameProfile(UUID.randomUUID(), player));
	}

	public void writeFakeTags(EntityLivingBase living, NBTTagCompound tag) {
		tag.setFloat("HealF", Short.MAX_VALUE);
		tag.setShort("Health", (short) Short.MAX_VALUE);
		tag.setShort("HurtTime", (short) 0);
		tag.setShort("DeathTime", (short) 0);
		tag.setShort("AttackTime", (short) 0);
		tag.setTag("ActiveEffects", new NBTTagList());
		tag.setTag("Attributes", new NBTTagList());
		tag.setShort("Fire", (short) 0);
		tag.setShort("Anger", (short) 0);
		tag.setInteger("Age", living.isChild() ? -24000 : 0);

		if (living instanceof EntityLiving) {
			EntityLiving living1 = (EntityLiving) living;

			NBTTagList tagList = new NBTTagList();

			for (int i = 0; i < living1.getLastActiveItems().length; ++i) {
				tagList.appendTag(new NBTTagCompound());
			}

			tag.setBoolean("CanPickUpLoot", true);
			tag.setTag("Equipment", tagList);
			tag.setBoolean("Leashed", false);
			tag.setBoolean("PersistenceRequired", true);
		}

		ArrayList<String> stripList = MorphHandler.getNBTTagsToStrip(living);
		for (String s : stripList) {
			tag.removeTag(s);
		}
		tag.removeTag("bukkit");
		tag.removeTag("InLove");
		tag.setInteger("MorphNBTProtocolNumber", NBT_PROTOCOL);// changed
																// everytime the
																// identifier
																// may change or
																// requires a
																// change.
	}

	public static String parseTag(NBTTagCompound tag) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> tags = new ArrayList<String>();

		HashMap tagMap;
		try {
			tagMap = ObfuscationReflectionHelper.getPrivateValue(
					NBTTagCompound.class, tag, ObfHelper.tagMap);
		} catch (Exception e) {
			ObfHelper.obfWarning();
			e.printStackTrace();
			tagMap = new HashMap();
		}

		for (Object obj : tagMap.entrySet()) {
			Entry e = (Entry) obj;
			tags.add(e.getKey().toString() + ":" + tagMap.get(e.getKey()));
		}

		Collections.sort(tags);

		sb.append("{");

		for (int i = 0; i < tags.size(); i++) {
			sb.append(tags.get(i));

			if (i != tags.size() - 1) {
				sb.append(",");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public boolean attemptRepairs(EntityLivingBase living, NBTTagCompound tag,
			int nbtProtocol) {
		// if nbtProtocol is 0, that means player is updating from 0.6.0.
		while (nbtProtocol < NBT_PROTOCOL) {
			if (nbtProtocol == 1) {
				tag.setInteger("MorphNBTProtocolNumber", NBT_PROTOCOL);// changed
																		// everytime
																		// the
																		// identifier
																		// may
																		// change
																		// or
																		// requires
																		// a
																		// change.

				NBTTagCompound fakeTag = new NBTTagCompound();
				living.writeEntityToNBT(fakeTag);
				writeFakeTags(living, fakeTag);

				identifier = living.getClass().toString() + parseTag(fakeTag);

				tag.setString("identifier", identifier);
			}
			nbtProtocol++;
		}
		tag.setInteger("MorphNBTProtocolNumber", nbtProtocol);
		return nbtProtocol >= NBT_PROTOCOL;
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof MorphState) {
			MorphState state = (MorphState) arg0;
			return identifier.compareTo(state.identifier);
		}
		return 0;
	}
}
