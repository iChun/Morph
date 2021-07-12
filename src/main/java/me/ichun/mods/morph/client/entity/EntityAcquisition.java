package me.ichun.mods.morph.client.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.ichunutil.client.tracker.ClientEntityTracker;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
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
public class EntityAcquisition extends Entity
{
    @Nonnull
    public LivingEntity livingOrigin;
    @Nonnull
    public LivingEntity livingAcquired;

    public boolean isMorphAcquisition;

    public ArrayList<Tendril> tendrils = new ArrayList<>();

    public int maxRequiredTendrils;
    public int age;

    public MorphRenderHandler.ModelRendererCapture acquiredCapture = new MorphRenderHandler.ModelRendererCapture();

    public EntityAcquisition(EntityType<?> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
        setInvisible(true);
        setInvulnerable(true);
        setEntityId(ClientEntityTracker.getNextEntId());
    }

    public EntityAcquisition setTargets(@Nonnull LivingEntity origin, @Nonnull LivingEntity acquired, boolean isMorphAcquisition)
    {
        this.livingOrigin = origin;
        this.livingAcquired = acquired;
        this.isMorphAcquisition = isMorphAcquisition;

        syncWithOriginPosition();

        if(isMorphAcquisition)
        {
            tendrils.add(new Tendril(null).headTowards(getTargetPos(), false));
        }
        return this;
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;

        if(!livingOrigin.isAlive() || !livingOrigin.world.getDimensionKey().equals(world.getDimensionKey())) //parent is "dead"
        {
            if(livingOrigin.removed)
            {
                remove();
            }
        }
        else if(age > Morph.configClient.acquisitionTendrilMaxChild * 10 + 100) //probably too long, kill it off
        {
            remove();

            if(livingOrigin instanceof PlayerEntity)
            {
                EntityBiomassAbility ability = Morph.EntityTypes.BIOMASS_ABILITY.create(world).setInfo((PlayerEntity)livingOrigin, 10, 0);
                ((ClientWorld)world).addEntity(ability.getEntityId(), ability);
            }
        }
        else //parent is "alive" and safe
        {
            this.setPosition(livingOrigin.getPosX(), livingOrigin.getPosY() + (livingOrigin.getHeight() / 2D), livingOrigin.getPosZ());
            this.setRotation(livingOrigin.rotationYaw, livingOrigin.rotationPitch);

            boolean allDone = !tendrils.isEmpty();
            boolean anyRetracting = false;
            boolean anyNonRetracting = false;
            for(Tendril tendril : tendrils)
            {
                if(!tendril.isDone())
                {
                    allDone = false;
                    tendril.tick();

                    if(tendril.retract)
                    {
                        anyRetracting = true;
                    }
                    else
                    {
                        anyNonRetracting = true;
                    }
                }
                else
                {
                    anyRetracting = true;
                }
            }

            if(isMorphAcquisition)
            {
                if(tendrils.size() < 5 && age % 2 == 0)
                {
                    tendrils.add(new Tendril(null).headTowards(getTargetPos(), false));
                    allDone = false;//do not remove, we're not done yet
                }
                if(anyRetracting && anyNonRetracting)
                {
                    for(Tendril tendril : tendrils)
                    {
                        tendril.propagateRetractToChild();
                    }
                }
            }
            else
            {
                if(tendrils.size() < maxRequiredTendrils && !acquiredCapture.infos.isEmpty() && age % 3 == 0)
                {
                    tendrils.add(new Tendril(null).headTowards(getTargetPos(), true));
                    allDone = false;//do not remove, we're not done yet
                }
            }

            if(allDone)
            {
                remove();

                if(livingOrigin instanceof PlayerEntity)
                {
                    EntityBiomassAbility ability = Morph.EntityTypes.BIOMASS_ABILITY.create(world).setInfo((PlayerEntity)livingOrigin, 10, 0);
                    ((ClientWorld)world).addEntity(ability.getEntityId(), ability);
                }
            }
        }
    }

    public Vector3d getTargetPos()
    {
        return livingAcquired.getPositionVec().add(0D, livingAcquired.getHeight() / 2D, 0D);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return livingOrigin.getBoundingBox().union(livingAcquired.getBoundingBox());
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return livingOrigin.isInRangeToRenderDist(distance) || livingAcquired.isInRangeToRenderDist(distance);
    }

