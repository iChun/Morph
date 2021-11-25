package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketInvalidateClientHealth extends AbstractPacket
{
    public PacketInvalidateClientHealth(){}

    @Override
    public void writeTo(PacketBuffer buf){}

    @Override
    public void readFrom(PacketBuffer buf){}

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(this::handleClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Minecraft.getInstance().player.hasValidHealth = false;
    }
}
