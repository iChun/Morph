package me.ichun.mods.morph.client.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

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

    public void syncWithOriginPosition()
    {
        double height = (player.getHeight() / 2D);
        this.setLocationAndAngles(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
        this.lastTickPosX = player.lastTickPosX;
        this.lastTickPosY = player.lastTickPosY;
        this.lastTickPosZ = player.lastTickPosZ;

        this.prevPosX = player.prevPosX;
        this.prevPosY = player.prevPosY;
        this.prevPosZ = player.prevPosZ;
    }
}
