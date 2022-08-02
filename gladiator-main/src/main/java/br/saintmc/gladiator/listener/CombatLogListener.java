package br.saintmc.gladiator.listener;

import br.saintmc.gladiator.combatlog.CombatLog;
import br.saintmc.gladiator.combatlog.manager.CombatLogManager;
import br.saintmc.gladiator.event.player.PlayerDamagePlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatLogListener implements Listener {

    private CombatLogManager manager;

    public CombatLogListener(CombatLogManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamagePlayerEvent e) {
        Player damager = e.getDamager();
        Player damaged = e.getDamaged();
        this.manager.newCombatLog(damaged.getUniqueId(), damager.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        this.manager.removeCombatLog(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        CombatLog log = manager.getCombatLog(p.getUniqueId());
        if (log == null)
            return;
        if (System.currentTimeMillis() < log.getTime()) {
            Player combatLogger = Bukkit.getPlayer(log.getCombatLogged());
            if (combatLogger != null)
                if (combatLogger.isOnline())
                    p.damage(10000.0, combatLogger);
        }
        manager.removeCombatLog(p.getUniqueId());
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID)
            return;
        Player p = (Player) event.getEntity();
        CombatLog log = this.manager.getCombatLog(p.getUniqueId());
        if (log == null)
            return;
        if (System.currentTimeMillis() < log.getTime()) {
            Player combatLogger = Bukkit.getPlayer(log.getCombatLogged());
            if (combatLogger != null)
                if (combatLogger.isOnline())
                    p.damage(10000.0, combatLogger);
        }
        this.manager.removeCombatLog(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        if (event.isCancelled())
            return;
        PlayerDamagePlayerEvent playerDamagePlayerEvent = new PlayerDamagePlayerEvent((Player) event.getDamager(), (Player) event.getEntity(), event.getDamage(), event.getFinalDamage());
        Bukkit.getPluginManager().callEvent(playerDamagePlayerEvent);
        event.setCancelled(playerDamagePlayerEvent.isCancelled());
    }
}

