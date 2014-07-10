package morph.client.morph;

import morph.client.model.ModelInfo;
import morph.client.model.ModelList;
import morph.client.model.ModelMorph;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.entity.player.EntityPlayer;

public class MorphInfoClient extends MorphInfo 
{

	public EntityPlayer player;
	
	public ModelInfo prevModelInfo;
	
	public ModelInfo nextModelInfo;
	
	public ModelMorph interimModel;

	public MorphInfoClient(String name, MorphState prev, MorphState next) 
	{
		super(name, prev, next);
		if(prev.entInstance != null)
		{
			prevModelInfo = ModelList.getModelInfo(prev.entInstance.getClass());
		}
		if(next.entInstance != null)
		{
			nextModelInfo = ModelList.getModelInfo(next.entInstance.getClass());
		}
		
		interimModel = new ModelMorph(this);
	}
	
}
