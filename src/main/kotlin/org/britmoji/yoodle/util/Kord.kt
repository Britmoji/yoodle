@file:OptIn(KordPreview::class)

package org.britmoji.yoodle.util

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.utils.ensureWebhook
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.live.live
import dev.kord.core.live.onUpdate
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.WebhookMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.britmoji.yoodle.bot
import org.britmoji.yoodle.config.BotConfig

private val taskContext = bot.kordRef.coroutineContext + CoroutineName("KordTaskScope")

/**
 * List of common colors for embeds.
 *
 * @author Koding
 */
@Suppress("unused")
object Colors {
    val SUCCESS = Color(0x4caf50)
    val ERROR = Color(0xf44336)
    val INFO = Color(0x2196f3)
}

/**
 * Create a new embed builder with a default color for
 * the given message. Provides consistent colors for
 * embeds.
 *
 * @param message The message to create the embed for.
 * @param builder The builder to use for the embed.
 */
suspend fun MessageCreateBuilder.feedback(message: String? = null, builder: suspend EmbedBuilder.() -> Unit = {}) {
    embed {
        color = Colors.INFO
        description = message
        builder()
    }
}

@Suppress("UnusedReceiverParameter", "unused")
fun CommandContext.error(message: String): Nothing = throw DiscordRelayedException(message)

/**
 * Logo as a byte array
 */
private val logoBytes by lazy {
    BotConfig::class.java.getResourceAsStream("/logo.png")!!.readBytes()
}

/**
 * Send a webhook message to the given channel.
 *
 * @param name The name of the webhook.
 * @param avatarUrl The avatar of the webhook.
 * @param builder The builder to use for the message.
 */
suspend fun TopGuildMessageChannel.sendWebhook(
    name: String? = null,
    avatarUrl: String? = null,
    builder: suspend WebhookMessageCreateBuilder.() -> Unit = {}
): Message {
    val botUsername = bot.kordRef.getSelf().username
    val hook = ensureWebhook(this, "$botUsername - Webhook") { logoBytes }

    return hook.execute(hook.token ?: error("No webhook token found")) {
        this.username = name
        this.avatarUrl = avatarUrl
        this.builder()
    }
}

/**
 * Forcefully suppress embeds in a message by editing it.
 */
suspend fun Message.removeEmbeds() {
    if (embeds.isNotEmpty()) {
        // Remove embeds from original message
        edit {
            flags = MessageFlags(MessageFlag.SuppressEmbeds)
        }
    } else {
        // Wait for the message to be edited by discord, so we can
        // remove the embeds from the original message
        val live = live()

        live.onUpdate {
            // Remove embeds from original message
            CoroutineScope(taskContext).launch {
                it.message.edit {
                    flags = MessageFlags(MessageFlag.SuppressEmbeds)
                }
            }

            // Stop listening
            live.cancel()
        }
    }
}