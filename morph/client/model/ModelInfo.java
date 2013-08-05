package morph.client.model;

import java.util.ArrayList;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RendererLivingEntity;

public class ModelInfo 
{
	public final Class entClass;
	public final RendererLivingEntity entRender;
	public final ModelBase modelParent;
	public final ArrayList<ModelRenderer> modelList;
	public final ArrayList<ModelRenderer> potentialArms;
	
	public ModelInfo(Class entityClass, RendererLivingEntity render, ModelBase modelInstance)
	{
		entClass = entityClass;
		entRender = render;
		modelParent = modelInstance;
		modelList = ModelHelper.getModelCubes(modelInstance);
		potentialArms = ModelHelper.getPotentialArm(modelInstance);

		System.out.println("arms!");
		System.out.println(modelInstance);
		System.out.println(potentialArms.size());
	}
}
