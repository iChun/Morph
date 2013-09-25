package morph.client.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import morph.api.Ability;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.ability.AbilityHandler;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.potion.PotionEffect;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient
	implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		Minecraft mc = Minecraft.getMinecraft();
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					String name = stream.readUTF();
					
					boolean morphing = stream.readBoolean();
					int morphProg = stream.readInt();

					NBTTagCompound prevTag = new NBTTagCompound();
					NBTTagCompound nextTag = new NBTTagCompound();
					if(stream.readBoolean())
					{
						prevTag = Morph.readNBTTagCompound(stream);
					}
					if(stream.readBoolean())
					{
						nextTag = Morph.readNBTTagCompound(stream);
						
						EntityPlayer player1 = mc.theWorld.getPlayerEntityByName(name);
						if(player1 != null)
						{
					        if (!player1.getActivePotionEffects().isEmpty())
					        {
					            NBTTagList nbttaglist = new NBTTagList();
					            Iterator iterator = player1.getActivePotionEffects().iterator();

					            while (iterator.hasNext())
					            {
					                PotionEffect potioneffect = (PotionEffect)iterator.next();
					                nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
					            }
					            nextTag.setTag("ActiveEffects", nbttaglist);
					        }
						}
					}
					
					MorphState prevState = new MorphState(mc.theWorld, name, "", null, true);
					MorphState nextState = new MorphState(mc.theWorld, name, "", null, true);
					
					prevState.readTag(mc.theWorld, prevTag);
					nextState.readTag(mc.theWorld, nextTag);
					
					//TODO check for mc.theplayer morphstate
//					prevState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerClient.getPlayerMorphs(event.entityPlayer.worldObj, event.entityPlayer.username), prevState);
//					nextState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerClient.getPlayerMorphs(event.entityPlayer.worldObj, event.entityPlayer.username), nextState);
					
					if(prevState.entInstance != null)
					{
						if(prevState.entInstance != mc.thePlayer)
						{
							prevState.entInstance.noClip = true;
						}
					}
					
					if(nextState.entInstance != null)
					{
						if(nextState.entInstance != mc.thePlayer)
						{
							nextState.entInstance.noClip = true;
						}
					}
					
//					System.out.println(prevEnt);
//					System.out.println(nextEnt);
					
					MorphInfoClient info = new MorphInfoClient(name, prevState, nextState);
					info.setMorphing(morphing);
					info.morphProgress = morphProg;
					
					MorphInfoClient info1 = Morph.proxy.tickHandlerClient.playerMorphInfo.get(name);
					if(info1 != null)
					{
						info.morphAbilities = info1.morphAbilities;
					}
					else
					{
						ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
						info.morphAbilities = new ArrayList<Ability>();
						for(Ability ability : newAbilities)
						{
							try
							{
								Ability clone = ability.clone();
								info.morphAbilities.add(clone);
							}
							catch(Exception e1)
							{
							}
						}

					}
					
					Morph.proxy.tickHandlerClient.playerMorphInfo.put(name, info);

					break;
				}
				case 1:
				{
					boolean clear = stream.readBoolean();
					
					if(clear)
					{
						Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
					}
					
					while(stream.readUTF().equalsIgnoreCase("state"))
					{
						MorphState state = new MorphState(mc.theWorld, mc.thePlayer.username, "", null, true);
						
						NBTTagCompound tag = Morph.readNBTTagCompound(stream);
						
						if(tag != null)
						{
							state.readTag(mc.theWorld, tag);
							
							String name = state.entInstance.getEntityName();
							
							if(name != null)
							{
								ArrayList<MorphState> states = Morph.proxy.tickHandlerClient.playerMorphCatMap.get(name);
								if(states == null)
								{
									states = new ArrayList<MorphState>();
									Morph.proxy.tickHandlerClient.playerMorphCatMap.put(name, states);
								}
								state = MorphHandler.addOrGetMorphState(states, state);
							}
						}
					}
					break;
				}
				case 2:
				{
					boolean clear = stream.readBoolean();
					if(clear)
					{
						AbilityHandler.trackedMap.clear();
						
						NBTTagCompound tag = Morph.readNBTTagCompound(stream);
						
						AbilityHandler.readAbilityFromNBT(tag);
					}
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
}
