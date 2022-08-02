package br.saintmc.gladiator.event;

import br.saintmc.commons.bukkit.event.NormalEvent;
import br.saintmc.gladiator.challenge.Challenge;

public class GladiatorClearEvent extends NormalEvent {

    private Challenge challenge;

    public GladiatorClearEvent(Challenge challenge) {
        this.challenge = challenge;
    }

    public Challenge getChallenge() {
        return challenge;
    }
}
