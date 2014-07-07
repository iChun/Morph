package morph.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.relauncher.Side;
import ichun.client.keybind.KeyBind;
import ichun.common.core.config.Config;
import ichun.common.core.config.ConfigHandler;
import ichun.common.core.config.IConfigUser;
import ichun.common.core.updateChecker.ModVersionChecker;
import ichun.common.core.updateChecker.ModVersionInfo;
import ichun.common.iChunUtil;
import morph.common.core.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.EnumMap;

@Mod(modid = "Morph", name = "Morph",
			version = Morph.version,
			dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".3.0,)",
            acceptableRemoteVersions = "[0.8.0,0.9.0)"
				)
public class Morph
    implements IConfigUser
{
	public static final String version = "0.8.0";
	
	@Instance("Morph")
	public static Morph instance;
	
	@SidedProxy(clientSide = "morph.client.core.ClientProxy", serverSide = "morph.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	private static Logger logger = LogManager.getLogger("Morph");
	
    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    public static Config config;
	
	public static ArrayList<Class<? extends EntityLivingBase>> blacklistedClasses = new ArrayList<Class<? extends EntityLivingBase>>();
    public static ArrayList<String> playerList = new ArrayList<String>();
    public static Class<? extends EntityLivingBase> classToKillForFlight = null;

    @Override
    public boolean onConfigChange(Config cfg, Property prop)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            if(prop.getName().equalsIgnoreCase("blacklistedMobs"))
            {
                parseBlacklist(prop.getString());
            }
            if(prop.getName().equalsIgnoreCase("blackwhitelistedPlayers"))
            {
                parsePlayerList(prop.getString());
            }
            if(prop.getName().equalsIgnoreCase("abilities") || prop.getName().equalsIgnoreCase("canSleepMorphed") || prop.getName().equalsIgnoreCase("allowMorphSelection") || prop.getName().equalsIgnoreCase("showPlayerLabel") || prop.getName().equalsIgnoreCase("disabledAbilities"))
            {
                Morph.proxy.tickHandlerServer.updateSession(null);
            }
        }
        else
        {
            Morph.proxy.tickHandlerServer.purgeSession = true;
        }
        return true;
    }

    //TODO what's the difference between getTotalWorldTime and getWorldTime?
    @EventHandler
	public void preLoad(FMLPreInitializationEvent event)
    {
        config = ConfigHandler.createConfig(event.getSuggestedConfigurationFile(), "morph", "Morph", logger, instance);

        config.setCurrentCategory("gameplay", "morph.config.cat.gameplay.name", "morph.config.cat.gameplay.comment");
        config.createIntBoolProperty("childMorphs", "morph.config.prop.childMorphs.name", "morph.config.prop.childMorphs.comment", true, false, false);
        config.createIntBoolProperty("playerMorphs", "morph.config.prop.playerMorphs.name", "morph.config.prop.playerMorphs.comment", true, false, true);
        config.createIntBoolProperty("bossMorphs", "morph.config.prop.bossMorphs.name", "morph.config.prop.bossMorphs.comment", true, false, false);

        config.createStringProperty("blacklistedMobs", "morph.config.prop.blacklistedMobs.name", "morph.config.prop.blacklistedMobs.comment", true, false, "");
        config.createStringProperty("blackwhitelistedPlayers", "morph.config.prop.blackwhitelistedPlayers.name", "morph.config.prop.blackwhitelistedPlayers.comment", true, false, "");
        config.createIntBoolProperty("listIsBlacklist", "morph.config.prop.listIsBlacklist.name", "morph.config.prop.listIsBlacklist.comment", true, false, false);

        config.createIntProperty("loseMorphsOnDeath", "morph.config.prop.loseMorphsOnDeath.name", "morph.config.prop.loseMorphsOnDeath.comment", true, false, 0, 0, 2);
        config.createIntBoolProperty("instaMorph", "morph.config.prop.instaMorph.name", "morph.config.prop.instaMorph.comment", true, false, false);

        config.createIntBoolProperty("NBTStripper", "morph.config.prop.NBTStripper.name", "morph.config.prop.NBTStripper.comment", false, false, true);
        config.createIntBoolProperty("useLocalResources", "morph.config.prop.useLocalResources.name", "morph.config.prop.useLocalResources.comment", false, false, false);

        config.createIntBoolProperty("canSleepMorphed", "morph.config.prop.canSleepMorphed.name", "morph.config.prop.canSleepMorphed.comment", true, true, false);
        config.createIntBoolProperty("allowMorphSelection", "morph.config.prop.allowMorphSelection.name", "morph.config.prop.allowMorphSelection.comment", true, true, true);

        config.createIntBoolProperty("showPlayerLabel", "morph.config.prop.showPlayerLabel.name", "morph.config.prop.showPlayerLabel.comment", true, true, false);

        config.setCurrentCategory("abilities", "morph.config.cat.abilities.name", "morph.config.cat.abilities.comment");
        config.createIntBoolProperty("abilities", "morph.config.prop.abilities.name", "morph.config.prop.abilities.comment", false, true, true);
        config.createStringProperty("customPatchLink", "morph.config.prop.customPatchLink.name", "morph.config.prop.customPatchLink.comment", false, false, "");

        config.createIntProperty("hostileAbilityMode", "morph.config.prop.hostileAbilityMode.name", "morph.config.prop.hostileAbilityMode.comment", true, false, 0, 0, 4);
        config.createIntProperty("hostileAbilityDistanceCheck", "morph.config.prop.hostileAbilityDistanceCheck.name", "morph.config.prop.hostileAbilityDistanceCheck.comment", true, false, 6, 0, 128);

        config.createIntProperty("disableEarlyGameFlight", "morph.config.prop.disableEarlyGameFlight.name", "morph.config.prop.disableEarlyGameFlight.comment", true, false, 0, 0, 2);
        config.createIntBoolProperty("disableEarlyGameFlightMode", "morph.config.prop.disableEarlyGameFlightMode.name", "morph.config.prop.disableEarlyGameFlightMode.comment", true, false, false);
        config.createStringProperty("customMobToKillForFlight", "morph.config.prop.customMobToKillForFlight.name", "morph.config.prop.customMobToKillForFlight.comment", false, false, "");

        config.createStringProperty("disabledAbilities", "morph.config.prop.disabledAbilities.name", "morph.config.prop.disabledAbilities.comment", false, true, "");

        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            config.setCurrentCategory("clientOnly", "morph.config.cat.clientOnly.name", "morph.config.cat.clientOnly.comment");
            config.createKeybindProperty("keySelectorUp", "morph.config.prop.keySelectorUp.name", "morph.config.prop.keySelectorUp.comment", 26, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorDown", "morph.config.prop.keySelectorDown.name", "morph.config.prop.keySelectorDown.comment", 27, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorLeft", "morph.config.prop.keySelectorLeft.name", "morph.config.prop.keySelectorLeft.comment", 26, true, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorRight", "morph.config.prop.keySelectorRight.name", "morph.config.prop.keySelectorRight.comment", 27, true, false, false, false, 0, false);

            iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindAttack);
            iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindUseItem);
            iChunUtil.proxy.registerKeyBind(new KeyBind(Keyboard.KEY_DELETE, false, false, false, false), null);
            config.createKeybindProperty("keySelectorSelect", "morph.config.prop.keySelectorSelect.name", "morph.config.prop.keySelectorSelect.comment", 28, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorCancel", "morph.config.prop.keySelectorCancel.name", "morph.config.prop.keySelectorCancel.comment", 1, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorRemoveMorph", "morph.config.prop.keySelectorRemoveMorph.name", "morph.config.prop.keySelectorRemoveMorph.comment", 14, false, false, false, false, 0, false);
            config.createKeybindProperty("keyFavourite", "morph.config.prop.keyFavourite.name", "morph.config.prop.keyFavourite.comment", 41, false, false, false, false, 0, false);

            config.createIntBoolProperty("handRenderOverride", "morph.config.prop.handRenderOverride.name", "morph.config.prop.handRenderOverride.comment", true, false, true);
            config.createIntBoolProperty("showAbilitiesInGui", "morph.config.prop.showAbilitiesInGui.name", "morph.config.prop.showAbilitiesInGui.comment", true, true, true);
            config.createIntProperty("sortMorphs", "morph.config.prop.sortMorphs.name", "morph.config.prop.sortMorphs.comment", true, false, 0, 0, 3);
            config.createIntBoolProperty("renderCrosshairInRadialMenu", "morph.config.prop.renderCrosshairInRadialMenu.name", "morph.config.prop.renderCrosshairInRadialMenu.comment", true, false, false);
        }
        if(!config.getString("customMobToKillForFlight").isEmpty())
        {
            try
            {
                Class clz = Class.forName(config.getString("customMobToKillForFlight"));
                if(EntityLivingBase.class.isAssignableFrom(clz))
                {
                    classToKillForFlight = clz;
                    Morph.console(clz.getName() + " was mapped for a custom mob to be killed for flight.", false);
                }
                else
                {
                    Morph.console(clz.getName() + " was mapped for a custom mob to be killed for flight but the class is not of an EntityLivingBase type!", true);
                }
            }
            catch(ClassNotFoundException e)
            {
                Morph.console("A class was mapped for a custom mob to be killed for flight but the class was not found!", true);
            }
        }


        morph.common.core.EventHandler eventHandler = new morph.common.core.EventHandler();

		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Morph", "1.7", version, false));
    }
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.initTickHandlers();
		proxy.initMod();
	}
	
	@EventHandler
	public void postLoad(FMLPostInitializationEvent event)
	{
		proxy.initPostMod();
	}
	
	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event)
	{
        Morph.config.resetSession();
        Morph.config.updateSession("allowFlight", 1); // Adds this custom field to the session.

        proxy.tickHandlerServer.purgeSession = false;

		proxy.initCommands(event.getServer());
	}
	
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event)
	{
        proxy.tickHandlerServer.saveData = null;
		proxy.tickHandlerServer.playerMorphInfo.clear();
		proxy.tickHandlerServer.playerMorphs.clear();
	}

    public static void parseBlacklist(String s)
    {
        Morph.blacklistedClasses.clear();

        String[] classes = s.split(", *");
        for(String className : classes)
        {
            if(!className.trim().isEmpty())
            {
                try
                {
                    Class clz = Class.forName(className.trim());
                    if(EntityLivingBase.class.isAssignableFrom(clz) && !Morph.blacklistedClasses.contains(clz))
                    {
                        Morph.blacklistedClasses.add(clz);
                        Morph.console("Blacklisting class: " + clz.getName(), false);
                    }
                }
                catch(Exception e)
                {
                    Morph.console("Could not find class to blacklist: " + className.trim(), true);
                }
            }
        }
    }

    public static void parsePlayerList(String s)
    {
        Morph.playerList.clear();

        String[] names = s.split(", *");
        boolean added = false;
        for(String playerName : names)
        {
            if(!playerName.trim().isEmpty())
            {
                added = true;
                if(!Morph.playerList.contains(playerName.trim()))
                {
                    Morph.playerList.add(playerName.trim());
                }
            }
        }
        if(!Morph.playerList.isEmpty())
        {
            StringBuilder sb = new StringBuilder(Morph.config.getInt("listIsBlacklist") == 0 ? "Blacklisted players: " : "Whitelisted players: ");
            for(int i = 0; i < Morph.playerList.size(); i++)
            {
                sb.append(Morph.playerList.get(i));
                if(i < Morph.playerList.size() - 1)
                {
                    sb.append(", ");
                }
            }
            Morph.console(sb.toString(), false);
        }
    }

    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
