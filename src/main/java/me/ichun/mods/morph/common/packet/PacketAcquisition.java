package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.client.entity.EntityAcquisition;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAcquisition extends AbstractPacket
{
    public int originId;
    public int acquiredId;

    public boolean isMorphAcquisition;

    public PacketAcquisition(){}

    public PacketAcquisition(int originId, int acquiredId, boolean isMorphAcquisition)
    {
        this.originId = originId;
        this.acquiredId = acquiredId;
        this.isMorphAcquisition = isMorphAcquisition;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(originId);
        buf.writeInt(acquiredId);
        buf.writeBoolean(isMorphAcquisition);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        originId = buf.readInt();
        acquiredId = buf.readInt();
        isMorphAcquisition = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        if(isMorphAcquisition && (Morph.configClient.acquisitionPlayAnimation == 1 || Morph.configClient.acquisitionPlayAnimation == 3)|| !isMorphAcquisition && Morph.configClient.acquisitionPlayAnimation >= 2)
        {
            handleClient(context);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity origin = mc.world.getEntityByID(originId);
            Entity acquired = mc.world.getEntityByID(acquiredId);

            if(origin instanceof LivingEntity && acquired instanceof LivingEntity)
            {
                LivingEntity livingAcquired = (LivingEntity)acquired;
                EntityAcquisition ent = Morph.EntityTypes.ACQUISITION.create(mc.world).setTargets((LivingEntity)origin, livingAcquired, isMorphAcquisition);
                mc.world.addEntity(ent.getEntityId(), ent);
                if(livingAcquired != mc.player)
                {
                    livingAcquired.remove(false);
                }
                else
                {
                    EntityHelper.faceEntity(livingAcquired, origin, 360F, 360F);
                }

                //block the hurt overlay/death rotation
                acquired.setLocationAndAngles(acquired.getPosX(), acquired.getPosY(), acquired.getPosZ(), acquired.rotationYaw, acquired.rotationPitch);
                livingAcquired.prevRenderYawOffset = livingAcquired.renderYawOffset;
                livingAcquired.prevSwingProgress = livingAcquired.swingProgress;
                livingAcquired.prevLimbSwingAmount = livingAcquired.limbSwingAmount;
                livingAcquired.prevRotationYawHead = livingAcquired.rotationYawHead;
                livingAcquired.deathTime = 0;
                livingAcquired.hurtTime = 0;
            }
        });
    }
}
