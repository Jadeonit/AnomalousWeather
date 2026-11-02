package ru.anomaly.weather.anomaly;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

/**
 * Описание зельевого эффекта из конфига.
 * Формат строки: {@code ЭФФЕКТ:УРОВЕНЬ:ДЛИТЕЛЬНОСТЬ_В_ТИКАХ}, например {@code POISON:0:60}.
 */
public record PotionSpec(PotionEffectType type, int durationTicks, int amplifier) {

    public static PotionSpec parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.trim().split(":");
        if (parts.length < 3) {
            return null;
        }
        PotionEffectType effect = resolve(parts[0]);
        if (effect == null) {
            return null;
        }
        return new PotionSpec(effect, Math.max(20, parseInt(parts[2], 100)), Math.max(0, parseInt(parts[1], 0)));
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static PotionEffectType resolve(String raw) {
        String name = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        PotionEffectType direct = switch (name) {
            case "poison", "яд", "отравление" -> PotionEffectType.POISON;
            case "slowness", "slow", "замедление" -> PotionEffectType.SLOWNESS;
            case "blindness", "blind", "слепота" -> PotionEffectType.BLINDNESS;
            case "hunger", "голод" -> PotionEffectType.HUNGER;
            case "nausea", "confusion", "тошнота" -> PotionEffectType.NAUSEA;
            case "weakness", "слабость" -> PotionEffectType.WEAKNESS;
            case "wither", "иссушение" -> PotionEffectType.WITHER;
            case "darkness", "тьма" -> PotionEffectType.DARKNESS;
            case "mining_fatigue", "fatigue", "усталость" -> PotionEffectType.MINING_FATIGUE;
            case "speed", "скорость" -> PotionEffectType.SPEED;
            case "regeneration", "регенерация" -> PotionEffectType.REGENERATION;
            case "resistance", "сопротивление" -> PotionEffectType.RESISTANCE;
            case "fire_resistance", "огнестойкость" -> PotionEffectType.FIRE_RESISTANCE;
            case "glowing", "свечение" -> PotionEffectType.GLOWING;
            case "levitation", "левитация" -> PotionEffectType.LEVITATION;
            case "jump_boost", "прыгучесть" -> PotionEffectType.JUMP_BOOST;
            case "invisibility", "невидимость" -> PotionEffectType.INVISIBILITY;
            case "water_breathing", "подводное_дыхание" -> PotionEffectType.WATER_BREATHING;
            case "night_vision", "ночное_зрение" -> PotionEffectType.NIGHT_VISION;
            default -> null;
        };
        if (direct != null) {
            return direct;
        }
        NamespacedKey key = NamespacedKey.fromString(name.contains(":") ? name : "minecraft:" + name);
        if (key == null) {
            return null;
        }
        return Registry.MOB_EFFECT.get(key);
    }
}
