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
            sender.sendMessage("Komenda tylko dla graczy.")
            return true
        }

        if (!sender.hasPermission("backupyfilipus.backup")) {
            sender.sendMessage("Nie masz uprawnien do tej komendy | backupyfilipus.backup")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("Uzyj /backup <gracz>")
            return true
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null) {
            sender.sendMessage("Nie znaleziono gracza z taka nazwa")
            return true
        }

        openBackupGUI(plugin, sender, targetPlayer)
        return true
    }
}