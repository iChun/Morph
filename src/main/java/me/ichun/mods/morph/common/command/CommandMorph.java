package me.ichun.mods.morph.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class CommandMorph
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("morph")
                .then(Commands.literal("resources")
                        .then(Commands.literal("reload")
                                .executes(context -> ResourceHandler.reloadAllResources())
                        )
                )
                .requires(p -> p.hasPermissionLevel(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("morph")
                                .then(Commands.literal("acquire")
                                        .then(Commands.literal("entity")
                                                .then(Commands.argument("target", EntityArgument.entity())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), EntityArgument.getEntity(context, "target"), true))
                                                )
                                        )
                                        .then(Commands.literal("type")
                                                .then(Commands.argument("entity_type", EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), EntitySummonArgument.getEntityId(context, "entity_type"), true))
                                                )
                                        )
                                        .then(Commands.literal("player")
                                                .then(Commands.argument("uuid", UUIDArgument.func_239194_a_())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), UUIDArgument.func_239195_a_(context, "uuid"), true))
                                                )
                                                .then(Commands.argument("name", StringArgumentType.word())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "name"), true))
                                                )
                                        )
                                )
                                .then(Commands.literal("morph")
                                        .then(Commands.literal("entity")
                                                .then(Commands.argument("target", EntityArgument.entity())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), EntityArgument.getEntity(context, "target"), false))
                                                )
                                        )
                                        .then(Commands.literal("type")
                                                .then(Commands.argument("entity_type", EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), EntitySummonArgument.getEntityId(context, "entity_type"), false))
                                                )
                                        )
                                        .then(Commands.literal("player")
                                                .then(Commands.argument("uuid", UUIDArgument.func_239194_a_())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), UUIDArgument.func_239195_a_(context, "uuid"), false))
                                                )
                                                .then(Commands.argument("name", StringArgumentType.word())
                                                        .executes(context -> createMorph(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "name"), false))
                                                )
                                        )
                                )
                                .then(Commands.literal("demorph")
                                        .executes(context -> demorphPlayer(context.getSource(), EntityArgument.getPlayer(context, "player")))
                                )
                                .then(Commands.literal("clean")
                                        //TODO runs the NBT modifies on all morphs for the player
                                )
                                //TODO remove or clear args??
                        )
                        .then(Commands.literal("biomass")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(context -> setBiomass(context.getSource(), EntityArgument.getPlayer(context, "player"), DoubleArgumentType.getDouble(context, "value")))
                                        )
                                )
                        )
                )
        );
        //TODO reextract
    }

    private static int setBiomass(CommandSource source, ServerPlayerEntity player, double value)
    {
        MorphHandler.INSTANCE.setBiomassAmount(player, value);
        return Command.SINGLE_SUCCESS;
    }

    //TODO these
    private static int createMorph(CommandSource source, ServerPlayerEntity player, Entity entity, boolean isAcquire)
    {
        return Command.SINGLE_SUCCESS;
    }

    private static int createMorph(CommandSource source, ServerPlayerEntity player, ResourceLocation type, boolean isAcquire)
    {
        return Command.SINGLE_SUCCESS;
    }

    private static int createMorph(CommandSource source, ServerPlayerEntity player, String name, boolean isAcquire)
    {
        return Command.SINGLE_SUCCESS;
    }

    private static int createMorph(CommandSource source, ServerPlayerEntity player, UUID uuid, boolean isAcquire)
    {
        return Command.SINGLE_SUCCESS;
    }

    private static int demorphPlayer(CommandSource source, ServerPlayerEntity player)
    {
        return Command.SINGLE_SUCCESS;
    }

}