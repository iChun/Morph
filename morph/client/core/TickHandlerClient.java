package morph.client.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.EntityRendererProxy;
import morph.client.render.RenderMorph;
import morph.client.render.RenderPlayerHand;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.EnumChatFormatting;
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
		
		renderHandInstance = new RenderPlayerHand();
		renderHandInstance.setRenderManager(RenderManager.instance);
		
		showWarning = ObfHelper.obfuscation;
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
//            double d0 = (double)mc.playerController.getBlockReachDistance();
//            mc.objectMouseOver = mc.renderViewEntity.rayTrace(d0, renderTick);

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
		if(showWarning)
		{
			showWarning = false;
			mc.thePlayer.addChatMessage("Alert - You are using an unfinished build of Morph! Please report any issues on the GitHub");
		}
		
		if(mc.currentScreen != null && selectorShow)
		{
			if(mc.currentScreen instanceof GuiIngameMenu)
			{
				mc.displayGuiScreen(null);
			}
			selectorShow = false;
			selectorTimer = selectorShowTime - selectorTimer;
			scrollTimerHori = scrollTime;
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
					String entName = state.entInstance.getEntityName();
					
					int i = 0;
					
					Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
					
					while(ite.hasNext())
					{
						Entry<String, ArrayList<MorphState>> e = ite.next();
						if(e.getKey().equalsIgnoreCase(entName))
						{
							selectorSelected = i;
							ArrayList<MorphState> states = e.getValue();
							
							for(int j = 0; j < states.size(); j++)
							{
								if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
								{
									selectorSelectedHori = j;
									break;
								}
							}
							
							break;
						}
						i++;
					}
					
				}
			}
		}
		if(scrollTimer > 0)
		{
			scrollTimer--;
		}
		if(scrollTimerHori > 0)
		{
			scrollTimerHori--;
		}
		
		if(clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
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
							info.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).username.equalsIgnoreCase(mc.thePlayer.username) ? mc.thePlayer.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - info.player.yOffset;
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
					if(info.player != null && !info.getMorphing())
					{
						ObfHelper.forceSetSize(info.player, info.nextState.entInstance.width, info.nextState.entInstance.height);
						info.player.setPosition(info.player.posX, info.player.posY, info.player.posZ);
						info.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).username.equalsIgnoreCase(mc.thePlayer.username) ? mc.thePlayer.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - info.player.yOffset;
					}
				}
				if(info.prevState.entInstance == null)
				{
					info.prevState.entInstance = info.player;
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
//						ObfHelper.forceUpdateEntityActionState(info.nextState.entInstance.getClass(), info.nextState.entInstance);
					}
				}
				
				if(info.player != null)
				{
					if(info.prevState.entInstance != null && info.nextState.entInstance != null)
					{
						info.player.ignoreFrustumCheck = true;
						
						info.prevState.entInstance.prevRotationYawHead = info.nextState.entInstance.prevRotationYawHead = info.player.prevRotationYawHead;
						info.prevState.entInstance.prevRotationYaw = info.nextState.entInstance.prevRotationYaw = info.player.prevRotationYaw;
						info.prevState.entInstance.prevRotationPitch = info.nextState.entInstance.prevRotationPitch = info.player.prevRotationPitch;
						info.prevState.entInstance.prevRenderYawOffset = info.nextState.entInstance.prevRenderYawOffset = info.player.prevRenderYawOffset;
						info.prevState.entInstance.prevLimbSwingAmount = info.nextState.entInstance.prevLimbSwingAmount = info.player.prevLimbSwingAmount;
						info.prevState.entInstance.prevSwingProgress = info.nextState.entInstance.prevSwingProgress = info.player.prevSwingProgress;
						info.prevState.entInstance.prevPosX = info.nextState.entInstance.prevPosX = info.player.prevPosX;
						info.prevState.entInstance.prevPosY = info.nextState.entInstance.prevPosY = info.player.prevPosY;
						info.prevState.entInstance.prevPosZ = info.nextState.entInstance.prevPosZ = info.player.prevPosZ;
						
						info.prevState.entInstance.rotationYawHead = info.nextState.entInstance.rotationYawHead = info.player.rotationYawHead;
						info.prevState.entInstance.rotationYaw = info.nextState.entInstance.rotationYaw = info.player.rotationYaw;
						info.prevState.entInstance.rotationPitch = info.nextState.entInstance.rotationPitch = info.player.rotationPitch;
						info.prevState.entInstance.renderYawOffset = info.nextState.entInstance.renderYawOffset = info.player.renderYawOffset;
						info.prevState.entInstance.limbSwingAmount = info.nextState.entInstance.limbSwingAmount = info.player.limbSwingAmount;
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
						info.prevState.entInstance.hurtTime = info.nextState.entInstance.hurtTime = info.player.hurtTime;
						info.prevState.entInstance.deathTime = info.nextState.entInstance.deathTime = info.player.deathTime;
						info.prevState.entInstance.isSwingInProgress = info.nextState.entInstance.isSwingInProgress = info.player.isSwingInProgress;
						
						info.prevState.entInstance.setSneaking(info.player.isSneaking());
						info.nextState.entInstance.setSneaking(info.player.isSneaking());
						info.prevState.entInstance.setSprinting(info.player.isSprinting());
						info.nextState.entInstance.setSprinting(info.player.isSprinting());
						info.prevState.entInstance.setInvisible(info.player.isInvisible());
						info.nextState.entInstance.setInvisible(info.player.isInvisible());
						info.prevState.entInstance.setHealth(info.prevState.entInstance.getMaxHealth() * (info.player.getHealth() / info.player.getMaxHealth()));
						info.nextState.entInstance.setHealth(info.nextState.entInstance.getMaxHealth() * (info.player.getHealth() / info.player.getMaxHealth()));
						
						if(info.nextState.entInstance instanceof EntityDragon)
						{
							info.nextState.entInstance.prevRotationYaw += 180F;
							info.nextState.entInstance.rotationYaw += 180F;
						}
						
						for(int i = 0; i < 5; i++)
						{
							if(info.nextState.entInstance.getCurrentItemOrArmor(i) == null && info.player.getCurrentItemOrArmor(i) != null || 
									info.nextState.entInstance.getCurrentItemOrArmor(i) != null && info.player.getCurrentItemOrArmor(i) == null || 
											info.nextState.entInstance.getCurrentItemOrArmor(i) != null && info.player.getCurrentItemOrArmor(i) != null && 
												!info.nextState.entInstance.getCurrentItemOrArmor(i).isItemEqual(info.player.getCurrentItemOrArmor(i)))
							{
								info.nextState.entInstance.setCurrentItemOrArmor(i, info.player.getCurrentItemOrArmor(i) != null ? info.player.getCurrentItemOrArmor(i).copy() : null);
							}
						}
					}
				}
			}
			
			if((Morph.keySelectorUpHold == 0 && !GuiScreen.isShiftKeyDown() && !GuiScreen.isCtrlKeyDown() && !(Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)) || Morph.keySelectorUpHold == 1 && GuiScreen.isShiftKeyDown() || Morph.keySelectorUpHold == 2 && GuiScreen.isCtrlKeyDown() || Morph.keySelectorUpHold == 3 && (Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184))) && !keySelectorUpDown && isPressed(Morph.keySelectorUp))
			{
				if(!selectorShow && mc.currentScreen == null)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;
					
					selectorSelected = 0;
					selectorSelectedHori = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						
						String entName = state.entInstance.getEntityName();
						
						int i = 0;
						
						Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
						
						while(ite.hasNext())
						{
							Entry<String, ArrayList<MorphState>> e = ite.next();
							if(e.getKey().equalsIgnoreCase(entName))
							{
								selectorSelected = i;
								ArrayList<MorphState> states = e.getValue();
								
								for(int j = 0; j < states.size(); j++)
								{
									if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
									{
										selectorSelectedHori = j;
										break;
									}
								}
								
								break;
							}
							i++;
						}
					}
				}
				else
				{
					selectorSelectedHori = 0;
					selectorSelectedPrev = selectorSelected;
					scrollTimerHori = scrollTimer = scrollTime;
					
					selectorSelected--;
					if(selectorSelected < 0)
					{
						selectorSelected = playerMorphCatMap.size() - 1;
					}
				}
			}
			if((Morph.keySelectorDownHold == 0 && !GuiScreen.isShiftKeyDown() && !GuiScreen.isCtrlKeyDown() && !(Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)) || Morph.keySelectorDownHold == 1 && GuiScreen.isShiftKeyDown() || Morph.keySelectorDownHold == 2 && GuiScreen.isCtrlKeyDown() || Morph.keySelectorDownHold == 3 && (Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184))) && !keySelectorDownDown && isPressed(Morph.keySelectorDown))
			{
				if(!selectorShow && mc.currentScreen == null)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;
					
					selectorSelected = 0;
					selectorSelectedHori = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						String entName = state.entInstance.getEntityName();
						
						int i = 0;
						
						Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
						
						while(ite.hasNext())
						{
							Entry<String, ArrayList<MorphState>> e = ite.next();
							if(e.getKey().equalsIgnoreCase(entName))
							{
								selectorSelected = i;
								ArrayList<MorphState> states = e.getValue();
								
								for(int j = 0; j < states.size(); j++)
								{
									if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
									{
										selectorSelectedHori = j;
										break;
									}
								}
								
								break;
							}
							i++;
						}
					}
				}
				else
				{
					selectorSelectedHori = 0;
					selectorSelectedPrev = selectorSelected;
					scrollTimerHori = scrollTimer = scrollTime;

					selectorSelected++;
					if(selectorSelected > playerMorphCatMap.size() - 1)
					{
						selectorSelected = 0;
					}
				}
			}
			
			if((Morph.keySelectorLeftHold == 0 && !GuiScreen.isShiftKeyDown() && !GuiScreen.isCtrlKeyDown() && !(Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)) || Morph.keySelectorLeftHold == 1 && GuiScreen.isShiftKeyDown() || Morph.keySelectorLeftHold == 2 && GuiScreen.isCtrlKeyDown() || Morph.keySelectorLeftHold == 3 && (Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184))) && !keySelectorLeftDown && isPressed(Morph.keySelectorLeft))
			{
				if(!selectorShow && mc.currentScreen == null)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;
					
					selectorSelected = 0;
					selectorSelectedHori = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						
						String entName = state.entInstance.getEntityName();
						
						int i = 0;
						
						Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
						
						while(ite.hasNext())
						{
							Entry<String, ArrayList<MorphState>> e = ite.next();
							if(e.getKey().equalsIgnoreCase(entName))
							{
								selectorSelected = i;
								ArrayList<MorphState> states = e.getValue();
								
								for(int j = 0; j < states.size(); j++)
								{
									if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
									{
										selectorSelectedHori = j;
										break;
									}
								}
								
								break;
							}
							i++;
						}
					}
				}
				else
				{
					selectorSelectedHoriPrev = selectorSelectedHori;
					scrollTimerHori = scrollTime;
					
					selectorSelectedHori--;
				}
			}
			if((Morph.keySelectorRightHold == 0 && !GuiScreen.isShiftKeyDown() && !GuiScreen.isCtrlKeyDown() && !(Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)) || Morph.keySelectorRightHold == 1 && GuiScreen.isShiftKeyDown() || Morph.keySelectorRightHold == 2 && GuiScreen.isCtrlKeyDown() || Morph.keySelectorRightHold == 3 && (Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184))) && !keySelectorRightDown && isPressed(Morph.keySelectorRight))
			{
				if(!selectorShow && mc.currentScreen == null)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;
					
					selectorSelected = 0;
					selectorSelectedHori = 0;
					
					MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
					if(info != null)
					{
						MorphState state = info.nextState;
						
						String entName = state.entInstance.getEntityName();
						
						int i = 0;
						
						Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
						
						while(ite.hasNext())
						{
							Entry<String, ArrayList<MorphState>> e = ite.next();
							if(e.getKey().equalsIgnoreCase(entName))
							{
								selectorSelected = i;
								ArrayList<MorphState> states = e.getValue();
								
								for(int j = 0; j < states.size(); j++)
								{
									if(states.get(j).identifier.equalsIgnoreCase(state.identifier))
									{
										selectorSelectedHori = j;
										break;
									}
								}
								
								break;
							}
							i++;
						}
					}
				}
				else
				{
					selectorSelectedHoriPrev = selectorSelectedHori;
					scrollTimerHori = scrollTime;
					
					selectorSelectedHori++;
				}
			}
			
			if(!keySelectorChooseDown && (isPressed(Morph.keySelectorSelect) || isPressed(mc.gameSettings.keyBindAttack.keyCode)))
			{
				if(selectorShow)
				{
					selectorShow = false;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;

					MorphInfoClient info = playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.username);
					
					MorphState selectedState = null;
					
					int i = 0;
					
					Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
					
					while(ite.hasNext())
					{
						Entry<String, ArrayList<MorphState>> e = ite.next();
						if(i == selectorSelected)
						{
							ArrayList<MorphState> states = e.getValue();
							
							for(int j = 0; j < states.size(); j++)
							{
								if(j == selectorSelectedHori)
								{
									selectedState = states.get(j);
									break;
								}
							}
							
							break;
						}
						i++;
					}
					
					if(selectedState != null && (info != null && !info.nextState.identifier.equalsIgnoreCase(selectedState.identifier) || info == null && !selectedState.playerMorph.equalsIgnoreCase(mc.thePlayer.username)))
					{
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);
						try
						{
							stream.writeBoolean(false);
							stream.writeUTF(selectedState.identifier);
							
							PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()));
						}
						catch(IOException e)
						{
							
						}
					}
					
				}
			}
			if(!keySelectorReturnDown && (isPressed(Morph.keySelectorCancel) || isPressed(mc.gameSettings.keyBindUseItem.keyCode)))
			{
				if(selectorShow)
				{
					selectorShow = false;
					selectorTimer = selectorShowTime - selectorTimer;
					scrollTimerHori = scrollTime;
				}
			}
			if(!keySelectorDeleteDown && (isPressed(Morph.keySelectorRemoveMorph) || isPressed(Keyboard.KEY_DELETE)))
			{
				if(selectorShow)
				{
					MorphInfoClient info = playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.username);
					
					MorphState selectedState = null;
					
					int i = 0;
					
					Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
					
					boolean multiple = false;
					boolean decrease = false;
					
					while(ite.hasNext())
					{
						Entry<String, ArrayList<MorphState>> e = ite.next();
						if(i == selectorSelected)
						{
							ArrayList<MorphState> states = e.getValue();
							
							for(int j = 0; j < states.size(); j++)
							{
								if(j == selectorSelectedHori)
								{
									selectedState = states.get(j);
									if(j == states.size() - 1)
									{
										decrease = true;
									}
									break;
								}
							}
							
							if(states.size() > 1)
							{
								multiple = true;
							}
							
							break;
						}
						i++;
					}

					if(selectedState != null && ((info == null || info != null && !info.nextState.identifier.equalsIgnoreCase(selectedState.identifier)) && !selectedState.playerMorph.equalsIgnoreCase(mc.thePlayer.username)))
					{
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);
						try
						{
							stream.writeBoolean(true);
							stream.writeUTF(selectedState.identifier);
							
							PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()));
							if(!multiple)
							{
								selectorSelected--;
								if(selectorSelected < 0)
								{
									selectorSelected = playerMorphCatMap.size() - 1;
								}
							}
							else if(decrease)
							{
								selectorSelectedHori--;
								if(selectorSelected < 0)
								{
									selectorSelected = 0;
								}
							}
						}
						catch(IOException e)
						{
							
						}
					}
					
				}
			}
			keySelectorUpDown = isPressed(Morph.keySelectorUp);
			keySelectorDownDown = isPressed(Morph.keySelectorDown);
			keySelectorLeftDown = isPressed(Morph.keySelectorLeft);
			keySelectorRightDown = isPressed(Morph.keySelectorRight);
			
			keySelectorChooseDown = isPressed(Morph.keySelectorSelect) || isPressed(mc.gameSettings.keyBindAttack.keyCode);
			keySelectorReturnDown = isPressed(Morph.keySelectorCancel) || isPressed(mc.gameSettings.keyBindUseItem.keyCode);
			keySelectorDeleteDown = isPressed(Morph.keySelectorRemoveMorph) || isPressed(Keyboard.KEY_DELETE);
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
			eyeHeight = mc.thePlayer.eyeHeight;
			mc.thePlayer.lastTickPosY -= ySize;
			mc.thePlayer.prevPosY -= ySize;
			mc.thePlayer.posY -= ySize;
			mc.thePlayer.eyeHeight = mc.thePlayer.getDefaultEyeHeight();
		}
		
