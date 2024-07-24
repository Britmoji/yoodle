package org.britmoji.yoodle.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.color
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalAttachment
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalEmoji
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.utils.canInteract
import com.kotlindiscord.kord.extensions.utils.download
import com.kotlindiscord.kord.extensions.utils.getTopRole
import com.kotlindiscord.kord.extensions.utils.selfMember
import dev.kord.common.Color
import dev.kord.core.behavior.createRole
import dev.kord.core.behavior.edit
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.StandardEmoji
import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class CustomizeExtension(override val name: String = "Customize") : Extension() {
    override suspend fun setup() {
        publicSlashCommand {
            name = "customize"
            description = "Change your color or icon"

            publicSubCommand(::RoleArguments) {
                name = "color"
                description = "Change your color"

                action {
                    val role = getOrCreateRole(this.member!!.asMember(), arguments.color)
                    role.edit { this.color = arguments.color }

                    this.member?.addRole(role.id)
                    respond { content = "Changed color to ${role.color.toHex()}" }
                }
            }

            publicSubCommand(::IconArguments) {
                name = "icon"
                description = "Change your icon"

                action {
                    val role = getOrCreateRole(this.member!!.asMember())

                    // Delete role
                    if (arguments.emoji == null && arguments.image == null) {
                        respond { content = "You need to specify an emoji or an image." }
                        return@action
                    }

                    // Get the icon as an image
                    when {
                        arguments.emoji != null -> when (val emoji = arguments.emoji!!) {
                            is StandardEmoji -> role.edit { this.unicodeEmoji = emoji.name }
                            is GuildEmoji -> role.edit { this.icon = emoji.image.getImage() }
                        }

                        arguments.image != null -> {
                            if (arguments.image?.isImage == false) {
                                respond { content = "That's not an image." }
                                return@action
                            }

                            if (arguments.image!!.size > 102400) {
                                respond { content = "That image is too big." }
                                return@action
                            }

                            role.edit {
                                this.icon = Image.raw(
                                    arguments.image!!.download(),
                                    Image.Format.fromContentType(arguments.image!!.contentType!!)
                                )
                            }
                        }
                    }

                    // Add role
                    this.member?.addRole(role.id)
                    respond { content = "Changed icon!" }
                }
            }

            publicSubCommand {
                name = "remove"
                description = "Remove your color and icon"

                action {
                    val role = getOrCreateRole(this.member!!.asMember())
                    role.delete()
                    respond { content = "Removed color and icon." }
                }
            }
        }
    }

    private suspend fun getOrCreateRole(member: Member, color: Color? = null): Role {
        val name = getRoleName(member)
        val existing = member.guild.roles.firstOrNull { it.name == name }
        if (existing != null) return existing

        val highestRole = member.guild.roles.toList()
            .filter { member.guild.selfMember().getTopRole()?.canInteract(it) == true }
            .maxByOrNull { it.rawPosition }

        return member.guild.createRole {
            this.name = name
            this.color = color
        }.also { it.changePosition(highestRole?.rawPosition ?: 0).collect() }
    }

    private inner class RoleArguments : Arguments() {
        val color by color {
            name = "color"
            description = "The color to set"
        }
    }

    private inner class IconArguments : Arguments() {
        val emoji by optionalEmoji {
            name = "emoji"
            description = "The emoji to set"
            ignoreErrors = true
        }

        val image by optionalAttachment {
            name = "image"
            description = "The image to set"
        }
    }

    private fun getRoleName(user: User): String = "🎨 ${user.id}"
    private fun Color.toHex(): String = "#${this.rgb.toString(16).padStart(6, '0')}"
}