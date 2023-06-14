package com.mythicalnetwork.mythicalspawner

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.logging.LogUtils
import com.mythicalnetwork.mythicalspawner.chain.ChainManager
import com.mythicalnetwork.mythicalspawner.chain.ChainSpawningInfluence
import com.mythicalnetwork.mythicalspawner.events.ServerEvents
import com.mythicalnetwork.mythicalspawner.events.SpawnHandler
import com.mythicalnetwork.mythicalspawner.force.MythicalSpawnChanceData
import com.mythicalnetwork.mythicalspawner.force.MythicalSpawnChanceListener
import com.mythicalnetwork.mythicalspawner.spawner.DefaultSpawnDataListener
import com.mythicalnetwork.mythicalspawner.spawner.MythicalSpawnerAbilityListener
import com.mythicalnetwork.mythicalspawner.spawner.MythicalSpawnerJsonListener
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.PlayerEvent.PlayerJoin
import dev.architectury.event.events.common.TickEvent
import dev.architectury.registry.ReloadListenerRegistry
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.player.Player
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents
import java.util.*


/**
 * With Kotlin, the Entrypoint can be defined in numerous ways. This is showcased on Fabrics' Github:
 * https://github.com/FabricMC/fabric-language-kotlin#entrypoint-samples
 */
class MythicalSpawner : ModInitializer {

    companion object {
        const val MODID = "mythicalspawner"
        val LOGGER = LogUtils.getLogger()
        var TIME_SINCE_LEGENDARY_SPAWN = 0
        var LAST_LEGENDARY_SPAWNED: Pokemon? = null
        var LAST_CAPTURED_TRAINER: Player? = null
        var LAST_CAPTURED_BIOME: String? = null
        val CONFIG: MythicalSpawnerConfig = MythicalSpawnerConfig.createAndLoad()
        var CHAIN_MANAGER: ChainManager? = null
    }

    override fun onInitialize(mod: ModContainer?) {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, MythicalSpawnerJsonListener.INSTANCE)
        ReloadListenerRegistry.register(PackType.SERVER_DATA, MythicalSpawnerAbilityListener.INSTANCE)
        ReloadListenerRegistry.register(PackType.SERVER_DATA, DefaultSpawnDataListener.INSTANCE)
        ReloadListenerRegistry.register(PackType.SERVER_DATA, MythicalSpawnChanceListener.INSTANCE)
        LOGGER.info("MythicalSpawner - Initialized")
        ServerLifecycleEvents.READY.register { server ->
            CHAIN_MANAGER = ChainManager(server.overworld())
        }
        EntityEvent.ADD.register { entity, world ->
            if (world.isClientSide) return@register EventResult.pass()
            if (entity is PokemonEntity) {
                SpawnHandler.onSpawn(entity)
                return@register EventResult.pass()
            }
            return@register EventResult.pass()
        }

        TickEvent.ServerLevelTick.SERVER_POST.register { server ->
            MythicalSpawnChanceData.tick(server)
            TIME_SINCE_LEGENDARY_SPAWN++
            CHAIN_MANAGER?.tick(server.overworld())
        }

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            PlayerSpawnerFactory.influenceBuilders.add { player -> ChainSpawningInfluence(player.uuid)}
        }

        ServerEvents.init()

        CobblemonEvents.POKEMON_CAPTURED.subscribe { event: PokemonCapturedEvent ->
            val pokemon: Pokemon = event.pokemon
            if (LAST_LEGENDARY_SPAWNED != null) {
                if (pokemon == LAST_LEGENDARY_SPAWNED) {
                    LAST_CAPTURED_TRAINER = event.player
                    LAST_CAPTURED_BIOME = Component.translatable(
                        Util.makeDescriptionId(
                            "biome",
                            event.player.level.getBiome(event.player.onPos).unwrapKey().get().location()
                        )
                    ).string

                }
            }
            CHAIN_MANAGER?.getChainData(event.player.uuid)?.let { chain ->
                if(chain.getChainedPokemon() == pokemon.species.resourceIdentifier.path){
                    chain.handleCapture(event.pokemon)
                } else {
                    chain.resetChain()
                    chain.setChainedPokemon(pokemon.species.resourceIdentifier.path)
                    chain.setPrettyName(pokemon.species.name.toString())
                }
            } ?: CHAIN_MANAGER?.addChain(event.player.uuid).also { chain ->
                chain?.setChainedPokemon(pokemon.species.resourceIdentifier.path)
                chain?.setPrettyName(pokemon.species.name.toString())
            }
        }
        setupPlaceholders()
        MythicalSpawnerCommands.registerCommands()
    }

    fun setupPlaceholders() {
        Placeholders.register(ResourceLocation("spawner", "last_captured_trainer")) { ctx, _ ->
            LAST_CAPTURED_TRAINER?.let {
                PlaceholderResult.value(Component.literal(it.name.string))
            } ?: PlaceholderResult.invalid("No trainer has captured a legendary yet!")
        }
        Placeholders.register(ResourceLocation("spawner", "last_captured_biome")) { ctx, _ ->
            if (LAST_CAPTURED_BIOME != null) {
                PlaceholderResult.value(Component.literal(LAST_CAPTURED_BIOME!!))
            } else {
                PlaceholderResult.invalid("No trainer has captured a legendary yet!")
            }
        }
        Placeholders.register(ResourceLocation("spawner", "last_captured_pokemon")) { ctx, _ ->
            if (LAST_LEGENDARY_SPAWNED != null) {
                PlaceholderResult.value(Component.literal(LAST_LEGENDARY_SPAWNED!!.species.name))
            } else {
                PlaceholderResult.invalid("No trainer has captured a legendary yet!")
            }

        }
        Placeholders.register(ResourceLocation("spawner", "time_since_last_spawn")) { _, _ ->
            PlaceholderResult.value(Component.literal(TIME_SINCE_LEGENDARY_SPAWN.toString()))

        }
    }


}