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
    var forcedSpawnMessage: String = "<gold>[Mythical] <c:#e38fd8>A <red>{pokemon_aspects}{spawn_pokemon}<c:#e38fd8> has spawned in a <red>{spawn_biome} <c:#e38fd8>biome near <red>{nearest_player} after {time_since_last_spawn}!"
    @JvmField
    var chanceSpawnMessage: String = "<gold>[Mythical] <c:#e38fd8>A <red>{pokemon_aspects}{spawn_pokemon}<c:#e38fd8> has spawned in a <red>{spawn_biome} <c:#e38fd8>biome near <red>{nearest_player}!"
    @JvmField
    var lastLegendCapturedMessage: String = "<gold>[Mythical] <c:#e38fd8>The last legendary pokemon captured was a <red>{last_captured_pokemon} <c:#e38fd8>in a <red>{last_captured_biome} <c:#e38fd8>biome by <red>{last_captured_trainer}!"
    @JvmField
    var lastLegendNotCaptureMessage: String = "<gold>[Mythical] <c:#e38fd8>The last legendary pokemon spawned was a <red>{last_captured_pokemon} <c:#e38fd8>in a <red>{last_captured_biome} <c:#e38fd8>biome! <c:#e38fd8>It has not been captured!"
    @JvmField
    var spawnerFilteredAspects: ArrayList<String> = arrayListOf("genderless", "male", "female")
    @JvmField
    var announcedSpecies: ArrayList<String> = arrayListOf("ironvaliant")
}