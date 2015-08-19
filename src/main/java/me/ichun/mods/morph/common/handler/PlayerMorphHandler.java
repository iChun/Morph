package me.ichun.mods.morph.common.handler;

import me.ichun.mods.morph.api.IApi;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.event.MorphAcquiredEvent;
import me.ichun.mods.morph.client.render.RenderMorph;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphVariant;
import me.ichun.mods.morph.common.packet.PacketUpdateActiveMorphs;
import me.ichun.mods.morph.common.packet.PacketUpdateMorphList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerMorphHandler implements IApi
{
    private static final PlayerMorphHandler INSTANCE = new PlayerMorphHandler();

    public static final ArrayList<Class<? extends EntityLivingBase>> blacklistedEntityClasses = new ArrayList<Class<? extends EntityLivingBase>>();
    public static final ArrayList<Class<? extends EntityLivingBase>> blackwhiteEntityClasses = new ArrayList<Class<? extends EntityLivingBase>>();

    public static PlayerMorphHandler getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        MorphApi.setApiImpl(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE); //For capturing player logins/logouts
    }

    @Override
    public boolean canPlayerMorph(EntityPlayer player)
    {
        if(Morph.config.listIsBlacklistPlayers == 0) //If the list is a whitelist... Check the whitelist.
        {
            for(String s : Morph.config.blackwhiteListedPlayers)
            {
                if(s.equalsIgnoreCase(player.getCommandSenderName()))
                {
                    return true;
                }
            }
        }
        else //The list is a blacklist. If the player name is in here, return false.
        {
            for(String s : Morph.config.blackwhiteListedPlayers)
            {
                if(s.equalsIgnoreCase(player.getCommandSenderName()))
                {
                    return false;
                }
            }
        }
        return true; //TODO check the Morph classic config here in the future.
    }

    @Override
    public boolean hasMorph(String playerName, Side side)
    {
        return side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName) || Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName);
    }

    @Override
    public float morphProgress(String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            return Morph.proxy.tickHandlerClient.morphsActive.get(playerName).morphTime / (float)Morph.config.morphTime;
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            return Morph.proxy.tickHandlerServer.morphsActive.get(playerName).morphTime / (float)Morph.config.morphTime;
        }
        return 1.0F;
    }

    @Override
    public float timeToCompleteMorph()
    {
        return Morph.config.morphTime;
    }

    @Override
    public EntityLivingBase getPrevMorphEntity(World worldInstance, String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.morphsActive.get(playerName);
            if(info != null && info.prevState != null)
            {
                return info.prevState.getEntInstance(worldInstance);
            }
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(playerName);
            if(info != null && info.prevState != null)
            {
                return info.prevState.getEntInstance(worldInstance);
            }
        }
        return null;
    }

    @Override
    public EntityLivingBase getMorphEntity(World worldInstance, String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.morphsActive.get(playerName);
            if(info != null)
            {
                return info.nextState.getEntInstance(worldInstance);
            }
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(playerName);
            if(info != null)
            {
                return info.nextState.getEntInstance(worldInstance);
            }
        }
        return null;
    }

    public static boolean isEntityMorphableConfig(EntityLivingBase entity)
    {
        for(Class clz : blacklistedEntityClasses)
        {
            if(clz.isInstance(entity.getClass()))
            {
                return false;
            }
        }
        if(Morph.config.listIsBlacklistMobs == 0) //If the list is a whitelist... Check the whitelist.
        {
            for(Class clz : blackwhiteEntityClasses)
            {
                if(clz.isInstance(entity.getClass()))
                {
                    return true;
                }
            }
        }
        else //The list is a blacklist. If the mob class is in here, return false.
        {
            for(Class clz : blackwhiteEntityClasses)
            {
                if(clz.isInstance(entity.getClass()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean forceDemorph(EntityPlayerMP player)
    {
        //TODO this
        return false;
    }

    @Override
    public boolean forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph)
    {
        if(!isEntityMorphableConfig(entityToMorph))
        {
            return false;
        }
        MorphVariant variant = MorphVariant.createVariant(entityToMorph);
        if(variant == null) //Variant could not be created.
        {
            return false;
        }
        return morphPlayer(player, variant);
    }

    @Override
    public boolean acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire, boolean forceMorph, boolean killEntityClientside)
    {
        if(Morph.config.childMorphs == 0 && entityToAcquire.isChild() || Morph.config.playerMorphs == 0 && entityToAcquire instanceof EntityPlayer || Morph.config.bossMorphs == 0 && entityToAcquire instanceof IBossDisplayData || player.getClass() == FakePlayer.class || player.playerNetServerHandler == null)
        {
            return false;
        }
        if(!isEntityMorphableConfig(entityToAcquire))
        {
            return false;
        }
        if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(player.getCommandSenderName()) && Morph.proxy.tickHandlerServer.morphsActive.get(player.getCommandSenderName()).isMorphing())
        {
            return false;
        }

        if(MinecraftForge.EVENT_BUS.post(new MorphAcquiredEvent(player, entityToAcquire)))
        {
            //Event was cancelled.
            return false;
        }

        MorphVariant variant = MorphVariant.createVariant(entityToAcquire);
        if(variant == null) //Variant could not be created.
        {
            return false;
        }

        ArrayList<MorphVariant> morphs = Morph.proxy.tickHandlerServer.playerMorphs.get(player.getCommandSenderName());
        boolean updatePlayer = false;
        for(MorphVariant var : morphs)
        {
            if(variant.entId.equals(MorphVariant.PLAYER_MORPH_ID)) //Special case players first
            {
                if(var.entId.equals(MorphVariant.PLAYER_MORPH_ID) && variant.playerName.equals(var.playerName))
                {
                    return false;
                }
            }
            else if(variant.entId.equals(var.entId)) //non-player variants
            {
                updatePlayer = MorphVariant.combineVariants(var, variant);
                if(!updatePlayer) //failed to merge for reasons. Return false acquisition.
                {
                    return false;
                }
                else
                {
                    //The variant should be a new variant so it'll be the latest entry in the variants list.
                    variant = var.createWithVariant(var.variants.get(var.variants.size() - 1));
                    Morph.channel.sendToPlayer(new PacketUpdateMorphList(false, variant), player);
                }
                break;
            }
        }

        if(!updatePlayer) //No preexisting variant exists.
        {
            morphs.add(variant);

            Morph.channel.sendToPlayer(new PacketUpdateMorphList(false, variant), player);
        }
        Collections.sort(morphs);

        //by this point the morph is acquired and sorted. Return true.

        if(forceMorph)
        {

        }

        savePlayerData(player);

        if(killEntityClientside)
        {
            //TODO spawn the client acquired entity
        }
        return true;
    }

    public boolean morphPlayer(EntityPlayer player, MorphVariant variant)
    {
        return false;
    }

    @Override
    public ResourceLocation getMorphSkinTexture()
    {
        return RenderMorph.morphSkin;
    }

    @Override
    public boolean isMorphApi()
    {
        return true;
    }


    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(loadPlayerData(event.player)) //if true, player has a morph
        {
            Morph.channel.sendToAllExcept(new PacketUpdateActiveMorphs(event.player.getCommandSenderName()), event.player);
        }
        ArrayList<MorphVariant> morphs = Morph.proxy.tickHandlerServer.getPlayerMorphs(event.player.getCommandSenderName());
        Morph.channel.sendToPlayer(new PacketUpdateActiveMorphs(null), event.player); //Send the player a list of everyone's morphs
        Morph.channel.sendToPlayer(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), event.player); //Send the player's morph list to them
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        savePlayerData(event.player);
        Morph.proxy.tickHandlerServer.morphsActive.remove(event.player.getCommandSenderName());
        Morph.proxy.tickHandlerServer.playerMorphs.remove(event.player.getCommandSenderName());
    }

    public static final String MORPH_DATA_NAME = "MorphSave";

    public void savePlayerData(EntityPlayer player)
    {
        if(player != null)//TODO remove this... this is for debugging purposes.
        {
            return;
        }
        NBTTagCompound tag = EntityHelperBase.getPlayerPersistentData(player, MORPH_DATA_NAME);

        //Save the current morphed state/variant
        MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(player.getCommandSenderName());
        if(info != null && !info.nextState.currentVariant.playerName.equals(player.getCommandSenderName())) //check that the info isn't null and the player isn't demorphing already anyways
        {
            tag.setTag("currentMorph", info.write(new NBTTagCompound()));
        }
        else
        {
            tag.removeTag("currentMorph");
        }

        //Save the morph variants
        ArrayList<MorphVariant> variants = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.getCommandSenderName());
        tag.setInteger("variantCount", variants.size());
        for(int i = 0; i < variants.size(); i++)
        {
            MorphVariant variant = variants.get(i);
            tag.setTag("variant_" + i, variant.write(new NBTTagCompound()));
        }

        //TODO maybe a command to purge the current morphs?
    }

    public boolean loadPlayerData(EntityPlayer player) //Returns true if the player has a morph and requires synching to the clients.
    {
        NBTTagCompound tag = EntityHelperBase.getPlayerPersistentData(player, MORPH_DATA_NAME);

        //Check if the player has a current morph.
        boolean update = false;
        if(tag.hasKey("currentMorph"))
        {
            MorphInfo info = new MorphInfo(player, null, null);
            info.read(tag.getCompoundTag("currentMorph"));
            if(info.morphTime < Morph.config.morphTime)
            {
                info.morphTime = Morph.config.morphTime;
            }
            Morph.proxy.tickHandlerServer.morphsActive.put(player.getCommandSenderName(), info);
            update = true;
        }

        //Load up the player's morph list.
        int morphCount = tag.getInteger("variantCount");
        ArrayList<MorphVariant> variants = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.getCommandSenderName());
        for(int i = 0; i < morphCount; i++)
        {
            MorphVariant variant = new MorphVariant("");
            variant.read(tag.getCompoundTag("variant_" + i));

            if(!variants.contains(variant))
            {
                variants.add(variant);
            }
        }

        return update;
    }
}
