package ru.anomaly.weather.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.anomaly.weather.AnomalousWeatherPlugin;
import ru.anomaly.weather.anomaly.ActiveAnomaly;
import ru.anomaly.weather.anomaly.AnomalyManager;
import ru.anomaly.weather.anomaly.AnomalyType;
import ru.anomaly.weather.anomaly.WeatherSettings;
import ru.anomaly.weather.protection.ProtectionItem;
import ru.anomaly.weather.util.Messages;
import ru.anomaly.weather.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Команда /anomaly — управление аномальной погодой. */
public final class AnomalyCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "status", "list", "start", "stop", "reload", "give", "protection", "help");

    private final AnomalousWeatherPlugin plugin;

    public AnomalyCommand(AnomalousWeatherPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AnomalyManager manager = plugin.anomalyManager();
        Messages messages = plugin.messages();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            messages.send(sender, "command-usage");
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "status" -> {
                if (!canView(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                ActiveAnomaly active = manager.active();
                if (active == null) {
                    messages.send(sender, "no-anomaly", "time", TimeUtil.format((int) manager.secondsUntilNextCheck()));
                } else {
                    messages.send(sender, "anomaly-status",
                            "weather", active.settings().displayName,
                            "time", TimeUtil.format(active.remaining()));
                }
            }
            case "list" -> {
                if (!canView(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                messages.send(sender, "list-header");
                for (WeatherSettings weatherSettings : manager.settings()) {
                    messages.send(sender, "list-entry",
                            "weather", weatherSettings.displayName,
                            "chance", String.format(Locale.ROOT, "%.1f", manager.effectiveChance(weatherSettings.type)),
                            "state", weatherSettings.enabled ? "<green>вкл" : "<red>выкл",
                            "protection", plugin.protectionManager().itemName(weatherSettings.type));
                }
            }
            case "protection" -> {
                if (!canView(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                messages.send(sender, "protection-header");
                for (WeatherSettings weatherSettings : manager.settings()) {
                    ProtectionItem item = plugin.protectionManager().item(weatherSettings.type);
                    if (item == null) {
                        continue;
                    }
                    messages.send(sender, "protection-entry",
                            "weather", weatherSettings.displayName,
                            "protection", item.name(),
                            "slot", slotName(item.slot().name()));
                }
            }
            case "start" -> {
                if (!isAdmin(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    messages.send(sender, "command-usage");
                    return true;
                }
                AnomalyType type = AnomalyType.byId(args[1]);
                if (type == null || manager.settingsOf(type) == null) {
                    messages.send(sender, "unknown-weather", "type", args[1]);
                    return true;
                }
                int seconds = 0;
                if (args.length >= 3) {
                    try {
                        seconds = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                        // оставляем случайную длительность
                    }
                }
                if (manager.startAnomaly(type, seconds)) {
                    ActiveAnomaly active = manager.active();
                    messages.send(sender, "started-by-command",
                            "weather", manager.settingsOf(type).displayName,
                            "time", TimeUtil.format(active == null ? seconds : active.duration()));
                }
            }
            case "stop" -> {
                if (!isAdmin(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                if (manager.active() == null) {
                    messages.send(sender, "no-anomaly", "time", TimeUtil.format((int) manager.secondsUntilNextCheck()));
                } else {
                    manager.stopAnomaly();
                    messages.send(sender, "stopped-by-command");
                }
            }
            case "reload" -> {
                if (!isAdmin(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                plugin.reloadEverything();
                messages.send(sender, "reloaded", "count", String.valueOf(manager.settings().size()));
            }
            case "give" -> {
                if (!isAdmin(sender)) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                if (args.length < 3) {
                    messages.send(sender, "command-usage");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    messages.send(sender, "player-not-found", "player", args[1]);
                    return true;
                }
                AnomalyType type = AnomalyType.byId(args[2]);
                if (type == null) {
                    messages.send(sender, "unknown-weather", "type", args[2]);
                    return true;
                }
                ItemStack item = plugin.protectionManager().create(type);
                if (item == null) {
                    messages.send(sender, "unknown-weather", "type", args[2]);
                    return true;
                }
                Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
                for (ItemStack rest : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), rest);
                }
                String itemName = plugin.protectionManager().itemName(type);
                messages.send(sender, "given", "protection", itemName, "player", target.getName());
                if (!target.equals(sender)) {
                    messages.send(target, "received", "protection", itemName);
                }
            }
            default -> messages.send(sender, "command-usage");
        }
        return true;
    }

    private boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("anomalousweather.admin");
    }

    private boolean canView(CommandSender sender) {
        return isAdmin(sender) || sender.hasPermission("anomalousweather.status");
    }

    private static String slotName(String slot) {
        return switch (slot) {
            case "HEAD" -> "голова";
            case "CHEST" -> "нагрудник";
            case "LEGS" -> "штаны";
            case "FEET" -> "сапоги";
            case "OFF_HAND" -> "левая рука";
            case "MAIN_HAND" -> "правая рука";
            default -> "любой слот";
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (subcommand.equals("start")) {
                return filter(typeIds(), args[1]);
            }
            if (subcommand.equals("give")) {
                List<String> names = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    names.add(player.getName());
                }
                return filter(names, args[1]);
            }
        }
        if (args.length == 3) {
            if (subcommand.equals("give")) {
                return filter(typeIds(), args[2]);
            }
            if (subcommand.equals("start")) {
                return filter(List.of("60", "120", "300"), args[2]);
            }
        }
        return List.of();
    }

    private List<String> typeIds() {
        List<String> ids = new ArrayList<>();
        for (WeatherSettings weatherSettings : plugin.anomalyManager().settings()) {
            ids.add(weatherSettings.type.name());
        }
        return ids;
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
