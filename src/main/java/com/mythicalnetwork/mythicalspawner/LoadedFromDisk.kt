package com.mythicalnetwork.mythicalspawner

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import org.quiltmc.qsl.base.api.event.EventAwareListener

@FunctionalInterface
interface LoadedFromDisk : EventAwareListener {
    fun onLoadedFromDisk(entity: Entity, level: Level, loadedFromDisk: Boolean): Boolean
}