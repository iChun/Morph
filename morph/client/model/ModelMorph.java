package morph.client.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class ModelMorph extends ModelBase 
{

	public MorphInfoClient morphInfo;
	
	public ArrayList<ModelRenderer> modelList;
	
	public ArrayList<ModelRenderer> prevModelList;
	public ArrayList<ModelRenderer> nextModelList;
	
	public Random rand;
	
	public ModelMorph(){}
	
	public ModelMorph(MorphInfoClient info)
	{
		morphInfo = info;
		rand = new Random();
		
		if(info != null && info.morphProgress < 80 && info.prevModelInfo != null && info.nextModelInfo != null)
		{
			prevModelList = ModelHelper.compileRenderableModels(info.prevModelInfo, info.prevState.entInstance);
			nextModelList = ModelHelper.compileRenderableModels(info.nextModelInfo, info.nextState.entInstance);
			
			modelList = ModelHelper.getModelCubesCopy(info.prevModelInfo, this, info.prevState.entInstance);
			
			for(int i = 0; i < nextModelList.size(); i++)
			{
				if(i >= modelList.size())
				{
					break;
				}
				
				ModelRenderer cubeCopy = modelList.get(i);
				ModelRenderer cubeNewParent = nextModelList.get(i);
				
				ModelHelper.createEmptyContents(this, cubeNewParent, cubeCopy, 0); 			
			}
			
			if(modelList.size() < nextModelList.size())
			{
				for(int i = modelList.size(); i < nextModelList.size(); i++)
				{
					ModelRenderer parentCube = nextModelList.get(i);
					try
					{
						int txOffsetX = (Integer)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, parentCube, ObfHelper.textureOffsetX);
						int txOffsetY = (Integer)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, parentCube, ObfHelper.textureOffsetY);
						ModelRenderer cubeCopy = new ModelRenderer(this, txOffsetX, txOffsetY);
						cubeCopy.mirror = parentCube.mirror;
						cubeCopy.textureHeight = parentCube.textureHeight;
						cubeCopy.textureWidth = cubeCopy.textureWidth;
						
						for(int j = 0; j < parentCube.cubeList.size(); j++)
						{
							ModelBox box = (ModelBox)parentCube.cubeList.get(j);
							float param7 = 0.0F;
							
							ModelBox randBox;
							if(modelList.size() > 0)
							{
								ModelRenderer randCube = modelList.get(rand.nextInt(modelList.size()));
								randBox = (ModelBox)randCube.cubeList.get(rand.nextInt(randCube.cubeList.size()));
							}
							else
							{
								ModelRenderer randParentCube = nextModelList.get(rand.nextInt(nextModelList.size()));
								randBox = (ModelBox)randParentCube.cubeList.get(rand.nextInt(randParentCube.cubeList.size()));
							}
							
							float x = randBox.posX1 + ((randBox.posX2 - randBox.posX1) > 0F ? rand.nextInt((int)(randBox.posX2 - randBox.posX1)) : 0F);
							float y = randBox.posY1 + ((randBox.posY2 - randBox.posY1) > 0F ? rand.nextInt((int)(randBox.posY2 - randBox.posY1)) : 0F);
							float z = randBox.posZ1 + ((randBox.posZ2 - randBox.posZ1) > 0F ? rand.nextInt((int)(randBox.posZ2 - randBox.posZ1)) : 0F);
							
							cubeCopy.addBox(x, y, z, 0, 0, 0, param7);
						}
	
						cubeCopy.setRotationPoint(parentCube.rotationPointX, parentCube.rotationPointY, parentCube.rotationPointZ);
						
						cubeCopy.rotateAngleX = parentCube.rotateAngleX;
						cubeCopy.rotateAngleY = parentCube.rotateAngleY;
						cubeCopy.rotateAngleZ = parentCube.rotateAngleZ;
						
						modelList.add(cubeCopy);
						
						ModelHelper.createEmptyContents(this, parentCube, cubeCopy, 0);
					}
					catch(Exception e)
					{
						ObfHelper.obfWarning();
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		GL11.glPushMatrix();

		ArrayList<ModelRenderer> prevCubes = prevModelList;
		ArrayList<ModelRenderer> nextCubes = nextModelList;
		
		for(int i = 0; i < nextCubes.size(); i++)
		{
			ModelRenderer cube = modelList.get(i);
			if(i < prevCubes.size() && morphInfo.morphProgress <= 40)
			{
				ModelRenderer currentMorphCube = prevCubes.get(i);
				ModelRenderer nextMorphCube = nextCubes.get(i);
				float mag = (float)Math.pow((morphInfo.morphProgress - 10F + Morph.proxy.tickHandlerClient.renderTick) / 30F, 2);
				cube.rotateAngleX = currentMorphCube.rotateAngleX + (nextMorphCube.rotateAngleX - currentMorphCube.rotateAngleX) * mag;
				cube.rotateAngleY = currentMorphCube.rotateAngleY + (nextMorphCube.rotateAngleY - currentMorphCube.rotateAngleY) * mag;
				cube.rotateAngleZ = currentMorphCube.rotateAngleZ + (nextMorphCube.rotateAngleZ - currentMorphCube.rotateAngleZ) * mag;
			}
			else
			{
				ModelRenderer nextMorphCube = nextCubes.get(i);
				cube.rotateAngleX = nextMorphCube.rotateAngleX;
				cube.rotateAngleY = nextMorphCube.rotateAngleY;
				cube.rotateAngleZ = nextMorphCube.rotateAngleZ;
			}
			if(i < prevCubes.size() && morphInfo.morphProgress <= 60)
			{
				ModelRenderer parentCube = prevCubes.get(i);
				ModelRenderer newParentCube = nextCubes.get(i);
				float mag = (float)Math.pow((morphInfo.morphProgress - 10F + Morph.proxy.tickHandlerClient.renderTick) / 50F, 2D);
				
				cube.rotationPointX = parentCube.rotationPointX + (newParentCube.rotationPointX - parentCube.rotationPointX) * mag;
				cube.rotationPointY = parentCube.rotationPointY + (newParentCube.rotationPointY - parentCube.rotationPointY) * mag;
				cube.rotationPointZ = parentCube.rotationPointZ + (newParentCube.rotationPointZ - parentCube.rotationPointZ) * mag;
			}
			else
			{
				ModelRenderer nextMorphCube = nextCubes.get(i);
				cube.rotationPointX = nextMorphCube.rotationPointX;
				cube.rotationPointY = nextMorphCube.rotationPointY;
				cube.rotationPointZ = nextMorphCube.rotationPointZ;
			}
		}

		if(morphInfo.morphProgress <= 60)
		{
			float param7 = 0.0F;
			float mag = (float)Math.pow((morphInfo.morphProgress - 10F + Morph.proxy.tickHandlerClient.renderTick) / 50F, 2);
			
			updateCubeMorph(modelList, prevCubes, nextCubes, param7, mag, 0);
		}
		
		FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
		FloatBuffer buffer1 = GLAllocation.createDirectFloatBuffer(16);
		
		GL11.glPushMatrix();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
		ObfHelper.invokePreRenderCallback(morphInfo.prevModelInfo.getRenderer(), morphInfo.prevModelInfo.getRenderer().getClass(), morphInfo.prevState.entInstance, Morph.proxy.tickHandlerClient.renderTick);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
		GL11.glPopMatrix();
		
		float prevScaleX = buffer1.get(0) / buffer.get(0);
		float prevScaleY = buffer1.get(5) / buffer.get(5);
		float prevScaleZ = buffer1.get(8) / buffer.get(8);

		GL11.glPushMatrix();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
		ObfHelper.invokePreRenderCallback(morphInfo.nextModelInfo.getRenderer(), morphInfo.nextModelInfo.getRenderer().getClass(), morphInfo.nextState.entInstance, Morph.proxy.tickHandlerClient.renderTick);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer1);
		GL11.glPopMatrix();
		
		float nextScaleX = buffer1.get(0) / buffer.get(0);
		float nextScaleY = buffer1.get(5) / buffer.get(5);
		float nextScaleZ = buffer1.get(8) / buffer.get(8);
		
		float progress = ((float)morphInfo.morphProgress - 10F + Morph.proxy.tickHandlerClient.renderTick) / 60F;

		float scaleX = prevScaleX + (nextScaleX - prevScaleX) * progress;
		float scaleY = prevScaleY + (nextScaleY - prevScaleY) * progress;
		float scaleZ = prevScaleZ + (nextScaleZ - prevScaleZ) * progress;

		double offset = (1.0F - scaleY) * Minecraft.getMinecraft().thePlayer.yOffset;
		
		GL11.glTranslated(0.0F, offset, 0.0F);
		
		GL11.glScalef(scaleX, scaleY, scaleZ);
		
		for(ModelRenderer cube : modelList)
		{
			cube.render(f5);
		}
		
		GL11.glPopMatrix();
	}
	
	@Override
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) 
	{
	}
	
	public static void updateCubeMorph(List morphCubesList, List currentMorphCubes, List nextMorphCubes, float param7, float mag, int depth)
	{
		if(morphCubesList == null || depth > 20)
		{
//			System.out.println("null :(");
//			System.out.println(depth);
			return;
		}
		if(currentMorphCubes == null)
		{
			currentMorphCubes = new ArrayList();
		}
		if(nextMorphCubes == null)
		{
			nextMorphCubes = new ArrayList();
		}
		try
		{								
			for(int i = 0; i < morphCubesList.size(); i++)
			{
				ModelRenderer cube = (ModelRenderer)morphCubesList.get(i);
				for(int j = cube.cubeList.size() - 1; j >= 0 ; j--)
				{
					ModelBox box = (ModelBox)cube.cubeList.get(j);
					
					float nextXpos = 0.0F;
					float nextYpos = 0.0F;
					float nextZpos = 0.0F;
					
					float prevXpos = 0.0F;
					float prevYpos = 0.0F;
					float prevZpos = 0.0F;
					
					int newXSize = 0;
					int newYSize = 0;
					int newZSize = 0;
					
					int prevXSize = 0;
					int prevYSize = 0;
					int prevZSize = 0;
					
					ModelBox newBox = null;
					ModelBox prevBox = null;
					
					ModelRenderer nextMorphCube = null;
					ModelRenderer currentMorphCube = null;

					if(i < nextMorphCubes.size()) 
					{
						nextMorphCube = (ModelRenderer)nextMorphCubes.get(i);

						cube.mirror = nextMorphCube.mirror;

						if(i < currentMorphCubes.size()) // meaning current morph has <= cubes than next morph
						{
							currentMorphCube = (ModelRenderer)currentMorphCubes.get(i);

							if(j < nextMorphCube.cubeList.size())
							{
								newBox = (ModelBox)nextMorphCube.cubeList.get(j);

								if(j < currentMorphCube.cubeList.size()) // meaning current cube has <= boxes than next cube
								{
									prevBox = (ModelBox)currentMorphCube.cubeList.get(j);													
								}
							}
							else if(j < currentMorphCube.cubeList.size()) // should enter since morphCubes is copy of current. Next morph does not have said boxes, so shrink to non existence.
							{
								prevBox = (ModelBox)currentMorphCube.cubeList.get(j);
							}
						}
						else if(j < nextMorphCube.cubeList.size()) 
						{
							newBox = (ModelBox)nextMorphCube.cubeList.get(j);
						}
					}
					else if(i < currentMorphCubes.size())
					{
						currentMorphCube = (ModelRenderer)currentMorphCubes.get(i);

						if(j < currentMorphCube.cubeList.size()) // should enter since morphCubes is copy of current. Next morph does not have said boxes, so shrink to non existence.
						{
							prevBox = (ModelBox)currentMorphCube.cubeList.get(j);
						}
					}
					
					if(newBox != null)
					{
						newXSize = (int)Math.abs(newBox.posX2 - newBox.posX1);
						newYSize = (int)Math.abs(newBox.posY2 - newBox.posY1);
						newZSize = (int)Math.abs(newBox.posZ2 - newBox.posZ1);
						
						nextXpos = newBox.posX1;
						nextYpos = newBox.posY1;
						nextZpos = newBox.posZ1;
					}
					if(prevBox != null)
					{
						prevXSize = (int)Math.abs(prevBox.posX2 - prevBox.posX1);
						prevYSize = (int)Math.abs(prevBox.posY2 - prevBox.posY1);
						prevZSize = (int)Math.abs(prevBox.posZ2 - prevBox.posZ1);
						
						prevXpos = prevBox.posX1;
						prevYpos = prevBox.posY1;
						prevZpos = prevBox.posZ1;
					}
					
					cube.cubeList.remove(j);
					
					cube.addBox(prevXpos + (nextXpos - prevXpos) * mag, prevYpos + (nextYpos - prevYpos) * mag, prevZpos + (nextZpos - prevZpos) * mag, (int)Math.round(prevXSize + (newXSize - prevXSize) * mag), (int)Math.round(prevYSize + (newYSize - prevYSize) * mag), (int)Math.round(prevZSize + (newZSize - prevZSize) * mag), param7);
					
					updateCubeMorph(cube.childModels, currentMorphCube == null ? null : currentMorphCube.childModels, nextMorphCube == null ? null : nextMorphCube.childModels, param7, mag, depth + 1);
				}
				ObfuscationReflectionHelper.setPrivateValue(ModelRenderer.class, cube, false, ObfHelper.compiled);
			}
		}
		catch(Exception e)
		{
			ObfHelper.obfWarning();
			e.printStackTrace();
		}
	}

	
	
	
	
	
	
	
	
	
	
	
}
