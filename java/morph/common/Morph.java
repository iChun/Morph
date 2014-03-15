package morph.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.relauncher.Side;
import ichun.core.iChunUtil;
import morph.client.core.ClientProxy;
import morph.client.render.HandRenderHandler;
import morph.common.core.CommonProxy;
import morph.common.core.ObfHelper;
import morph.common.core.SessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

@Mod(modid = "Morph", name = "Morph",
			version = Morph.version,
			dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".5.0,)"
				)
public class Morph
{
	public static final String version = "0.8.0";
	
	@Instance("Morph")
	public static Morph instance;
	
	@SidedProxy(clientSide = "morph.client.core.ClientProxy", serverSide = "morph.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	private static Logger logger;
	
	private static Configuration config;
	
	public static File configFolder;

    public static EnumMap<Side, FMLEmbeddedChannel> channels;
	
	public static int childMorphs;
	public static int playerMorphs;
	public static int bossMorphs;
	
	public static String blacklistedMobs;
	
	public static String whitelistedPlayers;
	
	public static int disableEarlyGameFlight;
	public static int loseMorphsOnDeath;
	public static int instaMorph;
	public static int abilities;
	public static int modAbilityPatch;
	public static int forceLocalModAbilityPatch;
	public static int modNBTStripper;
	
	public static int hostileAbilityMode;
	public static int hostileAbilityDistanceCheck;
	
	public static int canSleepMorphed;
	
	public static int keySelectorUp;
	public static int keySelectorDown;
	public static int keySelectorLeft;
	public static int keySelectorRight;
	
	public static int keySelectorUpHold;
	public static int keySelectorDownHold;
	public static int keySelectorLeftHold;
	public static int keySelectorRightHold;
	
	public static int keySelectorSelect;
	public static int keySelectorCancel;
	public static int keySelectorRemoveMorph;
	
	public static int keyFavourite;
	
	public static int renderCrosshairInRadialMenu;
	
	public static int handRenderOverride;
	
	public static int showAbilitiesInGui;
	public static int allowMorphSelection;
	
	public static int sortMorphs;
	
	public static ArrayList<Class<? extends EntityLivingBase>> blacklistedClasses = new ArrayList<Class<? extends EntityLivingBase>>();
	public static ArrayList<String> whitelistedPlayerNames = new ArrayList<String>();

	@EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		logger = LogManager.getLogger("Morph");

        //TODO migrate to iChunUtil config system.
		configFolder = event.getModConfigurationDirectory();
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		config.addCustomCategoryComment("gameplay", "These options affect the gameplay while using the mod.");
		
		childMorphs = addCommentAndReturnInt(config, "gameplay", "childMorphs", "Can you acquire child mob morphs?\nDisabled by default due to improper morph transitions\n0 = No\n1 = Yes", 0);
		playerMorphs = addCommentAndReturnInt(config, "gameplay", "playerMorphs", "Can you acquire player morphs?\n0 = No\n1 = Yes", 1);
		bossMorphs = addCommentAndReturnInt(config, "gameplay", "bossMorphs", "Can you acquire boss morphs?\nThis is disabled by default due to morphing issues with mobs like the EnderDragon, Twilight Forest's Hydra and Naga, etc.\n0 = No\n1 = Yes", 0);
		
		blacklistedMobs = addCommentAndReturnString(config, "gameplay", "blacklistedMobs", "Prevent players from acquiring these mobs as a morph.\nLeave blank to allow acquisition of all compatible mobs.\nFormatting is as follows: <class>, <class>, <class>\nExample: am2.entities.EntityBattleChicken, biomesoplenty.entities.EntityJungleSpider, thaumcraft.common.entities.monster.EntityWisp", "");
		
		whitelistedPlayers = addCommentAndReturnString(config, "gameplay", "whitelistedPlayers", "Only allow these players to use the Morph skill.\nLeave blank to allow all players to use the skill.\nFormatting is as follows: <name>, <name>, <name>\nExample: Cojomax99, pahimar, ohaiiChun", "");
		
		disableEarlyGameFlight = addCommentAndReturnInt(config, "gameplay", "disableEarlyGameFlight", "Disable the flight ability until a player...\n0 = Enable early game flight\n1 = ...has reached the nether\n2 = ...has killed the Wither", 0);
		loseMorphsOnDeath = addCommentAndReturnInt(config, "gameplay", "loseMorphsOnDeath", "Will you lose your morphs on death?\n0 = No\n1 = Yes, all morphs\n2 = Yes, the morph you're currently using", 0);
		instaMorph = addCommentAndReturnInt(config, "gameplay", "instaMorph", "Will you insta-morph into a new morph acquired?\n0 = No\n1 = Yes", 0);
		
		abilities = addCommentAndReturnInt(config, "gameplay", "abilities", "Enable abilities?\n0 = No\n1 = Yes", 1);
		modAbilityPatch = addCommentAndReturnInt(config, "gameplay", "modAbilityPatch", "Enable mod mob ability patching?\nThis support is mostly provided by the community and is not officially supported by the mod\nIf a mod mob you like doesn't have an ability, you can contribute to the mappings on the Morph Github page.\n0 = No\n1 = Yes", 1);
		forceLocalModAbilityPatch = addCommentAndReturnInt(config, "gameplay", "forceLocalModAbilityPatch", "Force the mod to use the local copy of the ModMobAbilitySupport?\nThis is meant for debugging purposes and for modified local mod mob abilities mappings.\nDo take note that mappings server and clientside are not synched so both ends will require the same mappings.\n0 = No\n1 = Yes", 0);
		
		modNBTStripper = addCommentAndReturnInt(config, "gameplay", "modNBTStripper", "Enable mod mob NBT Stripping?\nThis support is mostly provided by the community and is not officially supported by the mod\nThe stripper was added to remove non-essential information from the Entity NBT to remove duplicate morphs.\n0 = No\n1 = Yes", 1);
		
		hostileAbilityMode = addCommentAndReturnInt(config, "gameplay", "hostileAbilityMode", "Hostile Ability Modes\n0 = Off, hostile mobs attack you despite being morphed.\n1 = Hostile mobs do not attack you if you are a hostile mob.\n2 = Hostile mobs of different types do not attack you if you are a hostile mob but hostile mobs of the same kind do.\n3 = Hostile mobs of the same type do not attack you but hostile mobs of other types attack you.\n4 = Hostile mobs have a decreased detection range around you.\nIf you'd like to turn on Hostile Ability, I'd recommend Mode 2 (personal preference)", 0);
		hostileAbilityDistanceCheck = addCommentAndReturnInt(config, "gameplay", "hostileAbilityDistanceCheck", "Hostile Ability Distance Check for Hostile Ability Mode 4\nYou have to be *this* close before hostile mobs know you are not one of them.\nDefault: 6", 6);
		
		canSleepMorphed = addCommentAndReturnInt(config, "gameplay", "canSleepMorphed", "Can you sleep while morphed?\n0 = No\n1 = Yes", 0);
		allowMorphSelection = addCommentAndReturnInt(config, "gameplay", "allowMorphSelection", "Requested by SoundLogic\nCan you open the morph GUI?\n0 = No\n1 = Yes", 1);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			config.addCustomCategoryComment("client", "These options are client only.\nCheck here for key codes for the config: http://www.minecraftwiki.net/wiki/Key_codes");
			
			keySelectorUp = addCommentAndReturnInt(config, "client", "keySelectorUp", "Key Code to go up on the selector\nDefault: 26 ([)", 26);
			keySelectorDown = addCommentAndReturnInt(config, "client", "keySelectorDown", "Key Code to go down on the selector\nDefault: 27 (])", 27);
			keySelectorLeft = addCommentAndReturnInt(config, "client", "keySelectorLeft", "Key Code to go left on the selector\nDefault: 26 ([)", 26);
			keySelectorRight = addCommentAndReturnInt(config, "client", "keySelectorRight", "Key Code to go right on the selector\nDefault: 27 (])", 27);
			
			keySelectorUpHold = addCommentAndReturnInt(config, "client", "keySelectorUpHold", "Key required to hold to use up key on the selector\n0 = None\n1 = Shift\n2 = Ctrl\n3 = Alt\nDefault: 0", 0);
			keySelectorDownHold = addCommentAndReturnInt(config, "client", "keySelectorDownHold", "Key required to hold to use down key on the selector\n0 = None\n1 = Shift\n2 = Ctrl\n3 = Alt\nDefault: 0", 0);
			keySelectorLeftHold = addCommentAndReturnInt(config, "client", "keySelectorLeftHold", "Key required to hold to use left key on the selector\n0 = None\n1 = Shift\n2 = Ctrl\n3 = Alt\nDefault: 1", 1);
			keySelectorRightHold = addCommentAndReturnInt(config, "client", "keySelectorRightHold", "Key required to hold to use right key on the selector\n0 = None\n1 = Shift\n2 = Ctrl\n3 = Alt\nDefault: 1", 1);
			
			keySelectorSelect = addCommentAndReturnInt(config, "client", "keySelectorSelect", "Key Code to select morph on the selector.\nDefault: 28 (Enter/Return)", 28);
			keySelectorCancel = addCommentAndReturnInt(config, "client", "keySelectorCancel", "Key Code to close the selector.\nDefault: 1 (Esc)", 1);
			keySelectorRemoveMorph = addCommentAndReturnInt(config, "client", "keySelectorRemoveMorph", "Key Code to remove morph on the selector.\nDelete also works by default\nDefault: 14 (Backspace)", 14);
			
			keyFavourite = addCommentAndReturnInt(config, "client", "keyFavourite", "Key Code to favourite/unfavourite morph on the selector and show the radial menu.\nDefault: 41 (` [also known as ~])", 41);
			
			handRenderOverride = addCommentAndReturnInt(config, "client", "handRenderOverride", "Allow the mod to override player hand rendering?\n0 = No\n1 = Yes", 1);
			
			showAbilitiesInGui = addCommentAndReturnInt(config, "client", "showAbilitiesInGui", "Show the abilities the morph has in the GUI?\n0 = No\n1 = Yes", 1);
			
			sortMorphs = addCommentAndReturnInt(config, "client", "sortMorphs", "Sort the morphs in the GUI?\n0 = Order of acquisition (Server default)\n1 = Alphabetically (according to Operating System)\n2 = Alphabetically, and attempt to sort grouped morphs as well\n3 = Most recently used since connecting to the server", 0);

			renderCrosshairInRadialMenu = addCommentAndReturnInt(config, "client", "renderCrosshairInRadialMenu", "As per request, render the crosshair position when in the radial menu.\n0 = No\n1 = Yes", 0);
		}
		
