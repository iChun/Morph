package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityClimb extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/climb.png");

    @Override
    public String getType() {
        return "climb";
    }

    @Override
    public void tick()
    {
        if(getParent().collidedHorizontally)
        {
            getParent().motionY -= getParent().motionY * getStrength();
            if(getParent().isSneaking())
            {
                getParent().motionY += 0D * getStrength();
            }
            else
            {
                getParent().motionY += 0.2D * getStrength();
            }
            getParent().fallDistance -= getParent().fallDistance * getStrength();
        }
        getParent().fallDistance -= getParent().fallDistance * getStrength();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
