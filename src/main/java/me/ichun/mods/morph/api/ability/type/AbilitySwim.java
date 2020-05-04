package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class AbilitySwim extends Ability
{
    public boolean canSurviveOutOfWater;
    public int air;
    public float swimSpeed;
    public float landSpeed;
    public boolean canMaintainDepth;
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/swim.png");

    public AbilitySwim()
    {
        canSurviveOutOfWater = false;
        air = 8008135;
        swimSpeed = 1f;
        landSpeed = 1f;
        canMaintainDepth = false;
    }

    public AbilitySwim(boolean airBreather)
    {
        this();
        canSurviveOutOfWater = airBreather;
    }

    public AbilitySwim(boolean airBreather, float swimModifier, float landModifier, boolean maintainDepth)
    {
        this(airBreather);
        swimSpeed = swimModifier;
        landSpeed = landModifier;
        canMaintainDepth = maintainDepth;
        if(swimSpeed > 1.22F)
        {
            swimSpeed = 1.22F;
        }
    }

    @Override
    public Ability parse(String[] args)
    {
        canSurviveOutOfWater = Boolean.parseBoolean(args[0]);
        swimSpeed = Float.parseFloat(args[1]);
        landSpeed = Float.parseFloat(args[2]);
        canMaintainDepth = Boolean.parseBoolean(args[3]);
        if(swimSpeed > 1.22F)
        {
            swimSpeed = 1.22F;
        }
        return this;
    }

    @Override
    public String getType()
    {
        return "swim";
    }

    @Override
    public void tick()
    {
        if(air == 8008135)
        {
            air = getParent().getAir();
        }
        if(getParent().isInWater())
        {
            if(getParent().getEntityWorld().isRemote)
            {
                if(GuiIngameForge.renderAir)
                {
                    GuiIngameForge.renderAir = false;
                }
            }
            getParent().setAir(300);
            air = 300;
            if(swimSpeed != 1f && !(getParent() instanceof EntityPlayer && ((EntityPlayer)getParent()).capabilities.isFlying))
            {
                if(getParent().motionX > -swimSpeed && getParent().motionX < swimSpeed)
                {
                    getParent().motionX *= swimSpeed * 0.995F;
                }
                if(getParent().motionZ > -swimSpeed && getParent().motionZ < swimSpeed)
                {
                    getParent().motionZ *= swimSpeed * 0.995F;
                }
            }
            if(canMaintainDepth)
            {
                boolean isJumping = getParent().isJumping;
                if(!getParent().isSneaking() && !isJumping && getParent().isInsideOfMaterial(Material.WATER))
                {
                    getParent().motionY = 0f;
                }else{
                    if(isJumping)
                    {
                        getParent().motionY *= swimSpeed;
                    }
                }
            }
        }
        else if(!canSurviveOutOfWater)
        {
            int j = EnchantmentHelper.getRespirationModifier(getParent());
            air = j > 0 && getParent().getRNG().nextInt(j + 1) > 0 ? air : air - 1;

            if (air == -20)
            {
                air = 0;
                getParent().attackEntityFrom(DamageSource.DROWN, 2.0F);
            }

            if(!(getParent() instanceof EntityPlayer && ((EntityPlayer)getParent()).capabilities.isFlying) && landSpeed != 1f && air < 285)
            {
                if(getParent().motionX > -landSpeed && getParent().motionX < landSpeed)
                {
                    getParent().motionX *= landSpeed;
                }
                if(getParent().motionZ > -landSpeed && getParent().motionZ < landSpeed)
                {
                    getParent().motionZ *= landSpeed;
                }
            }
        }
    }

    @Override
    public void kill(ArrayList<Ability> nextAbilities)
    {
        if(getParent() != null && getParent().getEntityWorld().isRemote)
        {
            if(!GuiIngameForge.renderAir)
            {
                GuiIngameForge.renderAir = true;
            }
        }
    }

    @Override
    public Ability clone()
    {
        return new AbilitySwim(canSurviveOutOfWater, swimSpeed, landSpeed, canMaintainDepth);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRender()
    {
        if(!canSurviveOutOfWater)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null && mc.player == getParent() && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.capabilities.disableDamage)
            {
                mc.getTextureManager().bindTexture(Gui.ICONS);

                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int width = scaledresolution.getScaledWidth();
                int height = scaledresolution.getScaledHeight();

                int l1 = width / 2 + 91;
                int i2 = height - 39;
                IAttributeInstance attributeinstance = mc.player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                float f = (float)attributeinstance.getAttributeValue();
                float f1 = mc.player.getAbsorptionAmount();
                int j2 = MathHelper.ceil((f + f1) / 2.0F / 10.0F);
                int k2 = Math.max(10 - (j2 - 2), 3);
                int l2 = i2 - (j2 - 1) * k2 - 10;

                int k3 = air;
                int l4 = MathHelper.ceil((double)(air - 2) * 10.0D / 300.0D);
                int i4 = MathHelper.ceil((double)air * 10.0D / 300.0D) - l4;

                for (int k4 = 0; k4 < l4 + i4; ++k4)
                {
                    if (k4 < l4)
                    {
                        this.drawTexturedModalRect(l1 - k4 * 8 - 9, l2, 16, 18, 9, 9);
                    }
                    else
                    {
                        this.drawTexturedModalRect(l1 - k4 * 8 - 9, l2, 25, 18, 9, 9);
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(par1, par2 + par6, 0.0D).tex((par3 * f), (par4 + par6) * f1).endVertex();
        bufferBuilder.pos(par1 + par5, par2 + par6, 0.0D).tex((par3 + par5) * f, (par4 + par6) * f1).endVertex();
        bufferBuilder.pos(par1 + par5, par2, 0.0D).tex((par3 + par5) * f, par4 * f1).endVertex();
        bufferBuilder.pos(par1, par2, 0.0D).tex(par3 * f, par4 * f1).endVertex();
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
