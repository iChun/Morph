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
	}
	
	@Override
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) 
	{
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
