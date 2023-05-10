package com.mythicalnetwork.mythicalspawner

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

class DefaultSpawnDataListener : SimpleJsonResourceReloadListener(GSON, "default_spawns") {
    companion object {
        val GSON: Gson = Gson()
        val INSTANCE: DefaultSpawnDataListener = DefaultSpawnDataListener()
    }

    override fun apply(
        prepared: MutableMap<ResourceLocation, JsonElement>,
        manager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        prepared.forEach { (id, json) ->
            var obj: JsonObject = json.asJsonObject
            var defaultList: MutableList<String> = obj.get("default_spawns").asJsonArray.map { it.asString }.toMutableList()
            MythicalSpawner.LOGGER.info("Loaded default spawns for $id: $defaultList")
            defaultList.forEach { id ->
                MythicalSpawner.LOGGER.info("Adding $id to default spawns for $id")
                PokespawnerDataHolder.activateDataHolder(id)
            }
        }
    }
}