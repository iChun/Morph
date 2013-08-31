package morph.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import morph.api.Api;
import morph.client.core.ClientProxy;
import morph.client.core.PacketHandlerClient;
import morph.common.core.CommonProxy;
import morph.common.core.ConnectionHandler;
import morph.common.core.MapPacketHandler;
import morph.common.core.ObfHelper;
import morph.common.core.PacketHandlerServer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
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
			version = "0.0.7"
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			connectionHandler = ConnectionHandler.class,
			tinyPacketHandler = MapPacketHandler.class,
			clientPacketHandlerSpec = @SidedPacketHandler(channels = { "Morph" }, packetHandler = PacketHandlerClient.class),
			serverPacketHandlerSpec = @SidedPacketHandler(channels = { "Morph" }, packetHandler = PacketHandlerServer.class)
				)
public class Morph 
{
	public static final String version = "0.0.7";
	
	@Instance("Morph")
	public static Morph instance;
	
	@SidedProxy(clientSide = "morph.client.core.ClientProxy", serverSide = "morph.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	private static Logger logger;
	
	public static int childMorphs;
	public static int playerMorphs;
	public static int bossMorphs;
	public static int loseMorphsOnDeath;
	
	public static int keySelectorBack;
	public static int keySelectorForward;
	public static int keySelectorSelect;
	public static int keySelectorCancel;
	public static int keySelectorRemoveMorph;
	public static int handRenderOverride;
	
	public static ArrayList<Class<? extends EntityLivingBase>> blacklistedClasses = new ArrayList<Class<? extends EntityLivingBase>>();
	
	@EventHandler
	public void preLoad(FMLPreInitializationEvent event)
	{
		logger = Logger.getLogger("Morph");
		logger.setParent(FMLLog.getLogger());
		
		boolean isClient = proxy instanceof ClientProxy;

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		config.addCustomCategoryComment("gameplay", "These options affect the gameplay while using the mod.");
		
		childMorphs = addCommentAndReturnInt(config, "gameplay", "childMorphs", "Can you morph into child mobs?\nDisabled by default due to improper morph transitions\n0 = No\n1 = Yes", 0);
		playerMorphs = addCommentAndReturnInt(config, "gameplay", "playerMorphs", "Can you morph into players?\n0 = No\n1 = Yes", 1);
		bossMorphs = addCommentAndReturnInt(config, "gameplay", "bossMorphs", "Can you morph into bosses?\n0 = No\n1 = Yes", 0);
		
		loseMorphsOnDeath = addCommentAndReturnInt(config, "gameplay", "loseMorphsOnDeath", "Will you lose all your morphs on death?\n0 = No\n1 = Yes", 0);
		
		if(isClient)
		{
			config.addCustomCategoryComment("client", "These options are client only.Check here: http://www.minecraftwiki.net/wiki/Key_codes\nI would recommend binding keySelectorForward to -98 (Middle Mouse) to allow full use of the selector with the mouse.");
			keySelectorBack = addCommentAndReturnInt(config, "client", "keySelectorBack", "Key Code to go back on the selector\nDefault: 26 ([)", 26);
			keySelectorForward = addCommentAndReturnInt(config, "client", "keySelectorForward", "Key Code to go forward on the selector\nDefault: 27 (])", 27);
			keySelectorSelect = addCommentAndReturnInt(config, "client", "keySelectorSelect", "Key Code to select morph on the selector.\nDefault: 28 (Enter/Return)", 28);
			keySelectorCancel = addCommentAndReturnInt(config, "client", "keySelectorCancel", "Key Code to close the selector.\nDefault: 1 (Esc)", 1);
			keySelectorRemoveMorph = addCommentAndReturnInt(config, "client", "keySelectorRemoveMorph", "Key Code to remove morph on the selector.\nDelete also works by default\nDefault: 14 (Backspace)", 14);
			handRenderOverride = addCommentAndReturnInt(config, "client", "handRenderOverride", "Allow the mod to override player hand rendering?\n0 = No\n1 = Yes", 1);
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
		Api.blacklistEntity(EntitySlime.class);
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
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
		Morph.proxy.tickHandlerServer.saveData = null;
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
