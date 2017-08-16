package be.woutdev.lotto.game;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.TransactionResult.TransactionStatus;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.lotto.Lotto;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Created by Wout on 15/08/2017.
 */
public class LottoGame implements Runnable
{
    private final Lotto lotto;
    private final BigDecimal minimumEntry;
    private final List<LottoParticipant> participants;
    private boolean closed;
    private int endTaskId;
    private int announceTaskId;

    public LottoGame(Lotto lotto, UUID host, BigDecimal minimumEntry)
    {
        this.lotto = lotto;
        this.minimumEntry = minimumEntry;
        this.participants = Lists.newArrayList();
        this.closed = false;

        this.participants.add(new LottoParticipant(host, minimumEntry));
    }

    public BigDecimal getMinimumEntry() {
        return minimumEntry;
    }

    public List<LottoParticipant> getParticipants() {
        return participants;
    }

    public boolean isClosed() {
        return closed;
    }

    public BigDecimal getPot()
    {
        return getParticipants().stream().map(LottoParticipant::getBet).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getEndTaskId() {
        return endTaskId;
    }

    public boolean isParticipant(UUID uuid)
    {
        return getParticipants().stream().anyMatch(p -> p.getParticipant().equals(uuid));
    }

    public LottoParticipant getParticipant(UUID uuid)
    {
        return getParticipants().stream().filter(p -> p.getParticipant().equals(uuid)).findFirst().orElse(null);
    }

    public void start()
    {
        endTaskId = Bukkit.getScheduler().runTaskLater(lotto, this, 20L * 60 * 5).getTaskId();

        announceTaskId = Bukkit.getScheduler().runTaskTimer(lotto, () -> {
            if (isClosed() || lotto.getCurrentGame() != this)
            {
                Bukkit.getScheduler().cancelTask(announceTaskId);
                return;
            }

            Bukkit.broadcastMessage(lotto.getPrefix() + "There is a Lotto game in progress!");
            Bukkit.broadcastMessage(lotto.getPrefix() + "Enter with /bet [" + ChatColor.GOLD + EconomyAPI.getAPI().format(getMinimumEntry()) + " minimal entry" + ChatColor.YELLOW + "]");
        }, 20 * 60, 20 * 60).getTaskId();
    }

    public void end()
    {
        closed = true;

        Bukkit.broadcastMessage(lotto.getPrefix() + "LOTTO CLOSED! Total pot: " + ChatColor.GOLD + EconomyAPI.getAPI().format(getPot()) + ChatColor.YELLOW + "! Announcing winner in 5 seconds...");

        Bukkit.getScheduler().runTaskLater(lotto, () -> {
            Random random = new Random();

            double p = random.nextInt(getPot().intValueExact()  + 1);
            double cumulativeProb = 0.0;

            LottoParticipant winner = null;

            Collections.shuffle(participants);

            for (LottoParticipant participant : participants) {
                cumulativeProb += participant.getBet().doubleValue();

                if (p <= cumulativeProb) {
                    winner = participant;
                    break;
                }
            }

            if (winner == null)
            {
                Bukkit.getLogger().severe("Failed to select winner for Lotto! Game: " + toString());
                return;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(winner.getParticipant());

            Bukkit.broadcastMessage(lotto.getPrefix() + player.getName() + " won this Lotto game with a total pot of " + ChatColor.GOLD + EconomyAPI.getAPI().format(getPot()) + ChatColor.YELLOW + "! Congratulations!");

            Optional<Account> account = EconomyAPI.getAPI().getAccount(player.getUniqueId());

            account.ifPresent(account1 -> EconomyAPI.getAPI()
                .transact(EconomyAPI.getAPI().createTransaction(account1, TransactionType.DEPOSIT, getPot()))
                .addListener((t) -> {
                        if (!player.isOnline())
                            return;

                        Player onlinePlayer = (Player) player;

                        if (t.getResult().getStatus() == TransactionStatus.SUCCESS)
                        {
                            onlinePlayer.sendMessage(lotto.getPrefix() + "The pot money has been successfully transferred to your account!");
                        }
                        else
                        {
                            onlinePlayer.sendMessage(lotto.getPrefix() + ChatColor.RED + "Error transferring money to your account! (" + t.getResult().getStatus() + ")");
                        }
                    }
                ));

            lotto.setCurrentGame(null);
        }, 100L);
    }

    @Override
    public String toString() {
        return "LottoGame{" +
            ", minimumEntry=" + minimumEntry +
            ", participants=" + participants +
            ", closed=" + closed +
            '}';
    }

    @Override
    public void run() {
        end();
    }
}
