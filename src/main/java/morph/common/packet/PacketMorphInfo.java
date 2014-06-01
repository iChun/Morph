package morph.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import morph.api.Ability;
import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.ability.AbilityHandler;
import morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class PacketMorphInfo extends AbstractPacket
{

    public String playerName;
    public boolean morphing;
    public int morphProgress;
    public boolean hasPrevState;
    public boolean hasNextState;
    public NBTTagCompound prevTag;
    public NBTTagCompound nextTag;
    public boolean flying;

    public PacketMorphInfo(){}

    public PacketMorphInfo(String playerName, boolean morphing, int morphProgress, boolean hasPrevState, boolean hasNextState, NBTTagCompound prevTag, NBTTagCompound nextTag, boolean flying)
    {
        this.playerName = playerName;
        this.morphing = morphing;
        this.morphProgress = morphProgress;
        this.hasPrevState = hasPrevState;
        this.hasNextState = hasNextState;
        this.prevTag = prevTag;
        this.nextTag = nextTag;
        this.flying = flying;
    }

    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        ByteBufUtils.writeUTF8String(buffer, playerName);
        buffer.writeBoolean(hasPrevState);
        if(hasPrevState)
        {
            ByteBufUtils.writeTag(buffer, prevTag);
        }
        buffer.writeBoolean(hasNextState);
        if(hasNextState)
        {
            ByteBufUtils.writeTag(buffer, nextTag);
        }

        buffer.writeBoolean(morphing);
        buffer.writeInt(morphProgress);

        buffer.writeBoolean(flying);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side)
    {
        prevTag = new NBTTagCompound();
        nextTag = new NBTTagCompound();

        playerName = ByteBufUtils.readUTF8String(buffer);
        hasPrevState = buffer.readBoolean();
        if(hasPrevState)
        {
            prevTag = ByteBufUtils.readTag(buffer);
        }
        hasNextState = buffer.readBoolean();
        if(hasNextState)
        {
            nextTag = ByteBufUtils.readTag(buffer);
        }

        morphing = buffer.readBoolean();
        morphProgress = buffer.readInt();

        flying = buffer.readBoolean();
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

        if(hasNextState)
        {
            EntityPlayer player1 = mc.theWorld.getPlayerEntityByName(playerName);
            if(player1 != null)
            {
                if (!player1.getActivePotionEffects().isEmpty())
                {
                    NBTTagList nbttaglist = new NBTTagList();
                    Iterator iterator = player1.getActivePotionEffects().iterator();

                    while (iterator.hasNext())
                    {
                        PotionEffect potioneffect = (PotionEffect)iterator.next();
                        nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
                    }
                    nextTag.setTag("ActiveEffects", nbttaglist);
                }
            }
        }

        MorphState prevState = new MorphState(mc.theWorld, playerName, "", null, true);
        MorphState nextState = new MorphState(mc.theWorld, playerName, "", null, true);

        prevState.readTag(mc.theWorld, prevTag);
        nextState.readTag(mc.theWorld, nextTag);

        //TODO check for mc.theplayer morphstate
        //					prevState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerClient.getPlayerMorphs(event.entityPlayer.worldObj, event.entityPlayer.username), prevState);
        //					nextState = MorphHandler.addOrGetMorphState(Morph.proxy.tickHandlerClient.getPlayerMorphs(event.entityPlayer.worldObj, event.entityPlayer.username), nextState);

        if(prevState.entInstance != null)
        {
            if(prevState.entInstance != mc.thePlayer)
            {
                prevState.entInstance.noClip = true;
            }
        }

        if(nextState.entInstance != null)
        {
            if(nextState.entInstance != mc.thePlayer)
            {
                nextState.entInstance.noClip = true;
            }
        }

        //					System.out.println(prevEnt);
        //					System.out.println(nextEnt);

        MorphInfoClient info = new MorphInfoClient(playerName, prevState, nextState);
        info.setMorphing(morphing);
        info.morphProgress = morphProgress;

        MorphInfoClient info1 = Morph.proxy.tickHandlerClient.playerMorphInfo.get(playerName);
        if(info1 != null)
        {
            info.morphAbilities = info1.morphAbilities;
        }
        else
        {
            ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
            info.morphAbilities = new ArrayList<Ability>();
            for(Ability ability : newAbilities)
            {
                try
                {
                    Ability clone = ability.clone();
                    info.morphAbilities.add(clone);
                }
                catch(Exception e1)
                {
                }
            }
        }

        Morph.proxy.tickHandlerClient.playerMorphInfo.put(playerName, info);

        info.flying = flying;

        if(Morph.config.getInt("sortMorphs") == 3 && info.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName()))
        {
            String name1 = info.nextState.entInstance.getCommandSenderName();

            if(name1 != null)
            {
                ArrayList<String> order = new ArrayList<String>();
                Iterator<String> ite = Morph.proxy.tickHandlerClient.playerMorphCatMap.keySet().iterator();
                while(ite.hasNext())
                {
                    order.add(ite.next());
                }

                order.remove(name1);
                order.remove(mc.thePlayer.getCommandSenderName());

                order.add(0, name1);
                order.add(0, mc.thePlayer.getCommandSenderName());

                LinkedHashMap<String, ArrayList<MorphState>> bufferList = new LinkedHashMap<String, ArrayList<MorphState>>(Morph.proxy.tickHandlerClient.playerMorphCatMap);

                Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();

                for(int i = 0; i < order.size(); i++)
                {
                    Morph.proxy.tickHandlerClient.playerMorphCatMap.put(order.get(i), bufferList.get(order.get(i)));
                }
            }
        }
    }

}
