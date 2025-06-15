package org.britmoji.yoodle

import dev.kordex.core.ExtensibleBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.britmoji.yoodle.config.config
import org.britmoji.yoodle.extensions.BookmarkExtension
import org.britmoji.yoodle.extensions.EventsExtension
import org.britmoji.yoodle.extensions.GitHubExtension
import org.britmoji.yoodle.extensions.LinkReplacerExtension
import org.britmoji.yoodle.extensions.TranscodeExtension
import org.britmoji.yoodle.util.Colors
import org.britmoji.yoodle.util.feedback

private val logger = KotlinLogging.logger { }

lateinit var bot: ExtensibleBot

suspend fun main() {
    // Warn for missing config
    if (config.bot.token == "INSERT_TOKEN_HERE") {
        return logger.error { "Please configure the bot before running it. A default config has been created." }
    }

    // Create the bot
    bot = ExtensibleBot(config.bot.token) {
        applicationCommands {
            enabled = true
            defaultGuild(config.bot.guildId)
        }

        errorResponse { message, type ->
            feedback(type.error.message ?: message.key) {
                color = Colors.ERROR
            }
        }

        presence {
            watching("you sleep")
        }

        extensions {
            add(::GitHubExtension)
            add(::EventsExtension)
            add(::LinkReplacerExtension)
            add(::TranscodeExtension)
            add(::BookmarkExtension)
        }
    }

    bot.start()
}
