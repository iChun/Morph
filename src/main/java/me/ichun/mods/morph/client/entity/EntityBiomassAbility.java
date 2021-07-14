package me.ichun.mods.morph.client.entity;

import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class EntityBiomassAbility extends Entity
{
    @Nonnull
    public PlayerEntity player;

    public int fadeTime;
    public int solidTime;
    public int age;
    public MorphRenderHandler.ModelRendererCapture capture = new MorphRenderHandler.ModelRendererCapture();

    public EntityBiomassAbility(EntityType<?> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
        setInvisible(true);
        setInvulnerable(true);
        setEntityId(ClientEntityTracker.getNextEntId());
    }

    public EntityBiomassAbility setInfo(@Nonnull PlayerEntity player, int fadeTime, int solidTime)
    {
        this.player = player;
        this.fadeTime = fadeTime;
        this.solidTime = solidTime;

        syncWithOriginPosition();

        return this;
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;

        if(!player.isAlive() || !player.world.getDimensionKey().equals(world.getDimensionKey())) //parent is "dead"
        {
            if(player.removed)
            {
                remove();
            }
        }
        else if(age > (fadeTime * 2) + solidTime)
        {
            remove();
        }
        else //parent is "alive" and safe
        {
            this.setPosition(player.getPosX(), player.getPosY() + (player.getHeight() / 2D), player.getPosZ());
            this.setRotation(player.rotationYaw, player.rotationPitch);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return player.getRenderBoundingBox();
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return player.isInRangeToRenderDist(distance);
    }

    @Override
    public float getBrightness()
    {
        return player.getBrightness();
    }

    @Override
    protected void registerData(){}

    @Override
    public boolean writeUnlessRemoved(CompoundNBT compound) { return false; } //disable saving of entity

    @Override
    protected void readAdditional(CompoundNBT compound){}

    @Override
    protected void writeAdditional(CompoundNBT compound){}

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float getSkinAlpha(float partialTick)
    {
        float alpha;
        if(age < fadeTime)
        {
            alpha = EntityHelper.sineifyProgress(MathHelper.clamp((age + partialTick) / fadeTime, 0F, 1F));
        }
        else if(age >= fadeTime + solidTime)
        {
            alpha = EntityHelper.sineifyProgress(1F - MathHelper.clamp((age - (fadeTime + solidTime) + partialTick) / fadeTime, 0F, 1F));
        }
        else
        {
            alpha = 1F;
        }
        return alpha;
    }

    public void syncWithOriginPosition()
    {
        this.setLocationAndAngles(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
        this.lastTickPosX = player.lastTickPosX;
        this.lastTickPosY = player.lastTickPosY;
        this.lastTickPosZ = player.lastTickPosZ;

        this.prevPosX = player.prevPosX;
        this.prevPosY = player.prevPosY;
        this.prevPosZ = player.prevPosZ;
    }
}
