package ru.anomaly.weather.anomaly;

import java.util.Locale;

/** Типы аномальной погоды. */
public enum AnomalyType {

    /** Кислотный дождь — разъедает игроков и броню. */
    ACID_RAIN,
    /** Огненный дождь — поджигает всё под открытым небом. */
    FIRE_RAIN,
    /** Снежная буря — замораживает и замедляет. */
    BLIZZARD,
    /** Песчаная буря — слепит, иссушает и замедляет. */
    SANDSTORM,
    /** Токсичный туман — травит и мутит даже под крышей. */
    TOXIC_FOG,
    /** Грозовая аномалия — частые молнии в игроков. */
    THUNDERSTORM,
    /** Эндер-шторм — рандомно телепортирует игроков. */
    VOID_STORM;

    /** Поиск аномалии по названию (поддерживает русские алиасы). */
    public static AnomalyType byId(String value) {
        if (value == null) {
            return null;
        }
        String raw = value.trim();
        for (AnomalyType type : values()) {
            if (type.name().equalsIgnoreCase(raw) || type.name().replace('_', '-').equalsIgnoreCase(raw)) {
                return type;
            }
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "acid", "кислота", "кислотный", "кислотный_дождь" -> ACID_RAIN;
            case "fire", "огонь", "огненный", "огненный_дождь" -> FIRE_RAIN;
            case "snow", "снег", "буря", "снежная_буря" -> BLIZZARD;
            case "sand", "песок", "песчаная_буря" -> SANDSTORM;
            case "toxic", "яд", "туман", "токсичный_туман" -> TOXIC_FOG;
            case "thunder", "гроза", "молния", "грозовая_аномалия" -> THUNDERSTORM;
            case "void", "эндер", "эндер-шторм", "эндер_шторм" -> VOID_STORM;
            default -> null;
        };
    }
}
