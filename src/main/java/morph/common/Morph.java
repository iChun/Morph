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
			dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".5.0,)"
				)
public class Morph
    implements IConfigUser
{
	public static final String version = "0.8.0";
	
	@Instance("Morph")
	public static Morph instance;
	
	@SidedProxy(clientSide = "morph.client.core.ClientProxy", serverSide = "morph.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	private static Logger logger;
	
    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    public static Config config;
	
	public static ArrayList<Class<? extends EntityLivingBase>> blacklistedClasses = new ArrayList<Class<? extends EntityLivingBase>>();
	public static ArrayList<String> whitelistedPlayerNames = new ArrayList<String>();

    @Override
    public boolean onConfigChange(Config cfg, Property prop)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            if(prop.getName().equalsIgnoreCase("blacklistedMobs"))
            {
                parseBlacklist(prop.getString());
            }
            if(prop.getName().equalsIgnoreCase("whitelistedPlayers"))
            {
                parseWhitelist(prop.getString());
            }
            if(prop.getName().equalsIgnoreCase("abilities") || prop.getName().equalsIgnoreCase("canSleepMorphed") || prop.getName().equalsIgnoreCase("allowMorphSelection"))
            {
                Morph.proxy.tickHandlerServer.updateSession(null);
            }
        }
        return true;
    }

    @EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		logger = LogManager.getLogger("Morph");

        config = ConfigHandler.createConfig(event.getSuggestedConfigurationFile(), "morph", "Morph", logger, instance);

        config.setCurrentCategory("gameplay", "Gameplay", "These options affect the gameplay while using the mod.");
        config.createIntBoolProperty("childMorphs", "Child Morphs", "Can you acquire child mob morphs?\nDisabled by default due to improper morph transitions", true, false, false);
        config.createIntBoolProperty("playerMorphs", "Player Morphs", "Can you acquire player morphs?", true, false, true);
        config.createIntBoolProperty("bossMorphs", "Boss Morphs", "Can you acquire boss morphs?\nThis is disabled by default due to morphing issues with mobs like the EnderDragon, Twilight Forest's Hydra and Naga, etc.", true, false, false);

        config.createStringProperty("blacklistedMobs", "Blacklisted Mobs", "Prevent players from acquiring these mobs as a morph.\nLeave blank to allow acquisition of all compatible mobs.\nFormatting is as follows: <class>, <class>, <class>\nExample: am2.entities.EntityBattleChicken, biomesoplenty.entities.EntityJungleSpider, thaumcraft.common.entities.monster.EntityWisp", true, false, "");
        config.createStringProperty("whitelistedPlayers", "Whitelisted Players", "Only allow these players to use the Morph skill.\nLeave blank to allow all players to use the skill.\nFormatting is as follows: <name>, <name>, <name>\nExample: Cojomax99, pahimar, ohaiiChun", true, false, "");

        config.createIntProperty("loseMorphsOnDeath", "Lose Morphs on Death", "Will you lose your morphs on death?\n0 = No\n1 = Yes, all morphs\n2 = Yes, the morph you're currently using", true, false, 0, 0, 2);
        config.createIntBoolProperty("instaMorph", "Insta-Morph", "Will you insta-morph into a new morph acquired?", true, false, false);

        config.createIntBoolProperty("modNBTStripper", "Mod NBT Stripper", "Enable mod mob NBT Stripping?\nThis support is mostly provided by the community and is not officially supported by the mod\nThe stripper was added to remove non-essential information from the Entity NBT to remove duplicate morphs.", false, false, true);

        config.createIntBoolProperty("canSleepMorphed", "Can Sleep Morphed?", "Can you sleep while morphed?", true, true, false);
        config.createIntBoolProperty("allowMorphSelection", "Allow Morph Selection?", "Requested by SoundLogic\nCan you open the morph GUI?", true, true, true);

        //TODO custom config to link to other places?
        config.setCurrentCategory("abilities", "Abilities", "These settings are related to Morph's Abilities feature.");
        config.createIntBoolProperty("abilities", "Abilities", "Enable abilities?", false, true, true);
        config.createIntBoolProperty("modAbilityPatch", "Mod Ability Patch", "Enable mod mob ability patching?\nThis support is mostly provided by the community and is not officially supported by the mod\nIf a mod mob you like doesn't have an ability, you can contribute to the mappings on the Morph Github page.", false, false, true);
        config.createIntBoolProperty("forceLocalModAbilityPatch", "Force Local Mod Ability Patch", "Force the mod to use the local copy of the ModMobAbilitySupport?\nThis is meant for debugging purposes and for modified local mod mob abilities mappings.\nDo take note that mappings server and clientside are not synched so both ends will require the same mappings.", false, false, false);

        config.createStringProperty("customPatchLink", "Custom Mod Ability Patch Link", "Redirect the mod to a different JSON patch location rather then the default patch hosted on GitHub.\nIf you would like to use the default, leave this blank.\nThis also affects NBTStripper.json.\nIf the link to your file is \"https://raw.github.com/iChun/Morph/master/assets/morph/mod/ModMobSupport.json\", put \"https://raw.github.com/iChun/Morph/master\", you cannot change the \"/assets/morph/mod/ModMobSupport.json\" part of the link.", false, false, "");

        config.createIntProperty("hostileAbilityMode", "Hostile Ability Mode", "Hostile Ability Modes\n0 = Off, hostile mobs attack you despite being morphed.\n1 = Hostile mobs do not attack you if you are a hostile mob.\n2 = Hostile mobs of different types do not attack you if you are a hostile mob but hostile mobs of the same kind do.\n3 = Hostile mobs of the same type do not attack you but hostile mobs of other types attack you.\n4 = Hostile mobs have a decreased detection range around you.\nIf you'd like to turn on Hostile Ability, I'd recommend Mode 2 (personal preference)", true, false, 0, 0, 4);
        config.createIntProperty("hostileAbilityDistanceCheck", "Hostile Ability Distance Check", "Hostile Ability Distance Check for Hostile Ability Mode 4\nYou have to be *this* close before hostile mobs know you are not one of them.\nDefault: 6", true, false, 6, 0, 128);

        //TODO make a per-player or per-server config
        config.createIntProperty("disableEarlyGameFlight", "Disable Early Game Flight", "Disable the flight ability until a player...\n0 = Enable early game flight\n1 = ...has reached the nether\n2 = ...has killed the Wither", true, false, 0, 0, 2);

        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
            config.setCurrentCategory("clientOnly", "Client Only", "These settings are client-only.");
			config.createKeybindProperty("keySelectorUp", "Selector Up", "Key Code to go up on the selector\nDefault: 26 ([)", 26, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorDown", "Selector Down", "Key Code to go down on the selector\nDefault: 27 (])", 27, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorLeft", "Selector Left", "Key Code to go left on the selector\nDefault: 26 ([)", 26, true, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorRight", "Selector Right", "Key Code to go right on the selector\nDefault: 27 (])", 27, true, false, false, false, 0, false);

            //TODO will this throw a classnotfound error?
            iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindAttack);
            iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindUseItem);
            iChunUtil.proxy.registerKeyBind(new KeyBind(Keyboard.KEY_DELETE, false, false, false, false), null);
            config.createKeybindProperty("keySelectorSelect", "Selector Select", "Key Code to select morph on the selector\nDefault: 28 (Enter/Return)", 28, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorCancel", "Selector Cancel", "Key Code to close the selector\nDefault: 1 (Esc)", 1, false, false, false, false, 0, false);
            config.createKeybindProperty("keySelectorRemoveMorph", "Selector Remove Morph", "Key Code to remove morph on the selector.\nDelete also works by default\nDefault: 14 (Backspace)", 14, false, false, false, false, 0, false);

            config.createKeybindProperty("keyFavourite", "Selector Favourite/Radial Menu", "Key Code to favourite/unfavourite morph on the selector and show the radial menu.\nDefault: 41 (` [also known as ~])", 41, false, false, false, false, 0, false);

            config.createIntBoolProperty("handRenderOverride", "Hand Render Override", "Allow the mod to override player hand rendering?", true, false, true);

            config.createIntBoolProperty("showAbilitiesInGui", "Show Abilities In GUI", "Show the abilities the morph has in the GUI?", true, true, true);

            config.createIntProperty("sortMorphs", "Sort Morphs", "Sort the morphs in the GUI?\n0 = Order of acquisition (Server default)\n1 = Alphabetically (according to Operating System)\n2 = Alphabetically, and attempt to sort grouped morphs as well\n3 = Most recently used since connecting to the server", true, false, 0, 0, 3);

            config.createIntBoolProperty("renderCrosshairInRadialMenu", "Render Crosshair in Radial Menu", "As per request, render the crosshair position when in the radial menu.", true, false, false);
		}
		
        morph.common.core.EventHandler eventHandler = new morph.common.core.EventHandler();

		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);
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
        Morph.config.updateSession("allowFlight", true); // Adds this custom field to the session.

		proxy.initCommands(event.getServer());
	}
	
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event)
	{
		proxy.tickHandlerServer.playerMorphInfo.clear();
		proxy.tickHandlerServer.playerMorphs.clear();
		proxy.tickHandlerServer.saveData = null;
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

    public static void parseWhitelist(String s)
    {
        Morph.whitelistedPlayerNames.clear();

        String[] names = s.split(", *");
        boolean added = false;
        for(String playerName : names)
        {
            if(!playerName.trim().isEmpty())
            {
                added = true;
                if(!Morph.whitelistedPlayerNames.contains(playerName.trim()))
                {
                    Morph.whitelistedPlayerNames.add(playerName.trim());
                }
            }
        }
        if(!Morph.whitelistedPlayerNames.isEmpty())
        {
            StringBuilder sb = new StringBuilder("Whitelisted players: ");
            for(int i = 0; i < Morph.whitelistedPlayerNames.size(); i++)
            {
                sb.append(Morph.whitelistedPlayerNames.get(i));
                if(i < Morph.whitelistedPlayerNames.size() - 1)
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
