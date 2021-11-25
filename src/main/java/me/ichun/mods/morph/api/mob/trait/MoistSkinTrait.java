package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.client.Minecraft;
import net.minecraft.util.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MoistSkinTrait extends Trait<MoistSkinTrait>
{
    public Integer maxMoistness;

    public transient int moistness = -100;
    public transient int lastAir;

    public MoistSkinTrait()
    {
        type = "traitMoistSkin";
    }

    @Override
    public void addHooks()
    {
        if(maxMoistness == null)
        {
            maxMoistness = 2400;
        }
    }

    @Override
    public void tick(float strength)
    {
        if(moistness == -100)
        {
            moistness = maxMoistness;
        }

        if (player.isInWaterRainOrBubbleColumn())
        {
            moistness = maxMoistness;
        }
        else
        {
            moistness--;
            if(!player.world.isRemote && moistness <= 0)
            {
                player.attackEntityFrom(DamageSource.DRYOUT, 1.0F);
            }
        }

    }

    @Override
    public MoistSkinTrait copy()
    {
        return new MoistSkinTrait();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.AIR && Minecraft.getInstance().getRenderViewEntity() == player)
        {
            int moistToAir = (int)Math.floor((float)moistness / maxMoistness * 300F);

            if(moistToAir < player.getAir()) //if our moistness is lower than the air we have, then we override
            {
                lastAir =  player.getAir();

                player.setAir(moistToAir);
            }
            else
            {
                lastAir = -1000;
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if(event.getType() == RenderGameOverlayEvent.ElementType.AIR && Minecraft.getInstance().getRenderViewEntity() == player)
        {
            if(lastAir != -1000)
            {
                player.setAir(lastAir);
            }
        }
    }
}
