package br.saintmc.gladiator.listener;

import java.util.HashMap;
import java.util.Map;
import br.saintmc.gladiator.challenge.Challenge;
import br.saintmc.gladiator.event.GladiatorSpectatorEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpectatorListener implements Listener {

    private Map<Player, Challenge> spectatorMap = new HashMap();

    @EventHandler
    public void onGladiatorSpectator(GladiatorSpectatorEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == GladiatorSpectatorEvent.Action.LEAVE) {
            this.spectatorMap.remove(player);
        } else {
            this.spectatorMap.put(player, event.getChallenge());
        }
    }

    @EventHandler
    public void onGladiatorSpectator(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            if (this.spectatorMap.containsKey(player))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGladiatorSpectatorOld(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            if (this.spectatorMap.containsKey(player))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (this.spectatorMap.containsKey(event.getPlayer()))
            ((Challenge)this.spectatorMap.get(event.getPlayer())).removeSpectator(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.spectatorMap.containsKey(player))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.spectatorMap.containsKey(player))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.spectatorMap.containsKey(player))
            event.setCancelled(true);
    }
}
