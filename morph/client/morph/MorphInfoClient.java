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
	
	public ModelInfo nextEntInfo;
	
	public ModelMorph interimModel;

	public MorphInfoClient(String name, EntityLivingBase prev, EntityLivingBase next) 
	{
		super(name, prev, next);
		prevEntInfo = ModelList.getModelInfo(prev.getClass());
		
		nextEntInfo = ModelList.getModelInfo(next.getClass());
		
		interimModel = new ModelMorph(this);
	}
	
}
