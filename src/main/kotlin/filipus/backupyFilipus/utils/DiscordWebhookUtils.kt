package filipus.backupyFilipus.utils

import java.net.HttpURLConnection
import java.net.URL

fun sendDiscordWebhook(webhookUrl: String, message: String, embedTitle: String, embedDescription: String, embedImageUrl: String) {
    val url = URL(webhookUrl)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")

    val jsonPayload = """
        {
            "content": "$message",
            "embeds": [{
                "title": "$embedTitle",
                "description": "$embedDescription",
                "color": 3447003,
                "image": {
                    "url": "$embedImageUrl"
                }
            }]
        }
    """
    connection.outputStream.use { os ->
        os.write(jsonPayload.toByteArray(Charsets.UTF_8))
    }

    connection.inputStream.use { it.readBytes() } // To trigger the request
}