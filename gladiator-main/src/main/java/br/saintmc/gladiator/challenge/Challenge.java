package br.saintmc.gladiator.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import br.saintmc.commons.bukkit.api.item.ItemBuilder;
import br.saintmc.commons.bukkit.api.vanish.AdminMode;
import br.saintmc.commons.bukkit.api.vanish.VanishAPI;
import br.saintmc.gladiator.Gladiator;
import br.saintmc.gladiator.event.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public class Challenge {

    private Player player;
    private Player enimy;
    private Set<Player> spectatorSet;
    private List<Block> playerBlockList;
    private List<Block> blockList;

    public Player getPlayer() { return this.player; } private List<Item> itemList; private Location mainLocation; private long expireTime; private int time; private int witherTime;
    public Player getEnimy() { return this.enimy; }

    public Set<Player> getSpectatorSet() { return this.spectatorSet; }
    public List<Block> getPlayerBlockList() { return this.playerBlockList; }
    public List<Block> getBlockList() { return this.blockList; }
    public List<Item> getItemList() { return this.itemList; }

    public void setMainLocation(Location mainLocation) { this.mainLocation = mainLocation; }
    public Location getMainLocation() { return this.mainLocation; }

    public long getExpireTime() { return this.expireTime; }
    public int getTime() { return this.time; }
    public int getWitherTime() { return this.witherTime; }

    public Challenge(Player player, Player enimy) {
        this.player = player;
        this.enimy = enimy;
        this.spectatorSet = new HashSet();
        this.playerBlockList = new ArrayList();
        this.blockList = new ArrayList();
        this.itemList = new ArrayList();
        this.expireTime = System.currentTimeMillis() + 15000L;
    }

    public void spectate(Player player) {
        GladiatorTrySpectatorEvent event = new GladiatorTrySpectatorEvent(player, this);

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.spectatorSet.add(player);

            player.teleport(this.mainLocation.clone().add(0.0D, 2.0D, 0.0D));
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.setHealth(20.0D);

            Bukkit.getPluginManager().callEvent(new GladiatorSpectatorEvent(player, this, GladiatorSpectatorEvent.Action.JOIN));

            if (!AdminMode.getInstance().isAdmin(player))
                Stream.concat(this.spectatorSet.stream(), Arrays.asList(new Player[] { this.player, this.enimy }).stream())
                        .forEach(p -> p.sendMessage("§7" + player.getName() + " está assistindo!"));
        }
    }

    public void removeSpectator(Player player) {
        Stream.concat(this.spectatorSet.stream(), Arrays.asList(new Player[] { this.player, this.enimy }).stream())
                .forEach(p -> p.sendMessage("§7" + player.getName() + " não está mais assistindo!"));
        this.spectatorSet.remove(player);
        Bukkit.getPluginManager().callEvent(new GladiatorSpectatorEvent(player, this, GladiatorSpectatorEvent.Action.LEAVE));
    }

    public void start() {
        GladiatorStartEvent event = new GladiatorStartEvent(this);

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            handleInventory(this.player);
            handleInventory(this.enimy);

            VanishAPI.getInstance().hideAllPlayers(this.player);
            VanishAPI.getInstance().hideAllPlayers(this.enimy);

            this.player.showPlayer(this.enimy);
            this.enimy.showPlayer(this.player);
        }
    }

    public void finish(Player loser) {
        Bukkit.getPluginManager().callEvent(new GladiatorFinishEvent(this, loser, (loser == this.enimy) ? this.player : this.enimy));
        getSpectatorSet().forEach(player ->
                Bukkit.getPluginManager().callEvent(new GladiatorSpectatorEvent(player, this, GladiatorSpectatorEvent.Action.LEAVE)));

        VanishAPI.getInstance().getHideAllPlayers().remove(getPlayer().getUniqueId());
        VanishAPI.getInstance().updateVanishToPlayer(getPlayer());

        VanishAPI.getInstance().getHideAllPlayers().remove(getEnimy().getUniqueId());
        VanishAPI.getInstance().updateVanishToPlayer(getEnimy());
    }

    public void pulse() {
        this.time++;

        if (this.time % 180 == 0) {
            this.itemList.removeIf(item -> item.isDead());

            for (Item item : this.player.getWorld().getEntitiesByClass(Item.class)) {
                if (this.itemList.contains(item)) {
                    item.remove();
                    this.itemList.remove(item);
                }
            }
            Bukkit.getPluginManager().callEvent(new GladiatorClearEvent(this));
        }

        if (this.time == 300 + this.witherTime * 180) {
            this.enimy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 4));
            this.player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1200, 4));
            this.witherTime++;
        }

        Bukkit.getPluginManager().callEvent(new GladiatorPulseEvent(this));
    }


    public void addItem(Item item) { this.itemList.add(item); }



    public void removeItem(Item item) { this.itemList.remove(item); }



    public void addBlock(Block block) { this.playerBlockList.add(block); }



    public void removeBlock(Block block) { this.playerBlockList.remove(block); }



    public boolean hasExpired() { return (this.expireTime < System.currentTimeMillis()); }



    public boolean isInFight(Player target) { return (this.player == target || this.enimy == target); }



    public boolean isPlayerBlock(Block block) { return this.playerBlockList.contains(block); }


    public void handleInventory(Player player) {
        if (Gladiator.getPlugin().isOldMode()) {
            player.getInventory().clear();
            player.getInventory()
        }
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.getInventory().setItem(0, (new ItemBuilder()).name("§bEspada de diamante").type(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL).build());
        player.getInventory().setItem(1, new ItemStack(Material.COBBLE_WALL, 64));
        player.getInventory().setItem(2, new ItemStack(Material.LAVA_BUCKET));
        player.getInventory().setItem(3, new ItemStack(Material.WATER_BUCKET));
        player.getInventory().setItem(8, new ItemStack(Material.WOOD, 64));

        player.getInventory().setItem(27, new ItemStack(Material.LAVA_BUCKET));
        player.getInventory().setItem(28, new ItemStack(Material.LAVA_BUCKET));

        player.getInventory().setItem(17, new ItemStack(Material.STONE_AXE));
        player.getInventory().setItem(26, new ItemStack(Material.STONE_PICKAXE));

        player.getInventory().setItem(13, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(14, new ItemStack(Material.INK_SACK, 64, (short)3));
        player.getInventory().setItem(15, new ItemStack(Material.INK_SACK, 64, (short)3));
        player.getInventory().setItem(16, new ItemStack(Material.INK_SACK, 64, (short)3));

        player.getInventory().setItem(22, new ItemStack(Material.BOWL, 64));
        player.getInventory().setItem(23, new ItemStack(Material.INK_SACK, 64, (short)3));
        player.getInventory().setItem(24, new ItemStack(Material.INK_SACK, 64, (short)3));
        player.getInventory().setItem(25, new ItemStack(Material.INK_SACK, 64, (short)3));

        player.getInventory().setItem(9, new ItemStack(Material.IRON_HELMET));
        player.getInventory().setItem(10, new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setItem(11, new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setItem(12, new ItemStack(Material.IRON_BOOTS));

        player.getInventory().setItem(18, new ItemStack(Material.IRON_HELMET));
        player.getInventory().setItem(19, new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setItem(20, new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setItem(21, new ItemStack(Material.IRON_BOOTS));

        for (int x = 0; x < 20; x++) {
            player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.MUSHROOM_SOUP) });
        }
        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
    }
}
