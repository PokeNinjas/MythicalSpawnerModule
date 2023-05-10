package com.mythicalnetwork.mythicalspawner

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

class AbilityModifierDataHolder(
    var overrideAll: Boolean,
    var whitelistBool: Boolean,
    var speciesList: List<String>,
    var chance: Float
) {

    companion object {
        private val SPAWN_DATA: AbilityModifierDataHolder = AbilityModifierDataHolder(false, false, listOf(), 0.0f)
        val CODEC: Codec<AbilityModifierDataHolder> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("override_all_species").forGetter { it.overrideAll },
                Codec.BOOL.fieldOf("whitelist").forGetter { it.whitelistBool },
                Codec.STRING.listOf().fieldOf("species_list").forGetter { it.speciesList },
                Codec.FLOAT.fieldOf("chance").forGetter { it.chance }
            ).apply(instance) { overrideAll, whitelistBool, speciesList, chance ->
                AbilityModifierDataHolder(overrideAll, whitelistBool, speciesList, chance)
            }
        }

        fun getSpeciesData(): AbilityModifierDataHolder {
            return SPAWN_DATA
        }

        fun setSpeciesData(data: AbilityModifierDataHolder) {
            SPAWN_DATA.overrideAll = data.overrideAll
            SPAWN_DATA.whitelistBool = data.whitelistBool
            SPAWN_DATA.speciesList = data.speciesList
            SPAWN_DATA.chance = data.chance
        }
    }
}

