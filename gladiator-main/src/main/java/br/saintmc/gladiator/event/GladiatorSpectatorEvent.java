package br.saintmc.gladiator.event;

import br.saintmc.commons.bukkit.event.NormalEvent;
import br.saintmc.gladiator.challenge.Challenge;
import org.bukkit.entity.Player;

public class GladiatorSpectatorEvent extends NormalEvent {

    private Player player;
    private Action action;
    private Challenge challenge;

    public GladiatorSpectatorEvent(Player player, Challenge challenge, Action action) {
        this.player = player;
        this.challenge = challenge;
        this.action = action;

    }

    public enum Action {
        JOIN, LEAVE, SPECTATING;
    }

    public Player getPlayer() {
        return player;
    }

    public Action getAction() {
        return action;
    }

    public Challenge getChallenge() {
        return challenge;
    }
}
