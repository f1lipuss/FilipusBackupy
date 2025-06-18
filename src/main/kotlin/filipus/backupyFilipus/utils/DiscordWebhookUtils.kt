package filipus.backupyFilipus.utils

import org.bukkit.plugin.java.JavaPlugin
import java.net.URL
import java.net.HttpURLConnection
import filipus.backupyFilipus.utils.MessageProvider

fun sendDiscordWebhook(webhookUrl: String, message: String, embedTitle: String, embedDescription: String) {
    val url = URL(webhookUrl)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")

    val jsonPayload = """
        {
            "content": ${message.escapeJson()},
            "embeds": [{
                "title": ${embedTitle.escapeJson()},
                "description": ${embedDescription.escapeJson()},
                "color": 3447003
            }]
        }
    """.trimIndent()

    connection.outputStream.use { os ->
        os.write(jsonPayload.toByteArray(Charsets.UTF_8))
    }

    connection.inputStream.use { it.readBytes() }
}

fun sendDiscordWebhook(
    plugin: JavaPlugin,
    messageProvider: MessageProvider,
    templateKey: String,
    placeholders: Map<String, String> = emptyMap()
) {
    val webhookUrl = plugin.config.getString("discord.webhookUrl") ?: return
    val titleKey = "discord-$templateKey-title"
    val descriptionKey = "discord-$templateKey-description"

    var title = messageProvider.get(titleKey)
    var description = messageProvider.get(descriptionKey)

    placeholders.forEach { (key, value) ->
        title = title.replace("%$key%", value)
        description = description.replace("%$key%", value)
    }

    sendDiscordWebhook(webhookUrl, "", title, description)
}

private fun String.escapeJson(): String {
    return "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
