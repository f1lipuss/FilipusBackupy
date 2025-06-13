package filipus.backupyFilipus.listeners

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import filipus.backupyFilipus.BackupyFilipus
import filipus.backupyFilipus.utils.sendDiscordWebhook

class InventoryListener(private val plugin: BackupyFilipus) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.view // Use event.view to get the inventory view
        val title = inventory.title

        // Check if the inventory is your custom GUI
        if (!title.startsWith("Backupy: ")) return
        event.isCancelled = true // Cancel the event to prevent item movement

        val clickedItem = event.currentItem ?: return
        if (clickedItem.type != Material.CHEST) return

        val targetPlayerName = title.substringAfter("Backupy: ")
        val targetPlayer = plugin.server.getPlayer(targetPlayerName) ?: return

        val backups = plugin.backupManager.getPlayerBackups(targetPlayer)
        val clickedIndex = event.slot

        val selectedBackup = backups.entries.sortedByDescending { it.key }.getOrNull(clickedIndex) ?: return
        val items = selectedBackup.value

        plugin.backupManager.restoreInventory(targetPlayer, items)
        player.sendMessage("§aPomyslnie nadano backupa dla: $targetPlayerName§7.")
        targetPlayer.sendMessage("§aNadano ci Backupa! Odbierz: /backupodbierz")

        // Send message to Discord webhook
        val webhookUrl = plugin.config.getString("discord.webhookUrl") ?: return
        val embedTitle = "Backup Notification"
        val embedDescription = "Backup nadany dla gracza: $targetPlayerName przez $player.name"
        val embedImageUrl = "https://i.imgur.com/yourImage.png" // Podaj URL do swojego obrazu
        sendDiscordWebhook(webhookUrl, "", embedTitle, embedDescription, embedImageUrl)

        player.closeInventory()
    }
}