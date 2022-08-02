package br.saintmc.gladiator;

import br.saintmc.gladiator.challenge.manager.ChallengeManager;
import br.saintmc.gladiator.combatlog.manager.CombatLogManager;
import lombok.Getter;

@Getter
public class General {

    @Getter
    private static General instance;

    private ChallengeManager challengeManager;
    private CombatLogManager combatLogManager;

    public General() {
        instance = this;
    }

    public void onLoad() {

    }

    public void onEnable() {
        this.challengeManager = new ChallengeManager();
        this.combatLogManager = new CombatLogManager();
    }

    public void onDisable() {

    }
}
