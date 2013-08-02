package morph.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import morph.client.morph.MorphInfoClient;
import morph.common.core.ObfHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class ModelMorph extends ModelBase 
{

	public MorphInfoClient morphInfo;
	
	public ArrayList<ModelRenderer> modelList;
	
	public Random rand;
	
	public ModelMorph(){}
	
	public ModelMorph(MorphInfoClient info)
	{
		morphInfo = info;
		modelList = ModelHelper.getModelCubesCopy(info.prevEntInfo, this);
		rand = new Random();
		
		for(int i = 0; i < modelList.size(); i++)
		{
			if(i >= morphInfo.nextEntInfo.modelList.size())
			{
				break;
			}
			
			ModelRenderer cubeCopy = modelList.get(i);
			ModelRenderer cubeNewParent = morphInfo.nextEntInfo.modelList.get(i);
			
			ModelHelper.createEmptyContents(this, cubeNewParent, cubeCopy); 			
		}
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		for(ModelRenderer cube : modelList)
		{
			cube.render(f5);
		}
	}
	
	@Override
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) 
	{
	}
	
	public static void updateCubeMorph(List morphCubesList, List currentMorphCubes, List nextMorphCubes, float param7, float mag, int depth)
	{
		if(morphCubesList == null || depth > 20)
		{
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
