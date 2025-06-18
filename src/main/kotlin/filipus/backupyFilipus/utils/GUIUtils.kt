package filipus.backupyFilipus.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.managers.BackupManager
import java.text.SimpleDateFormat
import java.util.*

fun openBackupGUI(plugin: BackupyFilipus, player: Player, targetPlayer: Player) {
    val backups = plugin.backupManager.getPlayerBackups(targetPlayer)
    if (backups.isEmpty()) {
        player.sendMessage(plugin.messages.get("no-backups-found", mapOf("player" to targetPlayer.name)))
        return
    }

    val inventory = Bukkit.createInventory(null, 54, "Backupy ${targetPlayer.name}")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    backups.entries.sortedByDescending { it.key }.forEachIndexed { index, (time, backupInfo) ->
        if (index >= 54) return@forEachIndexed
        val chest = ItemStack(Material.CHEST)
        val meta = chest.itemMeta
        meta?.setDisplayName(plugin.messages.get("backup-item-name", mapOf("name" to targetPlayer.name, "number" to (index + 1).toString())))

        val lore = mutableListOf<String>()
        lore.add(plugin.messages.get("backup-item-lore-date", mapOf("DATE" to dateFormat.format(Date(time)))))
        if (backupInfo.givenBy != null) {
            lore.add(plugin.messages.get("backup-item-lore-given-by", mapOf("GIVEN_BY" to (backupInfo.givenBy ?: ""))))
        } else {
            lore.add(plugin.messages.get("backup-item-lore-not-given"))
        }
        lore.add(plugin.messages.get("backup-item-lore-click"))

        meta?.lore = lore
        chest.itemMeta = meta
        inventory.setItem(index, chest)
    }

    player.openInventory(inventory)
}

fun openBackupPreviewGUI(plugin: BackupyFilipus, player: Player, targetPlayer: Player, backupInfo: BackupManager.BackupInfo) {
    val inventory = Bukkit.createInventory(null, 54, plugin.messages.get("preview-backup-title", mapOf("player" to targetPlayer.name)))

    backupInfo.items.forEach { (slot, item) ->
        inventory.setItem(slot, item)
    }

    val xpButton = ItemStack(Material.EXPERIENCE_BOTTLE)
    val xpMeta = xpButton.itemMeta
    xpMeta?.setDisplayName(plugin.messages.get("xp-button-name", mapOf("level" to backupInfo.level.toString())))
    xpButton.itemMeta = xpMeta
    inventory.setItem(51, xpButton)

    val giveButton = ItemStack(Material.GREEN_CANDLE)
    val giveMeta = giveButton.itemMeta
    giveMeta?.setDisplayName(plugin.messages.get("give-button-name"))
    giveMeta?.lore = listOf(plugin.messages.get("give-button-lore"))
    giveButton.itemMeta = giveMeta
    inventory.setItem(52, giveButton)

    val backButton = ItemStack(Material.RED_CANDLE)
    val backMeta = backButton.itemMeta
    backMeta?.setDisplayName(plugin.messages.get("back-button"))
    backMeta?.lore = listOf(plugin.messages.get("back-button-lore"))
    backButton.itemMeta = backMeta
    inventory.setItem(53, backButton)

    player.openInventory(inventory)
}
