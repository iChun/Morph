package morph.client.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.EntityRendererProxy;
import morph.client.render.RenderMorph;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class TickHandlerClient 
	implements ITickHandler
{
	
	public TickHandlerClient()
	{
		renderMorphInstance = new RenderMorph(new ModelMorph(), 0.0F);
		renderMorphInstance.setRenderManager(RenderManager.instance);
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{      		
				preWorldTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld);
			}
		}
		else if (type.equals(EnumSet.of(TickType.RENDER)))
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

	public void preWorldTick(Minecraft mc, WorldClient world)
	{
		MorphInfo info = playerMorphInfo.get(mc.thePlayer.username);
		if(info != null)
		{
            double d0 = (double)mc.playerController.getBlockReachDistance();
            mc.objectMouseOver = mc.renderViewEntity.rayTrace(d0, renderTick);

//			motionMaintained[0] = mc.thePlayer.motionX;
//			motionMaintained[1] = mc.thePlayer.motionZ;
//			
//			ObfHelper.forcePushOutOfBlocks(mc.thePlayer, mc.thePlayer.posX - (double)mc.thePlayer.width * 0.35D, mc.thePlayer.boundingBox.minY + 0.5D, mc.thePlayer.posZ + (double)mc.thePlayer.width * 0.35D);
//            ObfHelper.forcePushOutOfBlocks(mc.thePlayer, mc.thePlayer.posX - (double)mc.thePlayer.width * 0.35D, mc.thePlayer.boundingBox.minY + 0.5D, mc.thePlayer.posZ - (double)mc.thePlayer.width * 0.35D);
//            ObfHelper.forcePushOutOfBlocks(mc.thePlayer, mc.thePlayer.posX + (double)mc.thePlayer.width * 0.35D, mc.thePlayer.boundingBox.minY + 0.5D, mc.thePlayer.posZ - (double)mc.thePlayer.width * 0.35D);
//            ObfHelper.forcePushOutOfBlocks(mc.thePlayer, mc.thePlayer.posX + (double)mc.thePlayer.width * 0.35D, mc.thePlayer.boundingBox.minY + 0.5D, mc.thePlayer.posZ + (double)mc.thePlayer.width * 0.35D);
//            
//			motionMaintained[2] = mc.thePlayer.motionX;
//			motionMaintained[3] = mc.thePlayer.motionZ;
//            
//           	maintainMotion = mc.thePlayer.motionX != motionMaintained[0] || mc.thePlayer.motionZ != motionMaintained[1];
		}
	}
	
	public void worldTick(Minecraft mc, WorldClient world)
	{
//		if(maintainMotion)
//		{
//			mc.thePlayer.moveEntity(motionMaintained[0] - motionMaintained[2], 0D, motionMaintained[1] - motionMaintained[3]);
//			mc.thePlayer.motionX = motionMaintained[0] - motionMaintained[2];
//			mc.thePlayer.motionZ = motionMaintained[1] - motionMaintained[3];
//		}
		if(mc.currentScreen != null && selectorShow)
		{
			if(mc.currentScreen instanceof GuiIngameMenu)
			{
				mc.displayGuiScreen(null);
			}
			selectorShow = false;
			selectorTimer = selectorShowTime - selectorTimer;
		}
		if(selectorTimer > 0)
		{
			selectorTimer--;
			if(selectorTimer == 0 && !selectorShow)
			{
				selectorSelected = 0;
				
				MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
				if(info != null)
				{
					MorphState state = info.nextState;
					for(int i = 0; i < playerMorphStates.size(); i++)
					{
						if(playerMorphStates.get(i).identifier.equalsIgnoreCase(state.identifier))
						{
							selectorSelected = i;
						}
					}
				}
			}
		}
		if(scrollTimer > 0)
		{
			scrollTimer--;
		}
		
		if(selectorShow)
		{
			int k = Mouse.getDWheel();
			if(k != 0)
			{
				if(k > 0)
				{
					selectorSelectedPrev = selectorSelected;
					scrollTimer = scrollTime;

					selectorSelected--;
					if(selectorSelected < 0)
					{
						selectorSelected = playerMorphStates.size() - 1;
					}
				}
				else
				{
					selectorSelectedPrev = selectorSelected;
					scrollTimer = scrollTime;

					selectorSelected++;
					if(selectorSelected > playerMorphStates.size() - 1)
					{
						selectorSelected = 0;
					}
				}
			}
			else
			{
				currentItem = mc.thePlayer.inventory.currentItem;
			}
			if(selectorTimer < 5)
			{
				mc.thePlayer.inventory.currentItem = currentItem;
			}
		}
		
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
						if(info.player != null)
						{
							ObfHelper.forceSetSize(info.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
							info.player.setPosition(info.player.posX, info.player.posY, info.player.posZ);
						}
					}
				}
				if(info.player != null && (info.player.dimension != mc.thePlayer.dimension || !info.player.isEntityAlive()))
				{
					info.player = null;
				}
				if(info.player == null)
				{
					info.player = world.getPlayerEntityByName(e.getKey());
					if(info.player != null)
					{
						ObfHelper.forceSetSize(info.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
						info.player.setPosition(info.player.posX, info.player.posY, info.player.posZ);
					}
				}
				if(info.prevState.entInstance == null)
				{
					info.prevState.entInstance = info.player;
					if(info.player != null)
					{
						ObfHelper.forceSetSize(info.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
						info.player.setPosition(info.player.posX, info.player.posY, info.player.posZ);
					}
				}
				
				if(info.morphProgress < 10)
				{
					if(info.prevState.entInstance != mc.thePlayer)
					{
						info.prevState.entInstance.onUpdate();
					}
				}
				else if(info.morphProgress > 70)
				{
					if(info.nextState.entInstance != mc.thePlayer)
					{
						info.nextState.entInstance.onUpdate();
					}
				}
			}
			
			if(!keySelectorBackDown && isPressed(Morph.keySelectorBack))
			{
				if(!selectorShow)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					
					selectorSelected = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						for(int i = 0; i < playerMorphStates.size(); i++)
						{
							if(playerMorphStates.get(i).identifier.equalsIgnoreCase(state.identifier))
							{
								selectorSelected = i;
							}
						}
					}
				}
				else
				{
					selectorSelectedPrev = selectorSelected;
					scrollTimer = scrollTime;
					
					selectorSelected--;
					if(selectorSelected < 0)
					{
						selectorSelected = playerMorphStates.size() - 1;
					}
				}
			}
			if(!keySelectorForwardDown && isPressed(Morph.keySelectorForward))
			{
				if(!selectorShow)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					
					selectorSelected = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						for(int i = 0; i < playerMorphStates.size(); i++)
						{
							if(playerMorphStates.get(i).identifier.equalsIgnoreCase(state.identifier))
							{
								selectorSelected = i;
							}
						}
					}
				}
				else
				{
					selectorSelectedPrev = selectorSelected;
					scrollTimer = scrollTime;

					selectorSelected++;
					if(selectorSelected > playerMorphStates.size() - 1)
					{
						selectorSelected = 0;
					}
				}
			}
			if(!keySelectorChooseDown && (isPressed(Keyboard.KEY_RETURN) || isPressed(mc.gameSettings.keyBindAttack.keyCode)))
			{
				if(selectorShow)
				{
					selectorShow = false;
					selectorTimer = selectorShowTime - selectorTimer;

					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					DataOutputStream stream = new DataOutputStream(bytes);
					try
					{
						stream.writeUTF(playerMorphStates.get(selectorSelected).identifier);
						
						PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()));
					}
					catch(IOException e)
					{
						
					}

					
				}
			}
			if(!keySelectorReturnDown && (isPressed(Keyboard.KEY_ESCAPE) || isPressed(mc.gameSettings.keyBindUseItem.keyCode)))
			{
				if(selectorShow)
				{
					selectorShow = false;
					selectorTimer = selectorShowTime - selectorTimer;
				}
			}
			keySelectorBackDown = isPressed(Morph.keySelectorBack);
			keySelectorForwardDown = isPressed(Morph.keySelectorForward);
			keySelectorChooseDown = isPressed(Keyboard.KEY_RETURN) || isPressed(mc.gameSettings.keyBindAttack.keyCode);
			keySelectorReturnDown = isPressed(Keyboard.KEY_ESCAPE) || isPressed(mc.gameSettings.keyBindUseItem.keyCode);
		}
	}

	public void playerTick(World world, EntityPlayer player)
	{
	}

	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
		if(mc.entityRenderer instanceof EntityRenderer && !(mc.entityRenderer instanceof EntityRendererProxy))
		{
			mc.entityRenderer = new EntityRendererProxy(mc);
		}
		
		this.renderTick = renderTick;
		
		MorphInfoClient info1 = playerMorphInfo.get(mc.thePlayer.username);
		if(info1 != null )
		{
			float prog = info1.morphProgress > 10 ? (((float)info1.morphProgress + renderTick) / 60F) : 0.0F;
			if(prog > 1.0F)
			{
				prog = 1.0F;
			}
			
			prog = (float)Math.pow(prog, 2);
			
			float prev = info1.prevState != null && !(info1.prevState.entInstance instanceof EntityPlayer) ? info1.prevState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
			float next = info1.nextState != null && !(info1.nextState.entInstance instanceof EntityPlayer) ? info1.nextState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
			ySize = mc.thePlayer.yOffset - (prev + (next - prev) * prog);
			mc.thePlayer.lastTickPosY -= ySize;
			mc.thePlayer.prevPosY -= ySize;
			mc.thePlayer.posY -= ySize;
			
		}
		
		for(Entry<String, MorphInfoClient> e : playerMorphInfo.entrySet())
		{
			MorphInfoClient info = e.getValue();
			
			if(info.prevState.entInstance != null && info.nextState.entInstance != null && info.player != null)
			{
				info.player.ignoreFrustumCheck = true;
				
				info.prevState.entInstance.prevRotationYawHead = info.nextState.entInstance.prevRotationYawHead = info.player.prevRotationYawHead;
				info.prevState.entInstance.prevRotationYaw = info.nextState.entInstance.prevRotationYaw = info.player.prevRotationYaw;
				info.prevState.entInstance.prevRotationPitch = info.nextState.entInstance.prevRotationPitch = info.player.prevRotationPitch;
				info.prevState.entInstance.prevRenderYawOffset = info.nextState.entInstance.prevRenderYawOffset = info.player.prevRenderYawOffset;
				info.prevState.entInstance.prevLimbYaw = info.nextState.entInstance.prevLimbYaw = info.player.prevLimbYaw;
				info.prevState.entInstance.prevSwingProgress = info.nextState.entInstance.prevSwingProgress = info.player.prevSwingProgress;
				info.prevState.entInstance.prevPosX = info.nextState.entInstance.prevPosX = info.player.prevPosX;
				info.prevState.entInstance.prevPosY = info.nextState.entInstance.prevPosY = info.player.prevPosY;
				info.prevState.entInstance.prevPosZ = info.nextState.entInstance.prevPosZ = info.player.prevPosZ;
				
				info.prevState.entInstance.rotationYawHead = info.nextState.entInstance.rotationYawHead = info.player.rotationYawHead;
				info.prevState.entInstance.rotationYaw = info.nextState.entInstance.rotationYaw = info.player.rotationYaw;
				info.prevState.entInstance.rotationPitch = info.nextState.entInstance.rotationPitch = info.player.rotationPitch;
				info.prevState.entInstance.renderYawOffset = info.nextState.entInstance.renderYawOffset = info.player.renderYawOffset;
				info.prevState.entInstance.limbYaw = info.nextState.entInstance.limbYaw = info.player.limbYaw;
				info.prevState.entInstance.swingProgress = info.nextState.entInstance.swingProgress = info.player.swingProgress;
				info.prevState.entInstance.limbSwing = info.nextState.entInstance.limbSwing = info.player.limbSwing;
				info.prevState.entInstance.posX = info.nextState.entInstance.posX = info.player.posX;
				info.prevState.entInstance.posY = info.nextState.entInstance.posY = info.player.posY;
				info.prevState.entInstance.posZ = info.nextState.entInstance.posZ = info.player.posZ;
				info.prevState.entInstance.motionX = info.nextState.entInstance.motionX = info.player.motionX;
				info.prevState.entInstance.motionY = info.nextState.entInstance.motionY = info.player.motionY;
				info.prevState.entInstance.motionZ = info.nextState.entInstance.motionZ = info.player.motionZ;
				info.prevState.entInstance.ticksExisted = info.nextState.entInstance.ticksExisted = info.player.ticksExisted;
				info.prevState.entInstance.onGround = info.nextState.entInstance.onGround = info.player.onGround;
				info.prevState.entInstance.isAirBorne = info.nextState.entInstance.isAirBorne = info.player.isAirBorne;
				info.prevState.entInstance.moveStrafing = info.nextState.entInstance.moveStrafing = info.player.moveStrafing;
				info.prevState.entInstance.moveForward = info.nextState.entInstance.moveForward = info.player.moveForward;
				info.prevState.entInstance.dimension = info.nextState.entInstance.dimension = info.player.dimension;
				info.prevState.entInstance.worldObj = info.nextState.entInstance.worldObj = info.player.worldObj;
				info.prevState.entInstance.ridingEntity = info.nextState.entInstance.ridingEntity = info.player.ridingEntity;
				info.prevState.entInstance.setSneaking(info.player.isSneaking());
				info.nextState.entInstance.setSneaking(info.player.isSneaking());
				info.prevState.entInstance.setSprinting(info.player.isSprinting());
				info.nextState.entInstance.setSprinting(info.player.isSprinting());
			}
		}
		
	}

	public void renderTick(Minecraft mc, World world, float renderTick)
	{
		MorphInfoClient info1 = playerMorphInfo.get(mc.thePlayer.username);
		if(info1 != null)
		{
			mc.thePlayer.lastTickPosY += ySize;
			mc.thePlayer.prevPosY += ySize;
			mc.thePlayer.posY += ySize;
		}
		
		if(selectorTimer > 0 || selectorShow)
		{
			GL11.glPushMatrix();
			
			float progress = (11F - ((float)selectorTimer + (1F - renderTick))) / 11F;
			
			if(selectorShow)
			{
				progress = 1.0F - progress;
			}
			
			if(selectorShow && selectorTimer == 0)
			{
				progress = 0.0F;
			}
			
			progress = (float)Math.pow(progress, 2);
			
			GL11.glTranslatef(-52F * progress, 0.0F, 0.0F);
			
	        ScaledResolution reso = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
	        
	        int gap = (reso.getScaledHeight() - (42 * 5)) / 2;
	        
	        double size = 42D;
	        double width1 = 0.0D;
	        
	        GL11.glPushMatrix();
	        
	        float progressV = (float)(scrollTime - (scrollTimer - renderTick)) / (float)scrollTime;
	        
	        progressV = (float)Math.pow(progressV, 2);
	        
	        if(progressV > 1.0F)
	        {
	        	progressV = 1.0F;
	        	selectorSelectedPrev = selectorSelected;
	        }
	        
	        
	        GL11.glTranslatef(0.0F, ((selectorSelected - selectorSelectedPrev) * 42F) * (1.0F - progressV), 0.0F);
	        
	        GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glDepthMask(false);
	        GL11.glColor4f(1f,1f,1f,1f);
	        GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
	        
	        for(int i = 0 ; i < playerMorphStates.size(); i++)
	        {
	        	double height1 = gap + size * (i - selectorSelected);
	        
		        mc.func_110434_K().func_110577_a(rlUnselected);
		        Tessellator tessellator = Tessellator.instance;
		        tessellator.startDrawingQuads();
				tessellator.setColorOpaque_F(1f,1f,1f);
		        tessellator.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
		        tessellator.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
		        tessellator.draw();
	        }
	        
        	int height1 = gap;
	        
	        GL11.glDepthMask(true);
	        GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
			
	        gap += 36;
	        
	        for(int i = 0 ; i < playerMorphStates.size(); i++)
	        {
	        	height1 = gap + (int)size * (i - selectorSelected);
	        
	        	MorphState state = playerMorphStates.get(i);
	        	
				drawEntityOnScreen(state.entInstance, 20, height1, 16, 2, 2, renderTick);
	        }
	        
	        GL11.glPopMatrix();
	        
	        if(selectorShow)
	        {
		        GL11.glEnable(3042 /*GL_BLEND*/);
		        GL11.glBlendFunc(770, 771);
		        
		        gap -= 36;
		        
		        height1 = gap;
		        
		        mc.func_110434_K().func_110577_a(rlSelected);
		        Tessellator tessellator = Tessellator.instance;
		        tessellator.startDrawingQuads();
				tessellator.setColorOpaque_F(1f,1f,1f);
		        tessellator.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
		        tessellator.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
		        tessellator.draw();
	
		        GL11.glDisable(3042 /*GL_BLEND*/);
	        }
			GL11.glPopMatrix();
		}
	}
	
    public void drawEntityOnScreen(EntityLivingBase ent, int posX, int posY, int scale, float par4, float par5, float renderTick)
    {
    	forceRender = true;
    	if(ent != null)
    	{
        	boolean hideGui = Minecraft.getMinecraft().gameSettings.hideGUI;
        	
        	Minecraft.getMinecraft().gameSettings.hideGUI = true;
        	
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();

	        GL11.glDisable(GL11.GL_ALPHA_TEST);

	        GL11.glTranslatef((float)posX, (float)posY, 50.0F);
	        
	        GL11.glEnable(3042 /*GL_BLEND*/);
	        GL11.glBlendFunc(770, 771);

	        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(ent.getEntityName(), 26, -32, 16777215);
	        
	        GL11.glDisable(3042 /*GL_BLEND*/);
	        
	        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        float f2 = ent.renderYawOffset;
	        float f3 = ent.rotationYaw;
	        float f4 = ent.rotationPitch;
	        float f5 = ent.rotationYawHead;

	        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-((float)Math.atan((double)(par5 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
	        GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
	        GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);

	        ent.renderYawOffset = (float)Math.atan((double)(par4 / 40.0F)) * 20.0F;
	        ent.rotationYaw = (float)Math.atan((double)(par4 / 40.0F)) * 40.0F;
	        ent.rotationPitch = -((float)Math.atan((double)(par5 / 40.0F))) * 20.0F;
	        ent.rotationYawHead = ent.renderYawOffset;
	        GL11.glTranslatef(0.0F, ent.yOffset, 0.0F);

	        float viewY = RenderManager.instance.playerViewY;
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
	        Tessellator.instance.setBrightness(240);

	        RenderManager.instance.playerViewY = viewY;
	        ent.renderYawOffset = f2;
	        ent.rotationYaw = f3;
	        ent.rotationPitch = f4;
	        ent.rotationYawHead = f5;

	        GL11.glEnable(GL11.GL_ALPHA_TEST);

	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	        
	        Minecraft.getMinecraft().gameSettings.hideGUI = hideGui;
    	}
    	forceRender = false;
    }
	
    public static boolean isPressed(int key)
    {
    	if(key < 0)
    	{
    		return Mouse.isButtonDown(key + 100);
    	}
    	return Keyboard.isKeyDown(key);
    }
	
	public long clock;
	
	public RenderMorph renderMorphInstance;
	
	public HashMap<String, MorphInfoClient> playerMorphInfo = new HashMap<String, MorphInfoClient>();
	
	public ArrayList<MorphState> playerMorphStates = new ArrayList<MorphState>();

	public float renderTick;
	
//	public double[] motionMaintained = new double[4];
//	public boolean maintainMotion;
	public float playerHeight = 1.8F;
	public float ySize;
	
	public boolean forceRender;
	public boolean renderingMorph;
	public byte renderingPlayer;
	
	public boolean keySelectorBackDown;
	public boolean keySelectorForwardDown;
	public boolean keySelectorChooseDown;
	public boolean keySelectorReturnDown;
	
	public boolean selectorShow;
	public int selectorTimer;
	public int selectorSelectedPrev;
	public int selectorSelected;
	public long systemTime;
	public int currentItem;
	public int scrollTimer;
	
	public final int selectorShowTime = 10;
	public final int scrollTime = 3;
	
	public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/guiSelected.png");
	public static final ResourceLocation rlUnselected= new ResourceLocation("morph", "textures/gui/guiUnselected.png");
	
}