package morph.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import ichun.common.core.util.ObfHelper;
import io.netty.buffer.ByteBuf;
import morph.api.Ability;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class PacketCompleteDemorph extends AbstractPacket
{
    public String playerName;

    public PacketCompleteDemorph(){}

    public PacketCompleteDemorph(String name)
    {
        this.playerName = name;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, playerName);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        playerName = ByteBufUtils.readUTF8String(buffer);
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
        EntityPlayer player1 = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
        if(player1 != null)
        {
            player1.ignoreFrustumCheck = true;
            MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(playerName);
            if(info != null)
            {
                ObfHelper.forceSetSize(player1.getClass(), player1, info.nextState.entInstance.width, info.nextState.entInstance.height);
                player1.setPosition(player1.posX, player1.posY, player1.posZ);
                player1.eyeHeight = player1.getDefaultEyeHeight();
                player1.ignoreFrustumCheck = false;
            }
        }

        MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(playerName);
        if(info != null)
        {
            for(Ability ability : info.morphAbilities)
            {
                if(ability.getParent() != null)
                {
                    ability.kill();
                }
            }
        }

        Morph.proxy.tickHandlerClient.playerMorphInfo.remove(playerName);
    }

}
