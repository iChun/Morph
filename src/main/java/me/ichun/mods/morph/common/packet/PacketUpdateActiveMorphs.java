package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.network.AbstractPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketUpdateActiveMorphs extends AbstractPacket
{
    public HashMap<String, MorphInfo> infosToSend = new HashMap<String, MorphInfo>();

    public PacketUpdateActiveMorphs(){}

    public PacketUpdateActiveMorphs(String player)
    {
        if(player != null)
        {
            infosToSend.put(player, Morph.proxy.tickHandlerServer.morphsActive.get(player));
        }
        else
        {
            infosToSend.putAll(Morph.proxy.tickHandlerServer.morphsActive);
        }
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        PacketBuffer pb = new PacketBuffer(buffer);
        for(Map.Entry<String, MorphInfo> e : infosToSend.entrySet())
        {
            ByteBufUtils.writeUTF8String(pb, e.getKey());
            pb.writeNBTTagCompoundToBuffer(e.getValue().write(new NBTTagCompound()));
        }
        ByteBufUtils.writeUTF8String(pb, "##endPacket");
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        PacketBuffer pb = new PacketBuffer(buffer);

        String name = ByteBufUtils.readUTF8String(pb);
        try
        {
            while(!name.equals("##endPacket"))
            {
                NBTTagCompound tag = pb.readNBTTagCompoundFromBuffer();

                MorphInfo info = new MorphInfo(null, null, null);
                info.read(tag);

                infosToSend.put(name, info);

                name = ByteBufUtils.readUTF8String(pb);
            }
        }
        catch(IOException ignored){}
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient();
    }

    @SideOnly(Side.CLIENT)
    public void handleClient()
    {
        for(Map.Entry<String, MorphInfo> e : infosToSend.entrySet())
        {
            if(e.getValue().nextState != null) //nextState was recreated successfully.
            {
                MorphInfoClient info = new MorphInfoClient(null, e.getValue().prevState, e.getValue().nextState);
                info.read(e.getValue().write(new NBTTagCompound()));
                if(Morph.proxy.tickHandlerClient.morphsActive.containsKey(e.getKey()))
                {
                    Morph.proxy.tickHandlerClient.morphsActive.get(e.getKey()).clean(); //prevent mem leaks.
                }
                Morph.proxy.tickHandlerClient.morphsActive.put(e.getKey(), info);
            }
        }
    }
}
