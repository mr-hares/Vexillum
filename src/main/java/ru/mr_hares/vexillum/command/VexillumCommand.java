package ru.mr_hares.vexillum.command;

import org.bukkit.command.CommandSender;
import ru.mr_hares.vexillum.utils.ColorUtil;
import ru.mr_hares.vexillum.utils.CommandTemplate;

import java.util.List;
import static ru.mr_hares.vexillum.Vexillum.*;

public class VexillumCommand extends CommandTemplate {
    public VexillumCommand() {
        super("vexillum", getInstance());
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) return;

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("vexillum.admin.reload")) {
                sender.sendMessage(ColorUtil.colorize(getML().getString("not-permission")));
                return;
            }

            String lang = getML().reload();
            if (lang == null) throw new NullPointerException();

            getInstance().reloadConfig();

            sender.sendMessage(ColorUtil.colorize(getML().getString("reload")
                    .replace("{lang}", lang)
            ));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of("reload");
        }

        return List.of();
    }
}
