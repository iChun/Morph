package morph.client.morph;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import morph.client.model.ModelHelper;
import morph.client.model.ModelInfo;
import morph.client.model.ModelList;
import morph.client.model.ModelMorph;
import morph.common.morph.MorphInfo;

public class MorphInfoClient extends MorphInfo 
{

	public EntityPlayer player;
	
	public ModelInfo prevEntInfo;
	public ModelMorph prevEntModel;
	
	public ModelInfo nextEntInfo;
	public ModelMorph nextEntModel;
	
	public ModelRenderer interimModel;

	public MorphInfoClient(EntityLivingBase prev, EntityLivingBase next) 
	{
		super(prev, next);
		prevEntInfo = ModelList.getModelInfo(prev.getClass());
		prevEntModel = new ModelMorph(this, prevEntInfo.modelParent);
		prevEntModel.modelList = ModelHelper.getModelCubesCopy(prevEntInfo, prevEntModel);
		
		nextEntInfo = ModelList.getModelInfo(next.getClass());
		nextEntModel = new ModelMorph(this, nextEntInfo.modelParent);
		nextEntModel.modelList = ModelHelper.getModelCubesCopy(nextEntInfo, nextEntModel);
	}
	
}
