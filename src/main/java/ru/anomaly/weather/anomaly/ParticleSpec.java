package ru.anomaly.weather.anomaly;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * Описание частиц из конфига. Поддерживает безопасный список типов
 * (без частиц, требующих доп. данных), а также DUST с настраиваемым цветом.
 */
public record ParticleSpec(Particle particle, String colorHex, float size, int count, double radius) {

    public static ParticleSpec from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String rawName = section.getString("type", "").trim().toUpperCase(Locale.ROOT);
        Particle particle = resolve(rawName);
        if (particle == null) {
            return null;
        }
        String color = section.getString("color", "#FFFFFF");
        float size = (float) section.getDouble("size", 1.0);
        int count = Math.max(1, section.getInt("count", 10));
        double radius = Math.max(1.0, section.getDouble("radius", 7.0));
        return new ParticleSpec(particle, color, size, count, radius);
    }

    public boolean needsData() {
        return particle == Particle.DUST;
    }

    public Color bukkitColor() {
        return parseColor(colorHex);
    }

    private static Color parseColor(String hex) {
        if (hex == null) {
            return Color.WHITE;
        }
        String raw = hex.trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        try {
            int rgb = Integer.parseInt(raw, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException exception) {
            return Color.WHITE;
        }
    }

    private static Particle resolve(String name) {
        if (name.isEmpty()) {
            return null;
        }
        return switch (name) {
            case "DUST", "REDSTONE" -> Particle.DUST;
            case "SMOKE" -> Particle.SMOKE;
            case "LARGE_SMOKE" -> Particle.LARGE_SMOKE;
            case "CAMPFIRE_COSY_SMOKE" -> Particle.CAMPFIRE_COSY_SMOKE;
            case "FLAME" -> Particle.FLAME;
            case "SOUL_FIRE_FLAME" -> Particle.SOUL_FIRE_FLAME;
            case "SNOWFLAKE" -> Particle.SNOWFLAKE;
            case "END_ROD" -> Particle.END_ROD;
            case "PORTAL" -> Particle.PORTAL;
            case "ELECTRIC_SPARK" -> Particle.ELECTRIC_SPARK;
            case "SPORE_BLOSSOM_AIR" -> Particle.SPORE_BLOSSOM_AIR;
            case "SNEEZE" -> Particle.SNEEZE;
            case "CRIT" -> Particle.CRIT;
            case "ENCHANTED_HIT" -> Particle.ENCHANTED_HIT;
            case "FIREWORK" -> Particle.FIREWORK;
            case "DRIPPING_WATER" -> Particle.DRIPPING_WATER;
            case "FALLING_WATER" -> Particle.FALLING_WATER;
            case "ASH" -> Particle.ASH;
            case "WHITE_ASH" -> Particle.WHITE_ASH;
            default -> null;
        };
    }
}
