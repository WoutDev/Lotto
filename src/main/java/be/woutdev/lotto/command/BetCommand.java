package be.woutdev.lotto.command;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.lotto.Lotto;
import be.woutdev.lotto.game.LottoGame;
import be.woutdev.lotto.game.LottoParticipant;
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
public class BetCommand implements CommandExecutor{
    private final Lotto lotto;

    public BetCommand(Lotto lotto) {
        this.lotto = lotto;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "This is a player-only command!");
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("lotto.bet") && !p.isOp())
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Permission denied.");
            return false;
        }

        if (lotto.getCurrentGame() == null || lotto.getCurrentGame().isClosed())
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "There is currently no game in accepting bets!");
            return false;
        }

        if (args.length != 1)
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Usage: /bet <amount>");
            return false;
        }

        BigDecimal bet;

        try
        {
            bet = new BigDecimal(args[0]);

            if (bet.doubleValue() < 1.00)
                throw new NumberFormatException("below 1.00");
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Usage: /bet <amount>");
            return false;
        }

        LottoGame game = lotto.getCurrentGame();

        if (bet.doubleValue() < game.getMinimumEntry().doubleValue())
        {
           sender.sendMessage(lotto.getPrefix() + ChatColor.RED + "Minimum entry is " + EconomyAPI.getAPI().format(game.getMinimumEntry()));
           return false;
        }

        Account account = EconomyAPI.getAPI().getAccount(p);

        if (account.getBalance().doubleValue() < bet.doubleValue())
        {
            p.sendMessage(lotto.getPrefix() + ChatColor.RED + "You do not have sufficient funds to bet that amount!");
            return false;
        }

        Transaction transaction = EconomyAPI.getAPI().createTransaction(account, TransactionType.WITHDRAW, bet);

        EconomyAPI.getAPI().transact(transaction).addListener((t) -> {
            if (t.getResult().getStatus() == TransactionStatus.SUCCESS) {
                if (game.isParticipant(p.getUniqueId())) {
                    BigDecimal totalBet = game.getParticipant(p.getUniqueId()).getBet().add(bet);

                    game.getParticipant(p.getUniqueId()).setBet(totalBet);

                    p.sendMessage(lotto.getPrefix() + "Successfully added to your current bet. Total bet: " + ChatColor.GOLD + EconomyAPI.getAPI().format(totalBet));
                } else {
                    game.getParticipants().add(new LottoParticipant(p.getUniqueId(), bet));

                    p.sendMessage(lotto.getPrefix() + "Successfully added bet of " + ChatColor.GOLD + EconomyAPI.getAPI().format(bet));
                }

                Bukkit.broadcastMessage(lotto.getPrefix() + p.getName() + " added a bet of " + EconomyAPI.getAPI().format(bet) + ". Total pot is now " + EconomyAPI.getAPI().format(game.getPot()) + "!");
            }
            else
            {
                p.sendMessage(lotto.getPrefix() + ChatColor.RED + "Error in transaction. (" + t.getResult().getStatus() + ")");
            }
        });

        return true;
    }
}
