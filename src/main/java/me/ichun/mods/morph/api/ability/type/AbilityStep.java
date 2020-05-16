package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class AbilityStep extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/step.png");
    public float stepHeight;

    public AbilityStep()
    {
        stepHeight = 1.0F;
    }

    public AbilityStep(float height)
    {
        stepHeight = height;
    }

    @Override
    public String getType()
    {
        return "step";
    }

    @Override
    public Ability parse(String[] args)
    {
        stepHeight = Float.parseFloat(args[0]);
        return this;
    }

    @Override
    public void tick()
    {
        MorphInfo info;

        EntityPlayer player = (EntityPlayer) getParent();
        if(!player.getEntityWorld().isRemote)
        {
            info = Morph.eventHandlerServer.morphsActive.get(player.getName());
        }
        else
        {
            info = Morph.eventHandlerClient.morphsActive.get(player.getName());
        }

        if(info != null && info.nextState.getEntInstance(player.getEntityWorld()).isChild())
        {
            return;
        }

        if(getParent().stepHeight != stepHeight)
        {
            getParent().stepHeight = stepHeight;
        }
    }

    @Override
    public Ability clone()
    {
        return new AbilityStep(stepHeight);
    }

    @Override
    public void kill(ArrayList<Ability> nextAbilities)
    {
        if(getParent() != null && getParent().stepHeight == stepHeight)
        {
            getParent().stepHeight = 0.5F;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean entityHasAbility(EntityLivingBase living)
    {
        return !living.isChild();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
