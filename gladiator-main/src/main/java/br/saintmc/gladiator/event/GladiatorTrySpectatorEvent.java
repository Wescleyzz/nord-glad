package br.saintmc.gladiator.event;

import br.saintmc.commons.bukkit.event.player.PlayerCancellableEvent;
import br.saintmc.gladiator.challenge.Challenge;
import org.bukkit.entity.Player;

public class GladiatorTrySpectatorEvent extends PlayerCancellableEvent {

    private Challenge challenge;

    public GladiatorTrySpectatorEvent(Player player, Challenge challenge) {
        super(player);
        this.challenge = challenge;
    }

    public Challenge getChallenge() {
        return challenge;
    }
}
