package morph.client.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import morph.common.Morph;
import morph.common.core.ObfHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class ModelHelper 
{
	public static Random rand = new Random();

	public static ArrayList<ModelRenderer> getModelCubesCopy(ModelInfo info, ModelBase base, EntityLivingBase ent)
	{
		if(ent != null)
		{
			info.forceRender(ent, 0.0D, -500D, 0.0D, 0.0F, 1.0F);

			ArrayList<ModelRenderer> modelList = ModelHelper.getModelCubes(getPossibleModel(info.getRenderer()));
			
			for(int i = 0; i < modelList.size(); i++)
			{
				ModelRenderer cube = modelList.get(i);
				try
				{
					ObfuscationReflectionHelper.setPrivateValue(ModelRenderer.class, cube, false, ObfHelper.compiled);
				}
				catch(Exception e)
				{
					ObfHelper.obfWarning();
					e.printStackTrace();
				}
			}
			info.forceRender(ent, 0.0D, -500D, 0.0D, 0.0F, 1.0F);
			
			ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>();
			
			for(int i = 0; i < modelList.size(); i++)
			{
				ModelRenderer cube = modelList.get(i);
				try
				{
					if((Boolean)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, cube, ObfHelper.compiled))
					{
						list.add(buildCopy(cube, base, 0, true));
					}
				}
				catch(Exception e)
				{
					ObfHelper.obfWarning();
					e.printStackTrace();
				}
			}
			return list;
		}
		else
		{
		
			ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>();
	
			for(int i = 0; i < info.modelList.size(); i++)
			{
				ModelRenderer cube = (ModelRenderer)info.modelList.get(i);
				list.add(buildCopy(cube, base, 0, true));
			}

			return list;
		}
	}
	
	public static ModelRenderer getPotentialArm(ModelBase parent)
	{
		if(parent != null)
		{
			Class clz = parent.getClass();
			if(clz == ModelHorse.class)
			{
				ModelHorse dummy = new ModelHorse();
				
				ModelRenderer leg = new ModelRenderer(dummy, 60, 29);
		        leg.addBox(-1.1F, -1.0F, -2.1F, 3, 8, 4);
		        leg.setRotationPoint(-4.0F, 9.0F, -8.0F);
		        
		        ModelRenderer shin = new ModelRenderer(dummy, 60, 41);
		        shin.addBox(-1.1F, 0.0F, -1.6F, 3, 5, 3);
		        shin.setRotationPoint(0.0F, 7.0F, 0.0F);
		        leg.addChild(shin);
		        
		        ModelRenderer hoof = new ModelRenderer(dummy, 60, 51);
		        hoof.addBox(-1.6F, 5.1F, -2.1F, 4, 3, 4);
		        hoof.setRotationPoint(0.0F, 0.0F, 0.0F);

		        shin.addChild(hoof);
		        
		        return leg;
			}
			else
			{
				while(clz != ModelBase.class && ModelBase.class.isAssignableFrom(clz))
				{
					try
					{
						Field[] fields = clz.getDeclaredFields();
						for(Field f : fields)
						{
							f.setAccessible(true);
							if(f.getType() == ModelRenderer.class)
							{
								if(clz == ModelBiped.class && (f.getName().equalsIgnoreCase("bipedRightArm") || f.getName().equalsIgnoreCase("f") || f.getName().equalsIgnoreCase("field_78112_f")) || 
									clz == ModelQuadruped.class && (f.getName().equalsIgnoreCase("leg3") || f.getName().equalsIgnoreCase("e") || f.getName().equalsIgnoreCase("field_78147_e")) ||
									clz == ModelCreeper.class && (f.getName().equalsIgnoreCase("leg3") || f.getName().equalsIgnoreCase("f") || f.getName().equalsIgnoreCase("field_78129_f")) ||
									clz == ModelIronGolem.class && (f.getName().equalsIgnoreCase("ironGolemRightArm") || f.getName().equalsIgnoreCase("c") || f.getName().equalsIgnoreCase("field_78177_c")) ||
									clz == ModelSpider.class && (f.getName().equalsIgnoreCase("spiderLeg7") || f.getName().equalsIgnoreCase("j") || f.getName().equalsIgnoreCase("field_78210_j")) ||
									clz == ModelWolf.class && (f.getName().equalsIgnoreCase("wolfLeg3") || f.getName().equalsIgnoreCase("e") || f.getName().equalsIgnoreCase("field_78182_e")) ||
									clz == ModelOcelot.class && (f.getName().equalsIgnoreCase("ocelotFrontRightLeg") || f.getName().equalsIgnoreCase("d") || f.getName().equalsIgnoreCase("field_78157_d")) ||
									clz != ModelBiped.class && clz != ModelQuadruped.class && clz != ModelCreeper.class && clz != ModelIronGolem.class && clz != ModelSpider.class && clz != ModelWolf.class && clz != ModelOcelot.class &&  
									(f.getName().contains("Right") || f.getName().contains("right")) && (f.getName().contains("arm") || f.getName().contains("hand") || f.getName().contains("Arm") || f.getName().contains("Hand")))
								{
									ModelRenderer arm = (ModelRenderer)f.get(parent);
									if(arm != null)
									{
										return arm; // Add normal parent fields
									}
								}
							}
							else if(f.getType() == ModelRenderer[].class && clz == ModelSquid.class && (f.getName().equalsIgnoreCase("squidTentacles") || f.getName().equalsIgnoreCase("b") || f.getName().equalsIgnoreCase("field_78201_b")))
							{
								return ((ModelRenderer[])f.get(parent))[0];
							}
						}
						clz = clz.getSuperclass();
					}
					catch(Exception e)
					{
						throw new UnableToAccessFieldException(new String[0], e);
					}
				}
			}
		}

		return null;
	}

	public static ArrayList<ModelRenderer> getModelCubes(ModelBase parent)
	{
		ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>();

		ArrayList<ModelRenderer[]> list1 = new ArrayList<ModelRenderer[]>();

		if(parent != null)
		{
			Class clz = parent.getClass();
			while(clz != ModelBase.class && ModelBase.class.isAssignableFrom(clz))
			{
				try
				{
					Field[] fields = clz.getDeclaredFields();
					for(Field f : fields)
					{
						f.setAccessible(true);
						if(f.getType() == ModelRenderer.class)
						{
							if(clz == ModelBiped.class && !(f.getName().equalsIgnoreCase("bipedCloak") || f.getName().equalsIgnoreCase("k") || f.getName().equalsIgnoreCase("field_78122_k")) || clz != ModelBiped.class)
							{
								ModelRenderer rend = (ModelRenderer)f.get(parent);
								if(rend != null)
								{
									list.add(rend); // Add normal parent fields
								}
							}
						}
						else if(f.getType() == ModelRenderer[].class)
						{
							ModelRenderer[] rend = (ModelRenderer[])f.get(parent);
							if(rend != null)
							{
								list1.add(rend);
							}
						}
					}
					clz = clz.getSuperclass();
				}
				catch(Exception e)
				{
					throw new UnableToAccessFieldException(new String[0], e);
				}
			}
		}

		for(ModelRenderer[] cubes : list1)
		{
			for(ModelRenderer cube : cubes)
			{
				if(cube != null && !list.contains(cube))
				{
					list.add(cube); //Add stuff like flying blaze rods stored in MR[] fields.
				}
			}
		}

		ArrayList<ModelRenderer> children = new ArrayList<ModelRenderer>();

		for(ModelRenderer cube : list)
		{
			for(ModelRenderer child : getChildren(cube, true, 0))
			{
				//				cube.addChild(child);
				if(!children.contains(child))
				{
					children.add(child);
				}
			}
		}

		//TODO hmm...... remove the children? or keep them?
		for(ModelRenderer child : children)
		{
			list.remove(child);
		}
		
		return list;
	}
	
	public static ModelBase getPossibleModel(Render rend)
	{
		ArrayList<ArrayList<ModelBase>> models = new ArrayList<ArrayList<ModelBase>>();

		if(rend != null)
		{
			try
			{
				Class clz = rend.getClass();
				while(clz != Render.class)
				{
					ArrayList<ModelBase> priorityLevel = new ArrayList<ModelBase>();
					
					Field[] fields = clz.getDeclaredFields();
					for(Field f : fields)
					{
						f.setAccessible(true);
						if(ModelBase.class.isAssignableFrom(f.getType()))
						{
							ModelBase base = (ModelBase)f.get(rend);
							if(base != null)
							{
								priorityLevel.add(base); // Add normal parent fields
							}
						}
						else if(ModelBase[].class.isAssignableFrom(f.getType()))
						{
							ModelBase[] modelBases = (ModelBase[])f.get(rend);
							if(modelBases != null)
							{
								for(ModelBase base : modelBases)
								{
									priorityLevel.add(base);
								}
							}
						}
					}
					
					models.add(priorityLevel);
					
					if(clz == RendererLivingEntity.class)
					{
						ArrayList<ModelBase> topPriority = new ArrayList<ModelBase>();
						for(Field f : fields)
						{
							f.setAccessible(true);
							if(ModelBase.class.isAssignableFrom(f.getType()) && (f.getName().equalsIgnoreCase("mainModel") || f.getName().equalsIgnoreCase("field_77045_g")))
							{
								ModelBase base = (ModelBase)f.get(rend);
								if(base != null)
								{
									topPriority.add(base);
								}
							}
						}
						models.add(topPriority);
					}
					clz = clz.getSuperclass();
				}
			}
			catch(Exception e)
			{
				throw new UnableToAccessFieldException(new String[0], e);
			}
		}

		ModelBase base1 = null;
		int priorityLevel = -1;
		int size = -1;

		int currentPriority = 0;
		
		for (int i = 0; i < models.size(); i++) 
		{
			ArrayList<ModelBase> modelList = models.get(i);
			for(ModelBase base : modelList)
			{
				ArrayList<ModelRenderer> mrs = getModelCubes(base);
				if(mrs.size() > size || mrs.size() == size && currentPriority > priorityLevel)
				{
					size = mrs.size();
					base1 = base;
					priorityLevel = currentPriority;
				}
			}
			currentPriority++;
		}

		return base1;
	}
	
	public static ArrayList<ModelRenderer> getChildren(ModelRenderer parent, boolean recursive, int depth)
	{
		ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>();
		if(parent.childModels != null && depth < 20)
		{
			for(int i = 0; i < parent.childModels.size(); i++)
			{
				ModelRenderer child = (ModelRenderer)parent.childModels.get(i);
				if(recursive)
				{
					ArrayList<ModelRenderer> children = getChildren(child, recursive, depth + 1);
					for(ModelRenderer child1 : children)
					{
						if(!list.contains(child1))
						{
							list.add(child1);
						}
					}
				}
				if(!list.contains(child))
				{
					list.add(child);
				}
			}
		}
		return list;
	}

	public static void createEmptyContents(ModelBase base, ModelRenderer ori, ModelRenderer copy, int depth)
	{
		if(depth > 20)
		{
			return;
		}
		if(copy.cubeList.size() < ori.cubeList.size())
		{
			for(int j = copy.cubeList.size(); j < ori.cubeList.size(); j++)
			{
				ModelBox box = (ModelBox)ori.cubeList.get(j);
				float param7 = 0.0F;

				ModelBox randBox = (ModelBox)ori.cubeList.get(rand.nextInt(ori.cubeList.size()));

				float x = randBox.posX1 + ((randBox.posX2 - randBox.posX1) > 0F ? rand.nextInt(((int)(randBox.posX2 - randBox.posX1) > 0) ? (int)(randBox.posX2 - randBox.posX1) : 1) : 0F);
				float y = randBox.posY1 + ((randBox.posY2 - randBox.posY1) > 0F ? rand.nextInt(((int)(randBox.posY2 - randBox.posY1) > 0) ? (int)(randBox.posY2 - randBox.posY1) : 1) : 0F);
				float z = randBox.posZ1 + ((randBox.posZ2 - randBox.posZ1) > 0F ? rand.nextInt(((int)(randBox.posZ2 - randBox.posZ1) > 0) ? (int)(randBox.posZ2 - randBox.posZ1) : 1) : 0F);

				copy.addBox(x, y, z, 0, 0, 0, param7);
			}
		}

		if(ori.childModels != null && (copy.childModels == null || copy.childModels.size() < ori.childModels.size()))
		{
			for(int i = 0; i < ori.childModels.size(); i++)
			{
				ModelRenderer childCopy = buildCopy((ModelRenderer)ori.childModels.get(i), base, 0, false);
				createEmptyContents(base, (ModelRenderer)ori.childModels.get(i), childCopy, depth + 1);
				copy.addChild(childCopy);
			}
		}
	}

	public static ModelRenderer buildCopy(ModelRenderer original, ModelBase copyBase, int depth, boolean hasFullModelBox)
	{
		int txOffsetX = 0;
		int txOffsetY = 0;
		try
		{
			txOffsetX = (Integer)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, original, ObfHelper.textureOffsetX);
			txOffsetY = (Integer)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, original, ObfHelper.textureOffsetY);
		}
		catch(Exception e)
		{
			ObfHelper.obfWarning();
			e.printStackTrace();
		}

		ModelRenderer cubeCopy = new ModelRenderer(copyBase, txOffsetX, txOffsetY);
		cubeCopy.mirror = original.mirror;
		cubeCopy.textureHeight = original.textureHeight;
		cubeCopy.textureWidth = original.textureWidth;

		for(int j = 0; j < original.cubeList.size(); j++)
		{
			ModelBox box = (ModelBox)original.cubeList.get(j);
			float param7 = 0.0F;

			if(hasFullModelBox)
			{
				cubeCopy.addBox(box.posX1, box.posY1, box.posZ1, (int)Math.abs(box.posX2 - box.posX1), (int)Math.abs(box.posY2 - box.posY1), (int)Math.abs(box.posZ2 - box.posZ1));
			}
			else
			{
				ModelBox randBox = (ModelBox)original.cubeList.get(rand.nextInt(original.cubeList.size()));

				float x = randBox.posX1 + ((randBox.posX2 - randBox.posX1) > 0F ? rand.nextInt(((int)(randBox.posX2 - randBox.posX1) > 0) ? (int)(randBox.posX2 - randBox.posX1) : 1) : 0F);
				float y = randBox.posY1 + ((randBox.posY2 - randBox.posY1) > 0F ? rand.nextInt(((int)(randBox.posY2 - randBox.posY1) > 0) ? (int)(randBox.posY2 - randBox.posY1) : 1) : 0F);
				float z = randBox.posZ1 + ((randBox.posZ2 - randBox.posZ1) > 0F ? rand.nextInt(((int)(randBox.posZ2 - randBox.posZ1) > 0) ? (int)(randBox.posZ2 - randBox.posZ1) : 1) : 0F);
				
				cubeCopy.addBox(x, y, z, (int)Math.abs(box.posX2 - box.posX1), (int)Math.abs(box.posY2 - box.posY1), (int)Math.abs(box.posZ2 - box.posZ1));
			}
		}

		if(original.childModels != null && depth < 20)
		{
			for(int i = 0; i < original.childModels.size(); i++)
			{
				ModelRenderer child = (ModelRenderer)original.childModels.get(i);
				cubeCopy.addChild(buildCopy(child, copyBase, depth + 1, hasFullModelBox));
			}
		}

		cubeCopy.setRotationPoint(original.rotationPointX, original.rotationPointY, original.rotationPointZ);

		cubeCopy.rotateAngleX = original.rotateAngleX;
		cubeCopy.rotateAngleY = original.rotateAngleY;
		cubeCopy.rotateAngleZ = original.rotateAngleZ;
		return cubeCopy;
	}

	public static int getModelHeight(ModelRenderer model) 
	{
		int height = 0;//Y1 lower than Y2
		for(int i = 0; i < model.cubeList.size(); i++)
		{
			ModelBox box = (ModelBox)model.cubeList.get(i);
			if((int)Math.abs(box.posY2 - box.posY1) > height)
			{
				height = (int)Math.abs(box.posY2 - box.posY1);
			}
		}
		return height;
	}

	public static ModelRenderer createMorphArm(ModelBase morph, ModelRenderer prevArm, ModelRenderer nextArm, int morphProgress, float renderTick) 
	{
		if(prevArm == null)
		{
			prevArm = new ModelRenderer(morph, 0, 0);
			prevArm.addBox(-3.0F, -2.0F, -2.0F, 0, 12, 0, 0.0F);
			prevArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
			//fake biped arm with no size
		}
		if(nextArm == null)
		{
			nextArm = new ModelRenderer(morph, 0, 0);
			nextArm.addBox(-3.0F, -2.0F, -2.0F, 0, 12, 0, 0.0F);
			nextArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
			//fake biped arm with no size
		}
		ModelRenderer cube = new ModelRenderer(morph, 0, 0);

		float mag = (float)Math.pow((morphProgress - 10F + renderTick) / 50F, 2D);
		
		cube.mirror = nextArm.mirror;
		
		if(mag > 1.0F)
		{
			mag = 1.0F;
		}
		
		for(int i = 0; i < nextArm.cubeList.size(); i++)
		{
			ModelBox newBox = (ModelBox)nextArm.cubeList.get(i);
			ModelBox prevBox = null;
			
			if(i < prevArm.cubeList.size())
			{
				prevBox = (ModelBox)prevArm.cubeList.get(i);
			}
			
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

			cube.addBox(prevXpos + (nextXpos - prevXpos) * mag, prevYpos + (nextYpos - prevYpos) * mag, prevZpos + (nextZpos - prevZpos) * mag, (int)Math.round(prevXSize + (newXSize - prevXSize) * mag), (int)Math.round(prevYSize + (newYSize - prevYSize) * mag), (int)Math.round(prevZSize + (newZSize - prevZSize) * mag), 0.0F);
		}
		
		if(morphProgress <= 60)
		{
			int heightDiff = 24 - ModelHelper.getModelHeight(prevArm) - ModelHelper.getModelHeight(nextArm);
			cube.rotationPointX = prevArm.rotationPointX + (nextArm.rotationPointX - prevArm.rotationPointX) * mag;
			cube.rotationPointY = prevArm.rotationPointY + (nextArm.rotationPointY - prevArm.rotationPointY) * mag + heightDiff;
			cube.rotationPointZ = prevArm.rotationPointZ + (nextArm.rotationPointZ - prevArm.rotationPointZ) * mag;
		}
		else
		{
			int heightDiff = 12 - ModelHelper.getModelHeight(nextArm);
			cube.rotationPointX = nextArm.rotationPointX;
			cube.rotationPointY = nextArm.rotationPointY + heightDiff;
			cube.rotationPointZ = nextArm.rotationPointZ;
		}

		return cube;
	}

	public static ArrayList<ModelRenderer> compileRenderableModels(ModelInfo modelInfo, EntityLivingBase ent) 
	{
		if(ent != null)
		{
			boolean forceRender = Morph.proxy.tickHandlerClient.forceRender;
			Morph.proxy.tickHandlerClient.forceRender = true;
			modelInfo.forceRender(ent, 0.0D, -500D, 0.0D, 0.0F, 1.0F);

			ArrayList<ModelRenderer> modelList = ModelHelper.getModelCubes(getPossibleModel(modelInfo.getRenderer()));

			for(int i = 0; i < modelList.size(); i++)
			{
				ModelRenderer cube = modelList.get(i);
				try
				{
					ObfuscationReflectionHelper.setPrivateValue(ModelRenderer.class, cube, false, ObfHelper.compiled);
				}
				catch(Exception e)
				{
					ObfHelper.obfWarning();
					e.printStackTrace();
				}
			}
			
			modelInfo.forceRender(ent, 0.0D, -500D, 0.0D, 0.0F, 1.0F);
			Morph.proxy.tickHandlerClient.forceRender = forceRender;
			
			ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>(modelList);
			
			for(int i = list.size() - 1; i >= 0 ; i--)
			{
				ModelRenderer cube = list.get(i);
				try
				{
					if(!(Boolean)ObfuscationReflectionHelper.getPrivateValue(ModelRenderer.class, cube, ObfHelper.compiled))
					{
						list.remove(i);
					}
				}
				catch(Exception e)
				{
					ObfHelper.obfWarning();
					e.printStackTrace();
				}
			}
			return list;
		}
		return modelInfo.modelList;
	}
}
