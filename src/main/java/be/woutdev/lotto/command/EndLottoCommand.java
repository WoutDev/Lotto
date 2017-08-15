package be.woutdev.lotto.command;

import be.woutdev.lotto.Lotto;
import be.woutdev.lotto.game.LottoGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 15/08/2017.
 */
public class EndLottoCommand implements CommandExecutor {
    private final Lotto lotto;

    public EndLottoCommand(Lotto lotto) {
        this.lotto = lotto;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This is a player-only command!");
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("lotto.end") && !p.isOp())
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Permission denied.");
            return false;
        }

        if (lotto.getCurrentGame() == null || lotto.getCurrentGame().isClosed())
        {
            sender.sendMessage(lotto.getPrefix() + "No active game found to end!");
            return false;
        }

        LottoGame game = lotto.getCurrentGame();
        Bukkit.getScheduler().cancelTask(game.getEndTaskId());

        game.end();

        p.sendMessage(lotto.getPrefix() + "Successfully ended the current Lotto game!");

        return true;
    }
}
