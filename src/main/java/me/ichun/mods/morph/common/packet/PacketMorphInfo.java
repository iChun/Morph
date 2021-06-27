package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketMorphInfo extends AbstractPacket
{
    public int entId;
    public CompoundNBT nbt;

    public PacketMorphInfo(){}

    public PacketMorphInfo(int id, CompoundNBT nbt)
    {
        this.entId = id;
        this.nbt = nbt;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(entId);
        buf.writeCompoundTag(nbt);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        entId = buf.readInt();
        nbt = buf.readCompoundTag();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(this::handleClient);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient()
    {
        Entity entity = Minecraft.getInstance().world.getEntityByID(entId);
        if(entity instanceof PlayerEntity && !entity.removed) // we use capabilities, if the entity is removed, then caps will error
        {
            MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)entity).read(nbt);
        }
    }
}