    @Override
    public float getBrightness()
    {
        return livingOrigin.getBrightness();
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
        double height = (livingOrigin.getHeight() / 2D);
        this.setLocationAndAngles(livingOrigin.getPosX(), livingOrigin.getPosY() + height, livingOrigin.getPosZ(), livingOrigin.rotationYaw, livingOrigin.rotationPitch);
        this.lastTickPosX = livingOrigin.lastTickPosX;
        this.lastTickPosY = livingOrigin.lastTickPosY + height;
        this.lastTickPosZ = livingOrigin.lastTickPosZ;

        this.prevPosX = livingOrigin.prevPosX;
        this.prevPosY = livingOrigin.prevPosY + height;
        this.prevPosZ = livingOrigin.prevPosZ;
    }

    public class Tendril
    {
        @Nullable
        private Tendril parent; //if null, is the base tendril

        private Tendril child;
        private Vector3d offset;
        private float yaw;
        private float pitch;
        private float lastHeight = 1F;
        private float height = 1F;

        private float maxGrowth = 7F + (float)rand.nextGaussian() * 2F;

        private boolean retract;
        private int retractTime;

        private float prevRotateSpin;
        private float rotateSpin;
        private float spinFactor = (float)rand.nextGaussian() * 15F;

        public int depth = 0;

        private MorphRenderHandler.ModelRendererCapture capture;

        public Tendril(Tendril parent)
        {
            this.parent = parent;
            if(parent != null)
            {
                this.offset = parent.getReachOffset().subtract(getVectorForRotation(parent.pitch, parent.yaw).mul(0.025F, 0.025F, 0.025F));
                this.depth = parent.depth + 1;
            }
            else
            {
                this.offset = new Vector3d(0D, 0D, 0D);
            }
        }

        public Tendril headTowards(Vector3d pos, boolean rev)
        {
            float randGaus = 5F;
            if(parent != null)
            {
                yaw = parent.yaw;
                pitch = parent.pitch;

                Vector3d origin = parent.getReachCoord();
                double d0 = pos.getX() - origin.getX();
                double d1 = pos.getY() - origin.getY();
                double d2 = pos.getZ() - origin.getZ();

                float maxChange = isMorphAcquisition ? 30F : 60F;

                double dist = MathHelper.sqrt(d0 * d0 + d2 * d2);
                float newYaw = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                float newPitch = (float)(-(MathHelper.atan2(d1, dist) * (double)(180F / (float)Math.PI)));
                this.pitch = EntityHelper.updateRotation(this.pitch, newPitch, maxChange);
                this.yaw = EntityHelper.updateRotation(this.yaw, newYaw, maxChange);
            }
            else
            {
                yaw = (rev ? (livingOrigin.renderYawOffset + 180F) : livingOrigin.renderYawOffset) % 360F;
                pitch = 0;

                if(!isMorphAcquisition)
                {
                    randGaus = 30F;
                }

                yaw += (5F + 25F * rand.nextFloat()) * (rand.nextBoolean() ? 1F : -1F);
                pitch += (float)rand.nextGaussian() * randGaus;
            }

            yaw += (float)rand.nextGaussian() * randGaus;
            pitch += (float)rand.nextGaussian() * randGaus;

            return this;
        }

