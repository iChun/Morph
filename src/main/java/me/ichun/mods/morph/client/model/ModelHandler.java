package me.ichun.mods.morph.client.model;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import us.ichun.mods.ichunutil.client.model.ModelHelper;

import java.util.HashMap;

public class ModelHandler
{
    private static HashMap<Class<? extends EntityLivingBase>, ModelInfo> entityModelMap = new HashMap<Class<? extends EntityLivingBase>, ModelInfo>();
    private static HashMap<String, ModelInfo> playerModelMap = new HashMap<String, ModelInfo>();

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

    public static void mapPlayerModels()
    {
        playerModelMap.put("default", new ModelInfo(EntityPlayer.class, ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().skinMap.get("default")), (((RenderPlayer)Minecraft.getMinecraft().getRenderManager().skinMap.get("default"))).mainModel));
        playerModelMap.put("slim", new ModelInfo(EntityPlayer.class, ((RenderPlayer)Minecraft.getMinecraft().getRenderManager().skinMap.get("slim")), (((RenderPlayer)Minecraft.getMinecraft().getRenderManager().skinMap.get("slim"))).mainModel));
    }

    public static ModelInfo getEntityModelInfo(EntityLivingBase entity)
    {
        if(entity instanceof AbstractClientPlayer)
        {
            String s = ((AbstractClientPlayer)entity).getSkinType();
            ModelInfo modelInfo = playerModelMap.get(s);
            return modelInfo != null ? modelInfo : playerModelMap.get("default");
        }
        return getEntityModelInfo(entity.getClass());
    }

    public static ModelInfo getEntityModelInfo(Class clz)
    {
        if(EntityPlayer.class.isAssignableFrom(clz))
        {
            return playerModelMap.get("default");
        }
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
        return info; //shouldn't be null
    }
}
