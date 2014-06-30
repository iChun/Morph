package morph.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;

public class PacketGuiInput extends AbstractPacket
{

    public int input; //0 = select; 1 = delete; 2 = favourite
    public String identifier;
    public boolean favourite;

    public PacketGuiInput(){}

    public PacketGuiInput(int input, String identifier, boolean favourite)
    {
        this.input = input;
        this.identifier = identifier;
        this.favourite = favourite;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        //Should always be clientside
        buffer.writeInt(input);
        ByteBufUtils.writeUTF8String(buffer, identifier);
        buffer.writeBoolean(favourite);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        //Should always be serverside
        input = buffer.readInt();
        identifier = ByteBufUtils.readUTF8String(buffer);
        favourite = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);

        if(!(info != null && info.getMorphing()) || input == 2)
        {
            MorphState state = MorphHandler.getMorphState((EntityPlayerMP)player, identifier);

            if(state != null)
            {
                switch(input)
                {
                    case 0:
                    {
                        //select
                        MorphState old = info != null ? info.nextState : Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player);

                        MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), old, state);
                        info2.setMorphing(true);

                        MorphInfo info3 = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);
                        if(info3 != null)
                        {
                            info2.morphAbilities = info3.morphAbilities;
                        }

                        Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player, info2);

                        PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

                        player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
                        break;
                    }
                    case 1:
                    {
                        //delete
                        if(info != null && info.nextState.identifier.equalsIgnoreCase(state.identifier) || state.playerMorph.equalsIgnoreCase(player.getCommandSenderName()))
                        {
                            break;
                        }
                        ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player);
                        states.remove(state);

                        MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);
                        break;
                    }
                    case 2:
                    {
                        //favourite
                        state.isFavourite = favourite;
                        break;
                    }
                }
            }
        }
    }
}
