package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.create.actionRow
import java.net.URL
import kotlin.math.max

class GitHubExtension(override val name: String = "GitHub") : Extension() {
    /**
     * https://github.com/traefik/traefik/blob/707d355d4ae4b0e3681d5b968928974251f4d9fb/pkg/plugins/plugins.go#L17-L26
     * https://github.com/traefik/traefik/blob/master/pkg/plugins/plugins.go#L17
     */
    private val gitHubLineRegex =
        Regex("https://github.com/(?<owner>[^/]+)/(?<repo>[^/]+)/(blob|tree)/(?<branch>[^/]+)/(?<path>.+)#L(?<startLine>\\d+)(-L(?<endLine>\\d+))?")

    // pkg/plugins/plugins.go
    private val languageRegex = Regex("(?<path>.+)\\.(?<extension>\\w+)(\\?.+)?$")

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                // Ignore non-github links
                failIfNot { gitHubLineRegex.containsMatchIn(event.message.content) }
            }

            action {
                // Match the regex
                val match = gitHubLineRegex.findAll(event.message.content)

                match.forEach {
                    // Get data
                    val owner = it.groups["owner"]?.value ?: return@forEach
                    val repo = it.groups["repo"]?.value ?: return@forEach
                    val branch = it.groups["branch"]?.value ?: return@forEach
                    val path = it.groups["path"]?.value ?: return@forEach
                    var startLine = it.groups["startLine"]?.value?.toIntOrNull() ?: return@forEach
                    var endLine = it.groups["endLine"]?.value?.toIntOrNull()

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
                    val url = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
                    val lines = URL(url).readText().split("\n")

                    // Infer language
                    val language = languageRegex.find(path)?.groups?.get("extension")?.value ?: "text"
                    if (!language.matches(Regex("[a-zA-Z0-9]+"))) {
                        return@forEach
                    }

                    // Create message
                    val message = lines.subList(startLine - 1, endLine).joinToString("\n")
                    val msgContent = "```${language}\n$message\n```"
                    if (msgContent.length > 2000) {
                        return@forEach
                    }

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