package com.mythicalnetwork.mythicalspawner;

import org.quiltmc.qsl.base.api.event.Event;

public class EntityEvents {
    public static Event<LoadedFromDisk> LOADED_FROM_DISK = Event.create(LoadedFromDisk.class, callbacks -> (entity, level, loadedFromDisk) -> {
        for (LoadedFromDisk callback : callbacks) {
            if(callback.onLoadedFromDisk(entity, level, loadedFromDisk)){
                return true;
            }
        }
        return false;
    });
}
