package com.mythicalnetwork.mythicalspawner.events

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mythicalnetwork.mythicalspawner.spawner.AbilityModifierDataHolder
import com.mythicalnetwork.mythicalspawner.MythicalSpawner
import com.mythicalnetwork.mythicalspawner.spawner.SpawnerDataHolder
import com.pokeninjas.kingdoms.common.redis.event.impl.backend.BroadcastMessageEvent
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.TextParserUtils
import eu.pb4.placeholders.api.node.TextNode
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import java.util.*

object SpawnHandler {

    fun onSpawn(pokemonEntity: PokemonEntity) {
        val pokemon: Pokemon = pokemonEntity.pokemon
        val alreadyModified: Boolean = pokemonEntity.tags.contains("pokespawner:modified")
        if (alreadyModified || pokemon.isPlayerOwned()) return
        pokemonEntity.addTag("pokespawner:modified")
        handleHiddenAbilitySpawns(pokemonEntity, pokemon)
        handleCustomSpawns(pokemonEntity, pokemon)
    }

    private fun handleHiddenAbilitySpawns(pokemonEntity: PokemonEntity, pokemon: Pokemon){
        val data = AbilityModifierDataHolder.getSpeciesData()
        var shouldModify: Boolean = false
        if(!data.whitelistBool){
            if(!data.speciesList.contains(pokemon.species.name.lowercase())){
                shouldModify = true
            }
        } else {
            if(data.speciesList.contains(pokemon.species.name.lowercase())){
                shouldModify = true
            }
        }
        if(data.overrideAll){
            shouldModify = true
        }
        if(shouldModify){
            if(Math.random() < data.chance){
                PokemonProperties.parse("hiddenability").apply(pokemon)
            }
        }
    }

    fun handleJoinEvent(pokemonEntity: PokemonEntity, level: Level){
        if (pokemonEntity.pokemon.getOwnerUUID() != null) return
        val legendaryOrMythic =
            pokemonEntity.pokemon.hasLabels("legendary") || pokemonEntity.pokemon.hasLabels("mythic") || MythicalSpawner.CONFIG.announcedSpecies().contains(pokemonEntity.pokemon.species.name.lowercase().replace("_", "").replace(" ", ""))
        if (legendaryOrMythic) {
            var nearestPlayer: Player? = level.getNearestPlayer(
                pokemonEntity,
                MythicalSpawner.CONFIG.playerSearchRange().toDouble()
            )
            MythicalSpawner.LAST_LEGENDARY_SPAWNED = pokemonEntity.pokemon
            MythicalSpawner.LAST_CAPTURED_BIOME = Component.translatable(Util.makeDescriptionId("biome", level.getBiome(pokemonEntity.onPos).unwrapKey().get().location())).string
            MythicalSpawner.LAST_CAPTURED_TRAINER = null
            if(pokemonEntity.tags.contains("forced_spawn")){
                var message: TextNode? = TextParserUtils.formatNodes(MythicalSpawner.CONFIG.forcedSpawnMessage())
                val placeholderMap: HashMap<String, Component> = HashMap()
                placeholderMap["nearest_player"] = Component.literal(nearestPlayer!!.name.string)
                placeholderMap["spawn_pokemon"] = Component.literal(pokemonEntity.pokemon.species.name)
                placeholderMap["spawn_biome"] = Component.translatable(Util.makeDescriptionId("biome", level.getBiome(pokemonEntity.onPos).unwrapKey().get().location()))
                placeholderMap["pokemon_aspects"] = Component.literal(parseAspects(pokemonEntity.pokemon.aspects.toString()))
                placeholderMap["time_since_last_spawn"] = Component.literal(formatTime(MythicalSpawner.TIME_SINCE_LEGENDARY_SPAWN.toLong()))
                message = Placeholders.parseNodes(message, Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM, placeholderMap)
                val component: Component = message.toText(PlaceholderContext.of(nearestPlayer).asParserContext(), true)
                if (message != null) {
                    BroadcastMessageEvent(Component.Serializer.toJson(component)).send()
                }
            } else {
                var message: TextNode? = TextParserUtils.formatNodes(MythicalSpawner.CONFIG.chanceSpawnMessage())
                val placeholderMap: HashMap<String, Component> = HashMap()
                placeholderMap["nearest_player"] = Component.literal(nearestPlayer!!.name.string)
                placeholderMap["spawn_pokemon"] = Component.literal(pokemonEntity.pokemon.species.name)
                placeholderMap["spawn_biome"] = Component.translatable(Util.makeDescriptionId("biome", level.getBiome(pokemonEntity.onPos).unwrapKey().get().location()))
                placeholderMap["pokemon_aspects"] = Component.literal(parseAspects(pokemonEntity.pokemon.aspects.toString()))
                message = Placeholders.parseNodes(message, Placeholders.ALT_PLACEHOLDER_PATTERN_CUSTOM, placeholderMap)
                val component: Component = message.toText(PlaceholderContext.of(nearestPlayer).asParserContext(), true)
                if (message != null) {
                    BroadcastMessageEvent(Component.Serializer.toJson(component)).send()
                }
            }
            MythicalSpawner.TIME_SINCE_LEGENDARY_SPAWN = 0
        }
    }

