package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.command.CommandMorph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.MorphSavedData;
import me.ichun.mods.morph.common.packet.PacketPlayerData;
import me.ichun.mods.morph.common.packet.PacketSessionSync;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event)
    {
        Entity entity = event.getObject();
        if(entity instanceof PlayerEntity && !(entity instanceof FakePlayer))
        {
            event.addCapability(MorphInfo.CAPABILITY_IDENTIFIER, new MorphInfo.CapProvider(new MorphInfo((PlayerEntity)entity)));
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.getEntityLiving().getEntityWorld().isRemote && event.getSource().getTrueSource() instanceof ServerPlayerEntity && !(event.getSource().getTrueSource() instanceof FakePlayer) && !event.getSource().getTrueSource().removed && event.getEntity().getEntityId() > 0)
        {
            MorphHandler.INSTANCE.handleMurderEvent((ServerPlayerEntity)event.getSource().getTrueSource(), event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onEntitySize(EntityEvent.Size event)
    {
        if(event.getEntity() instanceof PlayerEntity && !event.getEntity().removed && event.getEntity().getEntityId() > 0 && event.getEntity().ticksExisted > 0)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo((PlayerEntity)event.getEntity());
            if(info.isMorphed())
            {
                event.setNewSize(info.getMorphSize(1F));
                event.setNewEyeHeight(info.getMorphEyeHeight(1F));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && !event.player.removed && event.player.getEntityId() > 0)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(event.player);
            info.tick();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(!(event.getPlayer().getServer().isSinglePlayer() && event.getPlayer().getGameProfile().getName().equals(event.getPlayer().getServer().getServerOwner()))) //if the player is not the client in singleplayer
        {
            Morph.channel.sendTo(new PacketSessionSync(MorphHandler.BIOMASS_UPGRADES.values()), (ServerPlayerEntity)event.getPlayer());
        }
        Morph.channel.sendTo(new PacketPlayerData(MorphHandler.INSTANCE.getPlayerMorphData(event.getPlayer()).write(new CompoundNBT())), (ServerPlayerEntity)event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        MorphHandler.INSTANCE.getMorphInfo(event.getPlayer()).read(MorphHandler.INSTANCE.getMorphInfo(event.getOriginal()).write(new CompoundNBT()));
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(!event.getWorld().isRemote() && ((ServerWorld)event.getWorld()).getDimensionKey().equals(World.OVERWORLD))
        {
            MorphHandler.INSTANCE.setSaveData(((ServerWorld)event.getWorld()).getSavedData().getOrCreate(MorphSavedData::new, MorphSavedData.ID));
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        CommandMorph.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) //do this early so we do it before the server loads our world save.
    {
        ResourceHandler.loadBiomassUpgrades();
    }

    @SubscribeEvent
    public void onServerStopped(FMLServerStoppedEvent event)
    {
        MorphHandler.INSTANCE.setSaveData(null);
    }
}
