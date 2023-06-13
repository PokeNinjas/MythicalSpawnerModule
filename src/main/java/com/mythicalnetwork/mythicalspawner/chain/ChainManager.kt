package com.mythicalnetwork.mythicalspawner.chain

import com.mythicalnetwork.mythicalspawner.formatIvRangeValues
import com.mythicalnetwork.mythicalspawner.formatShinyCatchComboRates
import net.minecraft.server.level.ServerLevel
import java.util.UUID

class ChainManager(var LEVEL: ServerLevel) {
    private var users: MutableList<ChainUser> = mutableListOf()
    private var toRemove: MutableList<ChainUser> = mutableListOf()
    companion object {
        val SHINY_RATES: Map<IntRange, Float> = formatShinyCatchComboRates()
        val IV_RATES: Map<IntRange, Int> = formatIvRangeValues()
    }

    fun printUsers(): String {
        var string = ""
        for (user in users) {
            string += user.getPlayer().toString() + "\n"
        }
        return string
    }

    fun tick(level: ServerLevel) {
        toRemove.clear()
        for (chain in users) {
            chain.tick(level)
            if(chain.toRemove){
                toRemove.add(chain)
            }
        }
        for (chain in toRemove) {
            removeChain(chain)
        }
    }

    fun getChainData(player: UUID) : ChainUser? {
        for (chain in users) {
            if (chain.getPlayer() == player) {
                return chain
            }
        }
        return null
    }

    fun addChain(player: UUID): ChainUser {
        users.add(ChainUser(player))
        return getChainData(player)!!
    }

    fun addChain(player: ChainUser) {
        users.add(player)
    }

    fun removeChain(player: UUID) {
        users.remove(getChainData(player))
    }

    fun removeChain(player:ChainUser){
        users.remove(player)
    }

    fun resetChain(player: UUID) {
        getChainData(player)?.resetChain()
    }

    fun incrementChain(player: UUID) {
        getChainData(player)?.incrementChain()
    }

    fun setChainedPokemon(player: UUID, pokemon: String) {
        getChainData(player)?.setChainedPokemon(pokemon)
    }

    fun getChainedPokemon(player: UUID): String? {
        return getChainData(player)?.getChainedPokemon()
    }

    fun getChainLength(player: UUID): Int? {
        return getChainData(player)?.getChain()
    }

    fun setChainLength(player: UUID, chain: Int) {
        getChainData(player)?.setChain(chain)
    }

}