package be.woutdev.lotto.game;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by Wout on 15/08/2017.
 */
public class LottoParticipant {
    private final UUID participant;
    private BigDecimal bet;

    public LottoParticipant(UUID participant, BigDecimal initialBet) {
        this.participant = participant;
        this.bet = initialBet;
    }

    public UUID getParticipant() {
        return participant;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    @Override
    public String toString() {
        return "LottoParticipant{" +
            "participant=" + participant +
            ", bet=" + bet +
            '}';
    }
}
