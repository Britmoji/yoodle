package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.live.live
import dev.kord.core.live.onUpdate
import kotlinx.coroutines.cancel
import org.britmoji.yoodle.config.config
import org.britmoji.yoodle.util.sendWebhook

@OptIn(KordPreview::class)
class LinkReplacerExtension(override val name: String = "Link Replacer") : Extension() {
    private val urlRegex = Regex(
        "(?<protocol>https?://)?(?<domain>[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6})(?<path>[-a-zA-Z0-9()@:%_+.~&/=]*)",
        RegexOption.IGNORE_CASE
    )

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                failIfNot { urlRegex.containsMatchIn(event.message.content) }
            }

            action {
                if (event.member.isNullOrBot() || event.message.webhookId != null) return@action

                // Check it's a guild channel
                val channel = event.message.channel.asChannelOfOrNull<TopGuildMessageChannel>() ?: return@action

                val messageContent = event.message.content

                val suppressed = event.message.flags?.contains(MessageFlag.SuppressEmbeds)

                // Get matches
                val matches = urlRegex.findAll(messageContent)

                // Add matches
                val links = arrayListOf<String>()
                for (match in matches) {
                    // Extract match & groups
                    val protocol = match.groups["protocol"]?.value ?: continue
                    var domain = match.groups["domain"]?.value ?: continue
                    val path = match.groups["path"]?.value ?: ""

                    // Check if in spoiler
                    val spoiler = messageContent.split(domain, ignoreCase = true, limit = 2).map {
                        it.split("||").size
                    }.all { it % 2 != 1 }

                    // Replace domains
                    config.modules.linkReplacer.sites.entries
                        .find { (k, _) -> k.equals(domain, true) }
                        ?.also { (_, v) -> domain = v }
                        ?: continue

                    // Build link (ignoring query)
                    links.add(if (spoiler) "|| $protocol$domain$path ||" else "$protocol$domain$path")
                }

                // Check if we have any links
                if (links.isEmpty()) return@action

                if (event.message.embeds.isNotEmpty()) {
                    // Remove embeds from original message
                    event.message.edit {
                        flags = MessageFlags(MessageFlag.SuppressEmbeds)
                    }
                } else {
                    // Wait for the message to be edited by discord, so we can
                    // remove the embeds from the original message
                    val live = event.message.live()

                    live.onUpdate {
                        if (event.member.isNullOrBot() || event.message.webhookId != null) return@onUpdate

                        // Remove embeds from original message
                        event.message.edit {
                            flags = MessageFlags(MessageFlag.SuppressEmbeds)
                        }

                        // Stop listening
                        live.cancel()
                    }
                }

                // Create
                val hookMsg = channel.sendWebhook(event.member?.tag, event.member?.let { it.memberAvatar?.url ?: it.avatar?.url }) {
                    content = links.joinToString("\n")
                }

                if(suppressed == true) {
                    hookMsg.edit {
                        flags = MessageFlags(MessageFlag.SuppressEmbeds)
                    }
                }
            }
        }
    }
}