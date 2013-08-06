package morph.client.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import morph.common.core.ObfHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class ModelHelper 
{
	public static Random rand = new Random();
	
	public static ArrayList<ModelRenderer> getModelCubesCopy(ModelInfo info, ModelBase base)
	{
		ArrayList<ModelRenderer> list = new ArrayList<ModelRenderer>();
		
		for(int i = 0; i < info.modelList.size(); i++)
		{
			ModelRenderer cube = (ModelRenderer)info.modelList.get(i);
			list.add(buildCopy(cube, base, 0, true));
		}
		
		return list;
	}
	
	public static ArrayList<ModelRenderer> getPotentialArm(ModelBase parent)
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
							if(clz == ModelBiped.class && (f.getName().equalsIgnoreCase("bipedRightArm") || f.getName().equalsIgnoreCase("f") || f.getName().equalsIgnoreCase("field_78112_f")) || clz != ModelBiped.class && (f.getName().contains("Right") || f.getName().contains("right")) && (f.getName().contains("arm") || f.getName().contains("hand") || f.getName().contains("Arm") || f.getName().contains("Hand")))
							{
								list.add((ModelRenderer)f.get(parent)); // Add normal parent fields
							}
						}
						else if(f.getType() == ModelRenderer[].class && (f.getName().contains("Right") || f.getName().contains("right")) && (f.getName().contains("arm") || f.getName().contains("hand") || f.getName().contains("Arm") || f.getName().contains("Hand")))
						{
							list1.add((ModelRenderer[])f.get(parent));
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

		for(ModelRenderer[] renderers : list1)
		{
			for(ModelRenderer renderer : renderers)
			{
				if(!list.contains(renderer))
				{
					list.add(renderer);
				}
			}
		}
		
		return list;
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
								list.add((ModelRenderer)f.get(parent)); // Add normal parent fields
							}
						}
						else if(f.getType() == ModelRenderer[].class)
						{
							list1.add((ModelRenderer[])f.get(parent));
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
				if(!list.contains(cube))
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
		ArrayList<ModelBase> models = new ArrayList<ModelBase>();
		
		if(rend != null)
		{
			try
			{
				Class clz = rend.getClass();
				while(clz != Render.class)
				{
					Field[] fields = clz.getDeclaredFields();
					for(Field f : fields)
					{
						f.setAccessible(true);
						if(f.getType() == ModelBase.class)
						{
							models.add((ModelBase)f.get(rend)); // Add normal parent fields
						}
						else if(f.getType() == ModelBase[].class)
						{
							ModelBase[] modelBases = (ModelBase[])f.get(rend);
							for(ModelBase base : modelBases)
							{
								models.add(base);
							}
						}
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
		int size = -1;
		
		for(ModelBase base : models)
		{
			ArrayList<ModelRenderer> mrs = getModelCubes(base);
			if(mrs.size() > size)
			{
				size = mrs.size();
				base1 = base;
			}
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
	
	public static void createEmptyContents(ModelBase base, ModelRenderer ori, ModelRenderer copy)
	{
		if(copy.cubeList.size() < ori.cubeList.size())
		{
			for(int j = copy.cubeList.size(); j < ori.cubeList.size(); j++)
			{
				ModelBox box = (ModelBox)ori.cubeList.get(j);
				float param7 = 0.0F;
				
				ModelBox randBox = (ModelBox)ori.cubeList.get(rand.nextInt(ori.cubeList.size()));
				
				float x = randBox.posX1 + ((randBox.posX2 - randBox.posX1) > 0F ? rand.nextInt((int)(randBox.posX2 - randBox.posX1)) : 0F);
				float y = randBox.posY1 + ((randBox.posY2 - randBox.posY1) > 0F ? rand.nextInt((int)(randBox.posY2 - randBox.posY1)) : 0F);
				float z = randBox.posZ1 + ((randBox.posZ2 - randBox.posZ1) > 0F ? rand.nextInt((int)(randBox.posZ2 - randBox.posZ1)) : 0F);
				
				copy.addBox(x, y, z, 0, 0, 0, param7);
			}
		}
		
		if(ori.childModels != null && (copy.childModels == null || copy.childModels.size() < ori.childModels.size()))
		{
			for(int i = 0; i < ori.childModels.size(); i++)
			{
				copy.addChild(buildCopy((ModelRenderer)ori.childModels.get(i), base, 0, false));
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
				
				float x = randBox.posX1 + ((randBox.posX2 - randBox.posX1) > 0F ? rand.nextInt((int)(randBox.posX2 - randBox.posX1)) : 0F);
				float y = randBox.posY1 + ((randBox.posY2 - randBox.posY1) > 0F ? rand.nextInt((int)(randBox.posY2 - randBox.posY1)) : 0F);
				float z = randBox.posZ1 + ((randBox.posZ2 - randBox.posZ1) > 0F ? rand.nextInt((int)(randBox.posZ2 - randBox.posZ1)) : 0F);
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
}
