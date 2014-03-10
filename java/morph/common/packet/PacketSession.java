package morph.common.packet;

import cpw.mods.fml.relauncher.Side;
import ichun.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.common.core.SessionState;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSession extends AbstractPacket
{

    public PacketSession(){}

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeBoolean(SessionState.abilities);
        buffer.writeBoolean(SessionState.canSleepMorphed);
        buffer.writeBoolean(SessionState.allowMorphSelection);
        buffer.writeBoolean(SessionState.allowFlight);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        SessionState.abilities = buffer.readBoolean();
        SessionState.canSleepMorphed = buffer.readBoolean();
        SessionState.allowMorphSelection = buffer.readBoolean();
        SessionState.allowFlight = buffer.readBoolean();
    }
}
