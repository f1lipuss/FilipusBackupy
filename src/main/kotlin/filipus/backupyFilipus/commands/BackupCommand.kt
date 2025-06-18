package filipus.backupyFilipus.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.utils.openBackupGUI

class BackupCommand(private val plugin: BackupyFilipus) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.get("only-player-command"))
            return true
        }

        if (!sender.hasPermission("777Code.backup")) {
            sender.sendMessage(plugin.messages.get("no-permission"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.get("use-correct-command"))
            return true
        }

        val targetPlayer = Bukkit.getPlayerExact(args[0])
        if (targetPlayer == null) {
            sender.sendMessage(plugin.messages.get("player-not-found"))
            return true
        }

        openBackupGUI(plugin, sender, targetPlayer)
        return true
    }
}
