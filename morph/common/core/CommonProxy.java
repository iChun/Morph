package morph.common.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import morph.client.core.TickHandlerClient;
import morph.common.Morph;
import morph.common.ability.mod.AbilitySupport;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy 
{
	public void initMod()
	{
		String[] classes = Morph.blacklistedMobs.split(", *");
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
	
	public void initPostMod()
	{
		Iterator ite = EntityList.classToStringMapping.entrySet().iterator();
		while(ite.hasNext())
		{
			Entry e = (Entry)ite.next();
			Class clz = (Class)e.getKey();
			if(EntityLivingBase.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz))
			{
				compatibleEntities.add(clz);
			}
		}
		compatibleEntities.add(EntityPlayer.class);
		
		if(Morph.modAbilityPatch == 1)
		{
			AbilitySupport.getInstance().mapAbilities();
		}
	}
	
	public void initTickHandlers()
	{
		tickHandlerServer = new TickHandlerServer();
		TickRegistry.registerTickHandler(tickHandlerServer, Side.SERVER);
	}
	
	public void initCommands(MinecraftServer server)
	{
		ICommandManager manager = server.getCommandManager();
		if(manager instanceof CommandHandler)
		{
			CommandHandler handler = (CommandHandler)manager;
			handler.registerCommand(new CommandMorph());
		}
	}
	
	public TickHandlerClient tickHandlerClient;
	public TickHandlerServer tickHandlerServer;
	
	public ArrayList<Class> compatibleEntities = new ArrayList<Class>();
}
