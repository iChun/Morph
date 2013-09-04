package morph.client.render;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import morph.client.model.ModelHelper;
import morph.common.core.ObfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderPlayerHand extends RenderPlayer 
{
	public float progress;
	
	public RenderPlayer parent;
	
	public ModelBiped biped;
	
	public ModelRenderer replacement;
	
	public ResourceLocation resourceLoc;
	
	@Override
    public void renderFirstPersonArm(EntityPlayer par1EntityPlayer)
    {
    	if(replacement != null)
    	{
	        float f = 1.0F;
	        
	        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLoc); //try func_110776_a
	        
	        GL11.glColor4f(f, f, f, progress);
	    	GL11.glEnable(GL11.GL_BLEND);
	    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	
	    	ModelRenderer arm = biped.bipedRightArm;
    		biped.bipedRightArm = replacement;
	     	
    		//player arms are 12 blocks long
    		int heightDiff = 12 - ModelHelper.getModelHeight(replacement);
    		float rotX = replacement.rotationPointX;
    		float rotY = replacement.rotationPointY;
    		float rotZ = replacement.rotationPointZ;
    		
    		float angX = replacement.rotateAngleX;
    		float angY = replacement.rotateAngleY;
    		float angZ = replacement.rotateAngleZ;
    		
	        replacement.rotationPointX = arm.rotationPointX;
	        replacement.rotationPointY = arm.rotationPointY + heightDiff;
	        replacement.rotationPointZ = arm.rotationPointZ;
    		
	        biped.onGround = 0.0F;
	        biped.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, par1EntityPlayer);
	        biped.bipedRightArm.render(0.0625F);
	        
	        biped.bipedRightArm = arm;
	
	        replacement.rotationPointX = rotX;
	        replacement.rotationPointY = rotY;
	        replacement.rotationPointZ = rotZ;
	        
	        replacement.rotateAngleX = angX;
	        replacement.rotateAngleY = angY;
	        replacement.rotateAngleZ = angZ;
	        
	    	GL11.glDisable(GL11.GL_BLEND);
	
	        GL11.glColor4f(f, f, f, 1.0F);
       	}
    }
	
	public void setParent(RenderPlayer render)
	{
		if(parent != render)
		{
			try
			{
				biped = (ModelBiped)ObfuscationReflectionHelper.getPrivateValue(RenderPlayer.class, render, ObfHelper.modelBipedMain);
			}
			catch(Exception e)
			{
				ObfHelper.obfWarning();
				e.printStackTrace();
			}
		}
		parent = render;
	}
}
