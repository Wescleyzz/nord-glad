package br.saintmc.gladiator.listener;

import br.saintmc.commons.Commons;
import br.saintmc.commons.Constants;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.api.tablist.TabListAPI;
import br.saintmc.commons.bukkit.event.player.account.PlayerChangeGroupEvent;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.tag.Tag;
import br.saintmc.gladiator.Gladiator;
import br.saintmc.gladiator.gamer.Gamer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

public class PlayerListener implements Listener {

    public PlayerListener() {
        ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.MUSHROOM_SOUP));

        recipe.addIngredient(new MaterialData(Material.INK_SACK, (byte)3));
        recipe.addIngredient(new MaterialData(Material.BOWL));

        Bukkit.addRecipe(recipe);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        Player player = event.getPlayer();
        Gamer gamer = new Gamer(player);
        Gladiator.getPlugin().getGamerManager().loadGamer(player.getUniqueId(), gamer);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        for (Player online : Bukkit.getOnlinePlayers()) {
            constructTabList(online);
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Gladiator.getPlugin().getGamerManager().unloadGamer(event.getPlayer().getUniqueId());
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) { event.setCancelled(true); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) { event.setDeathMessage(null); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareItemCraft(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player p = (Player)event.getDamager();
        ItemStack sword = p.getItemInHand();

        if (sword != null && sword.getType() == Material.DIAMOND_SWORD)
            event.setDamage(event.getDamage() + 1.0D);
    }

    @EventHandler
    public void onPlayerChangeGroup(PlayerChangeGroupEvent event) {
        constructTabList(event.getPlayer());
    }

    public void constructTabList(Player player) {
        int players = BukkitMain.getPlugin().getServerManager().getTotalNumber();
        Member member = Commons.getMemberManager().getMember(player.getUniqueId());
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("§6§lSAINT");
        builder.append("\n");
        builder.append("\n");
        builder.append("§7Nickname: §e" + player.getName() + " §7Grupo: " + Tag.valueOf(member.getGroup().name()).getPrefix());
        builder.append("\n");
        builder.append("§7Servidor: §e" + member.getServerId());
        builder.append("\n");
        StringBuilder footer = new StringBuilder();
        footer.append("\n");
        footer.append("§b" + Constants.WEBSITE);
        footer.append("\n");
        footer.append("§e" + Constants.DISCORD);
        footer.append("\n");
        TabListAPI.setHeaderAndFooter(player, builder.toString(), footer.toString());
    }
}
