package morph.client.morph;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import morph.common.morph.MorphInfo;

public class MorphInfoClient extends MorphInfo 
{

	public ModelBase prevEntModel;
	
	public ModelBase nextEntModel;
	
	public ModelRenderer interimModel;

	public MorphInfoClient(EntityLivingBase prev, EntityLivingBase next) 
	{
		super(prev, next);
	}
	
}
