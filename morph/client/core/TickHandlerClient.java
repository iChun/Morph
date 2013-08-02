package morph.client.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient 
	implements ITickHandler
{
	
	public TickHandlerClient()
	{
//		renderMorphInstance = new RenderMorph(new ModelMorph(), 0.5F);
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
				if(info.player == null)
				{
					info.player = world.getPlayerEntityByName(e.getKey());
				}
				if(info.player != null)
				{
					info.prevEntInstance.prevRotationYawHead = info.nextEntInstance.prevRotationYawHead = info.player.prevRotationYawHead;
					info.prevEntInstance.prevRotationYaw = info.nextEntInstance.prevRotationYaw = info.player.prevRotationYaw;
					info.prevEntInstance.prevRotationPitch = info.nextEntInstance.prevRotationPitch = info.player.prevRotationPitch;
					info.prevEntInstance.prevRenderYawOffset = info.nextEntInstance.prevRenderYawOffset = info.player.prevRenderYawOffset;
					info.prevEntInstance.prevLimbYaw = info.nextEntInstance.prevLimbYaw = info.player.prevLimbYaw;
					info.prevEntInstance.prevSwingProgress = info.nextEntInstance.prevSwingProgress = info.player.prevSwingProgress;
					
					info.prevEntInstance.rotationYawHead = info.nextEntInstance.rotationYawHead = info.player.rotationYawHead;
					info.prevEntInstance.rotationYaw = info.nextEntInstance.rotationYaw = info.player.rotationYaw;
					info.prevEntInstance.rotationPitch = info.nextEntInstance.rotationPitch = info.player.rotationPitch;
					info.prevEntInstance.renderYawOffset = info.nextEntInstance.renderYawOffset = info.player.renderYawOffset;
					info.prevEntInstance.limbYaw = info.nextEntInstance.limbYaw = info.player.limbYaw;
					info.prevEntInstance.swingProgress = info.nextEntInstance.swingProgress = info.player.swingProgress;
					info.prevEntInstance.limbSwing = info.nextEntInstance.limbSwing = info.player.limbSwing;
					info.prevEntInstance.motionX = info.nextEntInstance.motionX = info.player.motionX;
					info.prevEntInstance.motionY = info.nextEntInstance.motionY = info.player.motionY;
					info.prevEntInstance.motionZ = info.nextEntInstance.motionZ = info.player.motionZ;
					info.prevEntInstance.ticksExisted = info.nextEntInstance.ticksExisted = info.player.ticksExisted;
					
				}
				
				if(info.morphProgress < 40)
				{
					info.prevEntInstance.onUpdate();
				}
				else
				{
					info.nextEntInstance.onUpdate();
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
	}

	public void renderTick(Minecraft mc, World world, float renderTick)
	{
	}
	
	public long clock;
	
	public RenderMorph renderMorphInstance;
	
	public HashMap<String, MorphInfoClient> playerMorphInfo = new HashMap<String, MorphInfoClient>();

	public float renderTick;
	
	public boolean renderingMorph;
}