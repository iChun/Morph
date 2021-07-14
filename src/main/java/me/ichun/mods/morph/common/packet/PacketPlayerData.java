package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPlayerData extends AbstractPacket
{
    public CompoundNBT nbt;

    public PacketPlayerData(){}

    public PacketPlayerData(CompoundNBT nbt)
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
        PlayerMorphData playerMorphData = new PlayerMorphData();
        playerMorphData.read(nbt);

        context.enqueueWork(() -> {
            Morph.eventHandlerClient.setPlayerMorphData(playerMorphData);
        });
    }
}
