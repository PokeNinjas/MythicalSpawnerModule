package com.mythicalnetwork.mythicalspawner

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos

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