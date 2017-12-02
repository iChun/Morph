package me.ichun.mods.morph.common.command;

import com.google.common.collect.Ordering;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.api.event.MorphAcquiredEvent;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphVariant;
import me.ichun.mods.morph.common.packet.PacketUpdateMorphList;
import net.minecraft.command.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class CommandMorph extends CommandBase
{
    public final Style TEXT_GRAY = new Style().setColor(TextFormatting.GRAY);
    public final ArrayList<String> entityNames = new ArrayList<>();

    @Override
    public String getName()
    {
        return "morph";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + this.getName() + " help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length > 0)
        {
            if(args[0].equalsIgnoreCase("help"))
            {
                //				<demorph|clear|morph|give> [player] [force (true/false) / entity name]
                sender.sendMessage(new TextComponentTranslation("morph.command.analyse").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.demorph").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.clean").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.remove").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.clear").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.morph").setStyle(TEXT_GRAY));
                sender.sendMessage(new TextComponentTranslation("morph.command.give").setStyle(TEXT_GRAY));
            }
            else
            {
                EntityPlayerMP player = null;
                boolean force = ((args[0].equalsIgnoreCase("demorph") || args[0].equalsIgnoreCase("clear")) && args.length == 3 && (args[2].equalsIgnoreCase("force") || args[2].equalsIgnoreCase("true")));
                try
                {
                    player = args.length == 1 ? getCommandSenderAsPlayer(sender) : getPlayer(server, sender, args[1]);
                }
                catch(PlayerNotFoundException e)
                {
                    if(!force)
                    {
                        throw e;
                    }
                }
                if(player == null) //we're forcing, player couldn't be found.
                {
                    EntityPlayerMP player1 = new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), DimensionManager.getWorld(0), EntityHelper.getGameProfile(args[1]), new PlayerInteractionManager(DimensionManager.getWorld(0)));
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().readPlayerDataFromFile(player1);
                    NBTTagCompound tag = EntityHelper.getPlayerPersistentData(player1, PlayerMorphHandler.MORPH_DATA_NAME);
                    boolean changed = false;
                    if(args[0].equalsIgnoreCase("demorph") && tag.hasKey("currentMorph"))
                    {
                        tag.removeTag("currentMorph");
                        changed = true;
                    }
                    if(args[0].equalsIgnoreCase("clear") && tag.hasKey("variantCount"))
                    {
                        tag.removeTag("variantCount");
                        int i = 0;
                        while(tag.hasKey("variant_" + i))
                        {
                            tag.removeTag("variant_" + i);
                            i++;
                        }
                        changed = true;
                    }
                    if(changed)
                    {
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().add(player1);
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().saveAllPlayerData();
                        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().remove(player1);
                        notifyCommandListener(sender, this, "morph.command.successfulForce", player1.getName());
                    }
                    else
                    {
                        notifyCommandListener(sender, this, "morph.command.playerNotFoundForcedNoTagsChanged", player1.getName());
                    }
                }
                else if(args[0].equalsIgnoreCase("demorph"))
                {
                    if(PlayerMorphHandler.getInstance().forceDemorph(player))
                    {
                        PlayerMorphHandler.getInstance().savePlayerData(player);
                        notifyCommandListener(sender, this, "morph.command.forcingDemorph", player.getName());
                    }
                    else
                    {
                        notifyCommandListener(sender, this, "morph.command.notInMorph", player.getName());
                    }
                }
                else if(args[0].equalsIgnoreCase("clean"))
                {
                    PlayerMorphHandler.getInstance().forceDemorph(player);
                    ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.playerMorphs.get(player.getName());
                    if(morphs != null && !morphs.isEmpty())
                    {
                        Morph.eventHandlerServer.playerMorphs.remove(player.getName());

                        ArrayList<MorphVariant> morphsToClean = new ArrayList<>();
                        for(int i = 0; i < morphs.size(); i++)
                        {
                            MorphVariant var = morphs.get(i);
                            if(!var.entId.startsWith("player:"))
                            {
                                morphsToClean.addAll(var.split());
                            }
                            else
                            {
                                //readd player morphs. nothing to clean.
                                ArrayList<MorphVariant> newMorphs = Morph.eventHandlerServer.playerMorphs.computeIfAbsent(player.getName(), v -> new ArrayList<>());
                                newMorphs.add(var);
                            }
                        }
                        morphs = Morph.eventHandlerServer.getPlayerMorphs(player);
                        for(MorphVariant var : morphsToClean)
                        {
                            EntityLivingBase living = var.createEntityInstance(player.getEntityWorld());
                            MorphVariant variant = MorphVariant.createVariant(living);
                            if(variant == null) //Variant could not be created.
                            {
                                continue;
                            }

                            variant.thisVariant.isFavourite = var.thisVariant.isFavourite;

                            int variantIndex = -2;
                            for(MorphVariant var1 : morphs)
                            {
                                if(variant.entId.equals(var1.entId)) //non-player variants
                                {
                                    variantIndex = MorphVariant.combineVariants(var1, variant);
                                    if(variantIndex == -2) //failed to merge for reasons. Return false acquisition.
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        //The variant should be a new variant so it'll be the latest entry in the variants list.
                                        variant = var1.createWithVariant(variantIndex == -1 ? var1.thisVariant : var1.variants.get(variantIndex));
                                    }
                                    break;
                                }
                            }

                            if(variantIndex == -2) //No preexisting variant exists.
                            {
                                morphs.add(variant);
                            }
                        }

                        Collections.sort(morphs);

                        PlayerMorphHandler.getInstance().savePlayerData(player);

                        Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player);
                        notifyCommandListener(sender, this, "morph.command.successful", player.getName());
                        return;
                    }
                    notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                }
                else if(args[0].equalsIgnoreCase("remove"))
                {
                    if(args.length == 3)
                    {
                        ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.playerMorphs.get(player.getName());
                        if(morphs != null && !morphs.isEmpty())
                        {
                            boolean found = false;
                            for(int i = morphs.size() - 1; i >= 0; i--)
                            {
                                MorphVariant var = morphs.get(i);
                                if(args[2].startsWith("player:") && var.entId.equalsIgnoreCase(MorphVariant.PLAYER_MORPH_ID) && !var.playerName.equalsIgnoreCase(player.getName()) && var.playerName.equalsIgnoreCase(args[2].substring("player:".length(), args[2].length())) || var.entId.equalsIgnoreCase(args[2]))
                                {
                                    morphs.remove(i);
                                    found = true;
                                }
                            }
                            if(found)
                            {
                                PlayerMorphHandler.getInstance().savePlayerData(player);
                                morphs = Morph.eventHandlerServer.getPlayerMorphs(player);
                                Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player); //Send the player's morph list to them
                                notifyCommandListener(sender, this, "morph.command.successful", player.getName());
                            }
                            else
                            {
                                notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                            }
                        }
                    }
                    else
                    {
                        throw new WrongUsageException(getUsage(sender));
                    }
                }
                else if(args[0].equalsIgnoreCase("analyse"))
                {
                    if(args.length == 3)
                    {
                        if(args[2].startsWith("player:"))
                        {
                            notifyCommandListener(sender, this, "morph.command.analysePlayer");
                            return;
                        }
                        ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.playerMorphs.get(player.getName());
                        if(morphs != null && !morphs.isEmpty())
                        {
                            for(MorphVariant var : morphs)
                            {
                                if(var.entId.equals(args[2]))
                                {
                                    EntityLivingBase living = var.createEntityInstance(player.getEntityWorld());
                                    if(var.thisVariant.invalid)
                                    {
                                        notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                                        return;
                                    }
                                    notifyCommandListener(sender, this, "morph.command.analyseClass", living.getClass().getName());

                                    TreeMap<String, NBTBase> tags = new TreeMap<>(Ordering.natural());
                                    tags.putAll(var.entTag.tagMap);
                                    TreeMap<String, NBTBase> added = new TreeMap<>(Ordering.natural());
                                    added.putAll(var.thisVariant.variantData.tagMap);
                                    TreeMap<String, NBTBase> removed = new TreeMap<>(Ordering.natural());
                                    for(String s : var.thisVariant.tagsToRemove)
                                    {
                                        removed.put(s, null);
                                    }
                                    for(MorphVariant.Variant variant : var.variants)
                                    {
                                        added.putAll(variant.variantData.tagMap);
                                        for(String s : variant.tagsToRemove)
                                        {
                                            removed.put(s, null);
                                        }
                                    }
                                    tags.putAll(added);
                                    tags.putAll(removed);
                                    tags.remove("Age");
                                    tags.remove("CanPickUpLoot");
                                    tags.remove("HealF");
                                    tags.remove("Health");
                                    tags.remove("Morph_HealthBalancing");
                                    tags.remove("NoAI");
                                    tags.remove("PersistenceRequired");
                                    added.remove("Morph_HealthBalancing");

                                    StringBuilder addedSb = new StringBuilder();
                                    for(Map.Entry<String, NBTBase> tag : tags.entrySet())
                                    {
                                        addedSb.append(tag.getKey());
                                        addedSb.append(", ");
                                        addedSb.append(" (");
                                        addedSb.append(tag.getValue() == null ? "null" : tag.getValue().getClass().getSimpleName().substring(6));
                                        addedSb.append("), ");
                                    }
                                    notifyCommandListener(sender, this, "morph.command.analyseAllTags", addedSb.toString().substring(0, addedSb.toString().length() - 2));

                                    TreeMap<String, NBTBase> allModified = new TreeMap<>(Ordering.natural());
                                    added.keySet().stream().filter(removed.keySet()::contains).forEach((k) -> allModified.put(k, added.get(k)));
                                    removed.keySet().stream().filter(added.keySet()::contains).forEach((k) -> allModified.put(k, added.get(k)));
                                    TreeSet<String> temp = new TreeSet<>(added.keySet());
                                    for(String s : removed.keySet())
                                    {
                                        added.remove(s);
                                    }
                                    for(String s : temp)
                                    {
                                        removed.remove(s);
                                    }

                                    StringBuilder modifiedSb = new StringBuilder();
                                    for(Map.Entry<String, NBTBase> tag : allModified.entrySet())
                                    {
                                        modifiedSb.append("+/-");
                                        modifiedSb.append(tag.getKey());
                                        modifiedSb.append(" (");
                                        modifiedSb.append(tag.getValue() == null ? "null" : tag.getValue().getClass().getSimpleName().substring(6));
                                        modifiedSb.append("), ");
                                    }
                                    for(Map.Entry<String, NBTBase> tag : added.entrySet())
                                    {
                                        modifiedSb.append("+");
                                        modifiedSb.append(tag.getKey());
                                        modifiedSb.append(" (");
                                        modifiedSb.append(tag.getValue() == null ? "null" : tag.getValue().getClass().getSimpleName().substring(6));
                                        modifiedSb.append("), ");
                                    }
                                    for(Map.Entry<String, NBTBase> tag : removed.entrySet())
                                    {
                                        modifiedSb.append("-");
                                        modifiedSb.append(tag.getKey());
                                        modifiedSb.append(" (");
                                        modifiedSb.append(tag.getValue() == null ? "null" : tag.getValue().getClass().getSimpleName().substring(6));
                                        modifiedSb.append("), ");
                                    }
                                    notifyCommandListener(sender, this, "morph.command.analyseVariantTags", modifiedSb.toString().substring(0, modifiedSb.toString().length() - 2));
                                    return;
                                }
                            }
                            notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                        }
                    }
                    else
                    {
                        throw new WrongUsageException(getUsage(sender));
                    }
                }
                else if(args[0].equalsIgnoreCase("clear"))
                {
                    Morph.eventHandlerServer.playerMorphs.remove(player.getName());
                    PlayerMorphHandler.getInstance().savePlayerData(player);
                    ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.getPlayerMorphs(player);
                    Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player); //Send the player's morph list to them
                    notifyCommandListener(sender, this, "morph.command.clearingMorphs", player.getName());
                }
                else if(args[0].equalsIgnoreCase("morph") || args[0].equalsIgnoreCase("give"))
                {
                    EntityLivingBase entToMorphTo = null;
                    if(args.length <= 2) //only arg and name defined, morph to the target
                    {
                        RayTraceResult mop = EntityHelper.getEntityLook(player, 5D);
                        if(mop.typeOfHit == RayTraceResult.Type.ENTITY)
                        {
                            if(mop.entityHit instanceof EntityLivingBase)
                            {
                                entToMorphTo = (EntityLivingBase)mop.entityHit;
                            }
                        }
                    }
                    else if(!args[2].equalsIgnoreCase("*"))
                    {
                        if(args[2].startsWith("player:"))
                        {
                            entToMorphTo = new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), DimensionManager.getWorld(0), EntityHelper.getGameProfile(args[2].substring("player:".length(), args[2].length())), new PlayerInteractionManager(DimensionManager.getWorld(0)));
                        }
                        else
                        {
                            Entity ent = EntityList.createEntityByIDFromName(new ResourceLocation(args[2]), player.getEntityWorld());
                            if(ent instanceof EntityLivingBase)
                            {
                                entToMorphTo = (EntityLivingBase)ent;
                            }
                        }
                    }
                    else if(args[0].equalsIgnoreCase("give"))
                    {
                        notifyCommandListener(sender, this, "...", player.getName());
                        boolean newMorph = false;
                        for(EntityEntry entry : ForgeRegistries.ENTITIES.getValues())
                        {
                            if(EntityLivingBase.class.isAssignableFrom(entry.getEntityClass()))
                            {
                                for(int i = 0; i < 100; i++)
                                {
                                    EntityLivingBase living = (EntityLivingBase)entry.newInstance(player.world);
                                    if(living != null)
                                    {
                                        if (living instanceof EntityLiving)
                                        {
                                            EntityLiving entityliving = (EntityLiving)living;
                                            entityliving.onInitialSpawn(player.world.getDifficultyForLocation(new BlockPos(entityliving)), (IEntityLivingData)null);
                                        }

                                        if(Morph.config.childMorphs == 0 && living.isChild() || Morph.config.playerMorphs == 0 && living instanceof EntityPlayer || Morph.config.bossMorphs == 0 && !living.isNonBoss() || player.getClass() == FakePlayer.class || player.connection == null)
                                        {
                                            continue;
                                        }
                                        if(!PlayerMorphHandler.isEntityMorphableConfig(living)) //is the mob in a blackwhitelist?
                                        {
                                            continue;
                                        }

                                        if(MinecraftForge.EVENT_BUS.post(new MorphAcquiredEvent(player, living)))
                                        {
                                            //Event was cancelled.
                                            continue;
                                        }

                                        MorphVariant variant = MorphVariant.createVariant(living);
                                        if(variant == null) //Variant could not be created.
                                        {
                                            continue;
                                        }

                                        ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.playerMorphs.get(player.getName());
                                        int updatePlayer = -2;
                                        boolean donotadd = false;
                                        for(MorphVariant var : morphs)
                                        {
                                            if(variant.entId.equals(MorphVariant.PLAYER_MORPH_ID)) //Special case players first
                                            {
                                                if(var.entId.equals(MorphVariant.PLAYER_MORPH_ID) && variant.playerName.equals(var.playerName))
                                                {
                                                    donotadd = true;
                                                }
                                            }
                                            else if(variant.entId.equals(var.entId)) //non-player variants
                                            {
                                                updatePlayer = MorphVariant.combineVariants(var, variant);
                                                if(updatePlayer == -2) //failed to merge for reasons. Return false acquisition.
                                                {
                                                    donotadd = true;
                                                }
                                                else
                                                {
                                                    //The variant should be a new variant so it'll be the latest entry in the variants list.
                                                    variant = var.createWithVariant(updatePlayer == -1 ? var.thisVariant : var.variants.get(updatePlayer));
                                                    newMorph = true;
                                                }
                                                break;
                                            }
                                        }

                                        if(updatePlayer == -2 && !donotadd) //No preexisting variant exists.
                                        {
                                            morphs.add(variant);
                                            newMorph = true;
                                        }
                                        Collections.sort(morphs);
                                    }
                                }
                            }
                        }
                        if(newMorph)
                        {
                            ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.getPlayerMorphs(player);
                            Morph.channel.sendTo(new PacketUpdateMorphList(true, morphs.toArray(new MorphVariant[morphs.size()])), player);
                            PlayerMorphHandler.getInstance().savePlayerData(player);
                            notifyCommandListener(sender, this, "morph.command.successful", player.getName());
                        }
                        else
                        {
                            notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                        }
                        return;
                    }
                    if(entToMorphTo != null)
                    {
                        if(args[0].equalsIgnoreCase("morph") && PlayerMorphHandler.getInstance().forceMorph(player, entToMorphTo) || args[0].equalsIgnoreCase("give") && PlayerMorphHandler.getInstance().acquireMorph(player, entToMorphTo, false, false))
                        {
                            notifyCommandListener(sender, this, "morph.command.successful", player.getName());
                        }
                        else
                        {
                            notifyCommandListener(sender, this, "morph.command.unsuccessful", player.getName());
                        }
                    }
                    else
                    {
                        notifyCommandListener(sender, this, "morph.command.cannotFindEntity");
                    }
                }
                else
                {
                    throw new WrongUsageException(getUsage(sender));
                }
            }
        }
        else
        {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if(entityNames.isEmpty())
        {
            for(EntityEntry entry : ForgeRegistries.ENTITIES.getValues())
            {
                if(EntityLivingBase.class.isAssignableFrom(entry.getEntityClass()))
                {
                    entityNames.add(ForgeRegistries.ENTITIES.getKey(entry).toString());
                }
            }
        }
        ArrayList<String> entityNamesWithPlayers = new ArrayList<>(entityNames);
        if(args.length >= 2 && args[0].equalsIgnoreCase("give"))
        {
            for(int i = 0; i < server.getOnlinePlayerNames().length; i++)
            {
                if(!args[1].equals(server.getOnlinePlayerNames()[i]))
                {
                    entityNamesWithPlayers.add("player:" + server.getOnlinePlayerNames()[i]);
                }
            }
        }
        if(args.length == 3 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("analyse")))
        {
            EntityPlayerMP player;
            try
            {
                player = getPlayer(server, sender, args[1]);
            }
            catch(CommandException e)
            {
                player = null;
            }
            if(player != null)
            {
                ArrayList<MorphVariant> morphs = Morph.eventHandlerServer.playerMorphs.get(player.getName());
                if(morphs != null && !morphs.isEmpty())
                {
                    ArrayList<String> names = new ArrayList<>();
                    for(MorphVariant var : morphs)
                    {
                        if(!(var.entId.equals(MorphVariant.PLAYER_MORPH_ID) && player.getName().equals(var.playerName)))
                        {
                            if(var.entId.equals(MorphVariant.PLAYER_MORPH_ID))
                            {
                                names.add("player:" + var.playerName);
                            }
                            else
                            {
                                names.add(var.entId);
                            }
                        }
                    }
                    return getListOfStringsMatchingLastWord(args, names);
                }
            }
        }
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "analyse", "demorph", "clean", "remove", "clear", "morph", "give", "help") : args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : args.length == 3 ? args[0].equalsIgnoreCase("demorph") || args[0].equalsIgnoreCase("clear") ? getListOfStringsMatchingLastWord(args, "true") : args[0].equalsIgnoreCase("morph") || args[0].equalsIgnoreCase("give") ? getListOfStringsMatchingLastWord(args, entityNamesWithPlayers) : getListOfStringsMatchingLastWord(args, "") : getListOfStringsMatchingLastWord(args, "");
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return args.length > 0 && !args[0].equalsIgnoreCase("help") && index == 1;
    }
}
