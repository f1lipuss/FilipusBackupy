package filipus.backupyFilipus.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import filipus.backupyFilipus.BackupyFilipus

class DeathListener(private val plugin: BackupyFilipus) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        try {
            plugin.backupManager.savePlayerInventory(player)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
