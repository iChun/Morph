package morph.common.packet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.client.entity.EntityMorphAcquisition;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
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
    public void readFrom(ByteBuf buffer, Side side)
    {
        entityID1 = buffer.readInt();
        entityID2 = buffer.readInt();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            handleClient(side, player);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        Minecraft mc = Minecraft.getMinecraft();

        Entity ent = mc.theWorld.getEntityByID(entityID1);
        Entity ent1 = mc.theWorld.getEntityByID(entityID2);

        if(ent instanceof EntityLivingBase && ent1 instanceof EntityLivingBase)
        {
            if(ent instanceof EntityPlayer)
            {
                EntityPlayer player1 = (EntityPlayer)ent;
                MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player1.getCommandSenderName());
                if(info != null)
                {
                    if(info.getMorphing())
                    {
                        ent = info.prevState.entInstance;
                    }
                    else
                    {
                        ent = info.nextState.entInstance;
                    }
                    if(player1 != mc.thePlayer)
                    {
                        player1.setDead();
                    }
                }
            }
            else
            {
                ent.setDead();
            }
            mc.theWorld.spawnEntityInWorld(new EntityMorphAcquisition(mc.theWorld, (EntityLivingBase)ent, (EntityLivingBase)ent1));
        }
    }

}