		config.save();

        ObfHelper.detectObfuscation();

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
		
		MinecraftForge.EVENT_BUS.register(new HandRenderHandler());
	}
	
	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event)
	{
		SessionState.abilities = Morph.abilities == 1;
		SessionState.canSleepMorphed = Morph.canSleepMorphed == 1;
		SessionState.allowMorphSelection = Morph.allowMorphSelection == 1;
		SessionState.allowFlight = true;
		
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

    //TODO remove these
    public static void saveConfig()
	{
		if(config != null)
		{
			if(proxy instanceof ClientProxy)
			{
				updatePropertyInt(config, "client", "keySelectorUp", keySelectorUp);
				updatePropertyInt(config, "client", "keySelectorDown", keySelectorDown);
				updatePropertyInt(config, "client", "keySelectorLeft", keySelectorLeft);
				updatePropertyInt(config, "client", "keySelectorRight", keySelectorRight);
				
				updatePropertyInt(config, "client", "keySelectorUpHold", keySelectorUpHold);
				updatePropertyInt(config, "client", "keySelectorDownHold", keySelectorDownHold);
				updatePropertyInt(config, "client", "keySelectorLeftHold", keySelectorLeftHold);
				updatePropertyInt(config, "client", "keySelectorRightHold", keySelectorRightHold);
				
				updatePropertyInt(config, "client", "keySelectorSelect", keySelectorSelect);
				updatePropertyInt(config, "client", "keySelectorCancel", keySelectorCancel);
				updatePropertyInt(config, "client", "keySelectorRemoveMorph", keySelectorRemoveMorph);
	
				updatePropertyInt(config, "client", "keyFavourite", keyFavourite);
			}

			updatePropertyString(config, "gameplay", "whitelistedPlayers", whitelistedPlayers);
			
			config.save();
		}
	}
	
	public static void updatePropertyInt(Configuration config, String cat, String propName, int value)
	{
		ConfigCategory category = config.getCategory(cat);
		if(category.containsKey(propName))
		{
            Property prop = category.get(propName);

            if (prop.getType() == null)
            {
                prop = new Property(prop.getName(), Integer.toString(value), Property.Type.INTEGER);
                category.put(propName, prop);
            }
            else
            {
            	prop.set(Integer.toString(value));
            }
		}
	}
	
	public static void updatePropertyString(Configuration config, String cat, String propName, String value)
	{
		ConfigCategory category = config.getCategory(cat);
		if(category.containsKey(propName))
		{
            Property prop = category.get(propName);

            if (prop.getType() == null)
            {
                prop = new Property(prop.getName(), value, Property.Type.STRING);
                category.put(propName, prop);
            }
            else
            {
            	prop.set(value);
            }
		}
	}

	public static int addCommentAndReturnInt(Configuration config, String cat, String s, String comment, int i) //Taken from iChun Util
	{
		Property prop = config.get(cat, s, i);
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getInt();
	}

	public static String addCommentAndReturnString(Configuration config, String cat, String s, String comment, String value)
	{
		Property prop = config.get(cat, s, value);
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getString();
	}
	
    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
