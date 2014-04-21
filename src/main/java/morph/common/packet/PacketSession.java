package morph.common.packet;

import cpw.mods.fml.relauncher.Side;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSession extends AbstractPacket
{

    public PacketSession(){}

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(Morph.config.getSessionInt("abilities"));
        buffer.writeInt(Morph.config.getSessionInt("canSleepMorphed"));
        buffer.writeInt(Morph.config.getSessionInt("allowMorphSelection"));
        buffer.writeInt(Morph.config.getSessionInt("allowFlight"));
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        Morph.config.updateSession("abilities", buffer.readInt());
        Morph.config.updateSession("canSleepMorphed", buffer.readInt());
        Morph.config.updateSession("allowMorphSelection", buffer.readInt());
        Morph.config.updateSession("allowFlight", buffer.readInt());
    }
}
