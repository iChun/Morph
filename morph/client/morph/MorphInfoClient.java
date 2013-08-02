package morph.client.morph;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import morph.client.model.ModelInfo;
import morph.client.model.ModelList;
import morph.common.morph.MorphInfo;

public class MorphInfoClient extends MorphInfo 
{

	public ModelInfo prevEntModel;
	
	public ModelInfo nextEntModel;
	
	public ModelRenderer interimModel;

	public MorphInfoClient(EntityLivingBase prev, EntityLivingBase next) 
	{
		super(prev, next);
		prevEntModel = ModelList.getModelInfo(prev.getClass());
		nextEntModel = ModelList.getModelInfo(next.getClass());
	}
	
}
