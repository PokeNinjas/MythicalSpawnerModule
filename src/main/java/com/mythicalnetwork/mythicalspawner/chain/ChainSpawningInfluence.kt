package com.mythicalnetwork.mythicalspawner.chain

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import net.minecraft.world.entity.Entity
import java.util.UUID

class ChainSpawningInfluence(var player: UUID) : SpawningInfluence {

    override fun affectBucketWeight(bucket: SpawnBucket, weight: Float): Float {
        if(bucket.name.contains("rare")){
            MythicalSpawner.CHAIN_MANAGER?.let { chainManager ->
                val chainData = chainManager.getChainData(this.player)
                MythicalSpawner.LOGGER.info("Testing chain data for bucket weight!")
                if (chainData != null) {
                    MythicalSpawner.LOGGER.info("Bucket: ${bucket.name} Chain data is not null! Weight: ${weight * chainData.getChainMultiplier()}")
                    return weight * chainData.getChainMultiplier()
                } else {
                    return weight
                }
            }
        }
        return weight
    }

    override fun affectSpawn(entity: Entity) {
        if(entity is PokemonEntity){
            MythicalSpawner.LOGGER.info("Testing chain data to spawn ${entity.pokemon.species.name.toString()}!")
        }
        super.affectSpawn(entity)
    }
}