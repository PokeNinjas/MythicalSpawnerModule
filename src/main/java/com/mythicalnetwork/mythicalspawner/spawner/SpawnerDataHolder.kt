package com.mythicalnetwork.mythicalspawner.spawner

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mythicalnetwork.mythicalspawner.BLOCKPOS_PAIR_CODEC
import com.mythicalnetwork.mythicalspawner.INT_PAIR_CODEC
import net.minecraft.core.BlockPos
import java.util.*

class SpawnerDataHolder(
    val species: String,
    val aspects: List<AspectChanceData>,
    val area: Optional<Pair<BlockPos, BlockPos>>,
    val dimension: Optional<String>, val biome: Optional<String>, val time: Optional<Pair<Int, Int>>,
    val weather: Optional<String>
) {

    override fun toString(): String {
        return "PokespawnerDataHolder(species=$species, aspects=$aspects, area=$area, dimension=$dimension, biome=$biome, time=$time, weather=$weather)"
    }
    companion object {
        val SPAWN_DATA:  MutableMap<String, SpawnerDataHolder> = mutableMapOf()
        val ACTIVE_DATA: MutableMap<String, SpawnerDataHolder> = mutableMapOf()
        val CODEC: Codec<SpawnerDataHolder> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("species").forGetter { it.species },
                AspectChanceData.CODEC.listOf().fieldOf("aspects").forGetter { it.aspects },
                BLOCKPOS_PAIR_CODEC.optionalFieldOf("area").forGetter { it.area },
                Codec.STRING.optionalFieldOf("dimension").forGetter { it.dimension },
                Codec.STRING.optionalFieldOf("biome").forGetter { it.biome },
                INT_PAIR_CODEC.optionalFieldOf("time").forGetter { it.time },
                Codec.STRING.optionalFieldOf("weather").forGetter { it.weather }
            ).apply(instance) { species, aspects, area, dimension, biome, time, weather ->
                SpawnerDataHolder(
                    species,
                    aspects,
                    area,
                    dimension,
                    biome,
                    time,
                    weather
                )
            }
        }
        fun getSpeciesData(species: String): List<SpawnerDataHolder> {
            return ACTIVE_DATA.filter { it.value.species == species }.values.toList()
        }

        fun activateDataHolder(id: String){
            val data = SPAWN_DATA[id]
            if (data != null){
                ACTIVE_DATA[id] = data
            }
        }

        fun deactivateDataHolder(id: String){
            ACTIVE_DATA.remove(id)
        }
    }
}