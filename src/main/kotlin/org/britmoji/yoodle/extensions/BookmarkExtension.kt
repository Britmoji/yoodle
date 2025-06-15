package org.britmoji.yoodle.extensions

import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.kordex.core.utils.addReaction
import dev.kordex.core.utils.getJumpUrl
import org.britmoji.yoodle.util.cloneMessage

class BookmarkExtension(override val name: String = "Bookmark") : Extension() {
    override suspend fun setup() {
        event<ReactionAddEvent> {
            action {
                // Check if it's a bookmark reaction
                if (event.emoji.name != "🔖") return@action

                // DM the user with a link to the message
                val channel = event.channel.asChannelOfOrNull<GuildChannel>() ?: return@action
                val message = event.message.asMessage()
                val dmChannel = event.user.getDmChannel()

                // Copy the message & add a prefix
                val prefix = """
                | > 🔖 ${channel.mention} (`${channel.guild.asGuild().name}`)${message.author?.let { " by ${it.mention} (`${it.tag}`)" } ?: ""}
                | > 🔗 ${event.message.asMessage().getJumpUrl()}
                """.trimMargin()

                // Send it
                val resultMessage = dmChannel.createMessage {
                    cloneMessage(message)
                    content = (prefix + "\n\n" + message.content).take(2000)
                }

                // Add a trash can reaction to the message
                resultMessage.addReaction("🗑️")
            }
        }
    }
}