        public void tick()
        {
            lastHeight = height;
            if(retract)
            {
                retractTime++;
            }

            if(child != null)
            {
                child.tick();
            }
            else
            {
                float distToEnt = livingOrigin.getDistance(livingAcquired);
                if(!isMorphAcquisition)
                {
                    distToEnt *= 2F;
                }
                if(!retract)
                {
                    if(height < maxGrowth)
                    {
                        float maxTendrilGrowth = Math.max(0.0625F, distToEnt / Morph.configClient.acquisitionTendrilMaxChild + (float)rand.nextGaussian() * 0.125F); //in blocks
                        height += maxTendrilGrowth * 16F;
                        if(getReachCoord().distanceTo(getTargetPos()) < Math.max(0.3F, maxTendrilGrowth)) //close enough?
                        {
                            if(!isMorphAcquisition && age <= 10)
                            {
                                height -= maxTendrilGrowth * 16F; //wait for age to finish
                                return;
                            }

                            child = new Tendril(this);

                            Vector3d pos = getTargetPos();
                            Vector3d origin = getReachCoord();
                            double d0 = pos.getX() - origin.getX();
                            double d1 = pos.getY() - origin.getY();
                            double d2 = pos.getZ() - origin.getZ();

                            double dist = MathHelper.sqrt(d0 * d0 + d2 * d2);
                            child.yaw = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                            child.pitch = (float)(-(MathHelper.atan2(d1, dist) * (double)(180F / (float)Math.PI)));
                            child.lastHeight = child.height = (float)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) / 16F;

                            if(isMorphAcquisition)
                            {
                                child.capture = acquiredCapture;
                                acquiredCapture = null;
                            }
                            else if(!acquiredCapture.infos.isEmpty())
                            {
                                child.capture = new MorphRenderHandler.ModelRendererCapture();
                                int count = (int)Math.ceil(Math.max(acquiredCapture.infos.size() / 10F, 1));
                                for(int x = 0; x < count && !acquiredCapture.infos.isEmpty(); x++)
                                {
                                    int i = rand.nextInt(acquiredCapture.infos.size());
                                    child.capture.infos.add(acquiredCapture.infos.get(i));
                                    acquiredCapture.infos.remove(i);
                                    if(x > 0)
                                    {
                                        maxRequiredTendrils--;
                                    }
                                }
                            }

                            child.propagateRetractToParent();
                        }

                        if(!isMorphAcquisition && acquiredCapture.infos.isEmpty()) //oops we're out of blocks. retract
                        {
                            propagateRetractToParent();
                        }
                    }
                    else
                    {
                        child = new Tendril(this).headTowards(getTargetPos(), false);
                    }
                }
                else if(retractTime <= 3)
                {
                    float maxTendrilGrowth = Math.max(0.0625F, distToEnt / Morph.configClient.acquisitionTendrilMaxChild + (float)rand.nextGaussian() * 0.125F); //in blocks
                    if(getReachCoord().distanceTo(getTargetPos()) > Math.max(0.5F, maxTendrilGrowth))
                    {
                        Vector3d pos = getTargetPos();
                        Vector3d origin = getReachCoord();
                        double d0 = pos.getX() - origin.getX();
                        double d1 = pos.getY() - origin.getY();
                        double d2 = pos.getZ() - origin.getZ();

                        double dist = MathHelper.sqrt(d0 * d0 + d2 * d2);
                        yaw = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                        pitch = (float)(-(MathHelper.atan2(d1, dist) * (double)(180F / (float)Math.PI)));

                        height += maxTendrilGrowth * 16F;
                    }
                }
                else if(height > 0 && retractTime > 6)
                {
                    prevRotateSpin = rotateSpin;
                    if(capture != null)
                    {
                        rotateSpin += spinFactor;
                    }

                    float maxTendrilGrowth = Math.max(0.0625F, distToEnt / (Morph.configClient.acquisitionTendrilMaxChild * 2F) + (float)rand.nextGaussian() * 0.125F); //in blocks
                    height -= maxTendrilGrowth * 16F;
                    if(height <= 0)
                    {
                        height = 0;

                        if(parent != null)
                        {
                            parent.child = null; //remove ourselves.
                            if(capture != null)
                            {
                                parent.capture = capture;
                                parent.prevRotateSpin = prevRotateSpin;
                                parent.rotateSpin = rotateSpin;
                            }
                        }
                    }
                }
            }
        }

        public boolean isDone()
        {
            return retract && height <= 0F;
        }

        public void propagateRetractToParent()
        {
            retract = true;
            if(parent != null)
            {
                parent.propagateRetractToParent();
            }
        }

        public void propagateRetractToChild()
        {
            retract = true;
            if(child != null)
            {
                child.propagateRetractToChild();
            }
        }

        public Vector3d getReachOffset()
        {
            float growth = height / 16F;
            return offset.add(getVectorForRotation(pitch, yaw).mul(growth, growth, growth));
        }

        public Vector3d getReachCoord()
        {
            return EntityAcquisition.this.getPositionVec().add(getReachOffset());
        }

        public float getWidth(float partialTick)
        {
            float width = 1F + (0.2F * remainingDepth(partialTick));
            if(width > 3.5F)
            {
                width = 3.5F;
            }
            return width;
        }

