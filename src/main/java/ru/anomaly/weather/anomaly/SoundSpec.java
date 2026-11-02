package ru.anomaly.weather.anomaly;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Описание фонового звука аномалии.
 * {@code key} — ванильный ключ звука, например {@code minecraft:weather.rain}.
 */
public record SoundSpec(String key, float volume, float pitch, int intervalSeconds) {

    public static SoundSpec from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String key = section.getString("key", "").trim();
        if (key.isEmpty()) {
            return null;
        }
        return new SoundSpec(
                key,
                (float) section.getDouble("volume", 0.7),
                (float) section.getDouble("pitch", 1.0),
                Math.max(1, section.getInt("interval-seconds", 6))
        );
    }
}
