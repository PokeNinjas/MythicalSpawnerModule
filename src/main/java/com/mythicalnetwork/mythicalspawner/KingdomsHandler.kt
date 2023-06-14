package com.mythicalnetwork.mythicalspawner

import com.pokeninjas.kingdoms.common.redis.event.impl.backend.BroadcastMessageEvent
import net.minecraft.network.chat.Component

object KingdomsHandler {
    fun broadcastCrossServerMessage(message: String) {
        BroadcastMessageEvent(message).send()
    }
}