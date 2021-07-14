package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateMorph extends AbstractPacket //Only used for the full list of variants. Singular morphs should not use this!
{
    public CompoundNBT nbt;

    public PacketUpdateMorph(){}

    public PacketUpdateMorph(CompoundNBT nbt)
    {
        this.nbt = nbt;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeCompoundTag(nbt);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        nbt = buf.readCompoundTag();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        MorphVariant variant = MorphVariant.createFromNBT(nbt);

        context.enqueueWork(() -> Morph.eventHandlerClient.updateMorph(variant));
    }
}
