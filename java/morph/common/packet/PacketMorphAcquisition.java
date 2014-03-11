package morph.common.packet;

import cpw.mods.fml.relauncher.Side;
import ichun.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.client.entity.EntityMorphAcquisition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class PacketMorphAcquisition extends AbstractPacket
{

    public int entityID1;
    public int entityID2;

    public PacketMorphAcquisition(){}

    public PacketMorphAcquisition(int id1, int id2)
    {
        entityID1 = id1;
        entityID2 = id2;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        //serverside only
        buffer.writeInt(entityID1);
        buffer.writeInt(entityID2);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            Minecraft mc = Minecraft.getMinecraft();

            Entity ent = mc.theWorld.getEntityByID(buffer.readInt());
            Entity ent1 = mc.theWorld.getEntityByID(buffer.readInt());

            if(ent instanceof EntityLivingBase && ent1 instanceof EntityLivingBase)
            {
                mc.theWorld.spawnEntityInWorld(new EntityMorphAcquisition(mc.theWorld, (EntityLivingBase)ent, (EntityLivingBase)ent1));
                ent.setDead();
            }
        }
    }
}
