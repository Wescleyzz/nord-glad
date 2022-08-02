package br.saintmc.gladiator.event;

import br.saintmc.commons.bukkit.event.NormalEvent;
import br.saintmc.gladiator.challenge.Challenge;
import org.bukkit.entity.Player;

public class GladiatorFinishEvent extends NormalEvent {

    private Challenge challenge;
    private Player winner;
    private Player loser;

    public GladiatorFinishEvent(Challenge challenge, Player loser, Player winner) {
        this.challenge = challenge;
        this.loser = loser;
        this.winner = winner;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public Player getLoser() {
        return loser;
    }

    public Player getWinner() {
        return winner;
    }
}
