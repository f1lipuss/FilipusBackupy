package filipus.backupyFilipus.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import filipus.backupyFilipus.BackupyFilipus

class BackupyReloadCommand(private val plugin: BackupyFilipus) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("777Code.backupreload")) {
            sender.sendMessage(plugin.messages.get("no-permission"))
            return true
        }

        try {
            plugin.reloadAllConfigs()
            sender.sendMessage(plugin.messages.get("config-reloaded"))
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error"
            sender.sendMessage(plugin.messages.get("config-reload-error", mapOf("error" to errorMessage)))
            plugin.logger.severe("Error reloading config: $errorMessage")
            e.printStackTrace()
        }

        return true
    }
}
