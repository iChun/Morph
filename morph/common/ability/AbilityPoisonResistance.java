package morph.common.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import morph.api.Ability;

public class AbilityPoisonResistance extends Ability {

    @Override
    public String getType() {
        return "poisonResistance";
    }

    public boolean hasPoisonEffect(EntityLivingBase entity) {
        boolean hasEffect = false;
        if(entity != null) {
            if(entity.isPotionActive(Potion.poison) || entity.isPotionActive(Potion.wither))
                hasEffect = true;
        }
        return hasEffect;
    }

    @Override
    public void tick() {
        if(hasPoisonEffect(this.getParent())) {
            this.getParent().removePotionEffect(Potion.poison.id);
            this.getParent().getActivePotionEffect(Potion.wither).duration--;
        }
    }

    @Override
    public void kill() {
    }

    @Override
    public Ability clone() {
        return new AbilityPoisonResistance();
    }

    @Override
    public void save(NBTTagCompound tag) {
    }

    @Override
    public void load(NBTTagCompound tag) {
    }

    @Override
    public void postRender() {
    }

    @Override
    public ResourceLocation getIcon() {
        // TODO Get someone to make a better icon -Lomeli12
        return iconResource;
    }

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/poisonResistance.png");

}
