package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class AbilityFireImmunity extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fire_immunity.png");

    @Override
    public String getType()
    {
        return "fireImmunity";
    }

    @Override
    public void tick()
    {
        if(!getParent().isImmuneToFire())
        {
            getParent().isImmuneToFire = true;
        }
        getParent().extinguish();
    }

    @Override
    public void kill(ArrayList<Ability> nextAbilities)
    {
        if(getParent() != null)
        {
            getParent().isImmuneToFire = false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
