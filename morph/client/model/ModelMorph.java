package morph.client.model;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import morph.client.morph.MorphInfoClient;
import morph.client.render.RenderMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelMorph extends ModelBase 
{

	public MorphInfoClient morphInfo;
	
	public ArrayList<ModelRenderer> modelList;
	
	public ModelBase modelParent;
	
	public ModelMorph(MorphInfoClient info, ModelBase parent)
	{
		morphInfo = info;
		modelParent = parent;
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		modelParent.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		
		modelParent.render(entity, f, f1, f2, f3, f4, f5);
		
//		Minecraft.getMinecraft().renderEngine.func_110577_a(RenderMorph.morphSkin);
//		
//		GL11.glScaled(1.001D, 1.001D, 1.001D);
//		modelParent.render(entity, f, f1, f2, f3, f4, f5);
	}
	
	@Override
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) 
	{
		modelParent.setLivingAnimations(par1EntityLivingBase, par2, par3, par4);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
