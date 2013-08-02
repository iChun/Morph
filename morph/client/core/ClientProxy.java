package morph.client.core;

import morph.client.model.ModelInfo;
import morph.client.model.ModelList;
import morph.common.core.CommonProxy;
import morph.common.core.ObfHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{

	@Override
	public void initMod()
	{
		super.initMod();
		
		for(int i = compatibleEntities.size() - 1; i >= 0; i--)
		{
			Render rend = RenderManager.instance.getEntityClassRenderObject(compatibleEntities.get(i));
			if(!(rend instanceof RendererLivingEntity))
			{
				compatibleEntities.remove(i);
			}
		}
		
		for(Class clz : compatibleEntities)
		{
			try
			{
				RendererLivingEntity rend = (RendererLivingEntity)RenderManager.instance.getEntityClassRenderObject(clz);
				ModelList.addModelInfo(clz, new ModelInfo(clz, rend, (ModelBase)ObfuscationReflectionHelper.getPrivateValue(RendererLivingEntity.class, rend, ObfHelper.mainModel)));
			}
			catch(Exception e)
			{
				ObfHelper.obfWarning();
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void initTickHandlers()
	{
		super.initTickHandlers();
		tickHandlerClient = new TickHandlerClient();
		TickRegistry.registerTickHandler(tickHandlerClient, Side.CLIENT);
	}
}
