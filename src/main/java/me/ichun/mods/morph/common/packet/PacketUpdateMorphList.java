package me.ichun.mods.morph.common.packet;

import com.google.common.collect.Ordering;
import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphState;
import me.ichun.mods.morph.common.morph.MorphVariant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class PacketUpdateMorphList extends AbstractPacket
{
    public boolean fullList;
    public MorphVariant[] morphVariants;

    public PacketUpdateMorphList(){}

    public PacketUpdateMorphList(boolean isFullList, MorphVariant...variants)
    {
        fullList = isFullList;
        morphVariants = variants;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        PacketBuffer pb = new PacketBuffer(buffer);

        buffer.writeBoolean(fullList);
        buffer.writeInt(morphVariants.length);

        for(MorphVariant var : morphVariants)
        {
            pb.writeCompoundTag(var.write(new NBTTagCompound()));
        }
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        PacketBuffer pb = new PacketBuffer(buffer);

        fullList = buffer.readBoolean();
        morphVariants = new MorphVariant[buffer.readInt()];

        try
        {
            for(int i = 0; i < morphVariants.length; i++)
            {
                morphVariants[i] = new MorphVariant("");
                morphVariants[i].read(pb.readCompoundTag());
            }
        }
        catch(IOException ignored){}
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        handleClient(player);
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(EntityPlayer player)
    {
        if(fullList)
        {
            Morph.eventHandlerClient.playerMorphs.clear();
        }

        //Split the variants into individual "variants" to be stored in states.
        ArrayList<MorphVariant> morphs = new ArrayList<>();
        for(MorphVariant var : morphVariants)
        {
            morphs.addAll(var.split());
        }

        ArrayList<MorphState> states = new ArrayList<>();
        for(MorphVariant var : morphs)
        {
            MorphState state = new MorphState(var);
            state.getEntInstance(player.getEntityWorld());
            states.add(state);
        }
        //Now that all the variants are stored in states and an entity created, lets sort them

        //TODO unforntunately we are currently sorting the list alphabetically except the player state is at the top.
        Collections.sort(states);
        for(int i = 0; i < states.size(); i++)
        {
            MorphState state = states.get(i);
            if(state.currentVariant.entId.equals(MorphVariant.PLAYER_MORPH_ID) && state.currentVariant.playerName.equals(player.getName()))
            {
                states.remove(i);
                states.add(0, state); //Self state should always be on top.
            }
        }
        boolean needsReorder = false;
        for(MorphState state : states)
        {
            ArrayList<MorphState> category = Morph.eventHandlerClient.playerMorphs.get(state.getName());
            if(category == null)
            {
                if(!fullList)
                {
                    needsReorder = true;
                }
                category = new ArrayList<>();
                Morph.eventHandlerClient.playerMorphs.put(state.getName(), category);
            }
            if(!category.contains(state))
            {
                category.add(state);
            }
        }
        if(needsReorder)
        {
            TreeMap<String, ArrayList<MorphState>> buffer = new TreeMap<>(Ordering.natural());
            buffer.putAll(Morph.eventHandlerClient.playerMorphs);
            ArrayList<MorphState> selfState = buffer.get(player.getName()); //This has to exist. If it doesn't exist something messed up.
            buffer.remove(player.getName());
            Morph.eventHandlerClient.playerMorphs.clear();
            Morph.eventHandlerClient.playerMorphs.put(player.getName(), selfState);
            for(Map.Entry<String, ArrayList<MorphState>> e : buffer.entrySet())
            {
                Morph.eventHandlerClient.playerMorphs.put(e.getKey(), e.getValue());
            }
        }
    }
}
