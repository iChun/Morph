package morph.client.model;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class ModelHelper 
{
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
							if(clz == ModelBiped.class && !(f.getName().equalsIgnoreCase("bipedCloak") || f.getName().equalsIgnoreCase("k") || f.getName().equalsIgnoreCase("field_78122_k")) || true)
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
				//TODO check that this isn't broken
				cube.addChild(child);
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
}
