package net.joseplay.jpDeathChest.commands;

import net.joseplay.allianceutils.api.extensions.interfaces.AllianceCommandExecutor;
import net.joseplay.allianceutils.api.menu.SimpleMenu;
import net.joseplay.jpDeathChest.gui.DeathChestsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeathChestCommand implements AllianceCommandExecutor {
    @Override
    public String getName() {
        return "deathchest";
    }

    @Override
    public List<String> alliances() {
        return List.of("dc");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof Player)) return;

        new DeathChestsGUI(SimpleMenu.RowsStyle.COMPACT).open((Player) commandSender);
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, String[] strings) {
        return List.of();
    }
}