     fun parseAspects(aspects: String): String {
        val builder = StringBuilder()
        val asp = aspects.replace("[", "").replace("]", "")
        asp.split(",").forEach { aspect ->
            if(aspect != " "){
                var message = humanize(aspect)
                builder.append(message)
                // check if the last character of aspect is whitespace using regex, if not, add a space
                if (!builder.toString().matches(".*\\s".toRegex())) {
                    builder.append(" ")
                }
            }
        }
        return builder.toString()
    }

    private fun humanize(str: String): String {
        val words = str.split("_")
        val builder = StringBuilder()
        words.forEach { w ->
            var word = w.replace(" ", "").replace("mythical", "")
            if (!MythicalSpawner.CONFIG.spawnerFilteredAspects().contains(word.lowercase(Locale.getDefault()))) {
                builder.append(word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }.replace("\\s".toRegex(), "."))
            }
        }
        return builder.toString()
    }


    fun handleCustomSpawns(pokemonEntity: PokemonEntity, pokemon: Pokemon) {
        val speciesData = SpawnerDataHolder.getSpeciesData(pokemon.species.showdownId())
        speciesData.forEach() { data ->
            var shouldModify: Boolean = true
            data.area.ifPresent { area ->
                val pos = pokemonEntity.onPos
                if (pos.x < area.first.x || pos.x > area.second.x || pos.y < area.first.y || pos.y > area.second.y || pos.z < area.first.z || pos.z > area.second.z) {
                    shouldModify = false
                }
            }
            data.dimension.ifPresent { dimension ->
                if (pokemonEntity.level.dimension().toString() != dimension) {
                    shouldModify = false
                }
            }
            data.biome.ifPresent { biome ->
                if (pokemonEntity.level.getBiome(pokemonEntity.onPos).unwrapKey().get().toString() != biome) {
                    shouldModify = false
                }
            }
            data.time.ifPresent { time ->
                val timeOfDay = pokemonEntity.level.dayTime
                if (timeOfDay < time.first || timeOfDay > time.second) {
                    shouldModify = false
                }
            }
            data.weather.ifPresent { weather ->
                if (pokemonEntity.level.isRaining && weather != "rain") {
                    shouldModify = false
                }
                if (pokemonEntity.level.isThundering && weather != "thunder") {
                    shouldModify = false
                }
            }
            if (shouldModify) {
                data.aspects.forEach() { aspect ->
                    if (Math.random() < aspect.chance) {
                        PokemonProperties.parse(aspect.aspect).apply(pokemon)
                    }
                }
            }
        }
    }

    fun formatTime(ticks: Long): String {
        var totalSeconds = ticks / 20
        // output format: 1d 2h 3m 4s. There are 20 ticks in a second.
        // calculate days, hours, minutes, and remaining seconds
        val days = totalSeconds / (60 * 60 * 24)
        totalSeconds -= days * (60 * 60 * 24)
        val hours = totalSeconds / (60 * 60)
        totalSeconds -= hours * (60 * 60)
        val minutes = totalSeconds / 60
        totalSeconds -= minutes * 60
        val seconds = totalSeconds
        var builder = StringBuilder()
        if (days > 0) {
            builder.append(days).append("d ")
        }
        if (hours > 0) {
            builder.append(hours).append("h ")
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ")
        }
        if (seconds > 0) {
            builder.append(seconds).append("s")
        }
        // remove the space at the end
        builder = StringBuilder(builder.toString().trim())
        return builder.toString()
    }
}

