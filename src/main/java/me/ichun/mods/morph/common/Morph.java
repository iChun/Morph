package me.ichun.mods.morph.common;

import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import me.ichun.mods.morph.client.core.EventHandlerClient;
import me.ichun.mods.morph.common.command.CommandMorph;
import me.ichun.mods.morph.common.core.Config;
import me.ichun.mods.morph.common.core.EventHandlerServer;
import me.ichun.mods.morph.common.core.ProxyCommon;
import me.ichun.mods.morph.common.handler.NBTHandler;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = Morph.MOD_ID, name = Morph.MOD_NAME,
        version = Morph.VERSION,
        guiFactory = iChunUtil.GUI_CONFIG_FACTORY,
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR + ".1.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR + ".1.0," + iChunUtil.VERSION_MAJOR + ".2.0)",
        acceptedMinecraftVersions = iChunUtil.MC_VERSION_RANGE
)
public class Morph
{
    public static final String MOD_NAME = "Morph";
    public static final String MOD_ID = "morph";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".1.2";

    @Mod.Instance(MOD_ID)
    public static Morph instance;

    @SidedProxy(clientSide = "me.ichun.mods.morph.client.core.ProxyClient", serverSide = "me.ichun.mods.morph.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    public static PacketChannel channel;

    public static Config config;

    public static SoundEvent soundMorph;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        proxy.preInit();

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerAboutToStartEvent event)
    {
        ICommandManager manager = event.getServer().getCommandManager();
        if(manager instanceof CommandHandler)
        {
            CommandHandler handler = (CommandHandler)manager;
            handler.registerCommand(new CommandMorph());
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        eventHandlerServer.morphsActive.clear();
        eventHandlerServer.playerMorphs.clear();
    }

    @Mod.EventHandler
    public void onIMCMessage(FMLInterModComms.IMCEvent event)
    {
        for(FMLInterModComms.IMCMessage message : event.getMessages())
        {
            if(message.key.equalsIgnoreCase("blacklist") && message.isStringMessage())
            {
                try
                {
                    Class clz = Class.forName(message.getStringValue());
                    if(EntityLivingBase.class.isAssignableFrom(clz) && !PlayerMorphHandler.blacklistedEntityClasses.contains(clz))
                    {
                        PlayerMorphHandler.blacklistedEntityClasses.add(clz);
                        LOGGER.info("Registered " + message.getStringValue() + " to Morph Entity blacklist");
                    }
                    else
                    {
                        LOGGER.info("Error adding " + message.getStringValue() + " to Morph Entity blacklist. Entity may already be in blacklist or may not be an EntityLivingBase!");
                    }
                }
                catch(ClassNotFoundException e)
                {
                    LOGGER.info("Error adding " + message.getStringValue() + " to Morph Entity blacklist. Class not found!");
                    e.printStackTrace();
                }
            }
            else if(message.key.equalsIgnoreCase("nbt_modifier") && message.isStringMessage()) //format <class>><key>><value>. EG: net.mc.ent.EntExample>NBTKey>Value
            {
                String[] split = message.getStringValue().split(">");
                if(split.length != 3)
                {
                    LOGGER.info("Error adding NBT modifier for class " + message.getStringValue() + ". Invalid argument count!");
                }
                else
                {
                    try
                    {
                        Class clz = Class.forName(split[0]);
                        if(EntityLivingBase.class.isAssignableFrom(clz))
                        {
                            NBTHandler.TagModifier tagModifier = NBTHandler.modModifiers.computeIfAbsent(clz, k -> new NBTHandler.TagModifier());
                            NBTHandler.handleModifier(tagModifier, split[1], split[2]);
                            NBTHandler.modModifiers.put(clz, tagModifier);
                            LOGGER.info("Registered " + message.getStringValue() + " to mod NBT modifiers");
                        }
                        else
                        {
                            LOGGER.info("Error adding " + message.getStringValue() + " to Morph Entity blacklist. Entity is not an EntityLivingBase!");
                        }
                    }
                    catch(ClassNotFoundException e)
                    {
                        LOGGER.info("Error adding NBT modifier for class " + message.getStringValue() + ". Class not found!");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
