package filipus.backupyFilipus

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.configuration.file.YamlConfiguration
import filipus.backupyFilipus.commands.BackupCommand
import filipus.backupyFilipus.commands.BackupOdbierzCommand
import filipus.backupyFilipus.commands.BackupyReloadCommand
import filipus.backupyFilipus.listeners.DeathListener
import filipus.backupyFilipus.listeners.InventoryListener
import filipus.backupyFilipus.managers.BackupManager
import filipus.backupyFilipus.utils.MessageProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.io.File

class BackupyFilipus : JavaPlugin() {

    lateinit var messages: MessageProvider
    lateinit var backupManager: BackupManager
        private set

    override fun onEnable() {
        loadConfig()

        val pendingRestoresFile = File(dataFolder, "pending_restores.yml")
        if (!pendingRestoresFile.exists()) {
            saveResource("pending_restores.yml", false)
        }

        saveResource("messages.yml", false)
        val messagesConfig = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))
        messages = MessageProvider(messagesConfig)

        backupManager = BackupManager(this)

        registerCommands()
        registerListeners()

        logger.info("777-Backupy włączony")
    }

    override fun onDisable() {
        logger.info("777-Backupy wyłączony")
    }

    private fun loadConfig() {
        saveDefaultConfig()
        reloadConfig()
    }

    private fun registerCommands() {
        val backupCommand = BackupCommand(this)
        val backupOdbierzCommand = BackupOdbierzCommand(this)
        val backupyReloadCommand = BackupyReloadCommand(this)

        getCommand("backup")?.setExecutor(backupCommand)
        getCommand("backupodbierz")?.setExecutor(backupOdbierzCommand)
        getCommand("backupreload")?.setExecutor(backupyReloadCommand)
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(DeathListener(this), this)
        server.pluginManager.registerEvents(InventoryListener(this), this)
    }

    fun reloadAllConfigs() {
        reloadConfig()

        val messagesConfig = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))
        messages = MessageProvider(messagesConfig)

        val pendingRestoresFile = File(dataFolder, "pending_restores.yml")
        if (pendingRestoresFile.exists()) {
            val pendingRestoresConfig = YamlConfiguration.loadConfiguration(pendingRestoresFile)
            backupManager.reloadPendingRestores(pendingRestoresConfig)
        }
    }
}
