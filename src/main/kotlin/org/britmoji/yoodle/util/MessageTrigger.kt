package org.britmoji.yoodle.util

import dev.kord.core.entity.Message
import java.net.URL

private val urlTransformer = Regex(
    "(?<protocol>https?://)?(?<domain>[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6})(?<path>[-a-zA-Z0-9()@:%_+.~&/=]*)",
    RegexOption.IGNORE_CASE
).asTransformer { URL(it.value) }

data class GitHubLink(
    val owner: String,
    val repo: String,
    val branch: String,
    val path: String,
    val startLine: Int?,
    val endLine: Int?
)

/**
 * https://github.com/traefik/traefik/blob/707d355d4ae4b0e3681d5b968928974251f4d9fb/pkg/plugins/plugins.go#L17-L26
 * https://github.com/traefik/traefik/blob/master/pkg/plugins/plugins.go#L17
 */
private val gitHubTransformer =
    Regex("https://github.com/(?<owner>[^/]+)/(?<repo>[^/]+)/(blob|tree)/(?<branch>[^/]+)/(?<path>[^#]+)(#L(?<startLine>\\d+)(-L(?<endLine>\\d+))?)?")
        .asTransformer {
            GitHubLink(
                it.groups["owner"]!!.value,
                it.groups["repo"]!!.value,
                it.groups["branch"]!!.value,
                it.groups["path"]!!.value,
                it.groups["startLine"]?.value?.toIntOrNull(),
                it.groups["endLine"]?.value?.toIntOrNull()
            )
        }

class RegexTransformer<T>(
    private val regex: Regex,
    private val transform: (MatchResult) -> T
) {
    /**
     * Wrapper for a regex to transform it into a nicer type
     *
     * @param input The input to match against
     * @return A list of the transformed matches
     */
    fun transform(input: String): List<T> = regex.findAll(input).map(transform).toList()
}

fun <T> Regex.asTransformer(transform: (MatchResult) -> T) = RegexTransformer(this, transform)

/**
 * Handles listening to messages with a set of potential triggers,
 * while respecting Discord's ability to silence embeds.
 */
class MessageTrigger<T>(private val regex: RegexTransformer<T>, private val validate: (T) -> Boolean) {

    companion object {
        /**
         * Create a new MessageTrigger instance to validate links.
         *
         * @param isValid A function that returns true if the given match is valid.
         */
        fun url(isValid: (URL) -> Boolean): MessageTrigger<URL> =
            MessageTrigger(urlTransformer) { isValid(it) }

        /**
         * Create a new MessageTrigger instance to validate GitHub links.
         *
         * @param isValid A function that returns true if the given match is valid.
         */
        fun gitHub(isValid: (GitHubLink) -> Boolean): MessageTrigger<GitHubLink> =
            MessageTrigger(gitHubTransformer) { isValid(it) }
    }

    /**
     * Checks if the message is valid.
     *
     * @param content The message to check.
     * @return A list of results.
     */
    private fun check(content: String): List<T> =
        regex.transform(content)
            .filter { validate(it) }
            .toList()

    /**
     * Checks if the message is valid and runs the trigger for
     * each matching group.
     *
     * @param message The message to check.
     * @param block The trigger to run for each group.
     */
    suspend fun run(message: Message, block: suspend (TriggerData<T>) -> Unit) {
        // Ignore webhooks
        if (message.webhookId != null) return

        // Check for valid links
        val links = check(message.content)
        if (links.isEmpty()) return

        // Run trigger for each link
        val stripped = message.content.replace(" ", "")
        links.forEach {
            // Check if in spoiler
            val spoiler = stripped.contains("||$it||", true)
            val suppressed = stripped.contains("<$it>", true)

            // Run trigger
            block(TriggerData(it, spoiler, suppressed))
        }
    }

    data class TriggerData<T>(val match: T, val spoiler: Boolean, val suppressed: Boolean) {
        /**
         * Appropriately styles some content based on the trigger.
         *
         * @param content The content to style.
         */
        fun style(content: String): String =
            when {
                spoiler -> "||$content||"
                suppressed -> "<$content>"
                else -> content
            }
    }
}