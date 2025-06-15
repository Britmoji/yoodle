package org.britmoji.yoodle.extensions

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import org.britmoji.yoodle.config.config

class OomfiesExtension(override val name: String = "oomfies") : Extension() {

    private val roleId by lazy { Snowflake(config.modules.oomfies.roleId) }

    private val oomfieGifs = listOf(
        "https://cdn.discordapp.com/attachments/820358777709527070/998078310531739668/attachment.gif",
        "https://c.tenor.com/vyVoc65OT78AAAAC/oomfie-twitter.gif",
        "https://c.tenor.com/4eTHttRLIfcAAAAC/bpd-fp.gif",
        "https://c.tenor.com/pQRJ07IeskcAAAAC/hello-chat-project-sekai.gif"
    )

    override suspend fun setup() {
        event<MessageCreateEvent> {
            action {
                if (roleId !in event.message.mentionedRoleIds) return@action

                // Send a random message
                event.message.channel.createMessage {
                    content = config.modules.oomfies.responses.random()
                }
            }
        }
    }
}