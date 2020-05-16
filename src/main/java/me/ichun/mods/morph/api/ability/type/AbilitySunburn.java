package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.AbilityHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySunburn extends Ability
{
    public AbilityFireImmunity fireImmunityInstance = new AbilityFireImmunity();
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/sunburn.png");

    @Override
    public String getType()
    {
        return "sunburn";
    }

    @Override
    public void tick()
    {
        boolean isChild = false;

        if(!getParent().getEntityWorld().isRemote && getParent() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)getParent();
            MorphInfo info = Morph.eventHandlerServer.morphsActive.get(player.getName());

            if(player.capabilities.isCreativeMode)
            {
                isChild = true;
            }

            if(info != null && info.nextState.getEntInstance(getParent().getEntityWorld()).isChild())
            {
                isChild = true;
            }
            //TODO for "The Thing" as well
        }

        if (getParent().getEntityWorld().isDaytime() && !getParent().getEntityWorld().isRemote && !isChild)
        {
            float f = getParent().getBrightness();

            if (f > 0.5F && getParent().getRNG().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && getParent().getEntityWorld().canBlockSeeSky(new BlockPos(MathHelper.floor(getParent().posX), MathHelper.floor(getParent().posY), MathHelper.floor(getParent().posZ))))
            {
                boolean flag = true;
                ItemStack itemstack = getParent().getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                Morph.LOGGER.info(itemstack);

                if(!(itemstack.getItem() instanceof ItemAir))
                {
                    if(itemstack.isItemStackDamageable())
                    {
                        itemstack.setItemDamage(itemstack.getItemDamage() + getParent().getRNG().nextInt(2));

                        if(itemstack.getItemDamage() >= itemstack.getMaxDamage())
                        {
                            getParent().renderBrokenItemStack(itemstack);
                            getParent().setItemStackToSlot(EntityEquipmentSlot.HEAD, null);
                        }
                    }

                    flag = false;
                }

                if(flag)
                {
                    getParent().setFire(8);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean entityHasAbility(EntityLivingBase living)
    {
        if(AbilityHandler.getInstance().hasAbility(living.getClass(), "fireImmunity") && fireImmunityInstance.entityHasAbility(living))
        {
            return false;
        }
        return !living.isChild();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
