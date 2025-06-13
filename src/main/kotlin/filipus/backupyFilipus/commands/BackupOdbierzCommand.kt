package filipus.backupyFilipus.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.utils.sendDiscordWebhook

class BackupOdbierzCommand(private val plugin: BackupyFilipus) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Ta komenda jest tylko dla graczy.")
            return true
        }

        val backupManager = plugin.backupManager

        if (!backupManager.hasPendingRestore(sender)) {
            sender.sendMessage("Nie masz żadnego zaplanowanego odbioru.")
            return true
        }

        val items = backupManager.getPendingRestore(sender)
        if (items == null) {
            sender.sendMessage("An error occurred while retrieving your backup.")
            return true
        }

        for (item in items) {
            if (item != null) {
                val leftover = sender.inventory.addItem(item)
                if (leftover.isNotEmpty()) {
                    sender.world.dropItemNaturally(sender.location, leftover.values.first())
                }
            }
        }

        backupManager.clearPendingRestore(sender)
        sender.sendMessage("Your backup has been restored!")

        // Get webhook URL from config
        val webhookUrl = plugin.config.getString("discord.webhookUrl") ?: return true
        val embedTitle = "Backup Restored"
        val embedDescription = "Backup odebrany przez gracza: ${sender.name}"
        val embedImageUrl = "https://i.imgur.com/yourImage.png" // Podaj URL do swojego obrazu
        sendDiscordWebhook(webhookUrl, "", embedTitle, embedDescription, embedImageUrl)

        return true
    }
}