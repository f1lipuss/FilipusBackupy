package filipus.backupyFilipus.listeners

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.utils.sendDiscordWebhook
import filipus.backupyFilipus.utils.openBackupGUI
import filipus.backupyFilipus.utils.openBackupPreviewGUI

class InventoryListener(private val plugin: BackupyFilipus) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.clickedInventory ?: return
        val title = event.view.title

        when {
            title.startsWith("Backupy ") -> handleBackupListClick(event, player)
            title.startsWith("Podgląd backupu: ") -> handleBackupPreviewClick(event, player)
            title.startsWith(plugin.messages.get("preview-backup-title", mapOf("player" to ""))) -> {
                event.isCancelled = true

                val targetPlayerName = title.substringAfter(plugin.messages.get("preview-backup-title", mapOf("player" to "")))
                val targetPlayer = plugin.server.getPlayer(targetPlayerName) ?: return

                when (event.currentItem?.type) {
                    Material.GREEN_CANDLE -> {
                        handleBackupGive(player, targetPlayer)
                    }
                    Material.RED_CANDLE -> {
                        openBackupGUI(plugin, player, targetPlayer)
                    }
                }
            }
        }
    }

    private fun handleBackupListClick(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true

        val clickedItem = event.currentItem ?: return
        if (clickedItem.type != Material.CHEST) return

        val targetPlayerName = event.view.title.substringAfter("Backupy ")
        val targetPlayer = plugin.server.getPlayer(targetPlayerName) ?: return

        val backups = plugin.backupManager.getPlayerBackups(targetPlayer)
        val clickedIndex = event.slot

        val selectedBackup = backups.entries.sortedByDescending { it.key }.getOrNull(clickedIndex) ?: return
        val backupInfo = selectedBackup.value

        openBackupPreviewGUI(plugin, player, targetPlayer, backupInfo)
    }

    private fun handleBackupPreviewClick(event: InventoryClickEvent, player: Player) {
        event.isCancelled = true

        val clickedItem = event.currentItem ?: return
        val targetPlayerName = event.view.title.substringAfter("Podgląd backupu: ")
        val targetPlayer = plugin.server.getPlayer(targetPlayerName) ?: return

        when (clickedItem.type) {
            Material.GREEN_CANDLE -> {
                handleBackupGive(player, targetPlayer)
            }
            Material.RED_CANDLE -> {
                openBackupGUI(plugin, player, targetPlayer)
            }
        }
    }

    private fun handleBackupGive(player: Player, targetPlayer: Player) {
        val backups = plugin.backupManager.getPlayerBackups(targetPlayer)
        val selectedBackup = backups.entries.sortedByDescending { it.key }.firstOrNull()?.value ?: return

        plugin.backupManager.addPendingRestore(
            targetPlayer,
            selectedBackup.items.values.toList(),
            selectedBackup.level,
            selectedBackup.exp,
            selectedBackup.totalExp
        )
        plugin.backupManager.markBackupAsGiven(targetPlayer, selectedBackup.timestamp, player.name)

        player.sendMessage(plugin.messages.get("backup-given", mapOf("target" to targetPlayer.name)))
        targetPlayer.sendMessage(plugin.messages.get("backup-received"))

        sendDiscordWebhook(
            plugin,
            plugin.messages,
            "backup-given",
            mapOf(
                "target" to targetPlayer.name,
                "admin" to player.name
            )
        )

        player.closeInventory()
    }
}
