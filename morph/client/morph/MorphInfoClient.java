package morph.client.morph;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import morph.client.model.ModelInfo;
import morph.client.model.ModelList;
import morph.common.morph.MorphInfo;

public class MorphInfoClient extends MorphInfo 
{

	public ModelInfo prevEntInfo;
	public ModelBase prevEntModel;
	
	public ModelInfo nextEntInfo;
	public ModelBase nextEntModel;
	
	public ModelRenderer interimModel;

	public MorphInfoClient(EntityLivingBase prev, EntityLivingBase next) 
	{
		super(prev, next);
		prevEntInfo = ModelList.getModelInfo(prev.getClass());
		nextEntInfo = ModelList.getModelInfo(next.getClass());
	}
	
}
