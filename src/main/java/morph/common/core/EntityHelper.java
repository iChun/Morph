package morph.common.core;

import ichun.common.core.EntityHelperBase;
import ichun.common.core.network.PacketHandler;
import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import morph.common.packet.PacketMorphAcquisition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;

public class EntityHelper extends EntityHelperBase
{
	public static boolean morphPlayer(EntityPlayerMP player, EntityLivingBase living, boolean kill)
	{
		return morphPlayer(player, living, kill, false);
	}
	
	public static boolean morphPlayer(EntityPlayerMP player, EntityLivingBase living, boolean kill, boolean forced)
	{
		if(Morph.config.getInt("childMorphs") == 0 && living.isChild() || Morph.config.getInt("playerMorphs") == 0 && living instanceof EntityPlayer || Morph.config.getInt("bossMorphs") == 0 && living instanceof IBossDisplayData || player.getClass() == FakePlayer.class || player.playerNetServerHandler == null)
		{
            return false;
		}
		for(Class<? extends EntityLivingBase> clz : Morph.blacklistedClasses)
		{
			if(clz.isInstance(living))
			{
				return false;
			}
		}
		if(!Morph.playerList.isEmpty() && (Morph.config.getInt("listIsBlacklist") == 0 ? Morph.playerList.contains(player.getCommandSenderName()) : !Morph.playerList.contains(player.getCommandSenderName())))
		{
			return false;
		}
		
		MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
		
//				EntityGiantZombie zomb = new EntityGiantZombie(living.worldObj);
//				zomb.setLocationAndAngles(event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, event.entityLiving.rotationYaw, event.entityLiving.rotationPitch);
//				event.entityLiving.worldObj.spawnEntityInWorld(zomb);
		
		if(!(living.writeToNBTOptional(new NBTTagCompound()) && !(living instanceof EntityPlayer) || living instanceof EntityPlayer))
		{
			return false;
		}
		
		if(info == null)
		{
			info = new MorphInfo(player.getCommandSenderName(), null, Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player));
		}
		else if(info.getMorphing() || info.nextState.entInstance == living)
		{
			return false;
		}
		
		byte isPlayer = (byte)((info.nextState.entInstance instanceof EntityPlayer && living instanceof EntityPlayer) ? 3 : info.nextState.entInstance instanceof EntityPlayer ? 1 : living instanceof EntityPlayer ? 2 : 0);
		
		if(!(info.nextState.entInstance instanceof EntityPlayer) && !info.nextState.entInstance.writeToNBTOptional(new NBTTagCompound()))
		{
			return false;
		}
		
		String username1 = (isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)info.nextState.entInstance).getCommandSenderName() : "";
		String username2 = (isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)living).getCommandSenderName() : "";
		
		NBTTagCompound prevTag = new NBTTagCompound();
		NBTTagCompound nextTag = new NBTTagCompound();

		info.nextState.entInstance.writeToNBTOptional(prevTag);
		living.writeToNBTOptional(nextTag);
		
		MorphState prevState = new MorphState(player.worldObj, player.getCommandSenderName(), username1, prevTag, false);
		MorphState nextState = new MorphState(player.worldObj, player.getCommandSenderName(), username2, nextTag, false);
		
		if(Morph.proxy.tickHandlerServer.hasMorphState(player, nextState))
		{
			return false;
		}
		
		prevState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player), prevState);
		nextState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player), nextState);
		
		if(nextState.identifier.equalsIgnoreCase(info.nextState.identifier))
		{
			return false;
		}
		
		if(Morph.config.getInt("instaMorph") == 1 || forced)
		{
			MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), prevState, nextState);
			info2.setMorphing(true);

			MorphInfo info3 = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
			if(info3 != null)
			{
				info2.morphAbilities = info3.morphAbilities;
			}
			
			Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player, info2);

            PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

			player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
		}
		
		if(kill)
		{
            PacketHandler.sendToDimension(Morph.channels, new PacketMorphAcquisition(living.getEntityId(), player.getEntityId()), player.dimension);
		}
		
		MorphHandler.updatePlayerOfMorphStates(player, nextState, false);
		
		return true;
	}
	
	public static boolean demorphPlayer(EntityPlayerMP player)
	{
		MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
		
		MorphState state1;
		
		MorphState state2 = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player);
		
		if(info != null)
		{
			state1 = info.nextState;
			MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), state1, state2);
			info2.setMorphing(true);
			
			MorphInfo info3 = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
			if(info3 != null)
			{
				info2.morphAbilities = info3.morphAbilities;
			}
			
			Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player, info2);

            PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

			player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
			
			return true;
		}
		return false;
	}
}
