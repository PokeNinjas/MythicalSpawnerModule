package com.mythicalnetwork.mythicalspawner

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import dev.architectury.event.events.common.CommandRegistrationEvent
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import me.lucko.fabric.api.permissions.v0.Permissions
import net.luckperms.api.LuckPermsProvider
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player

class MythicalSpawnerCommands {
    companion object {
        fun registerCommands() {
            CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
                val stack: LiteralArgumentBuilder<CommandSourceStack> = LiteralArgumentBuilder.literal("mythicalspawner")
                stack.then(MythicalSpawnerActivateCommand.register(dispatcher))
                    .then(MythicalSpawnerDeactivateCommand.register(dispatcher))
                dispatcher.register(stack)
                val lastLegend: LiteralArgumentBuilder<CommandSourceStack> = LiteralArgumentBuilder.literal("lastlegend")
                lastLegend.requires {
                    Permissions.check(it, "mythicalspawner.lastlegend")
                }.executes { context ->
                    val player: String = MythicalSpawner.LAST_CAPTURED_TRAINER?.name?.string ?: "No one"
                    if(player == "No one"){
                        val message: Component? = Placeholders.parseText(Component.literal(MythicalSpawner.CONFIG.lastLegendNotCaptureMessage()), PlaceholderContext.of(context.source.player))
                        if (message != null) {
                            context.source.sendSuccess(message, false)
                        } else {
                            context.source.sendFailure(Component.literal("The message is formatted incorrectly."))
                        }
                    } else {
                        val message: Component? = Placeholders.parseText(Component.literal(MythicalSpawner.CONFIG.lastLegendCapturedMessage()), PlaceholderContext.of(context.source.player))
                        if (message != null) {
                            context.source.sendSuccess(message, false)
                        } else {
                            context.source.sendFailure(Component.literal("The message is formatted incorrectly."))
                        }
                    }
                    return@executes 1
                }
            }
        }

        object MythicalSpawnerActivateCommand : Command<CommandSourceStack> {
            private val CMD: MythicalSpawnerActivateCommand = MythicalSpawnerActivateCommand
            private var SUGGESTION_PROVIDER: SuggestionProvider<CommandSourceStack> =
                SuggestionProvider { context, builder ->
                    return@SuggestionProvider SharedSuggestionProvider.suggest(PokespawnerDataHolder.SPAWN_DATA.keys, builder)
                }

            fun register(dispatcher: CommandDispatcher<CommandSourceStack>): ArgumentBuilder<CommandSourceStack, *> {
                val stack: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("activate").executes(CMD)
                stack.then(
                    Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGESTION_PROVIDER)
                        .requires {
                            Permissions.check(it, "mythicalspawner.activatespawner")
                        }.executes { context ->
                            val name = StringArgumentType.getString(context, "name")
                            val data: PokespawnerDataHolder? = PokespawnerDataHolder.SPAWN_DATA.getOrDefault(name, null)
                            if (data == null) {
                                context.source.sendFailure(
                                    net.minecraft.network.chat.Component.literal("No object found with that name!").withStyle { s -> s.withColor(ChatFormatting.RED) })
                                return@executes 0
                            }
                            PokespawnerDataHolder.activateDataHolder(name)
                            context.source.sendSuccess(
                                net.minecraft.network.chat.Component.literal("[MythicalSpawner - Activated $name]")
                                    .withStyle { s -> s.withColor(ChatFormatting.GREEN) }, true
                            )
                            return@executes 1
                        })
                return stack
            }

            override fun run(context: CommandContext<CommandSourceStack>?): Int {
                context!!.source.sendSuccess(
                    net.minecraft.network.chat.Component.literal("[MythicalSpawner - Deactivated]").withStyle { s -> s.withColor(ChatFormatting.GOLD) }, true
                )
                return 1
            }

        }

        object MythicalSpawnerDeactivateCommand : Command<CommandSourceStack> {
            private val CMD: MythicalSpawnerDeactivateCommand = MythicalSpawnerDeactivateCommand
            private var SUGGESTION_PROVIDER: SuggestionProvider<CommandSourceStack> =
                SuggestionProvider { _, builder ->
                    return@SuggestionProvider SharedSuggestionProvider.suggest(PokespawnerDataHolder.ACTIVE_DATA.keys, builder)
                }

            fun register(dispatcher: CommandDispatcher<CommandSourceStack>): ArgumentBuilder<CommandSourceStack, *> {
                val stack: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("deactivate").executes(CMD)
                stack.then(
                    Commands.argument("name", StringArgumentType.greedyString()).suggests(SUGGESTION_PROVIDER)
                        .requires {
                            Permissions.check(it, "mythicalspawner.deactivatespawner")
                        }.executes { context ->
                            val name = StringArgumentType.getString(context, "name")
                            val data: PokespawnerDataHolder? = PokespawnerDataHolder.SPAWN_DATA.getOrDefault(name, null)
                            if (data == null) {
                                context.source.sendFailure(
                                    net.minecraft.network.chat.Component.literal("No object found with that name!").withStyle { s -> s.withColor(ChatFormatting.RED) })
                                return@executes 0
                            }
                            PokespawnerDataHolder.deactivateDataHolder(name)
                            context.source.sendSuccess(
                                net.minecraft.network.chat.Component.literal("[MythicalSpawner - Deactivated $name]")
                                    .withStyle { s -> s.withColor(ChatFormatting.RED) }, true
                            )
                            return@executes 1
                        })
                return stack
            }

            override fun run(context: CommandContext<CommandSourceStack>?): Int {
                context!!.source.sendSuccess(
                    net.minecraft.network.chat.Component.literal("[MythicalSpawner - No option selected]").withStyle { s -> s.withColor(ChatFormatting.GOLD) },
                    true
                )
                return 1
            }

        }
    }
}