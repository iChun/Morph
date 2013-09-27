package morph.common.ability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import morph.api.Ability;

public class AbilityPoisonResistance extends Ability {

    @Override
    public String getType() {
        return "poisonResistance";
    }

    @Override
    public void tick() {
        if(this.getParent().isPotionActive(Potion.poison))
            this.getParent().removePotionEffect(Potion.poison.id);
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
        //Get someone to make a better icon -Lomeli12
        return iconResource;
    }

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/poisonResistance.png");

}
