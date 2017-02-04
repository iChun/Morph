package me.ichun.mods.morph.client.model;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import us.ichun.mods.ichunutil.client.model.ModelHelper;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;

import java.util.ArrayList;

public class ModelInfo
{
    public final Class entClass;
    public final Render entRenderer;
    public final ModelBase modelParent;
    public final ArrayList<ModelRenderer> modelList;
    public final ModelRenderer modelArm;

    public ModelInfo(Class clz, Render rend, ModelBase model)
    {
        entClass = clz;
        entRenderer = rend;
        modelParent = model;
        modelList = ModelHelper.getModelCubes(model);
        modelArm = ModelHelper.getPotentialArm(modelParent);
    }

    public void forceRender(Entity ent, double d, double d1, double d2, float f, float f1)
    {
        EntityHelperBase.storeBossStatus();

        if(Minecraft.getMinecraft().getRenderManager().renderEngine != null && Minecraft.getMinecraft().getRenderManager().livingPlayer != null)
        {
            try
            {
                entRenderer.doRender(ent, d, d1, d2, f, f1);
            }
            catch(Exception e)
            {
                Morph.logger.warn("A morph/model is causing an exception when Morph tries to render it! You might want to report this to the author of the Morphed mob (Not to Morph!): " + entRenderer.getClass().getName());
                e.printStackTrace();
            }
        }

        EntityHelperBase.restoreBossStatus();
    }
}
