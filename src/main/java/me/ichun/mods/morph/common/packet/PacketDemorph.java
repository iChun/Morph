package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

public class PacketDemorph extends AbstractPacket
{
    public String name;

    public PacketDemorph(){}

    public PacketDemorph(String name)
    {
        this.name = name;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, name);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        Morph.proxy.tickHandlerClient.morphsActive.remove(name);
    }
}
