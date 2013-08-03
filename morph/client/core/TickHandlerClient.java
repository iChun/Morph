package morph.client.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import morph.common.core.ObfHelper;
import morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient 
	implements ITickHandler
{
	
	public TickHandlerClient()
	{
		renderMorphInstance = new RenderMorph(new ModelMorph(), 0.5F);
		renderMorphInstance.setRenderManager(RenderManager.instance);
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				preRenderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{      		
				worldTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld);
			}
		}
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			playerTick((World)((EntityPlayer)tickData[0]).worldObj, (EntityPlayer)tickData[0]);
		}
		else if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				renderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT, TickType.PLAYER, TickType.RENDER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerClientMorph";
	}

	public void worldTick(Minecraft mc, WorldClient world)
	{
		if(clock != world.getWorldTime())
		{
			clock = world.getWorldTime();
			
			for(Entry<String, MorphInfoClient> e : playerMorphInfo.entrySet())
			{
				MorphInfoClient info = e.getValue();
				
				if(info.getMorphing())
				{
					info.morphProgress++;
					if(info.morphProgress > 80)
					{
						info.morphProgress = 80;
						info.setMorphing(false);
					}
				}
				if(info.player != null && (info.player.dimension != mc.thePlayer.dimension || !info.player.isEntityAlive()))
				{
					info.player = null;
				}
				if(info.player == null)
				{
					info.player = world.getPlayerEntityByName(e.getKey());
				}
				if(info.prevEntInstance == null)
				{
					info.prevEntInstance = info.player;
				}
				
				if(mc.thePlayer.username.equalsIgnoreCase("Notch") && info.player != null && info.player.username.equalsIgnoreCase("ohaiiChun"))
				{
//					System.out.println(mc.thePlayer);
//					System.out.println(info.nextEntInstance);
				}
				
				if(info.morphProgress < 10)
				{
					if(info.prevEntInstance != mc.thePlayer)
					{
//						if(info.player != null && info.prevEntInstance.dimension != info.player.dimension)
//						{
//							info.prevEntInstance.travelToDimension(info.player.dimension);
//						}
						info.prevEntInstance.onUpdate();
					}
				}
				else if(info.morphProgress > 70)
				{
					if(info.nextEntInstance != mc.thePlayer)
					{
//						if(info.player != null && info.nextEntInstance.dimension != info.player.dimension)
//						{
//							NBTTagCompound tag = new NBTTagCompound();
//							info.nextEntInstance.addEntityID(tag);
//							tag.setInteger("Dimension", info.player.dimension);
//							info.nextEntInstance = (EntityLivingBase)EntityList.createEntityFromNBT(tag, Minecraft.getMinecraft().theWorld);
//						}
						info.nextEntInstance.onUpdate();
					}
				}
			}
		}
	}

	public void playerTick(World world, EntityPlayer player)
	{
	}

	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
		this.renderTick = renderTick;
		
		for(Entry<String, MorphInfoClient> e : playerMorphInfo.entrySet())
		{
			MorphInfoClient info = e.getValue();
			
			if(info.prevEntInstance != null && info.nextEntInstance != null && info.player != null)
			{
				info.prevEntInstance.prevRotationYawHead = info.nextEntInstance.prevRotationYawHead = info.player.prevRotationYawHead;
				info.prevEntInstance.prevRotationYaw = info.nextEntInstance.prevRotationYaw = info.player.prevRotationYaw;
				info.prevEntInstance.prevRotationPitch = info.nextEntInstance.prevRotationPitch = info.player.prevRotationPitch;
				info.prevEntInstance.prevRenderYawOffset = info.nextEntInstance.prevRenderYawOffset = info.player.prevRenderYawOffset;
				info.prevEntInstance.prevLimbYaw = info.nextEntInstance.prevLimbYaw = info.player.prevLimbYaw;
				info.prevEntInstance.prevSwingProgress = info.nextEntInstance.prevSwingProgress = info.player.prevSwingProgress;
				info.prevEntInstance.prevPosX = info.nextEntInstance.prevPosX = info.player.prevPosX;
				info.prevEntInstance.prevPosY = info.nextEntInstance.prevPosY = info.player.prevPosY;
				info.prevEntInstance.prevPosZ = info.nextEntInstance.prevPosZ = info.player.prevPosZ;
				
				info.prevEntInstance.rotationYawHead = info.nextEntInstance.rotationYawHead = info.player.rotationYawHead;
				info.prevEntInstance.rotationYaw = info.nextEntInstance.rotationYaw = info.player.rotationYaw;
				info.prevEntInstance.rotationPitch = info.nextEntInstance.rotationPitch = info.player.rotationPitch;
				info.prevEntInstance.renderYawOffset = info.nextEntInstance.renderYawOffset = info.player.renderYawOffset;
				info.prevEntInstance.limbYaw = info.nextEntInstance.limbYaw = info.player.limbYaw;
				info.prevEntInstance.swingProgress = info.nextEntInstance.swingProgress = info.player.swingProgress;
				info.prevEntInstance.limbSwing = info.nextEntInstance.limbSwing = info.player.limbSwing;
				info.prevEntInstance.posX = info.nextEntInstance.posX = info.player.posX;
				info.prevEntInstance.posY = info.nextEntInstance.posY = info.player.posY;
				info.prevEntInstance.posZ = info.nextEntInstance.posZ = info.player.posZ;
				info.prevEntInstance.motionX = info.nextEntInstance.motionX = info.player.motionX;
				info.prevEntInstance.motionY = info.nextEntInstance.motionY = info.player.motionY;
				info.prevEntInstance.motionZ = info.nextEntInstance.motionZ = info.player.motionZ;
				info.prevEntInstance.ticksExisted = info.nextEntInstance.ticksExisted = info.player.ticksExisted;
				info.prevEntInstance.onGround = info.nextEntInstance.onGround = info.player.onGround;
				info.prevEntInstance.isAirBorne = info.nextEntInstance.isAirBorne = info.player.isAirBorne;
				info.prevEntInstance.moveStrafing = info.nextEntInstance.moveStrafing = info.player.moveStrafing;
				info.prevEntInstance.moveForward = info.nextEntInstance.moveForward = info.player.moveForward;
				info.prevEntInstance.dimension = info.nextEntInstance.dimension = info.player.dimension;
				info.prevEntInstance.worldObj = info.nextEntInstance.worldObj = info.player.worldObj;
				info.prevEntInstance.ridingEntity = info.nextEntInstance.ridingEntity = info.player.ridingEntity;
				info.prevEntInstance.setSneaking(info.player.isSneaking());
				info.nextEntInstance.setSneaking(info.player.isSneaking());
				info.prevEntInstance.setSprinting(info.player.isSprinting());
				info.nextEntInstance.setSprinting(info.player.isSprinting());
			}
		}

	}

	public void renderTick(Minecraft mc, World world, float renderTick)
	{
	}
	
	public long clock;
	
	public RenderMorph renderMorphInstance;
	
	public HashMap<String, MorphInfoClient> playerMorphInfo = new HashMap<String, MorphInfoClient>();

	public float renderTick;
	
	public boolean renderingMorph;
	public byte renderingPlayer;
}