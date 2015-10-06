package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.morph.client.entity.EntityMorphAcquisition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

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
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(acquiredId);
        buffer.writeInt(acquirerId);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        acquiredId = buffer.readInt();
        acquirerId = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        Minecraft mc = Minecraft.getMinecraft();
        Entity acquired = mc.theWorld.getEntityByID(acquiredId);
        Entity acquirer = mc.theWorld.getEntityByID(acquirerId);
        if(acquired instanceof EntityLivingBase && acquirer instanceof EntityLivingBase)
        {
            mc.theWorld.spawnEntityInWorld(new EntityMorphAcquisition(mc.theWorld, (EntityLivingBase)acquired, (EntityLivingBase)acquirer));
            acquired.setDead();
        }
    }
}
