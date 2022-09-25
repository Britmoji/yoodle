package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.MessageFlags
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import org.britmoji.yoodle.config.config
import org.britmoji.yoodle.util.sendWebhook

class LinkReplacerExtension(override val name: String = "Link Replacer") : Extension() {
    private val urlRegex = Regex(
        "(?<protocol>https?://)?(?<domain>[a-z0-9.-]+)(?<path>/[a-z0-9/._-]*)?(?<query>\\?[a-z0-9=&_]+)?",
        RegexOption.IGNORE_CASE
    )

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                failIfNot { urlRegex.containsMatchIn(event.message.content) }
            }

            action {
                if (event.member.isNullOrBot()) return@action

                // Check it's a guild channel
                val channel = event.message.channel.asChannelOfOrNull<TopGuildMessageChannel>() ?: return@action

                // Get matches
                val matches = urlRegex.findAll(event.message.content)

                // Add matches
                val links = arrayListOf<String>()
                for (match in matches) {
                    // Extract match & groups
                    val protocol = match.groups["protocol"]?.value ?: continue
                    var domain = match.groups["domain"]?.value ?: continue
                    val path = match.groups["path"]?.value ?: ""

                    // Replace domains
                    config.modules.linkReplacer.sites.entries
                        .find { (k, _) -> k.equals(domain, true) }
                        ?.also { (_, v) -> domain = v }
                        ?: continue

                    // Build link (ignoring query)
                    links.add("$protocol$domain$path")
                }

                // Check if we have any links
                if (links.isEmpty()) return@action

                // Create
                channel.sendWebhook(event.member?.tag, event.member?.avatar?.url) {
                    content = links.joinToString("\n")
                }

                // Remove embeds from original message
                event.message.edit {
                    flags = MessageFlags(MessageFlag.SuppressEmbeds)
                }
            }
        }
    }
}