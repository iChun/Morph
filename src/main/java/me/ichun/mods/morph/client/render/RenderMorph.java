package me.ichun.mods.morph.client.render;

import me.ichun.mods.morph.client.entity.EntityMorphAcquisition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderMorph extends RendererLivingEntity
{
    public static final ResourceLocation morphSkin = new ResourceLocation("morph", "textures/skin/morphskin.png");

    public RenderMorph(ModelBase par1ModelBase, float par2)
    {
        super(Minecraft.getMinecraft().getRenderManager(), par1ModelBase, par2);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        if(entity instanceof EntityMorphAcquisition)
        {
            setMainModel(((EntityMorphAcquisition)entity).model);
        }
        return morphSkin;
    }

    public void setMainModel(ModelBase base)
    {
        mainModel = base;
    }
}
