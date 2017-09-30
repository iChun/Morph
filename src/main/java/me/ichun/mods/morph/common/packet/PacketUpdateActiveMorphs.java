package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketUpdateActiveMorphs extends AbstractPacket
{
    public HashMap<String, MorphInfo> infosToSend = new HashMap<>();

    public PacketUpdateActiveMorphs(){}

    public PacketUpdateActiveMorphs(String player)
    {
        if(player != null)
        {
            infosToSend.put(player, Morph.eventHandlerServer.morphsActive.get(player));
        }
        else
        {
            infosToSend.putAll(Morph.eventHandlerServer.morphsActive);
        }
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        PacketBuffer pb = new PacketBuffer(buffer);
        for(Map.Entry<String, MorphInfo> e : infosToSend.entrySet())
        {
            ByteBufUtils.writeUTF8String(pb, e.getKey());
            pb.writeCompoundTag(e.getValue().write(new NBTTagCompound()));
        }
        ByteBufUtils.writeUTF8String(pb, "##endPacket");
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        PacketBuffer pb = new PacketBuffer(buffer);

        String name = ByteBufUtils.readUTF8String(pb);
        try
        {
            while(!name.equals("##endPacket"))
            {
                NBTTagCompound tag = pb.readCompoundTag();

                MorphInfo info = new MorphInfo(null, null, null);
                info.read(tag);

                infosToSend.put(name, info);

                name = ByteBufUtils.readUTF8String(pb);
            }
        }
        catch(IOException ignored){}
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        handleClient();
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        //nextState was recreated successfully.
        //prevent mem leaks.
        infosToSend.entrySet().stream().filter(e -> e.getValue().nextState != null).forEach(e ->
        {
            //TODO fix this
//            MorphInfoClient info = new MorphInfoClient(null, e.getValue().prevState, e.getValue().nextState);
//            info.read(e.getValue().write(new NBTTagCompound()));
//            if(Morph.eventHandlerClient.morphsActive.containsKey(e.getKey()))
//            {
//                Morph.eventHandlerClient.morphsActive.get(e.getKey()).clean(); //prevent mem leaks.
//            }
//            Morph.eventHandlerClient.morphsActive.put(e.getKey(), info);
//            if(e.getKey().equals(Minecraft.getMinecraft().player.getName()))
//            {
//                Morph.eventHandlerClient.renderHandInstance.reset(Minecraft.getMinecraft().world, info);
//            }
        });
    }
}
