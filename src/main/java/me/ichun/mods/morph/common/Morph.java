package me.ichun.mods.morph.common;

import me.ichun.mods.morph.common.core.CommonProxy;
import me.ichun.mods.morph.common.core.Config;
import me.ichun.mods.morph.common.packet.PacketUpdateActiveMorphs;
import me.ichun.mods.morph.common.packet.PacketUpdateMorphList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import us.ichun.mods.ichunutil.common.core.Logger;
import us.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import us.ichun.mods.ichunutil.common.core.network.ChannelHandler;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.iChunUtil;

@Mod(modid = Morph.MOD_NAME, name = Morph.MOD_NAME,
        version = Morph.VERSION,
        guiFactory = "us.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".5.0," + (iChunUtil.versionMC + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.versionMC +".0.0," + iChunUtil.versionMC + ".1.0)"
)
public class Morph
{
    public static final String MOD_NAME = "Morph";
    public static final String VERSION = iChunUtil.versionMC + ".0.0";

    @Mod.Instance(MOD_NAME)
    public static Morph instance;

    @SidedProxy(clientSide = "me.ichun.mods.morph.client.core.ClientProxy", serverSide = "me.ichun.mods.morph.common.core.CommonProxy")
    public static CommonProxy proxy;

    public static final Logger logger = Logger.createLogger(MOD_NAME);

    public static PacketChannel channel;

    public static Config config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = (Config)ConfigHandler.registerConfig(new Config(event.getSuggestedConfigurationFile()));

        proxy.preInit();

        channel = ChannelHandler.getChannelHandlers(MOD_NAME, PacketUpdateMorphList.class, PacketUpdateActiveMorphs.class);

        ModVersionChecker.register_iChunMod(new ModVersionInfo(MOD_NAME, iChunUtil.versionOfMC, VERSION, false));
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
    public void serverStopping(FMLServerStoppingEvent event)
    {
        proxy.tickHandlerServer.morphsActive.clear();
        proxy.tickHandlerServer.playerMorphs.clear();
    }
}
