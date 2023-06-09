package com.mythicalnetwork.mythicalspawner.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class ServerEvents {
    public static void init() {
        EntityEvents.LOADED_FROM_DISK.register(((entity, level, loadedFromDisk) -> {
            if(entity instanceof PokemonEntity pe)
                if(!loadedFromDisk){
                    SpawnHandler.INSTANCE.handleJoinEvent(pe, level);
                }
            return true;
        }));
    }
}
