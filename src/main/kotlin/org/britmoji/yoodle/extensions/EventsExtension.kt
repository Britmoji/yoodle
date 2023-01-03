package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import mu.KotlinLogging

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
                    "delete" -> {
                        event.interaction.message.delete()
                    }
                }
            }
        }
    }
}