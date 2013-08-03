package morph.client.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.client.model.ModelMorph;
import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
		if(mc.currentScreen != null && selectorShow)
		{
			selectorShow = false;
			selectorTimer = selectorShowTime - selectorTimer;
		}
		if(selectorTimer > 0)
		{
			selectorTimer--;
			if(selectorTimer == 0 && !selectorShow)
			{
				selectorSelected = 0;
			}
		}
		
		if(selectorShow)
		{
			int k = Mouse.getDWheel();
			if(k != 0)
			{
				if(k > 0)
				{
					selectorSelected--;
					if(selectorSelected < 0)
					{
						selectorSelected = selectorSize;
					}
				}
				else
				{
					selectorSelected++;
					if(selectorSelected > selectorSize)
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
				
				if(info.morphProgress < 10)
				{
					if(info.prevEntInstance != mc.thePlayer)
					{
						info.prevEntInstance.onUpdate();
					}
				}
				else if(info.morphProgress > 70)
				{
					if(info.nextEntInstance != mc.thePlayer)
					{
						info.nextEntInstance.onUpdate();
					}
				}
			}
			
			if(!keySelectorBackDown && isPressed(Morph.keySelectorBack))
			{
				if(!selectorShow)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
				}
				else
				{
					selectorSelected--;
					if(selectorSelected < 0)
					{
						selectorSelected = selectorSize;
					}
				}
			}
			if(!keySelectorForwardDown && isPressed(Morph.keySelectorForward))
			{
				if(!selectorShow)
				{
					selectorShow = true;
					selectorTimer = selectorShowTime - selectorTimer;
				}
				else
				{
					selectorSelected++;
					if(selectorSelected > selectorSize)
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
					//MORPH
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
	        GL11.glEnable(3042 /*GL_BLEND*/);
	        GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glDepthMask(false);
	        GL11.glBlendFunc(770, 771);
	        GL11.glColor4f(1f,1f,1f,1f);
	        GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
	        
	        for(int i = 0 ; i < 5; i++)
	        {
	        	double height1 = gap + size * i;
	        
		        mc.func_110434_K().func_110577_a(selectorSelected == i ? rlSelected : rlUnselected);
		        Tessellator tessellator = Tessellator.instance;
		        tessellator.startDrawingQuads();
				tessellator.setColorOpaque_F(1f,1f,1f);
		        tessellator.addVertexWithUV(width1, height1 + size, -90.0D, 0.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1 + size, -90.0D, 1.0D, 1.0D);
		        tessellator.addVertexWithUV(width1 + size, height1, -90.0D, 1.0D, 0.0D);
		        tessellator.addVertexWithUV(width1, height1, -90.0D, 0.0D, 0.0D);
		        tessellator.draw();
	        }
	        
	        GL11.glDepthMask(true);
	        GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
	        GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
	        GL11.glDisable(3042 /*GL_BLEND*/);
	        GL11.glColor4f(1f,1f,1f,1f);
	        GL11.glPopMatrix();
			
	        gap += 36;
	        
			drawEntityOnScreen(mc.thePlayer, 20, gap, 16, 2, 2, renderTick);
			drawEntityOnScreen(playerMorphInfo.get(mc.thePlayer.username).nextEntInstance, 20, gap + 42, 16, 2, 2, renderTick);
			
			int x = 0;
			for(int i = 0; i < world.loadedEntityList.size(); i++)
			{
				Entity ent = (Entity)world.loadedEntityList.get(i);
				if(ent instanceof EntityLivingBase)
				{
					x++;
					drawEntityOnScreen((EntityLivingBase)ent, 20, gap + 42 + (x * 42), 16, 2, 2, renderTick);
					if(x >= 3)
					{
						break;
					}
				}
			}
			GL11.glPopMatrix();
		}
	}
	
    public void drawEntityOnScreen(EntityLivingBase ent, int posX, int posY, int scale, float par4, float par5, float renderTick)
    {
    	forceRender = true;
    	if(ent != null)
    	{
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

	public float renderTick;
	
	public boolean forceRender;
	public boolean renderingMorph;
	public byte renderingPlayer;
	
	public boolean keySelectorBackDown;
	public boolean keySelectorForwardDown;
	public boolean keySelectorChooseDown;
	public boolean keySelectorReturnDown;
	
	public boolean selectorShow;
	public int selectorTimer;
	public int selectorSelected; //0 to 4;
	public long systemTime;
	public int currentItem;
	
	public final int selectorShowTime = 10;
	public final int selectorSize = 4;
	
	public static final ResourceLocation rlSelected = new ResourceLocation("morph", "textures/gui/guiSelected.png");
	public static final ResourceLocation rlUnselected= new ResourceLocation("morph", "textures/gui/guiUnselected.png");
	
}