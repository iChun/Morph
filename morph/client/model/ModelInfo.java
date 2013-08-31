package morph.client.model;

import java.util.ArrayList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelInfo 
{
	public final Class entClass;
	private final Render entRender;
	public final ModelBase modelParent;
	public final ArrayList<ModelRenderer> modelList;
	public final ModelRenderer assumedArm;
	
	public ModelInfo(Class entityClass, Render render, ModelBase modelInstance)
	{
		entClass = entityClass;
		entRender = render;
		modelParent = modelInstance;
		modelList = ModelHelper.getModelCubes(modelInstance);
		assumedArm = ModelHelper.getPotentialArm(modelInstance);
	}
	
	public Render getRenderer()
	{
		return entRender;
	}
	
	public void forceRender(Entity ent, double d, double d1, double d2, float f, float f1)
	{
//		if(entRender instanceof RendererLivingEntity && ent instanceof EntityLivingBase)
//		{
//			((RendererLivingEntity)entRender).func_130000_a((EntityLivingBase)ent, d, d1, d2, f, f1);
//		}
//		else
		{
			entRender.doRender(ent, d, d1, d2, f, f1);
		}
	}
}
