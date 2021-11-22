package me.ichun.mods.morph.api.mob.trait;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.morph.MorphInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class SunburnTrait extends Trait<SunburnTrait>
{
    public Boolean childrenAreImmune;

    public SunburnTrait()
    {
        type = "traitSunburn";
    }

    @Override
    public void tick(float strength)
    {
        if(player.isAlive() && shouldPlayerBurn() && isPlayerInDaylight())
        {
            boolean shouldBurn = true;

            ItemStack itemstack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageable()) {
                    itemstack.setDamage(itemstack.getDamage() + player.getRNG().nextInt(2));
                    if (itemstack.getDamage() >= itemstack.getMaxDamage()) {
                        player.sendBreakAnimation(EquipmentSlotType.HEAD);
                        player.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
                    }
                }

                shouldBurn = false;
            }

            if (shouldBurn)
            {
                player.setFire(8);
            }
        }
    }

    public boolean shouldPlayerBurn()
    {
        if(childrenAreImmune != null && childrenAreImmune)
        {
            MorphInfo morphInfo = MorphApi.getApiImpl().getMorphInfo(player);
            LivingEntity morphEntity = morphInfo.getActiveMorphEntity();
            return morphEntity != null && morphEntity.isChild();
        }
        return true;
    }

    public boolean isPlayerInDaylight() //taken from MobEntity.isInDaylight()
    {
        if (player.world.isDaytime() && !player.world.isRemote) {
            float f = player.getBrightness();
            BlockPos blockpos = player.getRidingEntity() instanceof BoatEntity ? (new BlockPos(player.getPosX(), (double)Math.round(player.getPosY()), player.getPosZ())).up() : new BlockPos(player.getPosX(), (double)Math.round(player.getPosY()), player.getPosZ());
            if (f > 0.5F && player.getRNG().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && player.world.canSeeSky(blockpos)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public SunburnTrait copy()
    {
        return null;
    }
}
