package org.britmoji.yoodle.extensions

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream

class TranscodeExtension(override val name: String = "Transcoder") : Extension() {

    private val logger = KotlinLogging.logger { }

    override suspend fun setup() {
        event<MessageCreateEvent> {
            action {
                // Check the attached files
                event.message.attachments.forEach {
                    // Check the filetype
                    when (it.contentType?.split(";")?.get(0) ?: return@forEach) {
                        "image/svg+xml" -> {
                            try {
                                // Transcode the SVG to PNG
                                val input = TranscoderInput(it.url)

                                // Store the bytes
                                val outputStream = ByteArrayOutputStream()
                                val output = TranscoderOutput(outputStream)

                                // Transcode
                                PNGTranscoder().transcode(input, output)

                                // Send the message
                                event.message.channel.createMessage {
                                    addFile(
                                        "image.png",
                                        ChannelProvider {
                                            outputStream.toByteArray().inputStream().toByteReadChannel()
                                        })
                                }

                                logger.info { "Transcode ${it.filename} from SVG -> PNG (${event.message.channel.id}" }
                            } catch (e: Exception) {
                                logger.error(e) { "Error transcoding ${it.filename} from SVG -> PNG (${event.message.channel.id}" }
                            }
                        }
                    }
                }
            }
        }
    }
}