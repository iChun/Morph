package morph.common.ability;

import morph.api.Ability;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AbilitySwim extends Ability {

	public boolean canSurviveOutOfWater;
	public int air;
	
	public AbilitySwim()
	{
		canSurviveOutOfWater = false;
		air = 8008135;
	}
	
	public AbilitySwim(boolean airBreather)
	{
		canSurviveOutOfWater = airBreather;
		air = 8008135;
	}
	
	@Override
	public Ability parse(String[] args)
	{
		try
		{
			canSurviveOutOfWater = Boolean.parseBoolean(args[0]);
		}
		catch(Exception e)
		{
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
		boolean flying = false;
		if(getParent().isInWater())
		{
			getParent().setAir(300);
			air = 300;
		}
		else if(!canSurviveOutOfWater)
		{
	        int j = EnchantmentHelper.getRespiration(getParent());
	        air = j > 0 && getParent().getRNG().nextInt(j + 1) > 0 ? air : air - 1;
	        
	        if (air == -20)
	        {
	        	air = 0;
	        	getParent().attackEntityFrom(DamageSource.drown, 2.0F);
	        }
		}
	}

	@Override
	public void kill() 
	{
	}

	@Override
	public Ability clone() 
	{
		return new AbilitySwim(canSurviveOutOfWater);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postRender() 
	{
		if(!canSurviveOutOfWater)
		{
			Minecraft mc = Minecraft.getMinecraft();
	        if (mc.currentScreen == null && mc.thePlayer == getParent() && !mc.thePlayer.isInsideOfMaterial(Material.water) && !mc.thePlayer.capabilities.disableDamage)
	        {
	        	mc.getTextureManager().bindTexture(Gui.icons);
	        	
	            ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
	            int width = scaledresolution.getScaledWidth();
	            int height = scaledresolution.getScaledHeight();
	            
	            int l1 = width / 2 + 91;
	            int i2 = height - 39;
	            AttributeInstance attributeinstance = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
	            float f = (float)attributeinstance.getAttributeValue();
	            float f1 = mc.thePlayer.getAbsorptionAmount();
	            int j2 = MathHelper.ceiling_float_int((f + f1) / 2.0F / 10.0F);
	            int k2 = Math.max(10 - (j2 - 2), 3);
	            int l2 = i2 - (j2 - 1) * k2 - 10;
	            
	        	int k3 = air;
	            int l4 = MathHelper.ceiling_double_int((double)(air - 2) * 10.0D / 300.0D);
	            int i4 = MathHelper.ceiling_double_int((double)air * 10.0D / 300.0D) - l4;
	            
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
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), (double)0.0D, (double)((float)(par3 + 0) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)0.0D, (double)((float)(par3 + par5) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), (double)0.0D, (double)((float)(par3 + par5) * f), (double)((float)(par4 + 0) * f1));
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), (double)0.0D, (double)((float)(par3 + 0) * f), (double)((float)(par4 + 0) * f1));
        tessellator.draw();
    }
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		tag.setBoolean("canSurviveOutOfWater", canSurviveOutOfWater);
	}

	@Override
	public void load(NBTTagCompound tag) 
	{
		canSurviveOutOfWater = tag.getBoolean("canSurviveOutOfWater");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/swim.png");
}
