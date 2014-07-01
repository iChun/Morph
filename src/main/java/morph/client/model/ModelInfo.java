package morph.client.model;

import java.util.ArrayList;

import morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;

public class ModelInfo 
{
	public final Class entClass;
	private final Render entRender;
	public final ModelBase modelParent;
	public final ArrayList<ModelRenderer> modelList;
	public final ModelRenderer assumedArm;
	
	public ModelInfo(Class entityClass, Render render, ModelBase modelInstance)
	{
		entClass = entityClass;
		entRender = render;
		modelParent = modelInstance;
		modelList = ModelHelper.getModelCubes(modelInstance);
		assumedArm = ModelHelper.getPotentialArm(modelInstance);
	}
	
	public Render getRenderer()
	{
		return entRender;
	}
	
	public void forceRender(Entity ent, double d, double d1, double d2, float f, float f1)
	{
	    float bossHealthScale = BossStatus.healthScale;
	    int bossStatusBarTime = BossStatus.statusBarTime;
	    String bossName = BossStatus.bossName;
	    boolean hasColorModifier = BossStatus.hasColorModifier;
		
		if(RenderManager.instance.renderEngine != null && RenderManager.instance.livingPlayer != null)
		{
            try
            {
                entRender.doRender(ent, d, d1, d2, f, f1);
            }
            catch(Exception e)
            {
                Morph.console("A morph/model is causing an exception when Morph tries to render it! You might want to report this to the author of the Morphed mob (Not to Morph!)", true);
            }
		}
		
	    BossStatus.healthScale = bossHealthScale;
	    BossStatus.statusBarTime = bossStatusBarTime;
	    BossStatus.bossName = bossName;
	    BossStatus.hasColorModifier = hasColorModifier;
	}
}
