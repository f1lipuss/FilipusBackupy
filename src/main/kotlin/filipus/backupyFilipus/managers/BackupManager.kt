package filipus.backupyFilipus.managers

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import filipus.backupyFilipus.BackupyFilipus
import java.io.File
import java.util.*

class BackupManager(private val plugin: BackupyFilipus) {

    private val backupsFolder = File(plugin.dataFolder, "backups").apply { mkdirs() }
    private val pendingRestoresFile = File(plugin.dataFolder, "pending_restores.yml")
    private var pendingRestoresConfig: FileConfiguration = YamlConfiguration.loadConfiguration(pendingRestoresFile)
    private val pendingRestores = mutableMapOf<UUID, RestoreData>()

    init {
        if (!pendingRestoresFile.exists()) {
            pendingRestoresFile.createNewFile()
        }
    }

    fun savePlayerInventory(player: Player) {
        val items = player.inventory.contents?.mapIndexed { index, item ->
            index to (item ?: ItemStack(Material.AIR))
        }?.toMap() ?: emptyMap()

        val backupInfo = BackupInfo(
            items,
            System.currentTimeMillis(),
            null,
            player.level,
            player.exp,
            player.totalExperience
        )

        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        val config = YamlConfiguration.loadConfiguration(backupFile)

        val deathTime = backupInfo.timestamp
        val deathKey = "deaths.$deathTime"

        backupInfo.items.forEach { (index, itemStack) ->
            config.set("$deathKey.inventory.$index", itemStack)
        }
        config.set("$deathKey.location", player.location)

        config.set("$deathKey.experience.level", backupInfo.level)
        config.set("$deathKey.experience.exp", backupInfo.exp.toDouble())
        config.set("$deathKey.experience.total", backupInfo.totalExp)

        config.save(backupFile)
    }

    data class BackupInfo(
        val items: Map<Int, ItemStack>,
        val timestamp: Long,
        var givenBy: String? = null,
        val level: Int,
        val exp: Float,
        val totalExp: Int
    )

    fun getPlayerBackups(player: Player): Map<Long, BackupInfo> {
        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        if (!backupFile.exists()) return emptyMap()

        val config = YamlConfiguration.loadConfiguration(backupFile)
        val backups = mutableMapOf<Long, BackupInfo>()

        config.getConfigurationSection("deaths")?.getKeys(false)?.forEach { timeString ->
            val time = timeString.toLong()
            val items = mutableMapOf<Int, ItemStack>()
            config.getConfigurationSection("deaths.$timeString.inventory")?.getKeys(false)?.forEach { slot ->
                items[slot.toInt()] = config.getItemStack("deaths.$timeString.inventory.$slot") ?: ItemStack(Material.AIR)
            }
            val givenBy = config.getString("deaths.$timeString.givenBy")
            val level = config.getInt("deaths.$timeString.experience.level", 0)
            val exp = config.getDouble("deaths.$timeString.experience.exp", 0.0).toFloat()
            val totalExp = config.getInt("deaths.$timeString.experience.total", 0)
            backups[time] = BackupInfo(items, time, givenBy, level, exp, totalExp)
        }

        return backups
    }

    fun markBackupAsGiven(player: Player, timestamp: Long, givenBy: String) {
        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        if (!backupFile.exists()) return

        val config = YamlConfiguration.loadConfiguration(backupFile)
        config.set("deaths.$timestamp.givenBy", givenBy)
        config.save(backupFile)
    }

    fun hasPendingRestore(player: Player): Boolean {
        return pendingRestoresConfig.contains(player.uniqueId.toString())
    }

    fun getPendingRestore(player: Player): RestoreData? {
        return pendingRestores[player.uniqueId]
    }

    fun addPendingRestore(player: Player, items: List<ItemStack>, level: Int, exp: Float, totalExp: Int) {
        pendingRestores[player.uniqueId] = RestoreData(items, level, exp, totalExp)
        savePendingRestores()
    }

    fun removePendingRestore(player: Player) {
        pendingRestores.remove(player.uniqueId)
        savePendingRestores()
    }

    fun clearPendingRestore(player: Player) {
        pendingRestoresConfig.set(player.uniqueId.toString(), null)
        savePendingRestores()
    }

    private fun savePendingRestores() {
        val config = YamlConfiguration()
        pendingRestores.forEach { (uuid, data) ->
            config.set("$uuid.items", data.items)
            config.set("$uuid.level", data.level)
            config.set("$uuid.exp", data.exp)
            config.set("$uuid.totalExp", data.totalExp)
        }
        config.save(pendingRestoresFile)
    }

    fun restoreInventory(player: Player, timestamp: Long) {
        val backupFile = File(backupsFolder, "${player.uniqueId}.yml")
        if (!backupFile.exists()) return

        val config = YamlConfiguration.loadConfiguration(backupFile)
        val deathKey = "deaths.$timestamp"

        // Przywracanie przedmiotów
        val inventory = player.inventory
        config.getConfigurationSection("$deathKey.inventory")?.getKeys(false)?.forEach { slotString ->
            val slot = slotString.toInt()
            val item = config.getItemStack("$deathKey.inventory.$slotString")
            if (item != null) {
                if (slot in 0..35) {
                    inventory.setItem(slot, item)
                } else if (slot in 36..39) {
                    // Sloty 36-39 to zbroja
                    inventory.setItem(slot - 36 + 36, item) // Konwersja na właściwe sloty zbroi
                } else if (slot == 40) {
                    // Slot 40 to offhand
                    inventory.setItem(40, item)
                }
            }
        }

        // Przywracanie doświadczenia
        val level = config.getInt("$deathKey.experience.level", 0)
        val exp = config.getDouble("$deathKey.experience.exp", 0.0).toFloat()
        player.level = level
        player.exp = exp

        // Usuwanie przywróconego backupu
        config.set(deathKey, null)
        config.save(backupFile)

        player.sendMessage(plugin.messages.get("backup-restored-success"))
    }

    fun reloadPendingRestores(config: FileConfiguration) {
        pendingRestoresConfig = config
    }

    fun openBackupPreviewGUI(player: Player, targetPlayer: Player, backupInfo: BackupInfo) {
        // Implementacja z GUIUtils.kt
    }
}

data class RestoreData(
    val items: List<ItemStack>,
    val level: Int,
    val exp: Float,
    val totalExp: Int
)
