package be.woutdev.lotto.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.lotto.Lotto;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Wout on 15/08/2017.
 */
public class PotCommand implements CommandExecutor {
    private final Lotto lotto;

    public PotCommand(Lotto lotto) {
        this.lotto = lotto;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (lotto.getCurrentGame() == null)
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "There is no game in progress right now.");
            return false;
        }

        sender.sendMessage(lotto.getPrefix() + "The current total pot is "
            + ChatColor.GOLD + EconomyAPI.getAPI().format(lotto.getCurrentGame().getPot())
            + ChatColor.YELLOW + " with "
            + ChatColor.GOLD + lotto.getCurrentGame().getParticipants().size()
            + ChatColor.YELLOW + " participants. Minimal entry is "
            + ChatColor.GOLD + EconomyAPI.getAPI().format(lotto.getCurrentGame().getMinimumEntry())
            + ChatColor.YELLOW + ".");

        return true;
    }
}
