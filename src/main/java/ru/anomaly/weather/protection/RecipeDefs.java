package ru.anomaly.weather.protection;

import org.bukkit.Material;
import ru.anomaly.weather.anomaly.AnomalyType;

import java.util.EnumMap;
import java.util.Map;

/** Рецепты крафта предметов защиты. */
public final class RecipeDefs {

    public record RecipeDef(String[] shape, Map<Character, Material> ingredients) {
    }

    private static final Map<AnomalyType, RecipeDef> DEFINITIONS = new EnumMap<>(AnomalyType.class);

    static {
        DEFINITIONS.put(AnomalyType.ACID_RAIN, new RecipeDef(
                new String[]{"SSS", "GHG"},
                Map.of('S', Material.SLIME_BALL, 'G', Material.GLASS, 'H', Material.LEATHER_HELMET)));

        DEFINITIONS.put(AnomalyType.FIRE_RAIN, new RecipeDef(
                new String[]{"BMB", " C "},
                Map.of('B', Material.BLAZE_POWDER, 'M', Material.MAGMA_CREAM, 'C', Material.LEATHER_CHESTPLATE)));

        DEFINITIONS.put(AnomalyType.BLIZZARD, new RecipeDef(
                new String[]{"W W", "WBW"},
                Map.of('W', Material.WHITE_WOOL, 'B', Material.LEATHER_BOOTS)));

        DEFINITIONS.put(AnomalyType.SANDSTORM, new RecipeDef(
                new String[]{"SGS", "SLS"},
                Map.of('S', Material.SAND, 'G', Material.GLASS, 'L', Material.LEATHER_LEGGINGS)));

        DEFINITIONS.put(AnomalyType.TOXIC_FOG, new RecipeDef(
                new String[]{" F ", "FHF", " F "},
                Map.of('F', Material.CHARCOAL, 'H', Material.IRON_HELMET)));

        DEFINITIONS.put(AnomalyType.THUNDERSTORM, new RecipeDef(
                new String[]{"RCR", " C "},
                Map.of('R', Material.REDSTONE, 'C', Material.COPPER_INGOT)));

        DEFINITIONS.put(AnomalyType.VOID_STORM, new RecipeDef(
                new String[]{" P ", "PEP", " P "},
                Map.of('P', Material.ENDER_PEARL, 'E', Material.ENDER_EYE)));
    }

    private RecipeDefs() {
    }

    public static RecipeDef get(AnomalyType type) {
        return DEFINITIONS.get(type);
    }
}
