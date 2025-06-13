package filipus.backupyFilipus.managers

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import filipus.backupyFilipus.BackupyFilipus
import java.io.File
import java.util.*

class BackupManager(private val plugin: BackupyFilipus) {

    private val backupsFolder = File(plugin.dataFolder, "backups").apply { mkdirs() }

    fun savePlayerInventory(player: Player) {
        val inventory = player.inventory.contents
        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        val config = YamlConfiguration.loadConfiguration(backupFile)

        val deathTime = Date().time
        val deathKey = "deaths.$deathTime"

        inventory.forEachIndexed { index, itemStack ->
            if (itemStack != null) {
                config.set("$deathKey.inventory.$index", itemStack)
            }
        }
        config.set("$deathKey.location", player.location)

        config.save(backupFile)
    }

    fun getPlayerBackups(player: Player): Map<Long, Map<Int, ItemStack>> {
        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        if (!backupFile.exists()) return emptyMap()

        val config = YamlConfiguration.loadConfiguration(backupFile)
        val backups = mutableMapOf<Long, Map<Int, ItemStack>>()

        config.getConfigurationSection("deaths")?.getKeys(false)?.forEach { timeString ->
            val time = timeString.toLongOrNull() ?: return@forEach
            val inventorySection = config.getConfigurationSection("deaths.$timeString.inventory") ?: return@forEach
            val inventory = mutableMapOf<Int, ItemStack>()
            inventorySection.getKeys(false).forEach { slotString ->
                val slot = slotString.toIntOrNull() ?: return@forEach
                val itemStack = inventorySection.getItemStack(slotString) ?: return@forEach
                inventory[slot] = itemStack
            }
            backups[time] = inventory
        }

        return backups
    }

    fun hasPendingRestore(player: Player): Boolean {
        return plugin.config.getConfigurationSection("pending_restores")?.contains(player.uniqueId.toString()) ?: false
    }

    fun getPendingRestore(player: Player): List<ItemStack>? {
        val pendingRestores = plugin.config.getConfigurationSection("pending_restores") ?: return null
        val items = pendingRestores.getList(player.uniqueId.toString()) as? List<ItemStack>
        return items
    }

    fun clearPendingRestore(player: Player) {
        val pendingRestores = plugin.config.getConfigurationSection("pending_restores")
        pendingRestores?.set(player.uniqueId.toString(), null)
        plugin.saveConfig()
    }

    fun restoreInventory(player: Player, items: Map<Int, ItemStack>) {
        val pendingRestores = plugin.config.getConfigurationSection("pending_restores") ?: plugin.config.createSection("pending_restores")
        pendingRestores.set(player.uniqueId.toString(), items.values.toList())
        plugin.saveConfig()
    }
}