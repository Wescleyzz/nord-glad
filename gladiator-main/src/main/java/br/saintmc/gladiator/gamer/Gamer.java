package br.saintmc.gladiator.gamer;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Gamer  {

    private Player player;
    private long combatStart;

    public Player getPlayer() { return this.player; }

    public long getCombatStart() { return this.combatStart; }

    public Gamer(Player player) {
        this.player = player;

        this.combatStart = -1L;
    }

    public boolean isInCombat() { return (this.combatStart + 10000L > System.currentTimeMillis()); }

    public void setCombat() { this.combatStart = System.currentTimeMillis(); }

    public void removeCombat() { this.combatStart = Long.MIN_VALUE; }

    public UUID getUuid() { return this.player.getUniqueId(); }
}
