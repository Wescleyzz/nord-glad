package br.saintmc.gladiator.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.cooldown.CooldownController;
import br.saintmc.commons.bukkit.api.cooldown.types.ItemCooldown;
import br.saintmc.commons.bukkit.api.item.ActionItemStack;
import br.saintmc.commons.bukkit.api.item.ItemBuilder;
import br.saintmc.commons.bukkit.event.UpdateEvent;
import br.saintmc.commons.bukkit.event.player.vanish.PlayerHideToPlayerEvent;
import br.saintmc.commons.bukkit.event.player.vanish.PlayerShowToPlayerEvent;
import br.saintmc.commons.core.utils.DateUtils;
import br.saintmc.gladiator.General;
import br.saintmc.gladiator.Gladiator;
import br.saintmc.gladiator.challenge.Challenge;
import br.saintmc.gladiator.event.GladiatorFinishEvent;
import br.saintmc.gladiator.event.GladiatorSpectatorEvent;
import br.saintmc.gladiator.event.GladiatorStartEvent;
import br.saintmc.gladiator.event.GladiatorTrySpectatorEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GladiatorListener implements Listener {

    private ItemStack queueJoin = (new ItemBuilder()).name("§cPartida rápida").type(Material.INK_SACK).durability(8).build();
    private ItemStack queueLeave = (new ItemBuilder()).name("§aPartida rápida").type(Material.INK_SACK).durability(10).build();

    private Map<Player, Map<Player, Challenge>> inviteMap = new HashMap();
    private Set<Player> fastQueue = new HashSet();
    private List<Challenge> challengeList = new ArrayList();

    private ActionItemStack normalItem = new ActionItemStack((new ItemBuilder()).type(Material.DIAMOND_SWORD).name("§bGladiator").build(), new ActionItemStack.Interact(ActionItemStack.InteractType.PLAYER) {
        public boolean onInteract(Player inviter, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            if (!(entity instanceof Player)) {
                return false;
            }
            Player player = (Player)entity;
            if (CooldownController.getInstance().hasCooldown(player, "Gladiator")) {
                return false;
            }
            if (General.getInstance().getChallengeManager().containsKey(player) || General.getInstance().getChallengeManager().containsKey(inviter)) {
                return false;
            }
            if (GladiatorListener.this.inviteMap.containsKey(player) && (
                    (Map)GladiatorListener.this.inviteMap.get(player)).containsKey(inviter)) {
                Challenge challenge = (Challenge)((Map)GladiatorListener.this.inviteMap.get(player)).get(inviter);
                if (!challenge.hasExpired()) {
                    player.sendMessage("§aO jogador " + inviter.getName() + " aceitou o seu desafio!");
                    inviter.sendMessage("§aVocê aceitou o desafio de " + player.getName() + "!");
                    challenge.start();
                    return false;
                }
            }
            Map<Player, Challenge> map = (Map)GladiatorListener.this.inviteMap.computeIfAbsent(inviter, v -> new HashMap());
            if (map.containsKey(player)) {
                Challenge challenge = (Challenge)map.get(player);

                if (!challenge.hasExpired()) {
                    inviter.sendMessage("§cAguarde " + DateUtils.getTime(challenge.getExpireTime()) + " para enviar outro convite a este jogador!");
                    return false;
                }
            }
            map.put(player, new Challenge(player, inviter));

            player.sendMessage("§aVocê recebeu um desafio de gladiator do jogador " + inviter.getName() + "!");
            inviter.sendMessage("§aVocê desafiou o jogador " + player.getName() + "!");
            CooldownController.getInstance().addCooldown(player, new ItemCooldown(player.getItemInHand(), "Gladiator", Long.valueOf(4L)));
            return false;
        }
    });

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == UpdateEvent.UpdateType.SECOND)
            this.challengeList.forEach(Challenge::pulse);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getItemInHand() == null || General.getInstance().getChallengeManager().containsKey(player)) {
            return;
        }
        if (player.getItemInHand().getType() == Material.INK_SACK) {
            if (player.getItemInHand().getDurability() == 8) {
                if (CooldownController.getInstance().hasCooldown(player, "Gladiator Rápido")) {
                    return;
                }
                if (this.fastQueue.isEmpty()) {
                    player.sendMessage("§aVocê entrou na fila do gladiator rápido!");
                    this.fastQueue.add(player);
                    player.setItemInHand(this.queueLeave);
                    CooldownController.getInstance().addCooldown(player, new ItemCooldown(player.getItemInHand(), "Gladiator Rápido", Long.valueOf(3L)));
                } else {
                    Player challenger = (Player)this.fastQueue.stream().findFirst().orElse(null);
                    (new Challenge(player, challenger)).start();

                    challenger.sendMessage("§aO jogador " + player.getName() + " irá batalhar com você!");
                    player.sendMessage("§aO jogador " + challenger.getName() + " irá batalhar com você!");
                }
            } else {
                player.sendMessage("§cVocê saiu da fila do gladiator rápido!");
                this.fastQueue.remove(player);
                player.setItemInHand(this.queueJoin);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGladiatorSpectator(GladiatorTrySpectatorEvent event) {
        if (this.challengeList.stream().filter(challenge -> challenge.isInFight(event.getPlayer())).findFirst().isPresent()) {
            event.setCancelled(true);
            return;
        }
        if (this.challengeList.stream().filter(challenge -> challenge.getSpectatorSet().contains(event.getPlayer())).findFirst().isPresent()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c§l> §fVocê já está espectando um duelo de gladiator!");
        }
    }

    @EventHandler
    public void onGladiatorSpectator(GladiatorSpectatorEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == GladiatorSpectatorEvent.Action.LEAVE) {
            player.teleport(handleInventory(player));
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerShowToPlayer(PlayerShowToPlayerEvent event) {
        if (General.getInstance().getChallengeManager().containsKey(event.getToPlayer())) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(event.getToPlayer());
            if (challenge.isInFight(event.getPlayer())) {
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerHideToPlayer(PlayerHideToPlayerEvent event) {
        if (General.getInstance().getChallengeManager().containsKey(event.getToPlayer())) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(event.getToPlayer());
            if (challenge.isInFight(event.getPlayer())) {
                event.setCancelled(true);
            } else {
                event.setCancelled(false);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onGladiatorStart(GladiatorStartEvent event) {
        Player player = event.getChallenge().getPlayer();
        Player enemy = event.getChallenge().getEnimy();

        handleClear(player);
        handleClear(enemy);

        General.getInstance().getChallengeManager().load(player, event.getChallenge());
        General.getInstance().getChallengeManager().load(enemy, event.getChallenge());
        this.challengeList.add(event.getChallenge());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGladiatorFinish(GladiatorFinishEvent event) {
        Player winner = event.getWinner();
        Player loser = event.getLoser();

        winner.teleport(handleInventory(winner));

        General.getInstance().getChallengeManager().unload(winner);
        General.getInstance().getChallengeManager().unload(loser);
        this.challengeList.remove(event.getChallenge());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) { event.getPlayer().teleport(handleInventory(event.getPlayer())); }


    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType() == Material.BOAT)
            event.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (General.getInstance().getChallengeManager().containsKey(player)) {
            ((Challenge) General.getInstance().getChallengeManager().getValue(player)).finish(player);
        }
        handleClear(player);
        player.setAllowFlight(false);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(handleInventory(event.getPlayer()));
        event.getPlayer().teleport(handleInventory(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        event.setDroppedExp(0);
        event.getDrops().clear();

        player.setHealth(20.0D);

        (new BukkitRunnable() {

            public void run() {
                player.teleport(GladiatorListener.this.handleInventory(player));
            }
        }).runTaskLater(Gladiator.getPlugin(), 7L);

        if (!General.getInstance().getChallengeManager().containsKey(player)) {
            return;
        }
        Player killer = player.getKiller();

        Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(player);

        if (killer == null) {
            killer = (challenge.getPlayer() == player) ? challenge.getEnimy() : challenge.getPlayer();
        }
        challenge.finish(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getEntity();

        if (General.getInstance().getChallengeManager().containsKey(player)) {
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent)event;

                if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                    Player target = (Player)entityDamageByEntityEvent.getDamager();

                    if (General.getInstance().getChallengeManager().containsKey(target)) {
                        Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(player);

                        if (challenge.isInFight(target)) {
                            event.setCancelled(false);
                        } else {
                            event.setCancelled(true);
                        }
                    }
                }
            } else {
                event.setCancelled(false);
            }
        } else {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (General.getInstance().getChallengeManager().containsKey(player))
        { if (event.getItemDrop().getItemStack().getType().name().contains("SWORD")) {
            event.setCancelled(true);
        } else {
            ((Challenge) General.getInstance().getChallengeManager().getValue(player)).addItem(event.getItemDrop());
        }  }
        else { event.setCancelled(true); }

    }
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (General.getInstance().getChallengeManager().containsKey(player)) {
            ((Challenge) General.getInstance().getChallengeManager().getValue(player)).removeItem(event.getItem());
        } else {
            event.setCancelled(true);
        }
    }
    private Location handleInventory(Player player) {
        player.setVelocity(new Vector(0, 0, 0));
        player.setHealth(20.0D);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.getInventory().setItem(3, this.normalItem.getItemStack());
        player.getInventory().setItem(5, this.queueJoin);

        for (PotionEffect potion : player.getActivePotionEffects()) {
            player.removePotionEffect(potion.getType());
        }
        player.updateInventory();

        return BukkitMain.getPlugin().getLocationFromConfig("spawn");
    }

    private void handleClear(Player player) {
        if (this.inviteMap.containsKey(player)) {
            this.inviteMap.remove(player);
        }
        if (this.fastQueue.contains(player)) {
            this.fastQueue.remove(player);
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (this.inviteMap.containsKey(online)) {
                Iterator<Map.Entry<Player, Challenge>> iterator = ((Map)this.inviteMap.get(online)).entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Player, Challenge> entry = (Map.Entry)iterator.next();

                    if (entry.getKey() == player)
                        iterator.remove();
                }
            }
        }
    }
}
