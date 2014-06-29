package morph.common.packet;

import cpw.mods.fml.relauncher.Side;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSession extends AbstractPacket
{

    public EntityPlayer player;

    public PacketSession(){}

    public PacketSession(EntityPlayer player)
    {
        this.player = player;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeBoolean(player == null); //is serverwide session packet
        buffer.writeInt(Morph.config.getSessionInt("abilities"));
        buffer.writeInt(Morph.config.getSessionInt("canSleepMorphed"));
        buffer.writeInt(Morph.config.getSessionInt("allowMorphSelection"));
        buffer.writeInt(player != null && Morph.config.getSessionInt("disableEarlyGameFlightMode") == 1 && Morph.config.getSessionInt("disableEarlyGameFlight") > 0 ? Morph.config.getSessionInt("disableEarlyGameFlight") == 1 ? (Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player).getBoolean("hasTravelledToNether") ? 1 : 0) : (Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player).getBoolean("hasKilledWither") ? 1 : 0) : Morph.config.getSessionInt("allowFlight"));
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        boolean isServerwideSession = buffer.readBoolean();
        Morph.config.updateSession("abilities", buffer.readInt());
        Morph.config.updateSession("canSleepMorphed", buffer.readInt());
        Morph.config.updateSession("allowMorphSelection", buffer.readInt());
        if(!isServerwideSession)
        {
            Morph.config.updateSession("allowFlight", buffer.readInt());
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player){} //hacky fix
}
