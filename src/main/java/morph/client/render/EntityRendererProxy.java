package morph.client.render;

import java.util.List;

import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class EntityRendererProxy extends EntityRenderer 
{
	private Entity pointedEntity;
	private Minecraft mc;
	
	public EntityRendererProxy(Minecraft par1Minecraft) 
	{
		super(par1Minecraft);
		mc = par1Minecraft;
	}
	
	@Override
    public void getMouseOver(float par1)
    {
        if (this.mc.renderViewEntity != null)
        {
            if (this.mc.theWorld != null)
            {
                this.mc.pointedEntityLiving = null;
                double d0 = (double)this.mc.playerController.getBlockReachDistance();
                
                float ySize = 0.0F;
                
                if(!Morph.proxy.tickHandlerClient.shiftedPosY)
                {
	                MorphInfo info1 = Morph.proxy.tickHandlerClient.playerMorphInfo.get(mc.thePlayer.username);
	        		if(info1 != null && mc.renderViewEntity == mc.thePlayer)
	        		{
	        			float prog = info1.morphProgress > 10 ? (((float)info1.morphProgress + par1) / 60F) : 0.0F;
	        			if(prog > 1.0F)
	        			{
	        				prog = 1.0F;
	        			}
	        			
	        			prog = (float)Math.pow(prog, 2);
	        			
	        			float prev = info1.prevState != null && !(info1.prevState.entInstance instanceof EntityPlayer) ? info1.prevState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
	        			float next = info1.nextState != null && !(info1.nextState.entInstance instanceof EntityPlayer) ? info1.nextState.entInstance.getEyeHeight() : mc.thePlayer.yOffset;
	        			ySize = mc.thePlayer.yOffset - (prev + (next - prev) * prog);
	        		}
	        		
	    			mc.thePlayer.lastTickPosY -= ySize;
	    			mc.thePlayer.prevPosY -= ySize;
	    			mc.thePlayer.posY -= ySize;
                }
        		
       			this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(d0, par1);
                
                
                double d1 = d0;
                Vec3 vec3 = this.mc.renderViewEntity.getPosition(par1);

                if (this.mc.playerController.extendedReach())
                {
                    d0 = 6.0D;
                    d1 = 6.0D;
                }
                else
                {
                    if (d0 > 3.0D)
                    {
                        d1 = 3.0D;
                    }

                    d0 = d1;
                }

                if (this.mc.objectMouseOver != null)
                {
                    d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
                }

                Vec3 vec31 = this.mc.renderViewEntity.getLook(par1);
                Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
                this.pointedEntity = null;
                float f1 = 1.0F;
                List list = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f1, (double)f1, (double)f1));
                double d2 = d1;

                for (int i = 0; i < list.size(); ++i)
                {
                    Entity entity = (Entity)list.get(i);

                    if (entity.canBeCollidedWith())
                    {
                        float f2 = entity.getCollisionBorderSize();
                        AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
                        MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                        if (axisalignedbb.isVecInside(vec3))
                        {
                            if (0.0D < d2 || d2 == 0.0D)
                            {
                                this.pointedEntity = entity;
                                d2 = 0.0D;
                            }
                        }
                        else if (movingobjectposition != null)
                        {
                            double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                            if (d3 < d2 || d2 == 0.0D)
                            {
                                if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract())
                                {
                                    if (d2 == 0.0D)
                                    {
                                        this.pointedEntity = entity;
                                    }
                                }
                                else
                                {
                                    this.pointedEntity = entity;
                                    d2 = d3;
                                }
                            }
                        }
                    }
                }

                if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null))
                {
                    this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity);

                    if (this.pointedEntity instanceof EntityLivingBase)
                    {
                        this.mc.pointedEntityLiving = (EntityLivingBase)this.pointedEntity;
                    }
                }
                
    			mc.thePlayer.lastTickPosY += ySize;
    			mc.thePlayer.prevPosY += ySize;
    			mc.thePlayer.posY += ySize;
            }
        }
    }
}
