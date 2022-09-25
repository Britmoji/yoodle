package org.britmoji.yoodle

import com.kotlindiscord.kord.extensions.ExtensibleBot
import mu.KotlinLogging
import org.britmoji.yoodle.config.config
import org.britmoji.yoodle.extensions.GitHubExtension
import org.britmoji.yoodle.extensions.InteractionExtension
import org.britmoji.yoodle.extensions.LinkReplacerExtension
import org.britmoji.yoodle.util.Colors
import org.britmoji.yoodle.util.feedback

private val logger = KotlinLogging.logger { }

suspend fun main() {
    // Warn for missing config
    if (config.bot.token == "INSERT_TOKEN_HERE") {
        return logger.error { "Please configure the bot before running it. A default config has been created." }
    }

    // Create the bot
    val bot = ExtensibleBot(config.bot.token) {
        applicationCommands {
            enabled = true
            defaultGuild(config.bot.guildId)
        }

        errorResponse { message, type ->
            feedback(type.error.message ?: message) {
                color = Colors.ERROR
            }
        }

        presence {
            watching("you sleep")
        }

        extensions {
            add(::GitHubExtension)
            add(::InteractionExtension)
            add(::LinkReplacerExtension)
        }
    }

    bot.start()
}
