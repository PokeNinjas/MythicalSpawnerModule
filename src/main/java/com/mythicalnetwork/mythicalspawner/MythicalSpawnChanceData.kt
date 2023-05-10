package com.mythicalnetwork.mythicalspawner

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.state.BlockState

class MythicalSpawnChanceData(
    var species: List<String>,
    var minTime: Int,
    var maxTime: Int,
    var minLevel: Int,
    var maxLevel: Int
) {
    companion object {
        var FORCED_LAST_SPAWN = false
        var CURRENT_TIMER = 0
        var CHOSEN_DATA: MythicalSpawnChanceData? = null
        val SPAWN_DATA: MutableMap<String, MythicalSpawnChanceData> = mutableMapOf()

        val CODEC: Codec<MythicalSpawnChanceData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.listOf().fieldOf("species").forGetter { it.species },
                Codec.INT.fieldOf("minTime").forGetter { it.minTime },
                Codec.INT.fieldOf("maxTime").forGetter { it.maxTime },
                Codec.INT.fieldOf("minLevel").forGetter { it.minLevel },
                Codec.INT.fieldOf("maxLevel").forGetter { it.maxLevel }
            ).apply(instance, ::MythicalSpawnChanceData)
        }

        fun tick(server: MinecraftServer) {
            if(SPAWN_DATA.isEmpty()) return
//            MythicalSpawner.LOGGER.info("Ticking Mythical Spawn Chance Data")
            if (CURRENT_TIMER == 0) {
                activateData()
                if (CHOSEN_DATA == null) return
                if (server.playerList.players.isEmpty()) return
                val randomPlayerEntity = server.playerList.players.random()
                val randomSpecies = Pokemon()
                randomSpecies.species =
                    (PokemonSpecies.getByName(CHOSEN_DATA!!.species.random()) ?: PokemonSpecies.getByName("ditto"))!!
                randomSpecies.level = (CHOSEN_DATA!!.minLevel..CHOSEN_DATA!!.maxLevel).random()
                val pokemonEntity = PokemonEntity(randomPlayerEntity.level, randomSpecies)
                val randomPosition = calculateSafeSpawnPosition(randomPlayerEntity)
                pokemonEntity.setPos(
                    randomPosition.x.toDouble(),
                    randomPosition.y.toDouble(),
                    randomPosition.z.toDouble()
                )
                pokemonEntity.addTag("forced_spawn")
                pokemonEntity.addTag("pokespawner:modified")
                SpawnHandler.handleCustomSpawns(pokemonEntity, pokemonEntity.pokemon)
                randomPlayerEntity.level.addFreshEntity(pokemonEntity)
                FORCED_LAST_SPAWN = true
                MythicalSpawner.LOGGER.info("Spawned ${randomSpecies.species.name} at $randomPosition with aspects: ${pokemonEntity.pokemon.aspects}")
            } else {
                CURRENT_TIMER--
                FORCED_LAST_SPAWN = false
            }
            FORCED_LAST_SPAWN = false
        }

        private fun calculateSafeSpawnPosition(playerEntity: Player): BlockPos {
            val closest: BlockPos = BlockPos.findClosestMatch(playerEntity.onPos, 32, 32) { blockPos: BlockPos ->
                val distance = playerEntity.onPos.distManhattan(blockPos)
                val blockstate: BlockState = playerEntity.level.getChunkAt(blockPos).getBlockState(blockPos)
                val blockstateUp = playerEntity.level.getChunkAt(blockPos.above()).getBlockState(blockPos.above())
                val blockstateDown = playerEntity.level.getChunkAt(blockPos.below()).getBlockState(blockPos.below())
                val isAir: Boolean = blockstate.isAir
                val isAirUp: Boolean = blockstateUp.isAir
                val isSolidDown: Boolean = blockstateDown.isRedstoneConductor(playerEntity.level, blockPos.below())
                val isTooClose: Boolean = distance < 4
                val isTooFar: Boolean = distance > 10
                isAir && isAirUp && isSolidDown && !isTooClose && !isTooFar
            }.orElse(playerEntity.onPos)
            return closest
        }

        private fun activateData() {
            CHOSEN_DATA = (!SPAWN_DATA.values.isEmpty()).let {
                SPAWN_DATA.values.random()
            } ?: return
            CURRENT_TIMER = ((CHOSEN_DATA?.minTime?.rangeTo(CHOSEN_DATA!!.maxTime))?.random() ?: 0) * 20 * 60
        }
    }
}