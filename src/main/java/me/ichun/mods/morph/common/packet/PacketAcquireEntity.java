package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.morph.client.entity.EntityMorphAcquisition;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketAcquireEntity extends AbstractPacket
{
    public int acquiredId;
    public int acquirerId;

    public PacketAcquireEntity(){}

    public PacketAcquireEntity(int edId, int erId)
    {
        this.acquiredId = edId;
        this.acquirerId = erId;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        buffer.writeInt(acquiredId);
        buffer.writeInt(acquirerId);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        acquiredId = buffer.readInt();
        acquirerId = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        Minecraft mc = Minecraft.getMinecraft();
        Entity acquired = mc.world.getEntityByID(acquiredId);
        Entity acquirer = mc.world.getEntityByID(acquirerId);
        if(acquired instanceof EntityLivingBase && acquirer instanceof EntityLivingBase && Morph.config.disableMorphAcquisitionAnimation != 1)
        {
            mc.world.spawnEntity(new EntityMorphAcquisition(mc.world, (EntityLivingBase)acquired, (EntityLivingBase)acquirer));
            acquired.setDead();
        }
    }
}
