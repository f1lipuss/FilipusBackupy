package filipus.backupyFilipus.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import filipus.backupyFilipus.BackupyFilipus
import java.text.SimpleDateFormat
import java.util.*

fun openBackupGUI(plugin: BackupyFilipus, player: Player, targetPlayer: Player) {
    val backups = plugin.backupManager.getPlayerBackups(targetPlayer)
    if (backups.isEmpty()) {
        player.sendMessage("Nie znaleziono smierci gracza: ${targetPlayer.name}.")
        return
    }

    val inventory = Bukkit.createInventory(null, 54, "Backupy: ${targetPlayer.name}")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    backups.entries.sortedByDescending { it.key }.forEachIndexed { index, (time, items) ->
        if (index >= 54) return@forEachIndexed
        val chest = ItemStack(Material.CHEST)
        val meta = chest.itemMeta
        meta?.setDisplayName("§8(${dateFormat.format(Date(time))}§8)")
        meta?.lore = listOf("§7Nacisnij aby wyslac backupa do gracza")
        chest.itemMeta = meta
        inventory.setItem(index, chest)
    }

    player.openInventory(inventory)
}