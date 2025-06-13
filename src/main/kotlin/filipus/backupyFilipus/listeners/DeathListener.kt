package filipus.backupyFilipus.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import filipus.backupyFilipus.BackupyFilipus

class DeathListener(private val plugin: BackupyFilipus) : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        plugin.backupManager.savePlayerInventory(player)
    }
}