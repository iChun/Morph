package morph.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderMorph extends RendererLivingEntity 
{

	public RenderMorph(ModelBase par1ModelBase, float par2) 
	{
		super(par1ModelBase, par2);
	}

	@Override
	protected ResourceLocation func_110775_a(Entity entity) 
	{
		return AbstractClientPlayer.field_110314_b;
	}
	
}
