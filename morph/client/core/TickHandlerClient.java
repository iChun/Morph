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

import morph.api.Ability;
import morph.client.gui.GuiKeyBinds;
import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.EntityRendererProxy;
import morph.client.render.RenderMorph;
import morph.client.render.RenderPlayerHand;
import morph.common.Morph;
import morph.common.ability.AbilityHandler;
import morph.common.ability.AbilityWitherResistance;
import morph.common.core.ObfHelper;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiControlsScrollPanel;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
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
		
		try
		{
			EntityPlayer.class.getDeclaredMethod("getPosition", float.class);
			shouldReplaceRenderer = false;
		}
		catch(NoSuchMethodException e)
		{
			try
			{
				EntityPlayer.class.getDeclaredMethod("func_70666_h", float.class);
				shouldReplaceRenderer = false;
			}
			catch(NoSuchMethodException e1)
			{
				shouldReplaceRenderer = true;
			}
		}
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
            if(Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen.getClass() == GuiControls.class && Morph.ingameKeybindEditorHook == 1)
            {
            	try
            	{
            		GuiControlsScrollPanel panel = (GuiControlsScrollPanel)ObfuscationReflectionHelper.getPrivateValue(GuiControls.class, (GuiControls)Minecraft.getMinecraft().currentScreen, "scrollPane");
            		int selected = (Integer)ObfuscationReflectionHelper.getPrivateValue(GuiControlsScrollPanel.class, panel, "selected");
            		if(selected != -1)
            		{
                		GameSettings options = (GameSettings)ObfuscationReflectionHelper.getPrivateValue(GuiControlsScrollPanel.class, panel, "options");
            			if(options.keyBindings[selected] == KeyBindHook.keyBindHook)
            			{
            				FMLCommonHandler.instance().showGuiScreen(new GuiKeyBinds(Minecraft.getMinecraft().currentScreen));
            			}
            		}
            	}
            	catch(Exception e)
            	{
            	}
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
//		MorphInfo info = playerMorphInfo.get(mc.thePlayer.username);
//		if(info != null)
//		{
//		}
	}
	
	public void worldTick(Minecraft mc, WorldClient world)
	{
		abilityScroll++;
		if(mc.currentScreen != null)
		{
			if(selectorShow)
			{
				if(mc.currentScreen instanceof GuiIngameMenu)
				{
					mc.displayGuiScreen(null);
				}
				selectorShow = false;
				selectorTimer = selectorShowTime - selectorTimer;
				scrollTimerHori = scrollTime;
			}
			if(radialShow)
			{
				radialShow = false;
			}
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
		if(radialTime > 0)
		{
			radialTime--;
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
							info.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).username.equalsIgnoreCase(mc.thePlayer.username) || info.player == mc.thePlayer ? mc.thePlayer.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - info.player.yOffset;
							
							ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
							ArrayList<Ability> oldAbilities = info.morphAbilities;
							info.morphAbilities = new ArrayList<Ability>();
							for(Ability ability : newAbilities)
							{
								try
								{
									Ability clone = ability.clone();
									clone.setParent(info.player);
									info.morphAbilities.add(clone);
								}
								catch(Exception e1)
								{
								}
							}
							for(Ability ability : oldAbilities)
							{
								boolean isRemoved = true;
								for(Ability newAbility : info.morphAbilities)
								{
									if(newAbility.getType().equalsIgnoreCase(ability.getType()))
									{
										isRemoved = false;
										break;
									}
								}
								if(isRemoved && ability.getParent() != null)
								{
									ability.kill();
								}
							}
						}
					}
				}
				if(info.player != null && (info.player.dimension != mc.thePlayer.dimension || !info.player.isEntityAlive() || !world.playerEntities.contains(info.player) || !info.player.isPlayerSleeping() && info.player.sleepTimer > 0))
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
						info.player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer)info.nextState.entInstance).username.equalsIgnoreCase(mc.thePlayer.username) || info.player == mc.thePlayer ? mc.thePlayer.getDefaultEyeHeight() : ((EntityPlayer)info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - info.player.yOffset;
						
						for(Ability ability : info.morphAbilities)
						{
							ability.setParent(info.player);
						}
					}
				}
				if(info.prevState.entInstance == null)
				{
					info.prevState.entInstance = info.player;
				}
				
				if(info.player != null)
				{
					for(Ability ability : info.morphAbilities)
					{
						if(ability.getParent() != null)
						{
							ability.tick();
						}
					}
					
					if(info.prevState.entInstance != null && info.nextState.entInstance != null)
					{
						info.player.ignoreFrustumCheck = true;
						
						if(info.morphProgress < 10)
						{
							if(info.prevState.entInstance != mc.thePlayer)
							{
								info.prevState.entInstance.lastTickPosY -= info.player.yOffset;
								info.prevState.entInstance.prevPosY -= info.player.yOffset;
								info.prevState.entInstance.posY -= info.player.yOffset;
								info.prevState.entInstance.setPosition(info.prevState.entInstance.posX, info.prevState.entInstance.posY, info.prevState.entInstance.posZ);
								info.prevState.entInstance.onUpdate();
								info.prevState.entInstance.lastTickPosY += info.player.yOffset;
								info.prevState.entInstance.prevPosY += info.player.yOffset;
								info.prevState.entInstance.posY += info.player.yOffset;
								info.prevState.entInstance.setPosition(info.prevState.entInstance.posX, info.prevState.entInstance.posY, info.prevState.entInstance.posZ);
							}
						}
						else if(info.morphProgress > 70)
						{
							if(info.nextState.entInstance != mc.thePlayer)
							{
								if(!(info.nextState.entInstance instanceof EntityPlayer || RenderManager.instance.getEntityRenderObject(info.nextState.entInstance) instanceof RenderBiped))
								{
									info.nextState.entInstance.yOffset = 0.0F;
								}
								info.nextState.entInstance.lastTickPosY -= info.player.yOffset;
								info.nextState.entInstance.prevPosY -= info.player.yOffset;
								info.nextState.entInstance.posY -= info.player.yOffset;
								info.nextState.entInstance.setPosition(info.nextState.entInstance.posX, info.nextState.entInstance.posY, info.nextState.entInstance.posZ);
								info.nextState.entInstance.onUpdate();
								info.nextState.entInstance.lastTickPosY += info.player.yOffset;
								info.nextState.entInstance.prevPosY += info.player.yOffset;
								info.nextState.entInstance.posY += info.player.yOffset;
								info.nextState.entInstance.setPosition(info.nextState.entInstance.posX, info.nextState.entInstance.posY, info.nextState.entInstance.posZ);
//								ObfHelper.forceUpdateEntityActionState(info.nextState.entInstance.getClass(), info.nextState.entInstance);
							}
						}

						
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
						info.prevState.entInstance.isAirBorne = info.nextState.entInstance.isAirBorne = info.player.isAirBorne;
						info.prevState.entInstance.moveStrafing = info.nextState.entInstance.moveStrafing = info.player.moveStrafing;
						info.prevState.entInstance.moveForward = info.nextState.entInstance.moveForward = info.player.moveForward;
						info.prevState.entInstance.dimension = info.nextState.entInstance.dimension = info.player.dimension;
						info.prevState.entInstance.worldObj = info.nextState.entInstance.worldObj = info.player.worldObj;
						info.prevState.entInstance.ridingEntity = info.nextState.entInstance.ridingEntity = info.player.ridingEntity;
						info.prevState.entInstance.hurtTime = info.nextState.entInstance.hurtTime = info.player.hurtTime;
						info.prevState.entInstance.deathTime = info.nextState.entInstance.deathTime = info.player.deathTime;
						info.prevState.entInstance.isSwingInProgress = info.nextState.entInstance.isSwingInProgress = info.player.isSwingInProgress;
						
						boolean prevOnGround = info.nextState.entInstance.onGround;
						info.prevState.entInstance.onGround = info.nextState.entInstance.onGround = info.player.onGround;

						if(info.player != mc.thePlayer)
						{
							info.nextState.entInstance.noClip = false;
							
							info.nextState.entInstance.boundingBox.setBB(info.player.boundingBox);
							info.nextState.entInstance.moveEntity(0.0D, -0.01D, 0.0D);
							
							info.prevState.entInstance.prevPosY = info.nextState.entInstance.prevPosY = info.player.prevPosY;
						}
						info.prevState.entInstance.noClip = info.nextState.entInstance.noClip = info.player.noClip;
						
						info.prevState.entInstance.setSneaking(info.player.isSneaking());
						info.nextState.entInstance.setSneaking(info.player.isSneaking());
						info.prevState.entInstance.setSprinting(info.player.isSprinting());
						info.nextState.entInstance.setSprinting(info.player.isSprinting());
						info.prevState.entInstance.setInvisible(info.player.isInvisible());
						info.nextState.entInstance.setInvisible(info.player.isInvisible());
						info.prevState.entInstance.setHealth(info.prevState.entInstance.getMaxHealth() * (info.player.getHealth() / info.player.getMaxHealth()));
						info.nextState.entInstance.setHealth(info.nextState.entInstance.getMaxHealth() * (info.player.getHealth() / info.player.getMaxHealth()));
						
						if(!(info.nextState.entInstance instanceof EntityPlayer || RenderManager.instance.getEntityRenderObject(info.nextState.entInstance) instanceof RenderBiped))
						{
							info.nextState.entInstance.yOffset = info.player.yOffset;
						}
						
						if(prevOnGround && !info.nextState.entInstance.onGround && info.nextState.entInstance instanceof EntitySlime)
						{
							((EntitySlime)info.nextState.entInstance).squishAmount = 0.6F;
						}
						
						if(info.nextState.entInstance instanceof EntityDragon)
						{
							info.nextState.entInstance.prevRotationYaw += 180F;
							info.nextState.entInstance.rotationYaw += 180F;
							((EntityDragon)info.nextState.entInstance).deathTicks = info.player.deathTime;
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
					if(info.flying && info.firstUpdate)
					{
						info.player.capabilities.isFlying = true;
						info.player.capabilities.allowFlying = true;
						info.player.sendPlayerAbilities();
					}
				}
				info.firstUpdate = false;
			}
			
			if((Morph.keySelectorUpHold == 0 && !GuiScreen.isShiftKeyDown() && !GuiScreen.isCtrlKeyDown() && !(Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)) || Morph.keySelectorUpHold == 1 && GuiScreen.isShiftKeyDown() || Morph.keySelectorUpHold == 2 && GuiScreen.isCtrlKeyDown() || Morph.keySelectorUpHold == 3 && (Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184))) && !keySelectorUpDown && isPressed(Morph.keySelectorUp))
			{
				abilityScroll = 0;
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
				abilityScroll = 0;
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
				abilityScroll = 0;
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
				abilityScroll = 0;
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
				else if(radialShow)
				{
					selectRadialMenu();
					radialShow = false;
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
				if(radialShow)
				{
					radialShow = false;
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

					if(selectedState != null && !selectedState.isFavourite && ((info == null || info != null && !info.nextState.identifier.equalsIgnoreCase(selectedState.identifier)) && !selectedState.playerMorph.equalsIgnoreCase(mc.thePlayer.username)))
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
			if(!keyFavouriteDown && isPressed(Morph.keyFavourite))
			{
				if(selectorShow)
				{
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
					
					if(selectedState != null && !selectedState.playerMorph.equalsIgnoreCase(selectedState.playerName))
					{
						selectedState.isFavourite = !selectedState.isFavourite;
						
						ByteArrayOutputStream bytes = new ByteArrayOutputStream();
						DataOutputStream stream = new DataOutputStream(bytes);
						try
						{
							stream.writeBoolean(selectedState.isFavourite);
							stream.writeUTF(selectedState.identifier);
							
							PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Morph.getNetId(), (short)1, bytes.toByteArray()));
						}
						catch(IOException e)
						{
						}
					}
				}
				else if(mc.currentScreen == null)
				{
					favouriteStates.clear();
					
					Iterator<Entry<String, ArrayList<MorphState>>> ite = playerMorphCatMap.entrySet().iterator();
					
					while(ite.hasNext())
					{
						Entry<String, ArrayList<MorphState>> e = ite.next();
						ArrayList<MorphState> states = e.getValue();
						
						for(int j = 0; j < states.size(); j++)
						{
							if(states.get(j).isFavourite)
							{
								favouriteStates.add(states.get(j));
							}
						}
					}
					
					radialPlayerYaw = mc.renderViewEntity.rotationYaw;
					radialPlayerPitch = mc.renderViewEntity.rotationPitch;
					
					radialDeltaX = radialDeltaY = 0;
					
					radialShow = true;
					radialTime = 3;
					
				}
			}
			if(keyFavouriteDown && !isPressed(Morph.keyFavourite))
			{
				if(radialShow)
				{
					selectRadialMenu();
					radialShow = false;
				}
			}
			keySelectorUpDown = isPressed(Morph.keySelectorUp);
			keySelectorDownDown = isPressed(Morph.keySelectorDown);
			keySelectorLeftDown = isPressed(Morph.keySelectorLeft);
			keySelectorRightDown = isPressed(Morph.keySelectorRight);
			
			keySelectorChooseDown = isPressed(Morph.keySelectorSelect) || isPressed(mc.gameSettings.keyBindAttack.keyCode);
			keySelectorReturnDown = isPressed(Morph.keySelectorCancel) || isPressed(mc.gameSettings.keyBindUseItem.keyCode);
			keySelectorDeleteDown = isPressed(Morph.keySelectorRemoveMorph) || isPressed(Keyboard.KEY_DELETE);
			
			keyFavouriteDown = isPressed(Morph.keyFavourite);
		}
		if(Morph.allowMorphSelection == 0 && selectorShow)
		{
			selectorShow = false;
			selectorTimer = 0;
		}
	}

	public void playerTick(World world, EntityPlayer player)
	{
	}

	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
		if(shouldReplaceRenderer && mc.entityRenderer instanceof EntityRenderer && !(mc.entityRenderer instanceof EntityRendererProxy))
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
			mc.thePlayer.eyeHeight = mc.thePlayer.height / playerHeight < 1.0F ? mc.thePlayer.height / playerHeight * mc.thePlayer.getDefaultEyeHeight() : mc.thePlayer.getDefaultEyeHeight();
			
			shiftedPosY = true;
		}
		
		if(radialShow)
		{
			Mouse.getDX();
			Mouse.getDY();
			mc.mouseHelper.deltaX = mc.mouseHelper.deltaY = 0;
			mc.renderViewEntity.prevRotationYawHead = mc.renderViewEntity.rotationYawHead = radialPlayerYaw;
			mc.renderViewEntity.prevRotationYaw = mc.renderViewEntity.rotationYaw = radialPlayerYaw;
			mc.renderViewEntity.prevRotationPitch = mc.renderViewEntity.rotationPitch = radialPlayerPitch;
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
			shiftedPosY = false;
			
			mc.thePlayer.lastTickPosY += ySize;
			mc.thePlayer.prevPosY += ySize;
			mc.thePlayer.posY += ySize;
			mc.thePlayer.eyeHeight = eyeHeight;
		}
		
	    float bossHealthScale = BossStatus.healthScale;
	    int bossStatusBarTime = BossStatus.statusBarLength;
	    String bossName = BossStatus.bossName;
	    boolean randVar = BossStatus.field_82825_d;
		
		if((selectorTimer > 0 || selectorShow) && !mc.gameSettings.hideGUI)
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
	        
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        GL11.glDepthMask(false);
	        GL11.glColor4f(1f,1f,1f,1f);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
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
			
			GL11.glDisable(GL11.GL_BLEND);
	        
        	int height1 = gap;
	        
	        GL11.glDepthMask(true);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
			
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
		        		
		        		float entSize = state.entInstance.width > state.entInstance.height ? state.entInstance.width : state.entInstance.height;

		        		float prog = j - selectorSelectedHori == 0 ? (!selectorShow ? scrollTimerHori - renderTick : (3F - scrollTimerHori + renderTick)) / 3F : 0.0F;
		        		prog = MathHelper.clamp_float(prog, 0.0F, 1.0F);

		        		float scaleMag = ((2.5F + (entSize - 2.5F) * prog) / entSize) ;
		        		
		        		drawEntityOnScreen(state, state.entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, true, j == states.size() - 1);
		        		
		        		GL11.glPopMatrix();
		        	}
	        	}
	        	else
	        	{
	        		MorphState state = states.get(0);
	        		float entSize = state.entInstance.width > state.entInstance.height ? state.entInstance.width : state.entInstance.height;

	        		float prog = selectorSelected == i ? (!selectorShow ? scrollTimer - renderTick : (3F - scrollTimer + renderTick)) / 3F : 0.0F;
	        		prog = MathHelper.clamp_float(prog, 0.0F, 1.0F);

	        		float scaleMag = (2.5F / entSize) ;
	        		drawEntityOnScreen(state, state.entInstance, 20, height1, entSize > 2.5F ? 16F * scaleMag : 16F, 2, 2, renderTick, selectorSelected == i, true);
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
		
		//////////////////////
		
		if(radialShow && !mc.gameSettings.hideGUI)
		{
	    	double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
	    	double magAcceptance = 0.8D;

	    	ScaledResolution reso = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
	    	
    		float prog = (3F - radialTime + renderTick) / 3F;
    		if(prog > 1.0F)
    		{
    			prog = 1.0F;
    		}
	        
			float rad = (mag > magAcceptance ? 0.85F : 0.82F) * prog;
			
    		int radius = 80;
    		radius *= Math.pow(prog, 0.5D);
			
	    	if(!mc.gameSettings.hideGUI)
	    	{
				GL11.glEnable(GL11.GL_BLEND);
		        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		        GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glPushMatrix();
		        GL11.glLoadIdentity();
	
		        GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glPushMatrix();
		        GL11.glLoadIdentity();
				
				int NUM_PIZZA_SLICES = 100;
				
	    		double zLev = 0.05D;
	    		
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				
				final int stencilBit = MinecraftForgeClient.reserveStencilBit();
				
				if(stencilBit >= 0)
				{
			        GL11.glEnable(GL11.GL_STENCIL_TEST);
			        GL11.glColorMask(false, false, false, false);
		
			        final int stencilMask = 1 << stencilBit;
			        
					GL11.glStencilMask(stencilMask);
					GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
					GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
					GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		
					rad = (mag > magAcceptance ? 0.85F : 0.82F) * prog * (257F / (float)reso.getScaledHeight());
					
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					
					GL11.glBegin(GL11.GL_TRIANGLE_FAN);
					GL11.glVertex3d(0, 0, zLev);
					for(int i = 0; i <= NUM_PIZZA_SLICES; i++){ //NUM_PIZZA_SLICES decides how round the circle looks.
					    double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
					    GL11.glVertex3d(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad, Math.sin(angle) * rad, zLev);
					}
					GL11.glEnd();
					
					GL11.glStencilFunc(GL11.GL_ALWAYS, 0, stencilMask);
					
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					
					rad = 0.44F * prog * (257F / (float)reso.getScaledHeight());
					
					GL11.glBegin(GL11.GL_TRIANGLE_FAN);
					GL11.glVertex3d(0, 0, zLev);
					for(int i = 0; i <= NUM_PIZZA_SLICES; i++){ //NUM_PIZZA_SLICES decides how round the circle looks.
					    double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
					    GL11.glVertex3d(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad, Math.sin(angle) * rad, zLev);
					}
					GL11.glEnd();
					
					GL11.glStencilMask(0x00);
					GL11.glStencilFunc(GL11.GL_EQUAL, stencilMask, stencilMask);
		
			        GL11.glColorMask(true, true, true, true);
				}
	
				rad = (mag > magAcceptance ? 0.85F : 0.82F) * prog * (257F / (float)reso.getScaledHeight());
				
				GL11.glColor4f(0.0F, 0.0F, 0.0F, mag > magAcceptance ? 0.6F : 0.4F);
			
				GL11.glBegin(GL11.GL_TRIANGLE_FAN);
				GL11.glVertex3d(0, 0, zLev);
				for(int i = 0; i <= NUM_PIZZA_SLICES; i++){ //NUM_PIZZA_SLICES decides how round the circle looks.
				    double angle = Math.PI * 2 * i / NUM_PIZZA_SLICES;
				    GL11.glVertex3d(Math.cos(angle) * reso.getScaledHeight_double() / reso.getScaledWidth_double() * rad, Math.sin(angle) * rad, zLev);
				}
				GL11.glEnd();
	
				if(stencilBit >= 0)
				{
					GL11.glDisable(GL11.GL_STENCIL_TEST);
				}
				
				MinecraftForgeClient.releaseStencilBit(stencilBit);
				
				GL11.glEnable(GL11.GL_TEXTURE_2D);
		        
				GL11.glPopMatrix();
	
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				
				GL11.glPopMatrix();
	    	}
			
			GL11.glPushMatrix();
	
	    	int showAb = Morph.showAbilitiesInGui;
	    	
	    	Morph.showAbilitiesInGui = 0;
	    	
	    	double radialAngle = -720F;
	    	if(mag > magAcceptance)
	    	{
	    		//is on radial menu
	    		double aSin = Math.toDegrees(Math.asin(Morph.proxy.tickHandlerClient.radialDeltaX));
	    		
	    		if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
	    		{
	    			radialAngle = aSin;
	    		}
	    		else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
	    		{
	    			radialAngle = 90D + (90D - aSin);
	    		}
	    		else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
	    		{
	    			radialAngle = 180D - aSin;
	    		}
	    		else if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
	    		{
	    			radialAngle = 270D + (90D + aSin);
	    		}
	    	}
	    	
	    	if(mag > 0.9999999D)
	    	{
	    		mag = Math.round(mag);
	    	}
	    	
	        GL11.glDepthMask(true);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	    	
	    	for(int i = 0; i < favouriteStates.size(); i++)
	    	{
	    		double angle = Math.PI * 2 * i / favouriteStates.size();
	    		
	    		angle -= Math.toRadians(90D);
	    		
	    		float leeway = 360F / favouriteStates.size();
	    		
	    		boolean selected = false;
	    		
	    		if(mag > magAcceptance * 0.75D && (i == 0 && (radialAngle < (leeway / 2) && radialAngle >= 0F || radialAngle > (360F) - (leeway / 2)) || i != 0 && radialAngle < (leeway * i) + (leeway / 2) && radialAngle > (leeway * i ) - (leeway / 2)))
	    		{
	    			selected = true;
	    		}
	    		
	    		favouriteStates.get(i).isFavourite = false;
	    		
        		float entSize = favouriteStates.get(i).entInstance.width > favouriteStates.get(i).entInstance.height ? favouriteStates.get(i).entInstance.width : favouriteStates.get(i).entInstance.height;

        		float scaleMag = entSize > 2.5F ? (float)((2.5F + (entSize - 2.5F) * (mag > magAcceptance && selected ? ((mag - magAcceptance) / (1.0F - magAcceptance)) : 0.0F)) / entSize) : 1.0F;
	    		
	    		drawEntityOnScreen(favouriteStates.get(i), favouriteStates.get(i).entInstance, reso.getScaledWidth() / 2 + (int)(radius * Math.cos(angle)), (reso.getScaledHeight() + 32) / 2 + (int)(radius * Math.sin(angle)), 16 * prog * scaleMag + (float)(selected ? 6 * mag : 0), 2, 2, renderTick, selected, true);
	    		favouriteStates.get(i).isFavourite = true;
	    	}
	    	
	    	Morph.showAbilitiesInGui = showAb;
	    	
			GL11.glPopMatrix();
		}

		
		//////////////////////
		
	    BossStatus.healthScale = bossHealthScale;
	    BossStatus.statusBarLength = bossStatusBarTime;
	    BossStatus.bossName = bossName;
	    BossStatus.field_82825_d = randVar;
		
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		for(Entry<String, MorphInfoClient> e : playerMorphInfo.entrySet())
		{
			MorphInfoClient morphInfo = e.getValue();
			for(Ability ability : morphInfo.morphAbilities)
			{
				ability.postRender();
			}
		}
	}
	
    public void drawEntityOnScreen(MorphState state, EntityLivingBase ent, int posX, int posY, float scale, float par4, float par5, float renderTick, boolean selected, boolean text)
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
	        
        	GL11.glTranslatef(0.0F, 0.0F, 100F);
	        if(text)
	        {
	        	if(radialShow)
	        	{
	        		GL11.glPushMatrix();
	        		float scaleee = 0.75F;
	        		GL11.glScalef(scaleee, scaleee, scaleee);
	        		String name = (selected ? EnumChatFormatting.YELLOW : (info != null && info.nextState.identifier.equalsIgnoreCase(state.identifier) || info == null && state.playerMorph.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username)) ? EnumChatFormatting.GOLD : "") + ent.getEntityName();
	        		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, (int)(-3 - (Minecraft.getMinecraft().fontRenderer.getStringWidth(name) / 2) * scaleee), 5, 16777215);
	        		GL11.glPopMatrix();
	        	}
	        	else
	        	{
	        		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow((selected ? EnumChatFormatting.YELLOW : (info != null && info.nextState.entInstance.getEntityName().equalsIgnoreCase(state.entInstance.getEntityName()) || info == null && ent.getEntityName().equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username)) ? EnumChatFormatting.GOLD : "") + ent.getEntityName(), 26, -32, 16777215);
	        	}
	        	
	        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        }
	        
        	if(state != null && !state.playerMorph.equalsIgnoreCase(state.playerName) && state.isFavourite)
        	{
    			double pX = 9.5D;
    			double pY = -33.5D;
				double size = 9D;
    			
	        	Minecraft.getMinecraft().getTextureManager().bindTexture(rlFavourite);
	        	
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		        Tessellator tessellator = Tessellator.instance;
				tessellator.setColorRGBA(255, 255, 255, 255);
				
		        tessellator.startDrawingQuads();
				double iconX = pX;
				double iconY = pY;
				
		        tessellator.addVertexWithUV(iconX, iconY + size, 0.0D, 0.0D, 1.0D);
		        tessellator.addVertexWithUV(iconX + size, iconY + size, 0.0D, 1.0D, 1.0D);
		        tessellator.addVertexWithUV(iconX + size, iconY, 0.0D, 1.0D, 0.0D);
		        tessellator.addVertexWithUV(iconX, iconY, 0.0D, 0.0D, 0.0D);
		        tessellator.draw();
		        
		        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.6F);
		        
		        tessellator.startDrawingQuads();
				iconX = pX + 1D;
				iconY = pY + 1D;
				
		        tessellator.addVertexWithUV(iconX, iconY + size, -1.0D, 0.0D, 1.0D);
		        tessellator.addVertexWithUV(iconX + size, iconY + size, -1.0D, 1.0D, 1.0D);
		        tessellator.addVertexWithUV(iconX + size, iconY, -1.0D, 1.0D, 0.0D);
		        tessellator.addVertexWithUV(iconX, iconY, -1.0D, 0.0D, 0.0D);
		        tessellator.draw();
        	}
	        
        	if(Morph.showAbilitiesInGui == 1)
        	{
	        	ArrayList<Ability> abilities = AbilityHandler.getEntityAbilities(ent.getClass());
	        	
	        	int abilitiesSize = abilities.size();
	    		for(int i = abilities.size() - 1; i >= 0; i--)
	    		{
	    			if(!abilities.get(i).entityHasAbility(ent))
	    			{
	    				abilitiesSize--;
	    			}
	    		}
	        	
	        	boolean shouldScroll = false; 
	        	
	        	final int stencilBit = MinecraftForgeClient.reserveStencilBit();
	        	
				if(stencilBit >= 0 && abilitiesSize > 3)
				{
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

					if(state != null && selectedState == state)
					{
						shouldScroll = true;
					}
					
					if(shouldScroll)
					{
						final int stencilMask = 1 << stencilBit;
						
				        GL11.glEnable(GL11.GL_STENCIL_TEST);
				        GL11.glColorMask(false, false, false, false);
	
				        GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
				        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);  // draw 1s on test fail (always)
				        GL11.glStencilMask(stencilMask);
				        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	
						drawColourOnScreen(255, 255, 255, 255, -20.5D, -32.5D, 40D, 35D, -10D);
						
						GL11.glStencilMask(0x00);
						GL11.glStencilFunc(GL11.GL_EQUAL, stencilMask, stencilMask);
	
						
				        GL11.glColorMask(true, true, true, true);
					}
				}
	        	
        		int offsetX = 0;
        		int offsetY = 0;
	        	for(int i = 0; i < (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3 ? abilities.size() * 2 : abilities.size()); i++)
	        	{
	        		Ability ability = abilities.get(i >= abilities.size() ? i - abilities.size() : i);
	        		
	        		if(!ability.entityHasAbility(ent) || (abilitiesSize > 3 && stencilBit >= 0 && abilities.size() > 3) && !shouldScroll && i >= 3)
	        		{
	        			continue;
	        		}
	        		ResourceLocation loc = ability.getIcon();
	        		if(loc != null)
	        		{
	        			double pX = -20.5D;
	        			double pY = -33.5D;
						double size = 12D;
	        			
						if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
						{
		        			int round = abilityScroll % (30 * abilities.size());
		        			
		        			pY -= (size + 1) * (double)(round + (double)renderTick) / 30D;
						}
	        			
			        	Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
			        	
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				        Tessellator tessellator = Tessellator.instance;
						tessellator.setColorRGBA(255, 255, 255, 255);
						
				        tessellator.startDrawingQuads();
						double iconX = pX + (offsetX * (size + 1));
						double iconY = pY + (offsetY * (size + 1));
						
				        tessellator.addVertexWithUV(iconX, iconY + size, 0.0D, 0.0D, 1.0D);
				        tessellator.addVertexWithUV(iconX + size, iconY + size, 0.0D, 1.0D, 1.0D);
				        tessellator.addVertexWithUV(iconX + size, iconY, 0.0D, 1.0D, 0.0D);
				        tessellator.addVertexWithUV(iconX, iconY, 0.0D, 0.0D, 0.0D);
				        tessellator.draw();
				        
				        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.6F);
				        
				        tessellator.startDrawingQuads();
						size = 12D;
						iconX = pX + 1D + (offsetX * (size + 1));
						iconY = pY + 1D + (offsetY * (size + 1));
						
				        tessellator.addVertexWithUV(iconX, iconY + size, -1.0D, 0.0D, 1.0D);
				        tessellator.addVertexWithUV(iconX + size, iconY + size, -1.0D, 1.0D, 1.0D);
				        tessellator.addVertexWithUV(iconX + size, iconY, -1.0D, 1.0D, 0.0D);
				        tessellator.addVertexWithUV(iconX, iconY, -1.0D, 0.0D, 0.0D);
				        tessellator.draw();

				        offsetY++;
				        if(offsetY == 3 && stencilBit < 0)
				        {
				        	offsetY = 0;
				        	offsetX++;
				        }
	        		}
	        	}
	        	
				if(stencilBit >= 0 && abilities.size() > 3 && shouldScroll)
				{
			        GL11.glDisable(GL11.GL_STENCIL_TEST);
				}
				
				MinecraftForgeClient.releaseStencilBit(stencilBit);
        	}
        	GL11.glTranslatef(0.0F, 0.0F, -100F);

	        
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
    
    public void selectRadialMenu()
    {
    	double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
    	double magAcceptance = 0.8D;

    	double radialAngle = -720F;
    	if(mag > magAcceptance)
    	{
    		//is on radial menu
    		double aSin = Math.toDegrees(Math.asin(Morph.proxy.tickHandlerClient.radialDeltaX));
    		
    		if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
    		{
    			radialAngle = aSin;
    		}
    		else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX >= 0)
    		{
    			radialAngle = 90D + (90D - aSin);
    		}
    		else if(Morph.proxy.tickHandlerClient.radialDeltaY < 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
    		{
    			radialAngle = 180D - aSin;
    		}
    		else if(Morph.proxy.tickHandlerClient.radialDeltaY >= 0 && Morph.proxy.tickHandlerClient.radialDeltaX < 0)
    		{
    			radialAngle = 270D + (90D + aSin);
    		}
    	}
    	else
    	{
    		return;
    	}
    	
    	if(mag > 0.9999999D)
    	{
    		mag = Math.round(mag);
    	}
    	
    	for(int i = 0; i < favouriteStates.size(); i++)
    	{
    		double angle = Math.PI * 2 * i / favouriteStates.size();
    		
    		angle -= Math.toRadians(90D);
    		
    		int radius = 80;
    		
    		float leeway = 360F / favouriteStates.size();
    		
    		boolean selected = false;
    		
    		if(mag > magAcceptance * 0.75D && (i == 0 && (radialAngle < (leeway / 2) && radialAngle >= 0F || radialAngle > (360F) - (leeway / 2)) || i != 0 && radialAngle < (leeway * i) + (leeway / 2) && radialAngle > (leeway * i ) - (leeway / 2)))
    		{
    			favouriteStates.get(i);
    			
    	        MorphInfoClient info = playerMorphInfo.get(Minecraft.getMinecraft().thePlayer.username);
    	        
    			if(info != null && !info.nextState.identifier.equalsIgnoreCase(favouriteStates.get(i).identifier) || info == null && !favouriteStates.get(i).playerMorph.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username))
    			{
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					DataOutputStream stream = new DataOutputStream(bytes);
					try
					{
						stream.writeBoolean(false);
						stream.writeUTF(favouriteStates.get(i).identifier);
						
						PacketDispatcher.sendPacketToServer(new Packet131MapData((short)Morph.getNetId(), (short)0, bytes.toByteArray()));
					}
					catch(IOException e)
					{
					}
	    			return;
    			}
    		}
    	}

    }
	
    public static boolean isPressed(int key)
    {
    	if(key < 0)
    	{
    		return Mouse.isButtonDown(key + 100);
    	}
    	return Keyboard.isKeyDown(key);
    }
    
	public static void drawColourOnScreen(int r, int g, int b, int alpha, double posX, double posY, double width, double height, double zLevel)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(r, g, b, alpha);
        tessellator.addVertex(posX		  , posY + height	, zLevel);
        tessellator.addVertex(posX + width, posY + height	, zLevel);
        tessellator.addVertex(posX + width, posY			, zLevel);
        tessellator.addVertex(posX		  , posY			, zLevel);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public long clock;
	
	public boolean shouldReplaceRenderer;
	
	public RenderMorph renderMorphInstance;
	public RenderPlayerHand renderHandInstance;
	public float playerRenderShadowSize = -1F;
	
	public HashMap<String, MorphInfoClient> playerMorphInfo = new HashMap<String, MorphInfoClient>();
	
	public LinkedHashMap<String, ArrayList<MorphState>> playerMorphCatMap = new LinkedHashMap<String, ArrayList<MorphState>>();

	public float renderTick;
	
	public float playerHeight = 1.8F;
	public float ySize;
	public float eyeHeight;
	public boolean shiftedPosY;
	
	public boolean allowRender;
	
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
	public boolean keyFavouriteDown;
	
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
	
	public int abilityScroll;
	
	public boolean radialShow;
	public float radialPlayerYaw;
	public float radialPlayerPitch;
	public double radialDeltaX;
	public double radialDeltaY;
	public int radialTime;
	
	public ArrayList<MorphState> favouriteStates = new ArrayList<MorphState>();
	
	public final int selectorShowTime = 10;
	public final int scrollTime = 3;

	public static final ResourceLocation rlFavourite = new ResourceLocation("morph", "textures/gui/fav.png");
	public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/guiSelected.png");
	public static final ResourceLocation rlUnselected = new ResourceLocation("morph", "textures/gui/guiUnselected.png");
	public static final ResourceLocation rlUnselectedSide = new ResourceLocation("morph", "textures/gui/guiUnselectedSide.png");
}