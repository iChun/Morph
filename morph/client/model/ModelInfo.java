package morph.client.model;

import java.util.ArrayList;

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
	    int bossStatusBarTime = BossStatus.statusBarLength;
	    String bossName = BossStatus.bossName;
	    boolean randVar = BossStatus.field_82825_d;
		
		if(RenderManager.instance.renderEngine != null && RenderManager.instance.livingPlayer != null)
		{
			entRender.doRender(ent, d, d1, d2, f, f1);
		}
		
	    BossStatus.healthScale = bossHealthScale;
	    BossStatus.statusBarLength = bossStatusBarTime;
	    BossStatus.bossName = bossName;
	    BossStatus.field_82825_d = randVar;
	}
}
