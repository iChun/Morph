package me.ichun.mods.morph.client.model;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import us.ichun.mods.ichunutil.client.model.ModelHelper;

import java.util.HashMap;

public class ModelHandler
{
    private static HashMap<Class<? extends EntityLivingBase>, ModelInfo> entityModelMap = new HashMap<Class<? extends EntityLivingBase>, ModelInfo>();

    public static void dissectForModels(Class<? extends EntityLivingBase> clz, Render rend)
    {
        if(rend instanceof RendererLivingEntity)
        {
            entityModelMap.put(clz, new ModelInfo(clz, rend, ((RendererLivingEntity)rend).mainModel));
        }
        else
        {
            entityModelMap.put(clz, new ModelInfo(clz, rend, ModelHelper.getPossibleModel(rend)));
        }
    }

    public static ModelInfo getEntityModelInfo(EntityLivingBase entity)
    {
        return getEntityModelInfo(entity.getClass());
    }

    public static ModelInfo getEntityModelInfo(Class clz)
    {
        ModelInfo info = null;
        while(clz != EntityLivingBase.class && info == null)
        {
            info = entityModelMap.get(clz);
            clz = clz.getSuperclass();
        }
        if(info == null)
        {
            Morph.logger.warn("Cannot find ModelInfo for " + clz.getName() + ". Attempting to generate one.");
            Render rend = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(clz);
            info = new ModelInfo(clz, rend, ModelHelper.getPossibleModel(rend));
            entityModelMap.put(clz, info);
        }
        return info; //can be null
    }
}
