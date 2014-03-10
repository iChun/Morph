package morph.common.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.core.network.PacketHandler;
import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import morph.common.packet.PacketMorphAcquisition;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

//TODO move parts of this into iChunUtil
public class EntityHelper 
{
	@SideOnly(Side.CLIENT)
    public static Render getEntityClassRenderObject(Class par1Class)
    {
        Render render = (Render)RenderManager.instance.entityRenderMap.get(par1Class);
        if (render == null && par1Class != Entity.class)
        {
            render = getEntityClassRenderObject(par1Class.getSuperclass());
        }
        return render;
    }
	
	public static boolean morphPlayer(EntityPlayerMP player, EntityLivingBase living, boolean kill)
	{
		return morphPlayer(player, living, kill, false);
	}
	
	public static boolean morphPlayer(EntityPlayerMP player, EntityLivingBase living, boolean kill, boolean forced)
	{
		if(Morph.childMorphs == 0 && living.isChild() || Morph.playerMorphs == 0 && living instanceof EntityPlayer || Morph.bossMorphs == 0 && living instanceof IBossDisplayData || player.getClass() == FakePlayer.class || player.playerNetServerHandler == null)
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
		if(!Morph.whitelistedPlayerNames.isEmpty() && !Morph.whitelistedPlayerNames.contains(player.getCommandSenderName()))
		{
			return false;
		}
		
		MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.getCommandSenderName());
		
//				EntityGiantZombie zomb = new EntityGiantZombie(living.worldObj);
//				zomb.setLocationAndAngles(event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, event.entityLiving.rotationYaw, event.entityLiving.rotationPitch);
//				event.entityLiving.worldObj.spawnEntityInWorld(zomb);
		
		if(!(living.writeToNBTOptional(new NBTTagCompound()) && !(living instanceof EntityPlayer) || living instanceof EntityPlayer))
		{
			return false;
		}
		
		if(info == null)
		{
			info = new MorphInfo(player.getCommandSenderName(), null, Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.getCommandSenderName()));
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
		
		prevState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.getCommandSenderName()), prevState);
		nextState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.getCommandSenderName()), nextState);
		
		if(nextState.identifier.equalsIgnoreCase(info.nextState.identifier))
		{
			return false;
		}
		
		if(Morph.instaMorph == 1 || forced)
		{
			MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), prevState, nextState);
			info2.setMorphing(true);

			MorphInfo info3 = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.getCommandSenderName());
			if(info3 != null)
			{
				info2.morphAbilities = info3.morphAbilities;
			}
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.getCommandSenderName(), info2);

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
		MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.getCommandSenderName());
		
		MorphState state1;
		
		MorphState state2 = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.getCommandSenderName());
		
		if(info != null)
		{
			state1 = info.nextState;
			MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), state1, state2);
			info2.setMorphing(true);
			
			MorphInfo info3 = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.getCommandSenderName());
			if(info3 != null)
			{
				info2.morphAbilities = info3.morphAbilities;
			}
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.getCommandSenderName(), info2);

            PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

			player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
			
			return true;
		}
		return false;
	}
	
	/*
	 * The following helper functions were taken out of iChunUtil. 
	 */
	
	public static MovingObjectPosition getEntityLook(EntityLivingBase ent, double d, boolean ignoreEntities, float renderTick)
	{
		if (ent == null)
		{
			return null;
		}

		double d1 = d;
		MovingObjectPosition mop = rayTrace(ent, d, renderTick);
		Vec3 vec3d = getPosition(ent, renderTick);

		if (mop != null)
		{
			d1 = mop.hitVec.distanceTo(vec3d);
		}

		double dd2 = d;

		if (d1 > dd2)
		{
			d1 = dd2;
		}

		d = d1;
		Vec3 vec3d1 = ent.getLook(renderTick);
		Vec3 vec3d2 = vec3d.addVector(vec3d1.xCoord * d, vec3d1.yCoord * d, vec3d1.zCoord * d);

		if (!ignoreEntities)
		{
			Entity entity1 = null;
			float f1 = 1.0F;
			List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.addCoord(vec3d1.xCoord * d, vec3d1.yCoord * d, vec3d1.zCoord * d).expand(f1, f1, f1));
			double d2 = 0.0D;

			for (int i = 0; i < list.size(); i++)
			{
				Entity entity = (Entity)list.get(i);

				if (!entity.canBeCollidedWith())
				{
					continue;
				}

				float f2 = entity.getCollisionBorderSize();
				AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2, f2);
				MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);

				if (axisalignedbb.isVecInside(vec3d))
				{
					if (0.0D < d2 || d2 == 0.0D)
					{
						entity1 = entity;
						d2 = 0.0D;
					}

					continue;
				}

				if (movingobjectposition == null)
				{
					continue;
				}

				double d3 = vec3d.distanceTo(movingobjectposition.hitVec);

				if (d3 < d2 || d2 == 0.0D)
				{
					entity1 = entity;
					d2 = d3;
				}
			}

			if (entity1 != null)
			{
				mop = new MovingObjectPosition(entity1);
			}
		}

		return mop;
	}

	public static Vec3 getPosition(Entity ent, float par1)
	{
		return getPosition(ent, par1, false);
	}

	public static Vec3 getPosition(Entity ent, float par1, boolean midPoint)
	{
		if (par1 == 1.0F)
		{
			return ent.worldObj.getWorldVec3Pool().getVecFromPool(ent.posX, midPoint ? ((ent.boundingBox.minY + ent.boundingBox.maxY) / 2D) : (ent.posY + (ent.worldObj.isRemote ? 0.0D : (ent.getEyeHeight() - 0.09D))), ent.posZ);
		}
		else
		{
			double var2 = ent.prevPosX + (ent.posX - ent.prevPosX) * (double)par1;
			double var4 = midPoint ? ((ent.boundingBox.minY + ent.boundingBox.maxY) / 2D) : (ent.prevPosY + (ent.worldObj.isRemote ? 0.0D : (ent.getEyeHeight() - 0.09D)) + (ent.posY - ent.prevPosY) * (double)par1);
			double var6 = ent.prevPosZ + (ent.posZ - ent.prevPosZ) * (double)par1;
			return ent.worldObj.getWorldVec3Pool().getVecFromPool(var2, var4, var6);
		}
	}

	public static MovingObjectPosition rayTrace(EntityLivingBase ent, double distance, float par3)
	{
		return rayTrace(ent, distance, par3, false);
	}

	public static MovingObjectPosition rayTrace(EntityLivingBase ent, double distance, float par3, boolean midPoint)
	{
		Vec3 var4 = getPosition(ent, par3, midPoint);
		Vec3 var5 = ent.getLook(par3);
		Vec3 var6 = var4.addVector(var5.xCoord * distance, var5.yCoord * distance, var5.zCoord * distance);
		return ent.worldObj.func_147447_a(var4, var6, false, false, true);
	}
	
}