//		ySize = 0.0F;
		
//		for(Entry<String, MorphInfoClient> e : playerMorphInfo.entrySet())
//		{
//			MorphInfoClient info = e.getValue();
//		}
		
	}

	public void renderTick(Minecraft mc, World world, float renderTick)
	{
		MorphInfoClient info = playerMorphInfo.get(mc.thePlayer.username);
		if(info != null)
		{
			mc.thePlayer.lastTickPosY += ySize;
			mc.thePlayer.prevPosY += ySize;
			mc.thePlayer.posY += ySize;
			mc.thePlayer.eyeHeight = eyeHeight;
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
	        
	        int maxShowable = (int)Math.ceil((double)reso.getScaledHeight() / size) + 2;
	        
	        if(selectorSelected == 0 && selectorSelectedPrev > 0 || selectorSelectedPrev == 0 && selectorSelected > 0)
	        {
	        	maxShowable = 150;
	        }
	        
	        float progressV = (float)(scrollTime - (scrollTimer - renderTick)) / (float)scrollTime;
	        
	        progressV = (float)Math.pow(progressV, 2);
	        
	        if(progressV > 1.0F)
	        {
	        	progressV = 1.0F;
	        	selectorSelectedPrev = selectorSelected;
	        }
	        
	        float progressH = (float)(scrollTime - (scrollTimerHori - renderTick)) / (float)scrollTime;
	        
	        progressH = (float)Math.pow(progressH, 2);
	        
	        if(progressH > 1.0F)
	        {
	        	progressH = 1.0F;
	        	selectorSelectedHoriPrev = selectorSelectedHori;
	        }
	        
	        GL11.glTranslatef(0.0F, ((selectorSelected - selectorSelectedPrev) * 42F) * (1.0F - progressV), 0.0F);
	        
	        GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glDepthMask(false);
	        GL11.glColor4f(1f,1f,1f,1f);
	        GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
	        
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(770, 771);
	        
	        int i = 0;
	        
			Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
	        
			while(ite.hasNext())
			{
				Entry<String, ArrayList<MorphState>> e = ite.next();
	        	
	        	if(i > selectorSelected + maxShowable || i < selectorSelected - maxShowable)
	        	{
	        		i++;
	        		continue;
	        	}
	        	
	        	double height1 = gap + size * (i - selectorSelected);
	        
	        	ArrayList<MorphState> states = e.getValue();
	        	if(states == null || states.isEmpty())
	        	{
	        		ite.remove();
	        		i++;
	        		break;
	        	}
	        	
		        Tessellator tessellator = Tessellator.instance;
	        	
	        	if(i == selectorSelected)
	        	{
		        	if(selectorSelectedHori < 0)
		        	{
		        		selectorSelectedHori = states.size() - 1;
		        	}
		        	if(selectorSelectedHori >= states.size())
		        	{
		        		selectorSelectedHori = 0;
		        	}
		        	
		        	boolean newSlide = false;
		        	
		        	if(progressV < 1.0F && selectorSelectedPrev != selectorSelected)
		        	{
		        		selectorSelectedHoriPrev = states.size() - 1;
		        		selectorSelectedHori = 0;
		        		newSlide = true;
		        	}
		        	if(!selectorShow)
		        	{
		        		selectorSelectedHori = states.size() - 1;
		        		newSlide = true;
		        	}
		        	else if(progress > 0.0F)
		        	{
		        		selectorSelectedHoriPrev = states.size() - 1;
		        		newSlide = true;
		        	}
		        	
		        	for(int j = 0; j < states.size(); j++)
		        	{
		        		GL11.glPushMatrix();
		        		
		        		GL11.glTranslated(newSlide && j == 0 ? 0.0D : ((selectorSelectedHori - selectorSelectedHoriPrev) * 42F) * (1.0F - progressH), 0.0D, 0.0D);

		        		mc.getTextureManager().bindTexture(states.size() == 1 || j == states.size() - 1 ? rlUnselected : rlUnselectedSide);
		        		
		        		double dist = size * (j - selectorSelectedHori);
		        		
				        tessellator.startDrawingQuads();
						tessellator.setColorOpaque_F(1f,1f,1f);
				        tessellator.addVertexWithUV(width1 + dist, height1 + size, -90.0D + j, 0.0D, 1.0D);
				        tessellator.addVertexWithUV(width1 + dist + size, height1 + size, -90.0D + j, 1.0D, 1.0D);
				        tessellator.addVertexWithUV(width1 + dist + size, height1, -90.0D + j, 1.0D, 0.0D);
				        tessellator.addVertexWithUV(width1 + dist, height1, -90.0D + j, 0.0D, 0.0D);
				        tessellator.draw();
				        
				        GL11.glPopMatrix();
		        	}
	        	}
	        	else
	        	{
			        mc.getTextureManager().bindTexture(rlUnselected);
			        tessellator.startDrawingQuads();
					tessellator.setColorOpaque_F(1f,1f,1f);
			        tessellator.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
			        tessellator.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
			        tessellator.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
			        tessellator.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
			        tessellator.draw();
	        	}
		        
		        i++;
	        }
			
			GL11.glDisable(3042 /*GL_BLEND*/);
	        
        	int height1 = gap;
	        
	        GL11.glDepthMask(true);
	        GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
			
	        gap += 36;
	        
	        i = 0;
	        
	        ite = playerMorphCatMap.entrySet().iterator();
	        
			while(ite.hasNext())
			{
				Entry<String, ArrayList<MorphState>> e = ite.next();
				
	        	if(i > selectorSelected + maxShowable || i < selectorSelected - maxShowable)
	        	{
	        		i++;
	        		continue;
	        	}
	        	
	        	height1 = gap + (int)size * (i - selectorSelected);
	        	
	        	ArrayList<MorphState> states = e.getValue();
	        	
	        	if(i == selectorSelected)
	        	{
		        	boolean newSlide = false;
		        	
		        	if(progressV < 1.0F && selectorSelectedPrev != selectorSelected)
		        	{
		        		selectorSelectedHoriPrev = states.size() - 1;
		        		selectorSelectedHori = 0;
		        		newSlide = true;
		        	}
		        	if(!selectorShow)
		        	{
		        		selectorSelectedHori = states.size() - 1;
		        		newSlide = true;
		        	}
	        		
		        	for(int j = 0; j < states.size(); j++)
		        	{
		        		MorphState state = states.get(j);
		        		GL11.glPushMatrix();
		        		
		        		GL11.glTranslated(newSlide && j == 0 ? 0.0D : ((selectorSelectedHori - selectorSelectedHoriPrev) * 42F) * (1.0F - progressH), 0.0D, 0.0D);
		        		
		        		double dist = size * (j - selectorSelectedHori);
		        		
		        		GL11.glTranslated(dist, 0.0D, 0.0D);
		        		
		        		drawEntityOnScreen(state, state.entInstance, 20, height1, 16, 2, 2, renderTick, true, j == states.size() - 1);
		        		
		        		GL11.glPopMatrix();
		        	}
	        	}
	        	else
	        	{
	        		MorphState state = states.get(0);
	        		drawEntityOnScreen(state, state.entInstance, 20, height1, 16, 2, 2, renderTick, selectorSelected == i, true);
	        	}
	        	
        		GL11.glTranslatef(0.0F, 0.0F, 20F);
				i++;
			}
	        
	        GL11.glPopMatrix();
	        
	        if(selectorShow)
	        {
		        GL11.glEnable(3042 /*GL_BLEND*/);
		        GL11.glBlendFunc(770, 771);
		        
		        gap -= 36;
		        
		        height1 = gap;
		        
		        mc.getTextureManager().bindTexture(rlSelected);
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
	
    public void drawEntityOnScreen(MorphState state, EntityLivingBase ent, int posX, int posY, int scale, float par4, float par5, float renderTick, boolean selected, boolean text)
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

	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        
			if(ent instanceof EntityDragon)
			{
				GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			}
	        
	        float viewY = RenderManager.instance.playerViewY;
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        
			if(ent instanceof EntityDragon)
			{
				GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
			}

	        GL11.glTranslatef(0.0F, -0.22F, 0.0F);
	        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255.0F * 0.8F, 255.0F * 0.8F);
	        Tessellator.instance.setBrightness(240);
	        
	        RenderManager.instance.playerViewY = viewY;
	        ent.renderYawOffset = f2;
	        ent.rotationYaw = f3;
	        ent.rotationPitch = f4;
	        ent.rotationYawHead = f5;

	        GL11.glPopMatrix();
	        
	        RenderHelper.disableStandardItemLighting();
	        
	        GL11.glPushMatrix();

	        GL11.glTranslatef((float)posX, (float)posY, 50.0F);
	        
	        GL11.glEnable(3042 /*GL_BLEND*/);
	        GL11.glBlendFunc(770, 771);

	        MorphInfoClient info = playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.username);
	        
	        if(text)
	        {
	        	GL11.glTranslatef(0.0F, 0.0F, 100F);
	        	Minecraft.getMinecraft().fontRenderer.drawStringWithShadow((selected ? EnumChatFormatting.YELLOW : (info != null && info.nextState.entInstance.getEntityName().equalsIgnoreCase(state.entInstance.getEntityName()) || info == null && ent.getEntityName().equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username)) ? EnumChatFormatting.GOLD : "") + ent.getEntityName(), 26, -32, 16777215);
	        	GL11.glTranslatef(0.0F, 0.0F, -100F);
	        }
	        
	        GL11.glDisable(3042 /*GL_BLEND*/);

	        GL11.glPopMatrix();
	        
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        
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
	
	public boolean showWarning;
	
	public RenderMorph renderMorphInstance;
	public RenderPlayerHand renderHandInstance;
	
	public HashMap<String, MorphInfoClient> playerMorphInfo = new HashMap<String, MorphInfoClient>();
	
	public LinkedHashMap<String, ArrayList<MorphState>> playerMorphCatMap = new LinkedHashMap<String, ArrayList<MorphState>>();

	public float renderTick;
	
//	public double[] motionMaintained = new double[4];
//	public boolean maintainMotion;
	public float playerHeight = 1.8F;
	public float ySize;
	public float eyeHeight;
	
	public boolean forceRender;
	public boolean renderingMorph;
	public byte renderingPlayer;
	
	public boolean keySelectorUpDown;
	public boolean keySelectorDownDown;
	public boolean keySelectorLeftDown;
	public boolean keySelectorRightDown;
	
	public boolean keySelectorChooseDown;
	public boolean keySelectorReturnDown;
	public boolean keySelectorDeleteDown;
	
	public boolean selectorShow;
	public int selectorTimer;
	public int selectorSelectedPrev;
	public int selectorSelected;
	public int selectorSelectedHoriPrev;
	public int selectorSelectedHori;
	public long systemTime;
	public int currentItem;
	public int scrollTimer;
	public int scrollTimerHori;
	
	public final int selectorShowTime = 10;
	public final int scrollTime = 3;
	
	public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/guiSelected.png");
	public static final ResourceLocation rlUnselected = new ResourceLocation("morph", "textures/gui/guiUnselected.png");
	public static final ResourceLocation rlUnselectedSide = new ResourceLocation("morph", "textures/gui/guiUnselectedSide.png");
	
}