package com.mythicalnetwork.mythicalspawner

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.block.Blocks

val SPECIES_CODEC: Codec<Species> = Codec.STRING.comapFlatMap(
    {s ->
        DataResult.success(PokemonSpecies.getByName(s))
    },
    {s -> s.name}
)

val STRING_PAIR_CODEC: Codec<Pair<String,String>> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.STRING.fieldOf("x").forGetter { it.first },
        Codec.STRING.fieldOf("y").forGetter { it.second }
    ).apply(instance, ::Pair)
}

val FLOAT_PAIR_CODEC: Codec<Pair<Float,Float>> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.FLOAT.fieldOf("x").forGetter { it.first },
        Codec.FLOAT.fieldOf("y").forGetter { it.second }
    ).apply(instance, ::Pair)
}

val INT_PAIR_CODEC: Codec<Pair<Int,Int>> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.INT.fieldOf("start").forGetter { it.first },
        Codec.INT.fieldOf("end").forGetter { it.second }
    ).apply(instance, ::Pair)
}

val BLOCKPOS_PAIR_CODEC: Codec<Pair<BlockPos, BlockPos>> = RecordCodecBuilder.create { instance ->
    instance.group(
        BlockPos.CODEC.fieldOf("corner_1").forGetter { it.first },
        BlockPos.CODEC.fieldOf("corner_2").forGetter { it.second }
    ).apply(instance, ::Pair)
}

fun formatShinyCatchComboRates(): Map<IntRange, Float> {
    val rates: String = MythicalSpawner.CONFIG.shinyCatchComboRates()
    val rateSubString = rates.split(",")
    val rateMap: MutableMap<IntRange, Float> = mutableMapOf()
    for (rate in rateSubString) {
        rate.replace(" ", "")
        val rateSplit = rate.split(":")
        val rateRange = rateSplit[0].replace(" ", "").split("-")
        val rateStart = rateRange[0].replace(" ", "").toInt()
        val rateEnd = rateRange[1].replace(" ", "").toInt()
        val rateValue = rateSplit[1].replace(" ", "").toFloat()
        rateMap[rateStart..rateEnd] = rateValue
    }
    return rateMap
}

fun formatIvRangeValues(): Map<IntRange, Int> {
    val ivs: String = MythicalSpawner.CONFIG.ivRangeValues()
    val ivSubString = ivs.split(",")
    val ivMap: MutableMap<IntRange, Int> = mutableMapOf()
    for (iv in ivSubString) {
        iv.replace(" ", "")
        val ivSplit = iv.split(":")
        val ivRange = ivSplit[0].replace(" ", "").split("-")
        val ivStart = ivRange[0].replace(" ", "").toInt()
        val ivEnd = ivRange[1].replace(" ", "").toInt()
        val ivValue = ivSplit[1].replace(" ", "").toInt()
        ivMap[ivStart..ivEnd] = ivValue
    }
    return ivMap
}

fun formatMultiplierRates(): Map<IntRange, Float> {
    val rates: String = MythicalSpawner.CONFIG.catchComboRareSpawnRates()
    val rateSubString = rates.split(",")
    val rateMap: MutableMap<IntRange, Float> = mutableMapOf()
    for (rate in rateSubString) {
        rate.replace(" ", "")
        val rateSplit = rate.split(":")
        val rateRange = rateSplit[0].replace(" ", "").split("-")
        val rateStart = rateRange[0].replace(" ", "").toInt()
        val rateEnd = rateRange[1].replace(" ", "").toInt()
        val rateValue = rateSplit[1].replace(" ", "").toFloat()
        rateMap[rateStart..rateEnd] = rateValue
    }
    return rateMap
}

fun checkSpawnConditions(
    pokemon: PokemonEntity,
    canSwim: Boolean,
    canFly: Boolean,
    canWalk: Boolean,
    worldPosition: BlockPos,
    level: ServerLevel?
): BlockPos? {
    // check all positions in the area (from config), if its a valid spawn location for the pokemon
    // check if the block below is solid, if the block is air, and if the blocks insied the pokemon's hitbox are air
    val range: Int = 16
    var blockPos: BlockPos? = null
    var blockList: MutableList<BlockPos> = mutableListOf()
    println("Pokemon: ${pokemon.pokemon.species.name}, canSwim: $canSwim, canFly: $canFly, canWalk: $canWalk")
    for (j in 0..5) {
        for (i in 0..5) {
            val blocksToCheck: MutableIterable<BlockPos>? = getRandomBlocks(worldPosition, range, level)
            blocksToCheck?.forEach { pos ->
                val blocks: MutableIterable<BlockPos>? = BlockPos.withinManhattan(
                    pos,
                    pokemon.getDimensions(Pose.STANDING).width.toInt() + 1,
                    pokemon.getDimensions(Pose.STANDING).height.toInt() + 1,
                    pokemon.getDimensions(Pose.STANDING).width.toInt() + 1
                )
                for (block in blocks!!) {
                    if (level!!.random.nextFloat() < 0.5) {
                        continue
                    }
                    if (!level!!.getBlockState(block).isAir) {
                        continue
                    }
                    if (block == worldPosition || block == worldPosition.north() || block == worldPosition.south() || block == worldPosition.east() || block == worldPosition.west() || block == worldPosition.north()
                            .east() || block == worldPosition.north().west() || block == worldPosition.south()
                            .east() || block == worldPosition.south().west()
                    ) {
                        continue
                    }
                    if (!canFly) {
                        if (!level!!.getBlockState(block.above()).isAir || !level!!.getBlockState(block.north()).isAir || !level!!.getBlockState(
                                block.south()
                            ).isAir || !level!!.getBlockState(block.east()).isAir || !level!!.getBlockState(block.west()).isAir || !level!!.getBlockState(
                                block.north().east()
                            ).isAir || !level!!.getBlockState(block.north().west()).isAir || !level!!.getBlockState(
                                block.south().east()
                            ).isAir || !level!!.getBlockState(block.south().west()).isAir
                        ) {
                            continue
                        }
                    }
                    if (canWalk && !canSwim && !canFly) {
                        if (!level!!.getBlockState(block.below()).isSolidRender(level!!, block)) {
                            continue
                        }
                    }
                    if (canSwim && !canWalk) {
                        if (!level!!.getBlockState(block.below()).`is`(Blocks.WATER)) {
                            continue
                        }
                    }
                    blockPos = block
                    blockList.add(block)
                    break
                }
            }
            if (blockPos != null) {
                break
            }
        }
    }
    if (blockList.isEmpty()) {
        return null
    }
    return blockList.random()
}

fun getRandomBlocks(pos: BlockPos, range: Int, level: ServerLevel?): MutableIterable<BlockPos>? {
    return BlockPos.randomInCube(level!!.random, 16, pos, range)
}