package ru.anomaly.weather;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anomaly.weather.anomaly.AnomalyManager;
import ru.anomaly.weather.command.AnomalyCommand;
import ru.anomaly.weather.listener.PlayerListener;
import ru.anomaly.weather.protection.ProtectionManager;
import ru.anomaly.weather.util.Messages;

/**
 * AnomalousWeather — аномальная погода для Paper 1.21+.
 *
 * <p>Обычная погода сервера не отключается: аномалии запускаются поверх неё
 * и выделяются BossBar'ом, тайтлами, частицами, звуками и подсказками.</p>
 */
public final class AnomalousWeatherPlugin extends JavaPlugin {

    private Messages messages;
    private ProtectionManager protectionManager;
    private AnomalyManager anomalyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messages = new Messages(this);
        this.protectionManager = new ProtectionManager(this);
        this.anomalyManager = new AnomalyManager(this);

        this.protectionManager.load();
        this.anomalyManager.load();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        AnomalyCommand executor = new AnomalyCommand(this);
        PluginCommand command = getCommand("anomaly");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Команда /anomaly не найдена в plugin.yml!");
        }

        getLogger().info("AnomalousWeather включён. Загружено аномалий: " + anomalyManager.settings().size());
    }

    @Override
    public void onDisable() {
        if (anomalyManager != null) {
            anomalyManager.shutdown();
        }
        if (protectionManager != null) {
            protectionManager.unregisterRecipes();
        }
    }

    /** Полная перезагрузка: config.yml, предметы защиты и аномалии. */
    public void reloadEverything() {
        reloadConfig();
        protectionManager.load();
        anomalyManager.load();
    }

    public Messages messages() {
        return messages;
    }

    public ProtectionManager protectionManager() {
        return protectionManager;
    }

    public AnomalyManager anomalyManager() {
        return anomalyManager;
    }
}
