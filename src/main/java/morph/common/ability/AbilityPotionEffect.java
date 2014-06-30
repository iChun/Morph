package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class AbilityPotionEffect extends Ability
{
    public int potionId;
    public int duration;
    public int amplifier;
    public boolean ambient;

    public AbilityPotionEffect()
    {
        this.potionId = 0;
        this.duration = 0;
        this.amplifier = 0;
        this.ambient = true;
    }

    public AbilityPotionEffect(int id, int dur, int amp, boolean amb)
    {
        this.potionId = id;
        this.duration = dur;
        this.amplifier = amp;
        this.ambient = amb;
    }

    public Ability parse(String[] args)
    {
        potionId = Integer.parseInt(args[0]);
        duration = Integer.parseInt(args[1]);
        amplifier = Integer.parseInt(args[2]);
        ambient = Boolean.parseBoolean(args[3]);
        return this;
    }

    @Override
    public String getType()
    {
        return "potionEffect";
    }

    @Override
    public void tick()
    {
    }

    @Override
    public void kill()
    {
    }

    @Override
    public Ability clone()
    {
        return new AbilityPotionEffect(potionId, duration, amplifier, ambient);
    }

    @Override
    public void save(NBTTagCompound tag)
    {
        tag.setInteger("potionId", potionId);
        tag.setInteger("duration", duration);
        tag.setInteger("amplifier", amplifier);
        tag.setBoolean("ambient", ambient);
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        potionId = tag.getInteger("potionId");
        duration = tag.getInteger("duration");
        amplifier = tag.getInteger("amplifier");
        ambient = tag.getBoolean("ambient");
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

    @Override
    public ResourceLocation getIcon()
    {
        return null;
    }
}
