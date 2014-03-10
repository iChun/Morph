package morph.client.render;

import morph.client.entity.EntityMorphAcquisition;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderMorph extends RendererLivingEntity 
{

	public static final ResourceLocation morphSkin = new ResourceLocation("morph", "textures/skin/morphskin.png");
	
	public RenderMorph(ModelBase par1ModelBase, float par2) 
	{
		super(par1ModelBase, par2);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) 
	{
		if(entity instanceof EntityMorphAcquisition)
		{
			setMainModel(((EntityMorphAcquisition)entity).model);
		}
		return morphSkin;
	}
	
	@Override
    protected void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6)
    {
    }
	
	public void setMainModel(ModelBase base)
	{
		mainModel = base;
	}
}
