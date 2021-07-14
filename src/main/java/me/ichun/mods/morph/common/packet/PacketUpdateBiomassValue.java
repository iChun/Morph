package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateBiomassValue extends AbstractPacket
{
    public double value;

    public PacketUpdateBiomassValue(){}

    public PacketUpdateBiomassValue(double value)
    {
        this.value = value;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeDouble(value);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        value = buf.readDouble();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            Morph.eventHandlerClient.morphData.biomass = value;

            Morph.eventHandlerClient.hudHandler.update(Morph.eventHandlerClient.morphData);
        });
    }
}
