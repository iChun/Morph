package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class AbilityFlightFlap extends Ability
{
    public Boolean resetUpwardsMotion; //reset motionY when the trigered?
    public Double upwardsMotion; //the upwards motion to set/add
    public Integer limit; //limit the number of times this can be triggered (until the entity hits the ground)

    public transient boolean onGround;
    public transient int limitCount;
    public transient boolean jumpKeyHeld;

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fly.png");

    @Override
    public String getType() {
        return "flightFlap";
    }

    @Override
    public void init()
    {
        onGround = false;
        limitCount = limit != null ? limit : -1;
        jumpKeyHeld = false;
    }

    @Override
    public void tick()
    {
        if(Morph.config.enableFlight == 0)
        {
            return;
        }
        if(getParent().world.isRemote)
        {
            tickClient();
        }
        getParent().fallDistance -= getParent().fallDistance * getStrength();
    }

    @SideOnly(Side.CLIENT)
    public void tickClient()
    {
        if(getParent() == Minecraft.getMinecraft().player && !Minecraft.getMinecraft().player.capabilities.isFlying)
        {
            if(getParent().onGround && !onGround && limit != null && limit > 0)
            {
                limitCount = limit;
            }
            if(!onGround && Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode()) && !jumpKeyHeld && (limitCount < 0 || limitCount > 0))
            {
                limitCount--;
                if(resetUpwardsMotion != null && resetUpwardsMotion)
                {
                    getParent().motionY = 0F;
                }
                double amount = (upwardsMotion != null ? upwardsMotion : 0.42D);
                if (getParent().isPotionActive(MobEffects.JUMP_BOOST))
                {
                    amount += (double)((float)(getParent().getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
                }
                amount *= getStrength();
                getParent().motionY += amount;
            }
            onGround = getParent().onGround;
            jumpKeyHeld = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump.getKeyCode());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon()
    {
        return iconResource;
    }
}
