package morph.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import ichun.core.network.AbstractPacket;
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
    public ArrayList<MorphState> states;

    public PacketMorphStates(){}

    public PacketMorphStates(boolean clear, ArrayList<MorphState> states)
    {
        this.clear = clear;
        this.states = states;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeBoolean(clear);

        for(int i = 0; i < states.size(); i++)
        {
            ByteBufUtils.writeUTF8String(buffer, "state");
            ByteBufUtils.writeTag(buffer, states.get(i).getTag());
        }
        ByteBufUtils.writeUTF8String(buffer, "##end");
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            Minecraft mc = Minecraft.getMinecraft();
            clear = buffer.readBoolean();

            if(clear)
            {
                Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
            }

            boolean requireReorder = false;
            while(ByteBufUtils.readUTF8String(buffer).equalsIgnoreCase("state"))
            {
                MorphState state = new MorphState(mc.theWorld, mc.thePlayer.getCommandSenderName(), "", null, true);

                NBTTagCompound tag = ByteBufUtils.readTag(buffer);

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
}
