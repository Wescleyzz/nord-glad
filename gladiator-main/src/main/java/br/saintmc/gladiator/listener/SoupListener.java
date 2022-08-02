package br.saintmc.gladiator.listener;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupListener implements Listener {

    @EventHandler
    public void onSoup(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR)
            return;
        if (item.getType() == Material.MUSHROOM_SOUP) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (((Damageable) p).getHealth() < ((Damageable) p).getMaxHealth() || p.getFoodLevel() < 20) {
                    int restores = 7;
                    event.setCancelled(true);
                    if (((Damageable) p).getHealth() < ((Damageable) p).getMaxHealth())
                        if (((Damageable) p).getHealth() + restores <= ((Damageable) p).getMaxHealth())
                            p.setHealth(((Damageable) p).getHealth() + restores);
                        else
                            p.setHealth(((Damageable) p).getMaxHealth());
                    else if (p.getFoodLevel() < 20)
                        if (p.getFoodLevel() + restores <= 20) {
                            p.setFoodLevel(p.getFoodLevel() + restores);
                            p.setSaturation(3);
                        } else {
                            p.setFoodLevel(20);
                            p.setSaturation(3);
                        }
                    item = new ItemStack(Material.BOWL);
                    p.setItemInHand(item);
                }
            }
        }
    }
}
