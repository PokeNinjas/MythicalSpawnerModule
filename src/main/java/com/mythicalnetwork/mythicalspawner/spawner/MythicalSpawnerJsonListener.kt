package com.mythicalnetwork.mythicalspawner.spawner

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

class MythicalSpawnerJsonListener() : SimpleJsonResourceReloadListener(GSON, "spawners") {
    companion object {
        val GSON: Gson = Gson()
        val INSTANCE: MythicalSpawnerJsonListener = MythicalSpawnerJsonListener()
    }
    override fun apply(
        prepared: MutableMap<ResourceLocation, JsonElement>,
        manager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        SpawnerDataHolder.SPAWN_DATA.clear()
        prepared.forEach { (id, json) ->
            val data: DataResult<SpawnerDataHolder> = SpawnerDataHolder.CODEC.parse(JsonOps.INSTANCE, json)
            if(data.error().isPresent){
                MythicalSpawner.LOGGER.error("Error parsing spawner data for $id: ${data.error().get()}")
                return@forEach
            }
            val dataHolder: SpawnerDataHolder = data.result().get()
            MythicalSpawner.LOGGER.info("Loaded spawner data for $id: ${dataHolder.species}")
            SpawnerDataHolder.SPAWN_DATA[id.toString()] = dataHolder
        }
    }
}