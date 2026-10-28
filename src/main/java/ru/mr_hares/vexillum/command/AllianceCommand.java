package ru.mr_hares.vexillum.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.mr_hares.vexillum.objects.PeopleDetails;
import ru.mr_hares.vexillum.utils.ColorUtil;
import ru.mr_hares.vexillum.utils.CommandTemplate;

import java.util.*;

import static ru.mr_hares.vexillum.Vexillum.*;

public class AllianceCommand extends CommandTemplate {
    private Map<UUID, UUID> invites = new HashMap<>();

    public AllianceCommand() {
        super("alliance", getInstance());
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.colorize(getML().getString("only-player")));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(String.join("\n", ColorUtil.colorize(getML().getStringList("help.command-list"))));
            return;
        }

        PeopleDetails himself = getDB().getPeopleDetails(player.getUniqueId());

        if (args[0].equalsIgnoreCase("create")) {
            if (himself != null) {
                player.sendMessage(ColorUtil.colorize(getML().getString("in-alliance")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(ColorUtil.colorize(getML().getString("help.use-create")));
                return;
            }

            if (!hasPrice(player)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-has-price")));
                return;
            }

            int max_length = getInstance().getConfig().getInt("alliance.max-name-length");
            int min_length = getInstance().getConfig().getInt("alliance.min-name-length");

            List<String> blockWords = getInstance().getConfig().getStringList("alliance.block-word-in-name");
            String name = String.join(" ", new ArrayList<>(List.of(args)).subList(1, args.length));

            if (name.length() > max_length || name.length() < min_length) {
                player.sendMessage(ColorUtil.colorize(getML().getString("name-length")
                        .replace("{min}", String.valueOf(min_length))
                        .replace("{max}", String.valueOf(max_length))
                ));
                return;
            }

            if (Arrays.stream(name.toLowerCase().split("\\W+")).anyMatch(blockWords::contains)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("has-block-word")));
                return;
            }

            List<String> names = getDB().getCityList().stream().map(city -> ColorUtil.strip(city.getName())).toList();
            if (names.contains(name)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("already-name")));
                return;
            }

            getDB().addCity(player.getUniqueId(), name, player.getLocation());
            removeItems(player);
            player.sendMessage(ColorUtil.colorize(getML().getString("alliance-create")
                    .replace("{name}", name)
            ));

            for (Player p: Bukkit.getOnlinePlayers()) {
                if (p != player) p.sendMessage(ColorUtil.colorize(getML().getString("create-broadcast")
                        .replace("{player}", player.getName())
                        .replace("{name}", name)
                ));
            }
        } else if (args[0].equalsIgnoreCase("rename")) {
            if (himself == null) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-in-alliance")));
                return;
            }

            if (himself.getRank() < 4) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-owner")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(ColorUtil.colorize(getML().getString("help.use-rename")));
                return;
            }

            int max_length = getInstance().getConfig().getInt("alliance.max-name-length");
            int min_length = getInstance().getConfig().getInt("alliance.min-name-length");

            List<String> blockWords = getInstance().getConfig().getStringList("alliance.block-word-in-name");
            String name = String.join(" ", new ArrayList<>(List.of(args)).subList(1, args.length));

            if (name.length() > max_length || name.length() < min_length) {
                player.sendMessage(ColorUtil.colorize(getML().getString("name-length")
                        .replace("{min}", String.valueOf(min_length))
                        .replace("{max}", String.valueOf(max_length))
                ));
                return;
            }

            if (Arrays.stream(name.toLowerCase().split("\\W+")).anyMatch(blockWords::contains)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("has-block-word")));
                return;
            }

            List<String> names = getDB().getCityList().stream().map(city -> ColorUtil.strip(city.getName())).toList();
            if (names.contains(name)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("already-name")));
                return;
            }

            getDB().editName(himself.getCityID(), name);
            player.sendMessage(ColorUtil.colorize(getML().getString("rename")
                    .replace("{name}", name)
            ));
        } else if (args[0].equalsIgnoreCase("invite")) {
            if (himself == null) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-in-alliance")));
                return;
            }

            if (himself.getRank() < 3) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-permission")));
                return;
            }

            if (args.length < 2) {
                player.sendMessage(ColorUtil.colorize(getML().getString("help.use-invite")));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ColorUtil.colorize(getML().getString("not-found-player")));
                return;
            }

            PeopleDetails tarDetails = getDB().getPeopleDetails(target.getUniqueId());
            if (tarDetails != null) {
                player.sendMessage(ColorUtil.colorize(getML().getString("player-in-alliance")));
                return;
            }

            if (sendInvite(player, target)) {
                player.sendMessage(ColorUtil.colorize(getML().getString("send-invite")));
                target.sendMessage(ColorUtil.parse(getML().getString("notify-invite")));
            } else {
                player.sendMessage(ColorUtil.colorize(getML().getString("already-invite")));
            }
        } else {
            player.sendMessage(String.join("\n", ColorUtil.colorize(getML().getStringList("help.command-list"))));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of("create", "menu", "info", "list", "rename", "invite", "accept", "decline", "kick");
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("invite")) {
                return null;
            }
        }

        return List.of();
    }

    private Map<String, Integer> getPrice() {
        Map<String, Integer> price = new HashMap<>();

        ConfigurationSection price_sc = getML().getYaml().getConfigurationSection("alliance.price-creating");
        if (price_sc == null) return price;

        for (String name: price_sc.getKeys(false)) {
            try {
                Material material = Material.valueOf(name);
                int amount = price_sc.getInt(name);
                if (amount > 0)
                    price.put(name, amount);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return price;
    }

    private boolean hasPrice(Player player) {
        Map<String, Integer> price = getPrice();
        Inventory inventory = player.getInventory();
        List<Boolean> booleans = new ArrayList<>();

        for (String key: price.keySet()) {
            int amount = price.get(key);
            booleans.add(inventory.contains(Material.valueOf(key), amount));
        }

        if (booleans.isEmpty()) return false;
        return booleans.stream().allMatch(Boolean.TRUE::equals);
    }

    private void removeItems(Player player) {
        Map<String, Integer> price = getPrice();
        Inventory inventory = player.getInventory();

        for (String key: price.keySet()) {
            int amount = price.get(key);
            ItemStack item = new ItemStack(Material.valueOf(key), amount);
            inventory.remove(item);
        }
    }

    private boolean sendInvite(Player player, Player target) {
        if (invites.containsKey(target.getUniqueId())) return false;

        invites.put(target.getUniqueId(), player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!invites.containsKey(target.getUniqueId())) return;
                invites.remove(target.getUniqueId());

                if (player.isOnline()) {
                    player.sendMessage(ColorUtil.colorize(getML().getString("time-up-invite")
                            .replace("{player}", target.getName())
                    ));
                }

                if (target.isOnline()) {
                    target.sendMessage(ColorUtil.colorize(getML().getString("time-up")));
                }
            }
        }.runTaskLater(getInstance(), 20L * 30);

        return true;
    }
}
