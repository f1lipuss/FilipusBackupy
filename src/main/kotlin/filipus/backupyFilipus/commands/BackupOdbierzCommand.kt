package filipus.backupyFilipus.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.utils.sendDiscordWebhook
import org.bukkit.inventory.ItemStack

class BackupOdbierzCommand(private val plugin: BackupyFilipus) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.get("player-only-command"))
            return true
        }

        val player = sender
        val restoreData = plugin.backupManager.getPendingRestore(player)

        if (restoreData == null) {
            player.sendMessage(plugin.messages.get("no-pending-backup"))
            plugin.logger.info("Gracz ${player.name} próbował odebrać backup, ale nie znaleziono pending restore")
            return true
        }

        try {
            // Przywróć przedmioty i XP
            player.inventory.clear()
            restoreData.items.forEach { item ->
                if (item != null) {
                    val leftover = player.inventory.addItem(item)
                    if (leftover.isNotEmpty()) {
                        leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
                    }
                }
            }

            player.level = restoreData.level
            player.exp = restoreData.exp
            player.totalExperience = restoreData.totalExp

            plugin.backupManager.removePendingRestore(player)
            player.sendMessage(plugin.messages.get("backup-restored-success"))
            plugin.logger.info("Pomyślnie przywrócono backup dla gracza: ${player.name}")

            sendDiscordWebhook(
                plugin,
                plugin.messages,
                "backup-restored",
                mapOf("player" to player.name)
            )
        } catch (e: Exception) {
            player.sendMessage(plugin.messages.get("backup-restore-error"))
            plugin.logger.severe("Error restoring backup for ${player.name}: ${e.message}")
            e.printStackTrace()
        }

        return true
    }
}
