package com.mythicalnetwork.mythicalspawner.chain

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.StatProvider
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.CobblemonWorldSpawnerManager
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.PokemonStats
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import com.mythicalnetwork.mythicalspawner.checkSpawnConditions
import com.mythicalnetwork.mythicalspawner.events.SpawnHandler
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import eu.pb4.placeholders.api.node.TextNode
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import java.util.Date
import java.util.HashMap
import java.util.UUID

class ChainUser(private val player: UUID, private var chain: Int = 0, private var chainedPokemon: String = "") {
    private var startTime: Date = Date()
    private var endTime: Date =
        (startTime.clone() as Date).also { it.hours += MythicalSpawner.CONFIG.chainTimeoutHours() }
    private var lastShinyAttempt: Date? = null
    private var chainedPokemonPrettyName: String = ""
    var toRemove: Boolean = false

    companion object {
        fun onComplete(action: (ChainUser) -> Unit, chainUser: ChainUser) {
            action(chainUser)
        }
    }

    fun setPrettyName(string: String) {
        chainedPokemonPrettyName = string
    }

    fun tick(level: ServerLevel) {
        if (Date().after(endTime)) {
            toRemove = true
            return
        }
        if (lastShinyAttempt == null) {
            lastShinyAttempt =
                (startTime.clone() as Date).also { it.seconds += MythicalSpawner.CONFIG.shinySpawnAttemptDelay() }
        }
        if (Date().after(lastShinyAttempt)) {
            lastShinyAttempt =
                (Date().clone() as Date).also { it.seconds += MythicalSpawner.CONFIG.shinySpawnAttemptDelay() }
            for (range in ChainManager.SHINY_RATES.keys) {
                if (chain in range) {
                    val rand = Math.random().toFloat()
                    var chainRange = ChainManager.SHINY_RATES[range]
                    if (chainRange == null) chainRange = 0.0f
                    val comp = rand <= 1 / chainRange
                    if (comp) {
                        // spawn shiny
                        val player: Player? = level.getPlayerByUUID(player)
                        if(player == null) {
                            toRemove = true
                            return
                        }
                        val pokemon: Pokemon =
                            PokemonSpecies.getByName(chainedPokemon.toLowerCase().replace(" ", ""))!!.create()
//                        CobblemonSpawnPools.WORLD_SPAWN_POOL.details.stream().map { s -> s is PokemonSpawnDetail }.toList().stream().map { ps -> (ps as PokemonSpawnDetail).conditions.forEach { c -> c.biomes.filter { b -> b. } } }
//                        CobblemonWorldSpawnerManager.spawnersForPlayers[this.player]?.influences?.forEach { influence ->
//                            influence.
//                        }
                        pokemon.shiny = true
                        val pokemonEntity: PokemonEntity = PokemonEntity(level, pokemon)
                        val spawnPos: BlockPos? =
                            checkSpawnConditions(pokemonEntity, false, false, true, player.onPos, level)
                        if (spawnPos != null) {
                            pokemonEntity.setPos(spawnPos.x.toDouble(), spawnPos.y.toDouble(), spawnPos.z.toDouble())
                            handleStatIncrease(pokemonEntity.pokemon)
                            level.addFreshEntity(pokemonEntity)
                        }
                    }
                }
            }
        }
    }

    fun handleCapture(pokemon: Pokemon) {
        if (pokemon.species.resourceIdentifier.path == chainedPokemon && chain < MythicalSpawner.CONFIG.chainLengthCap()) {
            incrementChain()
            handleStatIncrease(pokemon)
        } else {
            newChain(pokemon.species.resourceIdentifier.path)
        }
    }

    // modify stats of pokemon on capture
    fun handleStatIncrease(pokemon: Pokemon) {
        for (range in ChainManager.IV_RATES.keys) {
            if (chain in range) {
                pokemon.ivs = Cobblemon.statProvider.createEmptyIVs(ChainManager.IV_RATES[range]!!)
            }
        }
    }


    fun incrementChain() {
        chain++
        endTime.hours += MythicalSpawner.CONFIG.chainTimeoutHours()
        var message: TextNode? = TextParserUtils.formatNodes(MythicalSpawner.CONFIG.chainIncreaseMessage())
        val placeholderMap: HashMap<String, Component> = HashMap()
        placeholderMap["player"] = MythicalSpawner.CHAIN_MANAGER?.LEVEL?.getPlayerByUUID(player)?.name?.string?.let {
            Component.literal(
                it
            )
        } ?: Component.nullToEmpty(null)
        placeholderMap["pokemon"] = Component.literal(chainedPokemonPrettyName)
        placeholderMap["chain"] = Component.literal(chain.toString())
        message = Placeholders.parseNodes(message, Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM, placeholderMap)
        val component: Component =
            message.toText(PlaceholderContext.of(MythicalSpawner.CHAIN_MANAGER?.LEVEL?.server).asParserContext(), true)
        if (message != null) {
            MythicalSpawner.CHAIN_MANAGER?.LEVEL?.getPlayerByUUID(player)?.sendSystemMessage(component)
        }
    }

    fun resetChain() {
        chain = 0
    }

    private fun newChain(pokemon: String) {
        chain = 1
        chainedPokemon = pokemon
        startTime = Date()
        endTime = (startTime.clone() as Date).also { it.hours += MythicalSpawner.CONFIG.chainTimeoutHours() }
    }

    fun setChainedPokemon(pokemon: String) {
        chainedPokemon = pokemon
    }

    fun getChainedPokemon(): String {
        return chainedPokemon
    }

    fun getChain(): Int {
        return chain
    }

    fun getPlayer(): UUID {
        return player
    }

    fun setChain(chain: Int) {
        this.chain = chain
    }
}