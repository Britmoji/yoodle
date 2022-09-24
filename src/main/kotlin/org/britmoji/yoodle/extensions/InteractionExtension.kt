package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent

class InteractionExtension(override val name: String = "Interaction") : Extension() {
    override suspend fun setup() {
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