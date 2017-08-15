package be.woutdev.lotto;

import be.woutdev.lotto.command.BetCommand;
import be.woutdev.lotto.command.EndLottoCommand;
import be.woutdev.lotto.command.PotCommand;
import be.woutdev.lotto.command.StartLottoCommand;
import be.woutdev.lotto.game.LottoGame;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Wout on 15/08/2017.
 */
public class Lotto extends JavaPlugin {
    private LottoGame currentGame;

    @Override
    public void onEnable() {
        getCommand("startlotto").setExecutor(new StartLottoCommand(this));
        getCommand("bet").setExecutor(new BetCommand(this));
        getCommand("pot").setExecutor(new PotCommand(this));
        getCommand("endlotto").setExecutor(new EndLottoCommand(this));
    }

    public String getPrefix()
    {
        return ChatColor.GREEN + "Lotto " + ChatColor.DARK_GREEN + "» " + ChatColor.YELLOW;
    }

    public LottoGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(LottoGame game) {
        currentGame = game;
    }
}
