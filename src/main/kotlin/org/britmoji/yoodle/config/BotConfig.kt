package org.britmoji.yoodle.config

import kotlinx.serialization.Serializable
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
        val oomfies: Oomfies = Oomfies()
    ) {
        @Serializable
        data class LinkReplacer(
            val sites: Map<String, String> = emptyMap()
        )

        @Serializable
        data class Oomfies(
            val roleId: Long = 985917956070453248,
            val responses: List<String> = listOf(
                "https://cdn.discordapp.com/attachments/820358777709527070/998078310531739668/attachment.gif",
                "https://c.tenor.com/vyVoc65OT78AAAAC/oomfie-twitter.gif",
                "https://c.tenor.com/4eTHttRLIfcAAAAC/bpd-fp.gif",
                "https://c.tenor.com/pQRJ07IeskcAAAAC/hello-chat-project-sekai.gif",
                "https://c.tenor.com/SLreBaaFtIYAAAAC/tenor.gif",
                "https://c.tenor.com/MO_05FY8MJAAAAAC/tenor.gif",
                "https://c.tenor.com/6ew2pjgG3cwAAAAC/tenor.gif",
                "https://c.tenor.com/AD3gPLLDM_sAAAAC/tenor.gif",
                "https://c.tenor.com/vk7UUH-uSPAAAAAC/tenor.gif"
            )
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