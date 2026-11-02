package ru.anomaly.weather.anomaly;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.WeatherType;
import org.bukkit.configuration.ConfigurationSection;
import ru.anomaly.weather.AnomalousWeatherPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Настройки одной аномалии, прочитанные из config.yml. */
public final class WeatherSettings {

    public final AnomalyType type;
    public final boolean enabled;
    /** Вес аномалии: чем больше, тем чаще она выбирается. */
    public final double chance;
    public final String displayName;
    public final String title;
    public final String subtitle;
    public final BossBar.Color bossbarColor;
    public final WeatherType fakeWeather;
    public final boolean vanillaThunder;
    public final int minDuration;
    public final int maxDuration;
    public final boolean requireSky;
    public final boolean waterProtects;
    public final boolean affectMobs;
    public final double damageAmount;
    public final int damageInterval;
    public final int armorDamage;
    public final List<PotionSpec> effects;
    public final ParticleSpec particle;
    public final SoundSpec sound;
    public final int fireTicks;
    public final int freezeTicks;

    // Молнии (THUNDERSTORM)
    public final boolean lightningEnabled;
    public final boolean lightningReal;
    public final int lightningInterval;
    public final int lightningChance;
    public final double lightningDamage;
    public final int lightningFireTicks;

    // Телепортация (VOID_STORM)
    public final boolean teleportEnabled;
    public final int teleportInterval;
    public final int teleportChance;
    public final int teleportRadius;
    public final double teleportDamage;
    public final String teleportSound;

    private WeatherSettings(AnomalyType type, ConfigurationSection section, int globalMin, int globalMax) {
        this.type = type;
        this.enabled = section.getBoolean("enabled", true);
        this.chance = Math.max(0.0, section.getDouble("chance", 10.0));
        this.displayName = section.getString("display-name", "<white>" + type.name());
        this.title = section.getString("title", this.displayName);
        this.subtitle = section.getString("subtitle", "");
        this.bossbarColor = parseColor(section.getString("bossbar-color", "WHITE"));
        this.fakeWeather = parseFakeWeather(section.getString("fake-weather", "DOWNFALL"));
        this.vanillaThunder = section.getBoolean("vanilla-thunder", false);

        int min = section.getInt("duration.min-seconds", globalMin);
        int max = section.getInt("duration.max-seconds", globalMax);
        this.minDuration = min > 0 ? min : Math.max(30, globalMin);
        this.maxDuration = Math.max(this.minDuration, max > 0 ? max : globalMax);

        this.requireSky = section.getBoolean("require-sky", true);
        this.waterProtects = section.getBoolean("water-protects", false);
        this.affectMobs = section.getBoolean("affect-mobs", true);
        this.damageAmount = Math.max(0.0, section.getDouble("damage.amount", 0.0));
        this.damageInterval = Math.max(1, section.getInt("damage.interval-seconds", 5));
        this.armorDamage = Math.max(0, section.getInt("armor-durability-damage", 0));

        List<PotionSpec> parsedEffects = new ArrayList<>();
        for (String raw : section.getStringList("effects")) {
            PotionSpec spec = PotionSpec.parse(raw);
            if (spec != null) {
                parsedEffects.add(spec);
            } else {
                pluginWarn(type, "не удалось разобрать эффект '" + raw + "'");
            }
        }
        this.effects = List.copyOf(parsedEffects);

        this.particle = ParticleSpec.from(section.getConfigurationSection("particles"));
        this.sound = SoundSpec.from(section.getConfigurationSection("sound"));

        this.fireTicks = Math.max(0, section.getInt("fire-ticks", 0));
        this.freezeTicks = Math.max(0, section.getInt("freeze-ticks", 0));

        ConfigurationSection lightning = section.getConfigurationSection("lightning");
        this.lightningEnabled = lightning != null && lightning.getBoolean("enabled", true);
        this.lightningReal = lightning == null || lightning.getBoolean("real-lightning", true);
        this.lightningInterval = lightning == null ? 10 : Math.max(1, lightning.getInt("interval-seconds", 10));
        this.lightningChance = lightning == null ? 40 : clamp(lightning.getInt("chance-percent", 40));
        this.lightningDamage = lightning == null ? 4.0 : Math.max(0.0, lightning.getDouble("damage", 4.0));
        this.lightningFireTicks = lightning == null ? 0 : Math.max(0, lightning.getInt("fire-ticks", 0));

        ConfigurationSection teleport = section.getConfigurationSection("teleport");
        this.teleportEnabled = teleport != null && teleport.getBoolean("enabled", true);
        this.teleportInterval = teleport == null ? 8 : Math.max(1, teleport.getInt("interval-seconds", 8));
        this.teleportChance = teleport == null ? 35 : clamp(teleport.getInt("chance-percent", 35));
        this.teleportRadius = teleport == null ? 20 : Math.max(4, teleport.getInt("radius", 20));
        this.teleportDamage = teleport == null ? 0.0 : Math.max(0.0, teleport.getDouble("damage", 0.0));
        this.teleportSound = teleport == null
                ? "minecraft:entity.enderman.teleport"
                : teleport.getString("sound", "minecraft:entity.enderman.teleport");
    }

    public static WeatherSettings from(AnomalousWeatherPlugin plugin, AnomalyType type, int globalMin, int globalMax) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("weathers." + type.name());
        if (section == null) {
            return null;
        }
        return new WeatherSettings(type, section, globalMin, globalMax);
    }

    private static void pluginWarn(AnomalyType type, String message) {
        java.util.logging.Logger.getLogger("AnomalousWeather").warning(type.name() + ": " + message);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static BossBar.Color parseColor(String name) {
        if (name == null) {
            return BossBar.Color.WHITE;
        }
        try {
            return BossBar.Color.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.WHITE;
        }
    }

    private static WeatherType parseFakeWeather(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "DOWNFALL", "RAIN", "ДОЖДЬ" -> WeatherType.DOWNFALL;
            case "CLEAR", "ЯСНО" -> WeatherType.CLEAR;
            default -> null;
        };
    }
}
