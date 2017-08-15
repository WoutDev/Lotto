package be.woutdev.lotto.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.lotto.Lotto;
import be.woutdev.lotto.game.LottoGame;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 15/08/2017.
 */
public class StartLottoCommand implements CommandExecutor {
    private final Lotto lotto;

    public StartLottoCommand(Lotto lotto) {
        this.lotto = lotto;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This is a player-only command!");
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("lotto.start") && !p.isOp())
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Permission denied.");
            return false;
        }

        if (lotto.getCurrentGame() != null)
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "There is already a game in progress!");
            return false;
        }

        BigDecimal minimumBet = new BigDecimal(1);

        if (args.length == 1)
        {
            try {
                minimumBet = new BigDecimal(args[0]);

                if (minimumBet.doubleValue() < 1.00)
                    throw new NumberFormatException("below 1");
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Invalid minimum bet!");
                return false;
            }
        }

        BigDecimal finalMinimumBet = minimumBet;

        Account account = EconomyAPI.getAPI().getAccount(p);

        if (account.getBalance().doubleValue() < finalMinimumBet.doubleValue())
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Not enough funds to start this lotto game!");
            return false;
        }

        Transaction transaction = EconomyAPI.getAPI().createTransaction(account, TransactionType.WITHDRAW, minimumBet);

        EconomyAPI.getAPI().transact(transaction).addListener((t) -> {
            if (t.getResult().getStatus() == TransactionStatus.SUCCESS)
            {
                LottoGame game = new LottoGame(lotto, p.getUniqueId(), finalMinimumBet);
                lotto.setCurrentGame(game);
                game.start();

                Bukkit.broadcastMessage(lotto.getPrefix() + "A new lotto game just started, hosted by " + p.getName() + "! Minimum bet is "
                        + ChatColor.GOLD + EconomyAPI.getAPI().format(finalMinimumBet));
            }
            else
            {
                p.sendMessage(ChatColor.RED + "Economy: Error in transaction. (" + t.getResult().getStatus().toString() + ")");
            }
        });

        return true;
    }
}
