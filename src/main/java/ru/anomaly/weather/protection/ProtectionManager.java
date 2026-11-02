package ru.anomaly.weather.protection;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import ru.anomaly.weather.AnomalousWeatherPlugin;
import ru.anomaly.weather.anomaly.AnomalyType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Хранит предметы защиты и проверяет, надет ли нужный предмет.
 */
public final class ProtectionManager {

    private final AnomalousWeatherPlugin plugin;
    private final NamespacedKey protectionKey;
    private final Map<AnomalyType, ProtectionItem> items = new EnumMap<>(AnomalyType.class);
    private final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    private boolean recipesEnabled;
    private boolean unlockRecipesOnJoin;

    public ProtectionManager(AnomalousWeatherPlugin plugin) {
        this.plugin = plugin;
        this.protectionKey = new NamespacedKey(plugin, "anomaly_protection");
    }

    /** Перечитывает предметы защиты и заново регистрирует рецепты. */
    public void load() {
        items.clear();
        unregisterRecipes();

        for (AnomalyType type : AnomalyType.values()) {
            ConfigurationSection section = plugin.getConfig()
                    .getConfigurationSection("protection.items." + type.name());
            if (section == null) {
                continue;
            }
            ProtectionItem item = ProtectionItem.from(type, section);
            if (item == null) {
                plugin.getLogger().warning("Некорректный предмет защиты для аномалии " + type.name());
                continue;
            }
            items.put(type, item);
        }

        recipesEnabled = plugin.getConfig().getBoolean("protection.recipes.enabled", true);
        unlockRecipesOnJoin = plugin.getConfig().getBoolean("protection.recipes.unlock-on-join", true);
        if (recipesEnabled) {
            registerRecipes();
            if (unlockRecipesOnJoin) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    unlockRecipes(player);
                }
            }
        }
    }

    public ProtectionItem item(AnomalyType type) {
        return items.get(type);
    }

    /** Название предмета защиты для сообщений. */
    public String itemName(AnomalyType type) {
        ProtectionItem item = items.get(type);
        return item == null ? "<gray>не настроена" : item.name();
    }

    /** Создаёт предмет защиты (для команды /anomaly give). */
    public ItemStack create(AnomalyType type) {
        ProtectionItem item = items.get(type);
        return item == null ? null : item.buildItem(protectionKey);
    }

    /** Доля поглощаемого урона: 0.0 — защиты нет, 1.0 — полная защита. */
    public double reduction(Player player, AnomalyType type) {
        ProtectionItem item = items.get(type);
        if (item == null) {
            return 0.0;
        }
        return wears(player, item) ? item.damageReduction() : 0.0;
    }

    /** Блокирует ли защита негативные эффекты (яд, слепота и т.д.). */
    public boolean blocksEffects(Player player, AnomalyType type) {
        ProtectionItem item = items.get(type);
        return item != null && item.blocksEffects() && wears(player, item);
    }

    /** Находит предмет защиты по ItemStack (или null, если это не защита). */
    public ProtectionItem find(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        for (ProtectionItem item : items.values()) {
            if (item.matches(stack, protectionKey)) {
                return item;
            }
        }
        return null;
    }

    /** Нельзя ли терять этот предмет (выброс, рамки, портал Края). */
    public boolean preventsDrop(ItemStack stack) {
        ProtectionItem item = find(stack);
        return item != null && item.preventDrop();
    }

    /** Ключи зарегистрированных рецептов. */
    public List<NamespacedKey> recipeKeys() {
        return List.copyOf(registeredRecipes);
    }

    /** Открывает рецепты защиты в книге рецептов игрока. */
    public void unlockRecipes(Player player) {
        if (!unlockRecipesOnJoin) {
            return;
        }
        for (NamespacedKey key : registeredRecipes) {
            try {
                player.discoverRecipe(key);
            } catch (Throwable ignored) {
                // рецепт мог не зарегистрироваться — пропускаем
            }
        }
    }

    public boolean wears(Player player, ProtectionItem item) {
        if (item.slot() == ProtectionSlot.ANY) {
            for (ItemStack candidate : player.getInventory().getArmorContents()) {
                if (item.matches(candidate, protectionKey)) {
                    return true;
                }
            }
            return item.matches(player.getInventory().getItemInOffHand(), protectionKey)
                    || item.matches(player.getInventory().getItemInMainHand(), protectionKey);
        }
        return item.matches(itemInSlot(player, item.slot()), protectionKey);
    }

    private ItemStack itemInSlot(Player player, ProtectionSlot slot) {
        return switch (slot) {
            case HEAD -> player.getInventory().getHelmet();
            case CHEST -> player.getInventory().getChestplate();
            case LEGS -> player.getInventory().getLeggings();
            case FEET -> player.getInventory().getBoots();
            case OFF_HAND -> player.getInventory().getItemInOffHand();
            case MAIN_HAND -> player.getInventory().getItemInMainHand();
            case ANY -> null;
        };
    }

    private void registerRecipes() {
        for (Map.Entry<AnomalyType, ProtectionItem> entry : items.entrySet()) {
            AnomalyType type = entry.getKey();
            ProtectionItem item = entry.getValue();
            RecipeDefs.RecipeDef definition = RecipeDefs.get(type);
            if (definition == null) {
                continue;
            }
            NamespacedKey key = new NamespacedKey(plugin, "protection_" + type.name().toLowerCase(Locale.ROOT));
            ShapedRecipe recipe = new ShapedRecipe(key, item.buildItem(protectionKey));
            recipe.shape(definition.shape());
            definition.ingredients().forEach((symbol, material) -> recipe.setIngredient(symbol, material));
            try {
                Bukkit.addRecipe(recipe);
                registeredRecipes.add(key);
            } catch (Throwable throwable) {
                plugin.getLogger().warning("Не удалось зарегистрировать рецепт " + key + ": " + throwable.getMessage());
            }
        }
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : registeredRecipes) {
            try {
                Bukkit.removeRecipe(key);
            } catch (Throwable ignored) {
                // рецепта уже нет — не страшно
            }
        }
        registeredRecipes.clear();
    }
}
