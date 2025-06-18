package filipus.backupyFilipus.utils

import org.bukkit.configuration.file.FileConfiguration
import net.md_5.bungee.api.ChatColor

class MessageProvider(private val config: FileConfiguration) {
    fun get(key: String, placeholders: Map<String, String> = emptyMap()): String {
        val message = config.getString("messages.$key") ?: return "§cBrak wiadomości dla '$key'"
        val replacedMessage = placeholders.entries.fold(message) { acc, (placeholder, value) ->
            acc.replace("%$placeholder%", value.escapeFormatting())
        }
        return ChatColor.translateAlternateColorCodes('&', replacedMessage)
    }

    private fun String.escapeFormatting(): String {
        return replace("&", "&&").replace("§", "&§")
    }
}
