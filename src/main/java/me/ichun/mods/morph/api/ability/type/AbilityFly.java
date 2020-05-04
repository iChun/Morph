package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class AbilityFly extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fly.png");

    public boolean slowdownInWater;

    public AbilityFly()
    {
        slowdownInWater = true;
    }

    public AbilityFly(boolean slowdown)
    {
        slowdownInWater = slowdown;
    }

    @Override
    public String getType()
    {
        return "fly";
    }

    @Override
    public Ability parse(String[] args)
    {
        slowdownInWater = Boolean.parseBoolean(args[0]);
        return this;
    }

    @Override
    public void tick()
    {
        if(getParent() instanceof EntityPlayer)
        {
            if(Morph.config.enableFlight == 0)
            {
                return;
            }
            EntityPlayer player = (EntityPlayer)getParent();
            if(!player.capabilities.allowFlying)
            {
                player.capabilities.allowFlying = true;
                player.sendPlayerAbilities();
            }
            if(player.capabilities.isFlying && !player.capabilities.isCreativeMode)
            {
                double motionX = player.getEntityWorld().isRemote ? player.motionX : player.posX - player.lastTickPosX;
                double motionZ = player.getEntityWorld().isRemote ? player.motionZ : player.posZ - player.lastTickPosZ;
                int i = Math.round(MathHelper.sqrt(motionX * motionX + motionZ * motionZ) * 100.0F);

                if (i > 0 && i < 10)
                {
                    if(player.isInWater() && slowdownInWater)
                    {
                        player.addExhaustion(0.125F * (float)i * 0.01F);
                    }
                    else
                    {
                        player.addExhaustion(0.035F * (float)i * 0.01F);
                    }
                }
                else
                {
                    player.addExhaustion(0.002F);
                }

                if(player.getEntityWorld().isRemote && player.isInWater() && slowdownInWater)
                {
                    ArrayList<Ability> abilities = Morph.eventHandlerClient.morphsActive.get(player.getName()).nextState.abilities;

                    if(!abilities.isEmpty())
                    {
                        boolean swim = false;
                        for(Ability ability : abilities)
                        {
                            if(ability.getType().equalsIgnoreCase("swim"))
                            {
                                swim = true;
                                break;
                            }
                        }
                        if(!swim)
                        {
                            player.motionX *= 0.65D;
                            player.motionZ *= 0.65D;

                            player.motionY *= 0.2D;
                        }
                    }
                }
            }
        }
        getParent().fallDistance = 0.0F;
        //TODO make "Thing" take note of this so it can fly...
        //TODO save the flying boolean to nbt someday
    }

    @Override
    public void kill(ArrayList<Ability> nextAbilities)
    {
        EntityPlayer player = (EntityPlayer)getParent();
        if(player != null && !player.capabilities.isCreativeMode)
        {
            player.capabilities.allowFlying = false;
            if(player.capabilities.isFlying)
            {
                player.capabilities.isFlying = false;
            }
            player.sendPlayerAbilities();
        }
    }

    @Override
    public Ability clone()
    {
        return new AbilityFly(slowdownInWater);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
