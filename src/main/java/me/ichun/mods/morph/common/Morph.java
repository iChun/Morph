package me.ichun.mods.morph.common;

import me.ichun.mods.ichunutil.common.network.PacketChannel;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.client.config.ConfigClient;
import me.ichun.mods.morph.client.core.EventHandlerClient;
import me.ichun.mods.morph.client.core.KeyBinds;
import me.ichun.mods.morph.client.entity.EntityAcquisition;
import me.ichun.mods.morph.client.entity.EntityBiomassAbility;
import me.ichun.mods.morph.client.render.RenderEntityAcquisition;
import me.ichun.mods.morph.client.render.RenderEntityBiomassAbility;
import me.ichun.mods.morph.common.config.ConfigServer;
import me.ichun.mods.morph.common.core.EventHandlerServer;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.packet.*;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(Morph.MOD_ID)
public class Morph
{
    public static final String MOD_NAME = "Morph";
    public static final String MOD_ID = "morph";
    public static final String PROTOCOL = "1"; //Network protocol

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigServer configServer;
    public static ConfigClient configClient;

    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static PacketChannel channel;

    public Morph()
    {
        if(!ResourceHandler.init())
        {
            LOGGER.fatal("Error initialising Morph Resource Handler! Terminating init.");
            return;
        }
        configServer = new ConfigServer().init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Sounds.REGISTRY.register(bus);

        bus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(eventHandlerServer = new EventHandlerServer());

        MorphApi.setApiImpl(MorphHandler.INSTANCE);

        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL,
                PacketPlayerData.class,
                PacketRequestMorphInfo.class,
                PacketMorphInfo.class,
                PacketUpdateMorph.class,
                PacketSessionSync.class,
                PacketMorphInput.class,
                PacketAcquisition.class,
                PacketUpdateBiomass.class
        );

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            configClient = new ConfigClient().init();

            bus.addGenericListener(EntityType.class, Morph.EntityTypes::onEntityTypeRegistry);
            bus.addListener(this::onClientSetup);

            MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());

            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> me.ichun.mods.ichunutil.client.core.EventHandlerClient::getConfigGui);
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        //We don't need a proper IStorage / factory: https://github.com/MinecraftForge/MinecraftForge/issues/7622
        CapabilityManager.INSTANCE.register(MorphInfo.class, new Capability.IStorage<MorphInfo>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<MorphInfo> capability, MorphInfo instance, Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<MorphInfo> capability, MorphInfo instance, Direction side, INBT nbt)
            {

            }
        }, () -> null);
    }

    @OnlyIn(Dist.CLIENT)
    private void onClientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityTypes.ACQUISITION, new RenderEntityAcquisition.RenderFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityTypes.BIOMASS_ABILITY, new RenderEntityBiomassAbility.RenderFactory());

        KeyBinds.init();
    }

    public static class Sounds
    {
        private static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID); //.setRegistryName(new ResourceLocation("torched", "rpt") ??

        public static final RegistryObject<SoundEvent> MORPH = REGISTRY.register("morph", () -> new SoundEvent(new ResourceLocation("morph", "morph")));
    }

    public static class EntityTypes
    {
        public static EntityType<EntityAcquisition> ACQUISITION;
        public static EntityType<EntityBiomassAbility> BIOMASS_ABILITY;
        private static void onEntityTypeRegistry(final RegistryEvent.Register<EntityType<?>> entityTypeRegistryEvent) //we're doing it this way because it's a client-side entity and we don't want to sync registry values
        {
            ACQUISITION = EntityType.Builder.create(EntityAcquisition::new, EntityClassification.MISC)
                    .size(0.1F, 0.1F)
                    .disableSerialization()
                    .disableSummoning()
                    .immuneToFire()
                    .build("an entity from " + Morph.MOD_NAME + ". Ignore this.");
            BIOMASS_ABILITY = EntityType.Builder.create(EntityBiomassAbility::new, EntityClassification.MISC)
                    .size(0.1F, 0.1F)
                    .disableSerialization()
                    .disableSummoning()
                    .immuneToFire()
                    .build("an entity from " + Morph.MOD_NAME + ". Ignore this.");
        }
    }
}
