package com.mythicalnetwork.mythicalspawner.force

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

class MythicalSpawnChanceListener: SimpleJsonResourceReloadListener(GSON, "forced_spawns") {
    companion object {
        val GSON: Gson = Gson()
        val INSTANCE: MythicalSpawnChanceListener = MythicalSpawnChanceListener()
    }

    override fun apply(
        prepared: MutableMap<ResourceLocation, JsonElement>,
        manager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        prepared?.forEach { (id, json) ->
            val data: DataResult<MythicalSpawnChanceData> = MythicalSpawnChanceData.CODEC.parse(JsonOps.INSTANCE, json)
            if(data.error().isPresent){
                MythicalSpawner.LOGGER.error("Error parsing spawn chance data for $id: ${data.error().get()}")
                return@forEach
            }
            val dataHolder: MythicalSpawnChanceData = data.result().get()
            MythicalSpawner.LOGGER.info("Loaded forced spawn data data for $id: ${dataHolder.species}")
            MythicalSpawnChanceData.SPAWN_DATA[id.toString()] = dataHolder
        }
    }
}