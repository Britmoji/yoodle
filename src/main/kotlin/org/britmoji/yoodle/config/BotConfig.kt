package org.britmoji.yoodle.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Structure of the config file
 *
 * @author Koding
 */
@Serializable
data class BotConfig(
    val bot: Bot = Bot(),
    val modules: Modules = Modules()
) {
    @Serializable
    data class Bot(
        val token: String = "INSERT_TOKEN_HERE",
        val guildId: String? = null,
    )

    @Serializable
    data class Modules(
        val linkReplacer: LinkReplacer = LinkReplacer(),
    ) {
        @Serializable
        data class LinkReplacer(
            val sites: Map<String, String> = emptyMap()
        )
    }
}

/**
 * Global config object
 */
val config: BotConfig by lazy {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    val config = File("config.json")
        .also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText(json.encodeToString(BotConfig()))
            }
        }
        .readText()

    json.decodeFromString(config)
}