package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import morph.api.Ability;

public class AbilityWitherResistance extends Ability {

	@Override
	public String getType() 
	{
		return "witherResistance";
	}

	@Override
	public void tick() 
	{
		if (this.getParent().isPotionActive(Potion.wither))
			this.getParent().removePotionEffect(Potion.wither.id);
	}

	@Override
	public void kill() 
	{
	}

	@Override
	public Ability clone() 
	{
		return new AbilityWitherResistance();
	}

	@Override
	public void save(NBTTagCompound tag) 
	{
	}

	@Override
	public void load(NBTTagCompound tag) 
	{
	}

	@Override
	public void postRender() 
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean entityHasAbility(EntityLivingBase living) 
	{
		if (living instanceof EntitySkeleton) 
		{
			EntitySkeleton skele = (EntitySkeleton) living;
			if (skele.getSkeletonType() != 1) 
			{
				return false;
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}

	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/witherResistance.png");
}