        public float remainingDepth(float partialTick)
        {
            int depth = 0;
            Tendril aParent = this;
            Tendril aChild = aParent.child;

            while(aChild != null)
            {
                depth += Math.min((aChild.lastHeight + (aChild.height - aChild.lastHeight) * partialTick) / aChild.maxGrowth, 1F);

                aParent = aChild;
                aChild = aParent.child;
            }

            return depth;
        }

        public void createModelRenderer(ArrayList<ModelRenderer> renderers, float partialTick)
        {
            if(child != null)
            {
                child.createModelRenderer(renderers, partialTick);
            }

            ModelRenderer model = new ModelRenderer(64, 64, rand.nextInt(8), rand.nextInt(8));
            float width = getWidth(partialTick);
            float halfWidth = width / 2F;
            model.addBox(-halfWidth, -halfWidth, 0F, width, width, lastHeight + (height - lastHeight) * partialTick);
            model.rotationPointX = (float)(offset.getX() * 16F);
            model.rotationPointY = (float)(offset.getY() * 16F);
            model.rotationPointZ = (float)(offset.getZ() * 16F);
            model.rotateAngleX = (float)Math.toRadians(pitch);
            model.rotateAngleY = (float)Math.toRadians(-yaw);
            renderers.add(model);
        }

        public void renderCapture(EntityAcquisition acquisition, MatrixStack stack, IVertexBuilder vertexBuilder, int light, int overlay, float partialTick)
        {
            if(child != null)
            {
                child.renderCapture(acquisition, stack, vertexBuilder, light, overlay, partialTick);
            }
            else if(capture != null) //only at tendril endpoints.
            {
                float renderHeight = lastHeight + (height - lastHeight) * partialTick;
                float heightOffset = renderHeight / 16F;
                Vector3d look = getVectorForRotation(pitch, yaw);
                Vector3d renderPoint = offset.add(look.mul(heightOffset, heightOffset, heightOffset));

                float alpha = 1F;
                if(acquisition.livingOrigin == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
                {
                    alpha = MathHelper.clamp((depth + 1) / (float)Morph.configClient.acquisitionTendrilPartOpacity, 0F, 1F);
                }

                if(alpha > 0F)
                {
                    float scale;
                    double distToEnt = acquisition.livingOrigin.getDistance(acquisition.livingAcquired);
                    double distToRenderPoint = MathHelper.sqrt(acquisition.getDistanceSq(acquisition.getPositionVec().add(renderPoint)));
                    if(distToEnt > 0D)
                    {
                        scale = (float)(Math.min(distToEnt, (distToRenderPoint + 0.5D)) / distToEnt); //+1 to make the render still show the entity slightly as it's being pulled in.
                    }
                    else
                    {
                        scale = 0F;
                    }

                    stack.push();
                    stack.translate(renderPoint.getX(), renderPoint.getY(), renderPoint.getZ());
                    stack.scale(scale, scale, scale);
                    float rot = prevRotateSpin + (rotateSpin - prevRotateSpin) * partialTick;
                    stack.rotate(Vector3f.ZP.rotationDegrees(rot));
                    stack.rotate(Vector3f.YP.rotationDegrees(rot));
                    stack.translate(0D, -(acquisition.livingAcquired.getHeight() / 2D), 0D);
                    if(isMorphAcquisition)
                    {
                        capture.render(stack, vertexBuilder, light, overlay, alpha);
                    }
                    else
                    {
                        for(MorphRenderHandler.ModelRendererCapture.CaptureInfo info : capture.infos)
                        {
                            MatrixStack identityStack = new MatrixStack();
                            stack.push();
                            MatrixStack.Entry e = RenderHelper.createInterimStackEntry(identityStack.getLast(), info.e, MathHelper.clamp(scale * 3F, 0F, 1F));
                            MatrixStack.Entry last = stack.getLast();
                            last.getMatrix().mul(e.getMatrix());
                            last.getNormal().mul(e.getNormal());
                            info.createAndRender(stack, vertexBuilder, light, overlay, 1F, 1F, 1F, alpha);
                            stack.pop();
                        }
                    }
                    stack.pop();
                }
            }
        }
    }
}
