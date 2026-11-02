package ru.anomaly.weather.protection;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.anomaly.weather.anomaly.AnomalyType;
import ru.anomaly.weather.util.Msg;

import java.util.ArrayList;
import java.util.List;

/**
 * Предмет защиты от конкретной аномалии.
 * У предмета есть скрытый PDC-ключ, поэтому переименованная вещь не поможет.
 */
public record ProtectionItem(
        AnomalyType type,
        Material material,
        ProtectionSlot slot,
        String name,
        List<String> lore,
        boolean glow,
        boolean unbreakable,
        int customModelData,
        double damageReduction,
        boolean blocksEffects,
        boolean preventDrop
) {

    public static ProtectionItem from(AnomalyType type, ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null || !material.isItem()) {
            return null;
        }
        ProtectionSlot slot = ProtectionSlot.byName(section.getString("slot", "HEAD"));
        String name = section.getString("name", "<white>" + type.name());
        List<String> lore = new ArrayList<>(section.getStringList("lore"));
        boolean glow = section.getBoolean("glow", true);
        boolean unbreakable = section.getBoolean("unbreakable", false);
        int customModelData = section.getInt("custom-model-data", 0);
        double reduction = Math.min(1.0, Math.max(0.0, section.getDouble("damage-reduction", 1.0)));
        boolean blocksEffects = section.getBoolean("blocks-effects", true);
        // Для эндер-оберега защита от потери включена даже в старых конфигах
        boolean preventDrop = section.getBoolean("prevent-drop", type == AnomalyType.VOID_STORM);
        return new ProtectionItem(type, material, slot, name, List.copyOf(lore), glow, unbreakable,
                customModelData, reduction, blocksEffects, preventDrop);
    }

    public ItemStack buildItem(NamespacedKey protectionKey) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(Msg.parse(name));
            if (!lore.isEmpty()) {
                meta.lore(lore.stream().map(Msg::parse).toList());
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            if (glow) {
                meta.setEnchantmentGlintOverride(true);
            }
            if (unbreakable) {
                meta.setUnbreakable(true);
            }
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            meta.getPersistentDataContainer().set(protectionKey, PersistentDataType.STRING, type.name());
        });
        return stack;
    }

    /** Является ли предмет защитой именно от этой аномалии. */
    public boolean matches(ItemStack stack, NamespacedKey protectionKey) {
        if (stack == null || stack.getType().isAir() || stack.getType() != material) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        String value = meta.getPersistentDataContainer().get(protectionKey, PersistentDataType.STRING);
        return type.name().equals(value);
    }
}
