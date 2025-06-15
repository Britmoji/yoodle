package org.britmoji.yoodle.extensions

import dev.kord.common.entity.ChannelType
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import io.github.oshai.kotlinlogging.KotlinLogging

class EventsExtension(override val name: String = "Interaction") : Extension() {

    private val logger = KotlinLogging.logger { }

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                logger.info { "Logged in as ${event.self.tag}" }
            }
        }

        event<GuildButtonInteractionCreateEvent> {
            action {
                when (event.interaction.componentId) {
                    "delete" -> event.interaction.message.delete()
                }
            }
        }

        event<ReactionAddEvent> {
            action {
                val channel = event.channel.asChannelOrNull() ?: return@action
                val user = event.user.asUserOrNull() ?: return@action

                if (event.emoji.name == "🗑️" && channel.data.type == ChannelType.DM && !user.isBot) {
                    event.message.delete()
                }
            }
        }
    }
}