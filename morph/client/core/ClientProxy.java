package morph.client.core;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import morph.common.core.CommonProxy;

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
	}
	
	@Override
	public void initTickHandlers()
	{
		super.initTickHandlers();
		tickHandlerClient = new TickHandlerClient();
		TickRegistry.registerTickHandler(tickHandlerClient, Side.CLIENT);
	}
}
