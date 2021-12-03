package me.ichun.mods.morph.common;

import me.ichun.mods.ichunutil.common.data.AdvancementGen;
import me.ichun.mods.ichunutil.common.network.PacketChannel;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
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
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.mob.TraitHandler;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.packet.*;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.*;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(Morph.MOD_ID)
public class Morph
{
    public static final String MOD_NAME = "Morph";
    public static final String MOD_ID = "morph";
    public static final String PROTOCOL = "2"; //Network protocol

    public static final Logger LOGGER = LogManager.getLogger();

    public static ConfigServer configServer;
    public static ConfigClient configClient;

    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static PacketChannel channel;

    public Morph()
    {
        if(!ResourceHandler.setupEnv())
        {
            LOGGER.fatal("Error initialising Morph Resource Handler! Terminating init.");
            return;
        }

        configServer = new ConfigServer().init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Sounds.REGISTRY.register(bus);

        bus.addListener(this::onCommonSetup);
        bus.addListener(this::processIMC);
        bus.addListener(this::finishLoading);

        MinecraftForge.EVENT_BUS.register(eventHandlerServer = new EventHandlerServer());

        MinecraftForge.EVENT_BUS.addListener(Advancements::onGatherData); //Data generation

        MorphApi.setApiImpl(MorphHandler.INSTANCE);

        channel = new PacketChannel(new ResourceLocation(MOD_ID, "channel"), PROTOCOL,
                PacketPlayerData.class,
                PacketRequestMorphInfo.class,
                PacketMorphInfo.class,
                PacketUpdateMorph.class,
                PacketSessionSync.class,
                PacketMorphInput.class,
                PacketAcquisition.class,
                PacketUpdateBiomassValue.class,
                PacketUpdateBiomassUpgrades.class,
                PacketInvalidateClientHealth.class,
                PacketOpenGenerator.class
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

    private void processIMC(InterModProcessEvent event)
    {
        //Register mod trait.
        event.getIMCStream(m -> m.equalsIgnoreCase("trait")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(o instanceof Class)
            {
                Class clz = (Class)o;
                if(Trait.class.isAssignableFrom(clz))
                {
                    try
                    {
                        Trait t = (Trait)clz.newInstance();
                        if(t.type != null && !t.type.isEmpty())
                        {
                            TraitHandler.registerTrait(t.type, clz);
                            LOGGER.info("IMC: Registering trait type {} from mod {}", t.type, msg.getSenderModId());
                        }
                        else
                        {
                            LOGGER.warn("IMC: Invalid trait type from {}", msg.getSenderModId());
                        }
                    }
                    catch(InstantiationException | IllegalAccessException e)
                    {
                        LOGGER.error("IMC: Error retrieving trait type from {}", msg.getSenderModId());
                        e.printStackTrace();
                    }
                }
                else
                {
                    LOGGER.warn("IMC: Non-Trait class type trait from {}", msg.getSenderModId());
                }
            }
            else
            {
                LOGGER.warn("IMC: Non-class type trait from {}", msg.getSenderModId());
            }
        });

        //Register mod mob data
        event.getIMCStream(m -> m.equalsIgnoreCase("mob")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(o instanceof MobData)
            {
                MobData data = (MobData)o;
                if(data.forEntity != null && !data.forEntity.isEmpty())
                {
                    ResourceLocation rl = new ResourceLocation(data.forEntity);

                    MobDataHandler.registerMobData(rl, data);

                    LOGGER.info("IMC: Registering MobData for {} from mod {}", rl.toString(), msg.getSenderModId());
                }
                else
                {
                    LOGGER.warn("IMC: Invalid MobData forEntity from {}", msg.getSenderModId());
                }
            }
            else
            {
                LOGGER.warn("IMC: Non-MobData object from {}", msg.getSenderModId());
            }
        });

        //Register mod morph synchers
        event.getIMCStream(m -> m.equalsIgnoreCase("morphSync")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(o instanceof BiConsumer)
            {
                BiConsumer consumer = (BiConsumer)o;

                MorphHandler.INSTANCE.getModPlayerMorphSyncConsumers().add(consumer);

                LOGGER.info("IMC: Registering morph sync BiConsumer from mod {}", msg.getSenderModId());
            }
            else
            {
                LOGGER.warn("IMC: Non-BiConsumer morph sync object from {}", msg.getSenderModId());
            }
        });

