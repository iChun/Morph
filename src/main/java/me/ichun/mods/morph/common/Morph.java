package me.ichun.mods.morph.common;

import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import me.ichun.mods.morph.client.core.EventHandlerClient;
import me.ichun.mods.morph.common.core.Config;
import me.ichun.mods.morph.common.core.EventHandlerServer;
import me.ichun.mods.morph.common.core.ProxyCommon;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = Morph.MOD_ID, name = Morph.MOD_NAME,
        version = Morph.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR + ".4.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR + ".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class Morph
{
    public static final String MOD_NAME = "Morph";
    public static final String MOD_ID = "morph";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

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
        }
    }
}
