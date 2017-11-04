package me.ichun.mods.morph.common.core;

import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import me.ichun.mods.morph.common.morph.MorphVariant;
import me.ichun.mods.morph.common.packet.PacketDemorph;
import me.ichun.mods.morph.common.packet.PacketUpdateActiveMorphs;
import me.ichun.mods.morph.common.packet.PacketUpdateMorphList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onRegisterSound(RegistryEvent.Register<SoundEvent> event)
    {
        ResourceLocation rs = new ResourceLocation("morph", "morph");
        Morph.soundMorph = new SoundEvent(rs).setRegistryName(rs);

        event.getRegistry().register(Morph.soundMorph);
    }

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event)
    {
        EntityPlayer.SleepResult stats = EntityPlayer.SleepResult.OTHER_PROBLEM;
        if(Morph.config.canSleepMorphed == 0)
        {
            if(!event.getEntityPlayer().getEntityWorld().isRemote && Morph.eventHandlerServer.morphsActive.containsKey(event.getEntityPlayer().getName()))
            {
                event.setResult(stats);
                event.getEntityPlayer().sendMessage(new TextComponentTranslation("morph.denySleep"));
            }
            else if(event.getEntityPlayer().getEntityWorld().isRemote && Morph.eventHandlerClient.morphsActive.containsKey(event.getEntityPlayer().getName()))
            {
                event.setResult(stats);
            }
        }
    }

    @SubscribeEvent
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event)
    {
        if(event.getEntity() instanceof EntityPlayer && (event.getSound().equals(SoundEvents.ENTITY_PLAYER_HURT) || event.getSound().equals(SoundEvents.ENTITY_PLAYER_DEATH)))
        {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            if(!player.getEntityWorld().isRemote && Morph.eventHandlerServer.morphsActive.get(player.getName()) != null)
            {
                MorphInfo info = Morph.eventHandlerServer.morphsActive.get(player.getName());
                EntityLivingBase entInstance = info.getMorphProgress(0F) < 0.5F ? info.prevState.getEntInstance(player.getEntityWorld()) : info.nextState.getEntInstance(player.getEntityWorld());
                event.setSound(event.getSound().equals(SoundEvents.ENTITY_PLAYER_HURT) ? EntityHelper.getHurtSound(entInstance, entInstance.getClass()) : EntityHelper.getDeathSound(entInstance, entInstance.getClass()));
            }
            else if(player.getEntityWorld().isRemote && Morph.eventHandlerClient.morphsActive.get(player.getName()) != null)
            {
                MorphInfo info = Morph.eventHandlerClient.morphsActive.get(player.getName());
                EntityLivingBase entInstance = info.getMorphProgress(0F) < 0.5F ? info.prevState.getEntInstance(player.getEntityWorld()) : info.nextState.getEntInstance(player.getEntityWorld());
                event.setSound(event.getSound().equals(SoundEvents.ENTITY_PLAYER_HURT) ? EntityHelper.getHurtSound(entInstance, entInstance.getClass()) : EntityHelper.getDeathSound(entInstance, entInstance.getClass()));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.getEntityLiving().getEntityWorld().isRemote)
        {
            if(Morph.config.loseMorphsOnDeath >= 1 && event.getEntityLiving() instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();

                MorphInfo info = Morph.eventHandlerServer.morphsActive.get(player.getName());

                if(info != null)
                {
                    //demorph the player.
                    MorphInfo newInfo = new MorphInfo(player, info.nextState, new MorphState(MorphVariant.createVariant(player)));
                    newInfo.morphTime = 0;
                    Morph.eventHandlerServer.morphsActive.put(player.getName(), newInfo);
                    Morph.channel.sendToAll(new PacketUpdateActiveMorphs(player.getName()));
                    EntityHelper.playSoundAtEntity(player, Morph.soundMorph, player.getSoundCategory(), 1.0F, 1.0F);
                }

                ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.getPlayerMorphs(player.getName());

                if(Morph.config.loseMorphsOnDeath == 1)
                {
                    //remove all the morphs
                    morphs.clear();
                    morphs = Morph.eventHandlerServer.getPlayerMorphs(player.getName());
                }
                else
                {
                    //remove the morph the player is using.
                    morphs.remove(info.nextState.currentVariant);
                }

                //Update the player with the new morphs list.
                Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player); //Send the player's morph list to them

                //save the player data
                PlayerMorphHandler.getInstance().savePlayerData(player);
            }
            if(event.getSource().getTrueSource() instanceof EntityPlayerMP && event.getEntityLiving() != event.getSource().getTrueSource())
            {
                EntityPlayerMP player = (EntityPlayerMP)event.getSource().getTrueSource();

                if(PlayerMorphHandler.getInstance().canPlayerMorph(player))
                {
                    EntityLivingBase living = event.getEntityLiving(); //entity to acquire

                    if(event.getEntityLiving() instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player1 = (EntityPlayerMP)event.getEntityLiving();

                        MorphInfo info = Morph.eventHandlerServer.morphsActive.get(player1.getName());
                        if(info != null)
                        {
                            if(info.isMorphing() && info.prevState != null)
                            {
                                living = info.prevState.getEntInstance(player1.getEntityWorld());
                            }
                            else
                            {
                                living = info.nextState.getEntInstance(player1.getEntityWorld());
                            }
                        }
                    }

                    PlayerMorphHandler.getInstance().acquireMorph(player, living, Morph.config.instaMorph == 1, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Iterator<Map.Entry<String, MorphInfo>> ite = Morph.eventHandlerServer.morphsActive.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<String, MorphInfo> e = ite.next();
                MorphInfo info = e.getValue();

                info.tick();

                if(!info.isMorphing() && info.nextState.currentVariant.playerName.equals(info.getPlayer().getName())) //Player has fully demorphed
                {
                    ite.remove();
                    Morph.channel.sendToAll(new PacketDemorph(info.getPlayer().getName()));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.START)
        {
            if(event.player.getEntityWorld().playerEntities.contains(event.player))
            {
                MorphInfo info = morphsActive.get(event.player.getName());
                if(info != null && info.getPlayer() != event.player)
                {
                    info.setPlayer(event.player);
                }
            }
        }
    }

    public ArrayList<MorphVariant> getPlayerMorphs(String name)
    {
        ArrayList<MorphVariant> morphs = playerMorphs.get(name);
        if(morphs == null)
        {
            morphs = new ArrayList<>();
            MorphVariant variant = new MorphVariant(MorphVariant.PLAYER_MORPH_ID).setPlayerName(name);
            variant.thisVariant.isFavourite = true;
            morphs.add(variant); //Add the player self's morph variant when getting this list.
            playerMorphs.put(name, morphs);
        }
        return morphs;
    }

    public HashMap<String, MorphInfo> morphsActive = new HashMap<>(); //These are the active morphs. Entity instance are retreived from here
    public HashMap<String, ArrayList<MorphVariant>> playerMorphs = new HashMap<>();//These are the available morphs for each player. No entity instance is required or created here.
}
