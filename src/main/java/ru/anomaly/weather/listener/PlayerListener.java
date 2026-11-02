package ru.anomaly.weather.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import ru.anomaly.weather.AnomalousWeatherPlugin;

/**
 * События игроков: BossBar/визуал, открытие рецептов и защита предметов
 * от случайной потери (выброс, рамки, портал Края).
 */
public final class PlayerListener implements Listener {

    private final AnomalousWeatherPlugin plugin;

    public PlayerListener(AnomalousWeatherPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.protectionManager().unlockRecipes(event.getPlayer());
        plugin.anomalyManager().handleJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.anomalyManager().handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.anomalyManager().syncPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> plugin.anomalyManager().syncPlayer(event.getPlayer()));
    }

    /** Запрет выбрасывать защитный предмет (Q). */
    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.protectionManager().preventsDrop(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(plugin.messages().get("item-cannot-drop"));
        }
    }

    /** Запрет вставлять защитный предмет в рамку портала Края (он бы сгорел). */
    @EventHandler(ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.END_PORTAL_FRAME) {
            return;
        }
        if (plugin.protectionManager().preventsDrop(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(plugin.messages().get("item-cannot-drop"));
        }
    }

    /** Запрет вешать защитный предмет в рамку. */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (plugin.protectionManager().preventsDrop(item)) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(plugin.messages().get("item-cannot-drop"));
        }
    }
}
