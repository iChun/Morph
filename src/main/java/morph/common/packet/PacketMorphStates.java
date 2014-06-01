package morph.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class PacketMorphStates extends AbstractPacket
{

    public boolean clear;
    public ArrayList<NBTTagCompound> stateTags = new ArrayList<NBTTagCompound>();

    public PacketMorphStates(){}

    public PacketMorphStates(boolean clear, ArrayList<MorphState> states)
    {
        this.clear = clear;
        for(MorphState state : states)
        {
            stateTags.add(state.getTag());
        }
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeBoolean(clear);

        for(NBTTagCompound tag : stateTags)
        {
            ByteBufUtils.writeUTF8String(buffer, "state");
            ByteBufUtils.writeTag(buffer, tag);
        }
        ByteBufUtils.writeUTF8String(buffer, "##end");
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        clear = buffer.readBoolean();
        while(ByteBufUtils.readUTF8String(buffer).equalsIgnoreCase("state"))
        {
            stateTags.add(ByteBufUtils.readTag(buffer));
        }
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            handleClient(side, player);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if(clear)
        {
            Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
        }

        boolean requireReorder = false;
        for(NBTTagCompound tag : stateTags)
        {
            MorphState state = new MorphState(mc.theWorld, mc.thePlayer.getCommandSenderName(), "", null, true);

            if(tag != null)
            {
                state.readTag(mc.theWorld, tag);

                String name = state.entInstance.getCommandSenderName();

                if(name != null)
                {
                    ArrayList<MorphState> states = Morph.proxy.tickHandlerClient.playerMorphCatMap.get(name);
                    if(states == null)
                    {
                        requireReorder = true;
                        states = new ArrayList<MorphState>();
                        Morph.proxy.tickHandlerClient.playerMorphCatMap.put(name, states);
                    }
                    MorphHandler.addOrGetMorphState(states, state);
                }
            }
        }

        if(requireReorder)
        {
            MorphHandler.reorderMorphs(Minecraft.getMinecraft().thePlayer.getCommandSenderName(), Morph.proxy.tickHandlerClient.playerMorphCatMap);
        }
    }
}
