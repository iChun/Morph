package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDemorph extends AbstractPacket
{
    public String name;

    public PacketDemorph(){}

    public PacketDemorph(String name)
    {
        this.name = name;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, name);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        Morph.eventHandlerClient.morphsActive.remove(name);
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }
}
