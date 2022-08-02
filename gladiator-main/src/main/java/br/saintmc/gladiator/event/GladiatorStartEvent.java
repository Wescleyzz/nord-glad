package br.saintmc.gladiator.event;

import br.saintmc.commons.bukkit.event.NormalEvent;
import br.saintmc.gladiator.challenge.Challenge;
import org.bukkit.event.Cancellable;

public class GladiatorStartEvent extends NormalEvent implements Cancellable {

    private Challenge challenge;
    private boolean cancelled;

    public GladiatorStartEvent(Challenge challenge) {
        this.challenge = challenge;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
