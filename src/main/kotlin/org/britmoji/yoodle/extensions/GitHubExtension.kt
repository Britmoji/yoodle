package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.actionRow
import org.britmoji.yoodle.util.MessageTrigger
import org.britmoji.yoodle.util.removeEmbeds
import java.net.URL
import kotlin.math.max

class GitHubExtension(override val name: String = "GitHub") : Extension() {

    // pkg/plugins/plugins.go
    private val languageRegex = Regex("(?<path>.+)\\.(?<extension>\\w+)(\\?.+)?$")

    private val trigger = MessageTrigger.gitHub { it.startLine != null }

    override suspend fun setup() {
        event<MessageCreateEvent> {
            action {
                // Find matches
                trigger.run(event.message) {
                    if (it.suppressed || it.spoiler) return@run

                    var startLine = it.match.startLine!!
                    var endLine = it.match.endLine

                    // If only one line is specified, have a default of 6 lines
                    if (endLine == null) {
                        startLine = max(startLine - 3, 1)
                        endLine = startLine + 6
                    }

                    // Swap start and end if start is greater than end
                    if (startLine > endLine) {
                        val temp = startLine
                        startLine = endLine
                        endLine = temp
                    }

                    // Read lines
                    val url =
                        "https://raw.githubusercontent.com/${it.match.owner}/${it.match.repo}/${it.match.branch}/${it.match.path}"
                    val lines = URL(url).readText().split("\n")

                    // Infer language
                    val language = languageRegex.find(it.match.path)?.groups?.get("extension")?.value ?: "text"
                    if (!language.matches(Regex("[a-zA-Z0-9]+"))) return@run

                    // Create message
                    val message = lines.subList(startLine - 1, endLine).joinToString("\n")
                    val msgContent = "```${language}\n$message\n```"
                    if (msgContent.length > 2000) return@run

                    event.message.removeEmbeds()
                    event.message.channel.createMessage {
                        content = msgContent

                        actionRow {
                            interactionButton(ButtonStyle.Danger, "delete") {
                                label = "Delete"
                            }
                        }
                    }
                }
            }
        }
    }
}