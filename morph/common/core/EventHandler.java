package morph.common.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandler 
{

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onSoundLoad(SoundLoadEvent event)
	{
		for(int i = 1; i <= 6; i++)
		{
			event.manager.soundPoolSounds.addSound("morph:morph" + i + ".ogg");
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onRenderEntity(RenderPlayerEvent.Pre event)
	{
		if(Morph.proxy.tickHandlerClient.renderingMorph)
		{
			event.setCanceled(true);
			return;
		}
		if(Morph.proxy.tickHandlerClient.playerMorphInfo.containsKey(event.entityPlayer.username))
		{
			event.setCanceled(true);
			
			double par2 = Morph.proxy.tickHandlerClient.renderTick;
	        double d0 = event.entityPlayer.lastTickPosX + (event.entityPlayer.posX - event.entityPlayer.lastTickPosX) * (double)par2;
	        double d1 = event.entityPlayer.lastTickPosY + (event.entityPlayer.posY - event.entityPlayer.lastTickPosY) * (double)par2;
	        double d2 = event.entityPlayer.lastTickPosZ + (event.entityPlayer.posZ - event.entityPlayer.lastTickPosZ) * (double)par2;
	        float f1 = event.entityPlayer.prevRotationYaw + (event.entityPlayer.rotationYaw - event.entityPlayer.prevRotationYaw) * (float)par2;
	        int i = event.entityPlayer.getBrightnessForRender((float)par2);

	        if (event.entityPlayer.isBurning())
	        {
	            i = 15728880;
	        }

	        int j = i % 65536;
	        int k = i / 65536;
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

	        MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(event.entityPlayer.username);
	        
	        if(info != null)
	        {
	        	Morph.proxy.tickHandlerClient.renderingMorph = true;
	        	GL11.glPushMatrix();
	        	
//	        	ObfuscationReflectionHelper.setPrivateValue(RendererLivingEntity.class, info.prevEntInfo.entRender, info.prevEntModel, ObfHelper.mainModel);
	        	
//	        	GL11.glTranslated(-2 * (d0 - RenderManager.renderPosX), -2 * (d1 - RenderManager.renderPosY), -2 * (d2 - RenderManager.renderPosZ));
	        	
//	        	GL11.glScalef(1.0F, -1.0F, -1.0F);
	        	
//	        	info.prevEntModel.modelParent.setRotationAngles(0.0F, 0.0F, 0.0F, event.entityPlayer.rotationPitch, event.entityPlayer.rotationYaw, 0.625F, info.prevEntInstance);
//	        	info.prevEntModel.modelParent.render(info.prevEntInstance, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
	        	info.nextEntInfo.entRender.func_130000_a(info.nextEntInstance, 0.0D, 0.0D - event.entityPlayer.yOffset, 0.0D, f1, Morph.proxy.tickHandlerClient.renderTick);
	        	
	        	GL11.glPopMatrix();
	        	Morph.proxy.tickHandlerClient.renderingMorph = false;
	        }
		}
	}
	
	@ForgeSubscribe
	public void onInteract(EntityInteractEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer() && event.target instanceof EntityLivingBase)
		{
			MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(event.entityPlayer.username);
			
			
			if(!(event.target.addEntityID(new NBTTagCompound()) && !(event.target instanceof EntityPlayer) || event.target instanceof EntityPlayer))
			{
				System.out.println("stop");
				return;
			}
			
			if(info == null)
			{
				info = new MorphInfo((EntityLivingBase)event.target, event.entityPlayer);
			}
			else if(info.getMorphing() || info.nextEntInstance == event.target)
			{
				System.out.println("stop1");
				return;
			}
			
			byte isPlayer = (byte)((info.nextEntInstance instanceof EntityPlayer && event.target instanceof EntityPlayer) ? 3 : info.nextEntInstance instanceof EntityPlayer ? 1 : event.target instanceof EntityPlayer ? 2 : 0);
			
			if(!(info.nextEntInstance instanceof EntityPlayer) && !info.nextEntInstance.addEntityID(new NBTTagCompound()))
			{
				System.out.println("stop2");
				return;
			}
			
			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "morph:morph", 1.0F, 1.0F);
			
			MorphInfo info2 = new MorphInfo(info.nextEntInstance, (EntityLivingBase)event.target);
			info2.setMorphing(true);
			
			Morph.proxy.tickHandlerServer.playerMorphInfo.put(event.entityPlayer.username, info2);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);
			try
			{
				stream.writeByte(0); //id
				stream.writeUTF(event.entityPlayer.username);
				
				stream.writeByte(isPlayer);
				stream.writeUTF((isPlayer == 1 || isPlayer == 3) ? ((EntityPlayer)info.nextEntInstance).username : "");
				stream.writeUTF((isPlayer == 2 || isPlayer == 3) ? ((EntityPlayer)event.target).username : "");
				
				NBTTagCompound prevTag = new NBTTagCompound();
				NBTTagCompound nextTag = new NBTTagCompound();

				info.nextEntInstance.addEntityID(prevTag);
				event.target.addEntityID(nextTag);
				
				Morph.writeNBTTagCompound(prevTag, stream);
				Morph.writeNBTTagCompound(nextTag, stream);
				
				PacketDispatcher.sendPacketToAllPlayers(new Packet250CustomPayload("Morph", bytes.toByteArray()));
			}
			catch(IOException e)
			{
				
			}
		}
	}
	
}
