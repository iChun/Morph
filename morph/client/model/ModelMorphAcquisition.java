package morph.client.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import morph.client.entity.EntityMorphAcquisition;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;

public class ModelMorphAcquisition extends ModelMorph
{
	public EntityMorphAcquisition morphEnt;
	
	public ModelInfo acquired;
	public ModelInfo acquirer;
	
	public ArrayList<ModelRenderer> modelListCopy;
	
	public float renderYaw;
	
	public ModelMorphAcquisition(EntityMorphAcquisition ent)
	{
		super(null);
		this.morphEnt = ent;
		this.acquired = ModelList.getModelInfo(ent.acquired.getClass());
		this.acquirer = ModelList.getModelInfo(ent.acquirer.getClass());
		if(acquired != null)
		{
			this.modelList = ModelHelper.getModelCubesCopy(acquired, this, ent.acquired);
			this.modelListCopy = ModelHelper.getModelCubesCopy(acquired, this, ent.acquired);
			
			for(ModelRenderer cube : modelList)
			{
				cube.rotationPointY -= 8.0D;
			}
			for(ModelRenderer cube : modelListCopy)
			{
				cube.rotationPointY -= 8.0D;
			}
		}
		
		this.renderYaw = ent.acquired.renderYawOffset;
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		GL11.glPushMatrix();

		GL11.glRotatef(renderYaw, 0.0F, 1.0F, 0.0F);
		
		if(morphEnt.progress <= 40F)
		{
			float param7 = 0.0F;
			float mag = (float)Math.pow((morphEnt.progress + Morph.proxy.tickHandlerClient.renderTick) / 40F, 2);
			
			updateCubeMorph(modelList, modelListCopy, null, param7, mag, 0);
		}
		
		FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
		FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);
		
		GL11.glPushMatrix();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
		ObfHelper.invokePreRenderCallback(acquired.getRenderer(), acquired.getRenderer().getClass(), morphEnt.acquired, Morph.proxy.tickHandlerClient.renderTick);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
		GL11.glPopMatrix();
		
		float prevScaleX = buffer1.get(0) / buffer.get(0);
		float prevScaleY = buffer1.get(5) / buffer.get(5);
		float prevScaleZ = buffer1.get(8) / buffer.get(8);

		GL11.glPushMatrix();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
		ObfHelper.invokePreRenderCallback(acquirer.getRenderer(), acquirer.getRenderer().getClass(), morphEnt.acquirer, Morph.proxy.tickHandlerClient.renderTick);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
		GL11.glPopMatrix();
		
		float nextScaleX = buffer1.get(0) / buffer.get(0);
		float nextScaleY = buffer1.get(5) / buffer.get(5);
		float nextScaleZ = buffer1.get(8) / buffer.get(8);
		
		float progress = ((float)morphEnt.progress + Morph.proxy.tickHandlerClient.renderTick) / 50F;
		if(progress < 0.0F)
		{
			progress = 0.0F;
		}
		
		progress = (float)Math.pow(progress, 2);

		float scaleX = prevScaleX + (nextScaleX - prevScaleX) * progress;
		float scaleY = prevScaleY + (nextScaleY - prevScaleY) * progress;
		float scaleZ = prevScaleZ + (nextScaleZ - prevScaleZ) * progress;

		double offset = (1.0F - scaleY) * Minecraft.getMinecraft().thePlayer.yOffset;
		
		GL11.glTranslated(0.0F, offset, 0.0F);
		
		GL11.glScalef(scaleX, scaleY, scaleZ);

		for(int i = 0; i < modelList.size(); i++)
		{
			ModelRenderer cube = modelList.get(i);
			
			rand.setSeed(i * 1000);
			GL11.glRotatef(560F * rand.nextFloat() * progress, rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress, rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress, rand.nextFloat() * (rand.nextFloat() > 0.5F ? -1 : 1) * progress);
			GL11.glTranslated(rand.nextDouble() * progress * 0.3D, rand.nextDouble() * progress * 0.3D, rand.nextDouble() * progress * 0.3D);
			cube.render(f5);
		}
		
		GL11.glPopMatrix();
	}
}
