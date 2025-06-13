package filipus.backupyFilipus

import org.bukkit.plugin.java.JavaPlugin
import filipus.backupyFilipus.commands.BackupCommand
import filipus.backupyFilipus.commands.BackupOdbierzCommand
import filipus.backupyFilipus.listeners.DeathListener
import filipus.backupyFilipus.listeners.InventoryListener
import filipus.backupyFilipus.managers.BackupManager
import org.bukkit.command.Command

class BackupyFilipus : JavaPlugin() {

    lateinit var backupManager: BackupManager
        private set

    override fun onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig()

        backupManager = BackupManager(this)

        registerCommands()
        registerListeners()

        logger.info("BackupyFilipus WYLACZONY")
    }

    override fun onDisable() {
        logger.info("BackupyFilipus WLACZONY")
    }

    private fun registerCommands() {
        val backupCommand = BackupCommand(this)
        val backupOdbierzCommand = BackupOdbierzCommand(this)

        registerCommand("backup", backupCommand)
        registerCommand("backupodbierz", backupOdbierzCommand)
    }

    private fun registerCommand(name: String, executor: org.bukkit.command.CommandExecutor) {
        val fallbackPrefix = description.name.lowercase()
        val commandMap = server.commandMap
        val newCommand = object : Command(name) {
            override fun execute(sender: org.bukkit.command.CommandSender, commandLabel: String, args: Array<out String>): Boolean {
                return executor.onCommand(sender, this, commandLabel, args)
            }
        }
        commandMap.register(fallbackPrefix, newCommand)
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(DeathListener(this), this)
        server.pluginManager.registerEvents(InventoryListener(this), this)
    }
}