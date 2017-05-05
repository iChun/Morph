package me.ichun.mods.morph.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphVariant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

public class PacketGuiInput extends AbstractPacket
{
    public String identifier;
    public int id; //0 = select, 1 = favourite, 2 = delete...?
    public boolean flag; //used for favourite to set the favourite state

    public PacketGuiInput(){}

    public PacketGuiInput(String ident, int id, boolean flag)
    {
        this.identifier = ident;
        this.id = id;
        this.flag = flag;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, identifier);
        buffer.writeInt(id);
        buffer.writeBoolean(flag);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        identifier = ByteBufUtils.readUTF8String(buffer);
        id = buffer.readInt();
        flag = buffer.readBoolean();
    }

    @Override
    public AbstractPacket execute(Side side, EntityPlayer player)
    {
        ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.getPlayerMorphs(player.getName());
        boolean found = false;
        for(int i = morphs.size() - 1; i >= 0; i--)
        {
            MorphVariant variant = morphs.get(i);
            MorphVariant.Variant var = variant.getVariantByIdentifier(identifier);
            if(var != null)
            {
                found = true;
                switch(id)
                {
                    case 0: //select to morph into
                    {
                        PlayerMorphHandler.getInstance().morphPlayer(player, variant.createWithVariant(var));
                        break;
                    }
                    case 1: //favouriting a morph
                    {
                        var.isFavourite = flag;
                        break;
                    }
                    case 2: //deleting a morph
                    {
                        found = false;
                        if(variant.deleteVariant(var))
                        {
                            morphs.remove(i);
                        }
                        break;
                    }
                }
                PlayerMorphHandler.getInstance().savePlayerData(player);
                break;
            }
        }

        if(!found)
        {
            Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player);
        }
        return null;
    }

    @Override
    public Side receivingSide()
    {
        return Side.SERVER;
    }
}