        //Register third party mob NBT tag setters
        event.getIMCStream(m -> m.equalsIgnoreCase("variantNbtSetter")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(o instanceof BiConsumer)
            {
                BiConsumer consumer = (BiConsumer)o;

                MorphHandler.INSTANCE.getVariantNbtTagSetters().add(consumer);

                LOGGER.info("IMC: Registering variant NBT setter BiConsumer from mod {}", msg.getSenderModId());
            }
            else
            {
                LOGGER.warn("IMC: Non-BiConsumer NBT setter object from {}", msg.getSenderModId());
            }
        });

        //Register third party mob NBT tag readers
        event.getIMCStream(m -> m.equalsIgnoreCase("variantNbtReader")).forEach(msg -> {
            Object o = msg.getMessageSupplier().get();
            if(o instanceof BiConsumer)
            {
                BiConsumer consumer = (BiConsumer)o;

                MorphHandler.INSTANCE.getVariantNbtTagReaders().add(consumer);

                LOGGER.info("IMC: Registering variant NBT reader BiConsumer from mod {}", msg.getSenderModId());
            }
            else
            {
                LOGGER.warn("IMC: Non-BiConsumer NBT reader object from {}", msg.getSenderModId());
            }
        });
    }

    private void finishLoading(FMLLoadCompleteEvent event)
    {
        ResourceHandler.loadResources();
    }

    public static class Advancements implements Consumer<Consumer<Advancement>>
    {
        @SubscribeEvent
        public static void onGatherData(GatherDataEvent event)
        {
            DataGenerator gen = event.getGenerator();
            if(event.includeServer()) {
                gen.addProvider(new AdvancementGen(gen, new Advancements()));
            }
        }

        @Override
        public void accept(Consumer<Advancement> consumer)
        {
            //Advancement Data Gen
            //vanilla
            Advancement advancement = Advancement.Builder.builder().withDisplay(Blocks.RED_NETHER_BRICKS, new TranslationTextComponent("advancements.nether.root.title"), new TranslationTextComponent("advancements.nether.root.description"), new ResourceLocation("textures/gui/advancements/backgrounds/nether.png"), FrameType.TASK, false, false, false).withCriterion("entered_nether", ChangeDimensionTrigger.Instance.toWorld(World.THE_NETHER)).register(consumer, "nether/root");
            Advancement advancement2 = Advancement.Builder.builder().withParent(advancement).withDisplay(Blocks.NETHER_BRICKS, new TranslationTextComponent("advancements.nether.find_fortress.title"), new TranslationTextComponent("advancements.nether.find_fortress.description"), (ResourceLocation)null, FrameType.TASK, true, true, false).withCriterion("fortress", PositionTrigger.Instance.forLocation(LocationPredicate.forFeature(Structure.FORTRESS))).register(consumer, "nether/find_fortress");

            //morph
            Advancement.Builder.builder().withParent(advancement2).withDisplay(Items.WITHER_ROSE, new TranslationTextComponent("morph.advancement.unlock_biomass.title"), new TranslationTextComponent("morph.advancement.unlock_biomass.description"), null, FrameType.CHALLENGE, true, true, false).withCriterion("wither_and_regen", EffectsChangedTrigger.Instance.forEffect(MobEffectsPredicate.any().addEffect(Effects.WITHER).addEffect(Effects.REGENERATION))).register(consumer, UNLOCK_BIOMASS.toString());
        }

        public static final ResourceLocation UNLOCK_BIOMASS = new ResourceLocation("morph", "morph/unlock_biomass");
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
