package com.mythicalnetwork.mythicalspawner

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
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
        PokespawnerDataHolder.SPAWN_DATA.clear()
        prepared.forEach { (id, json) ->
            val data: DataResult<PokespawnerDataHolder> = PokespawnerDataHolder.CODEC.parse(JsonOps.INSTANCE, json)
            if(data.error().isPresent){
                MythicalSpawner.LOGGER.error("Error parsing spawner data for $id: ${data.error().get()}")
                return@forEach
            }
            val dataHolder: PokespawnerDataHolder = data.result().get()
            MythicalSpawner.LOGGER.info("Loaded spawner data for $id: ${dataHolder.species}")
            PokespawnerDataHolder.SPAWN_DATA[id.toString()] = dataHolder
        }
    }
}