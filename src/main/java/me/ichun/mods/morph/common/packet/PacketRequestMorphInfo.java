package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class PacketRequestMorphInfo extends AbstractPacket
{
    public UUID playerId;

    public PacketRequestMorphInfo(){}

    public PacketRequestMorphInfo(UUID id)
    {
        playerId = id;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeUniqueId(playerId);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        playerId = buf.readUniqueId();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender().getServerWorld().getPlayerByUuid(playerId);
            if(player != null && !player.removed)
            {
                Morph.channel.sendTo(new PacketMorphInfo(player.getEntityId(), MorphHandler.INSTANCE.getMorphInfo(player).write(new CompoundNBT())), context.getSender());
            }
        });
    }
}
