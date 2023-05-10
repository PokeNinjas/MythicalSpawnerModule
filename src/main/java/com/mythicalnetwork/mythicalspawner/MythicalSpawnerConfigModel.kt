package com.mythicalnetwork.mythicalspawner

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu
import java.lang.reflect.Array
import java.util.ArrayList


@Modmenu(modId = "mythicalspawner")
@Config(name = "mythical-spawner", wrapperName = "MythicalSpawnerConfig")
class MythicalSpawnerConfigModel {

    @JvmField
    var playerSearchRange: Int = 100
    @JvmField
    var forcedSpawnMessage: String = ""
    @JvmField
    var chanceSpawnMessage: String = "<gold>[Mythical] <c:#e38fd8>A <red>{pokemon_aspects}{spawn_pokemon}<c:#e38fd8> has spawned in a <red>{spawn_biome} <c:#e38fd8>biome near <red>{nearest_player}!"
    @JvmField
    var lastLegendCapturedMessage: String = ""
    @JvmField
    var lastLegendNotCaptureMessage: String = ""
    @JvmField
    var spawnerFilteredAspects: ArrayList<String> = arrayListOf("genderless", "male", "female")
    @JvmField
    var announcedSpecies: ArrayList<String> = arrayListOf("ironvaliant")
}