package ru.anomaly.weather.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import ru.anomaly.weather.AnomalousWeatherPlugin;

/**
 * Сообщения из секции {@code messages} конфига.
 * Поддерживает плейсхолдеры вида {@code {weather}} и {prefix}.
 */
public final class Messages {

    private final AnomalousWeatherPlugin plugin;

    public Messages(AnomalousWeatherPlugin plugin) {
        this.plugin = plugin;
    }

    public Component get(String key, String... placeholders) {
        String raw = plugin.getConfig().getString("messages." + key, "<red>[нет сообщения: " + key + "]");
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        raw = raw.replace("{prefix}", prefix);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return Msg.parse(raw);
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }
}
