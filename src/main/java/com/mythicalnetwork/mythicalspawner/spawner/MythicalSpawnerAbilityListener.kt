package com.mythicalnetwork.mythicalspawner.spawner

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

class MythicalSpawnerAbilityListener: SimpleJsonResourceReloadListener(GSON, "ability_spawners"), PreparableReloadListener {
    companion object {
        val GSON: Gson = Gson()
        val INSTANCE: MythicalSpawnerAbilityListener = MythicalSpawnerAbilityListener()
    }
    override fun apply(
        prepared: MutableMap<ResourceLocation, JsonElement>,
        manager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        prepared.forEach { (id, json) ->
            val data: DataResult<AbilityModifierDataHolder> = AbilityModifierDataHolder.CODEC.parse(JsonOps.INSTANCE, json)
            if(data.error().isPresent){
                MythicalSpawner.LOGGER.error("Error parsing ability spawner data for $id: ${data.error().get()}")
                return@forEach
            }
            val dataHolder: AbilityModifierDataHolder = data.result().get()
            MythicalSpawner.LOGGER.info("Loaded ability spawner data for $id: ${dataHolder.speciesList}")
            AbilityModifierDataHolder.setSpeciesData(dataHolder)
        }
    }
}