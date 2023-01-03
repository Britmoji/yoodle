package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.isNullOrBot
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import org.britmoji.yoodle.config.config
import org.britmoji.yoodle.util.MessageTrigger
import org.britmoji.yoodle.util.removeEmbeds
import org.britmoji.yoodle.util.sendWebhook

class LinkReplacerExtension(override val name: String = "Link Replacer") : Extension() {

    private val trigger = MessageTrigger.url { config.modules.linkReplacer.sites.keys.contains(it.host) }

    override suspend fun setup() {
        event<MessageCreateEvent> {
            action {
                if (event.member.isNullOrBot()) return@action

                // Check it's a guild channel
                val channel = event.message.channel.asChannelOfOrNull<TopGuildMessageChannel>() ?: return@action

                // Run
                val links = arrayListOf<String>()
                trigger.run(event.message) {
                    // Replace domains
                    val domain = config.modules.linkReplacer.sites
                        .filterKeys { k -> k.equals(it.match.host, true) }
                        .values.firstOrNull() ?: return@run

                    // Build new URL
                    if (it.suppressed) return@run
                    links.add(it.style("${it.match.protocol}://$domain${it.match.path}"))
                }

                // Check if we have any links
                if (links.isEmpty()) return@action

                // Create
                event.message.removeEmbeds()
                channel.sendWebhook(event.member?.tag, event.member?.let { it.memberAvatar?.url ?: it.avatar?.url }) {
                    content = links.joinToString("\n")
                }
            }
        }
    }
}