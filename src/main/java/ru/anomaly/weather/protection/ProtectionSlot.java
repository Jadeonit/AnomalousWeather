package ru.anomaly.weather.protection;

import java.util.Locale;

/** Слот, в котором нужно носить предмет защиты. */
public enum ProtectionSlot {

    HEAD,
    CHEST,
    LEGS,
    FEET,
    OFF_HAND,
    MAIN_HAND,
    /** Любой броневой слот или рука. */
    ANY;

    public static ProtectionSlot byName(String raw) {
        if (raw == null) {
            return HEAD;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "HEAD", "HELMET", "ГОЛОВА", "ШЛЕМ" -> HEAD;
            case "CHEST", "CHESTPLATE", "НАГРУДНИК" -> CHEST;
            case "LEGS", "LEGGINGS", "ШТАНЫ" -> LEGS;
            case "FEET", "BOOTS", "САПОГИ" -> FEET;
            case "OFF_HAND", "OFFHAND", "ЛЕВАЯ_РУКА" -> OFF_HAND;
            case "MAIN_HAND", "MAINHAND", "РУКА" -> MAIN_HAND;
            case "ANY", "ЛЮБОЙ" -> ANY;
            default -> HEAD;
        };
    }
}
