package morph.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import morph.client.core.ClientProxy;
import morph.client.core.PacketHandlerClient;
import morph.common.ability.AbilityHandler;
import morph.common.core.CommonProxy;
import morph.common.core.ConnectionHandler;
import morph.common.core.MapPacketHandler;
import morph.common.core.ObfHelper;
import morph.common.core.PacketHandlerServer;
import morph.common.core.SessionState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "Morph", name = "Morph",
			version = Morph.version,
			dependencies = "required-after:Forge@[9.10.1.859,)"
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			connectionHandler = ConnectionHandler.class,
			tinyPacketHandler = MapPacketHandler.class,
			clientPacketHandlerSpec = @SidedPacketHandler(channels = { "Morph" }, packetHandler = PacketHandlerClient.class),
			serverPacketHandlerSpec = @SidedPacketHandler(channels = { "Morph" }, packetHandler = PacketHandlerServer.class),
			versionBounds = "[0.4.0,0.5.0)"
				)
public class Morph 
{
	public static final String version = "0.4.0";
	
	@Instance("Morph")
	public static Morph instance;
	
	@SidedProxy(clientSide = "morph.client.core.ClientProxy", serverSide = "morph.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	private static Logger logger;
	
	public static File configFolder;
	
	public static int childMorphs;
	public static int playerMorphs;
	public static int bossMorphs;
	
	public static int loseMorphsOnDeath;
	public static int instaMorph;
	public static int abilityTracker;
	public static int abilities;
//	public static int modAbilityPatch;
	public static int hostileAbilityMode;
	public static int hostileAbilityDistanceCheck;
	
	public static int canSleepMorphed;
	
	/*public static int keySelectorUp;
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
	
	public static int keyFavourite;*/
	
	public static int handRenderOverride;
	
	public static int showAbilitiesInGui;
	public static int allowMorphSelection;
	
	public static ArrayList<Class<? extends EntityLivingBase>> blacklistedClasses = new ArrayList<Class<? extends EntityLivingBase>>();

	@EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		logger = Logger.getLogger("Morph");
		logger.setParent(FMLLog.getLogger());
		
		boolean isClient = proxy instanceof ClientProxy;

		configFolder = event.getModConfigurationDirectory();
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		config.addCustomCategoryComment("gameplay", "These options affect the gameplay while using the mod.");
		
		childMorphs = addCommentAndReturnInt(config, "gameplay", "childMorphs", "Can you acquire child mob morphs?\nDisabled by default due to improper morph transitions\n0 = No\n1 = Yes", 0);
		playerMorphs = addCommentAndReturnInt(config, "gameplay", "playerMorphs", "Can you acquire player morphs?\n0 = No\n1 = Yes", 1);
		bossMorphs = addCommentAndReturnInt(config, "gameplay", "bossMorphs", "Can you acquire boss morphs?\n0 = No\n1 = Yes", 0);
		
		loseMorphsOnDeath = addCommentAndReturnInt(config, "gameplay", "loseMorphsOnDeath", "Will you lose all your morphs on death?\n0 = No\n1 = Yes", 0);
		instaMorph = addCommentAndReturnInt(config, "gameplay", "instaMorph", "Will you insta-morph into a new morph acquired?\n0 = No\n1 = Yes", 0);
		
		abilityTracker = addCommentAndReturnInt(config, "gameplay", "abilityTracker", "Allow the mod to randomly track mobs to scan for abilities?\nWARNING: This is not 100% accurate, and is provided \"as is\". It may or may not give mod mobs the right abilities.\nMay or may not be resource intensive, most likely not.\nThere is also a community provided mod patch support that maps some, but not all the abilities to some mods.\n0 = No\n1 = Yes", 0);
		abilities = addCommentAndReturnInt(config, "gameplay", "abilities", "Enable abilities?\n0 = No\n1 = Yes", 1);
//		modAbilityPatch = addCommentAndReturnInt(config, "gameplay", "modAbilityPatch", "Enable mod mob ability patching?\nThis support is mostly provided by the community and is not officially supported by the mod\n0 = No\n1 = Yes", 1);
		
		hostileAbilityMode = addCommentAndReturnInt(config, "gameplay", "hostileAbilityMode", "Hostile Ability Modes\n0 = Off, hostile mobs attack you despite being morphed.\n1 = Hostile mobs do not attack you if you are a hostile mob.\n2 = Hostile mobs of different types do not attack you if you are a hostile mob but hostile mobs of the same kind do.\n3 = Hostile mobs of the same type do not attack you but hostile mobs of other types attack you.\n4 = Hostile mobs have a decreased detection range around you.\nIf you'd like to turn on Hostile Ability, I'd recommend Mode 2 (personal preference)", 0);
		hostileAbilityDistanceCheck = addCommentAndReturnInt(config, "gameplay", "hostileAbilityDistanceCheck", "Hostile Ability Distance Check for Hostile Ability Mode 4\nYou have to be *this* close before hostile mobs know you are not one of them.\nDefault: 6", 6);
		
		canSleepMorphed = addCommentAndReturnInt(config, "gameplay", "canSleepMorphed", "Can you sleep while morphed?\n0 = No\n1 = Yes", 0);
		allowMorphSelection = addCommentAndReturnInt(config, "gameplay", "allowMorphSelection", "Requested by SoundLogic\nCan you open the morph GUI?\n0 = No\n1 = Yes", 1);
		
		if(isClient)
		{
			config.addCustomCategoryComment("client", "These options are client only.\nCheck here for key codes for the config: http://www.minecraftwiki.net/wiki/Key_codes");
			
			/*keySelectorUp = addCommentAndReturnInt(config, "client", "keySelectorUp", "Key Code to go up on the selector\nDefault: 26 ([)", 26);
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
		*/	
			handRenderOverride = addCommentAndReturnInt(config, "client", "handRenderOverride", "Allow the mod to override player hand rendering?\n0 = No\n1 = Yes", 1);
			
			showAbilitiesInGui = addCommentAndReturnInt(config, "client", "showAbilitiesInGui", "Show the abilities the morph has in the GUI?\n0 = No\n1 = Yes", 1);
		}
		
		config.save();
		
		MinecraftForge.EVENT_BUS.register(new morph.common.core.EventHandler());
		
		GameRegistry.registerPlayerTracker(new ConnectionHandler());
		
		ObfHelper.detectObfuscation();
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
	public void serverStarting(FMLServerStartingEvent event)
	{
		SessionState.abilities = Morph.abilities == 1;
		SessionState.canSleepMorphed = Morph.canSleepMorphed == 1;
		SessionState.allowMorphSelection = Morph.allowMorphSelection == 1;
		
		proxy.initCommands(event.getServer());
		
		NBTTagCompound tag = null;
		
    	try
    	{
    		File file = new File(Morph.configFolder, "morphAbilities.dat");
    		if(!file.exists())
    		{
    			return;
    		}
    		else
    		{
    			tag = CompressedStreamTools.readCompressed(new FileInputStream(file));
    		}
    	}
    	catch(EOFException e)
    	{
    		Morph.console("Mod ability data is corrupted! Attempting to read from backup.", true);
    		try
    		{
	    		File file = new File(Morph.configFolder, "morphAbilities_backup.dat");
	    		if(!file.exists())
	    		{
	    			Morph.console("No backup detected!", true);
	    			return;
	    		}
	    		tag = CompressedStreamTools.readCompressed(new FileInputStream(file));

	    		File file1 = new File(Morph.configFolder, "morphAbilities.dat");
	    		file1.delete();
	    		file.renameTo(file1);
	    		Morph.console("Restoring mod ability data from backup.", false);
    		}
    		catch(Exception e1)
    		{
    			Morph.console("Even your backup data is corrupted. What have you been doing?!", true);
    			return;
    		}
    	}
    	catch(IOException e)
    	{
    		Morph.console("Failed to read mod ability save data!", true);
    		return;
    	}

    	AbilityHandler.readAbilitiesFromNBT(tag);
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
		proxy.tickHandlerServer.activeEntTrackers.clear();
		proxy.tickHandlerServer.entTrackerResults.clear();
		proxy.tickHandlerServer.saveData = null;
	}
	
    public static NBTTagCompound readNBTTagCompound(DataInput par0DataInput) throws IOException
    {
        short short1 = par0DataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            par0DataInput.readFully(abyte);
            return CompressedStreamTools.decompress(abyte);
        }
    }

    public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutput.writeShort((short)abyte.length);
            par1DataOutput.write(abyte);
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
	
    public static int getNetId()
    {
    	return ((NetworkModHandler)FMLNetworkHandler.instance().findNetworkModHandler(Morph.instance)).getNetworkId();
    }

    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	logger.log(warning ? Level.WARNING : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
