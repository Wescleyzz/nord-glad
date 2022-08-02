package br.saintmc.gladiator.listener;

import java.util.List;

import br.saintmc.commons.Commons;
import br.saintmc.commons.Constants;
import br.saintmc.commons.bukkit.BukkitMain;
import br.saintmc.commons.bukkit.bukkit.BukkitMember;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.gladiator.General;
import br.saintmc.gladiator.challenge.Challenge;
import br.saintmc.gladiator.event.GladiatorClearEvent;
import br.saintmc.gladiator.event.GladiatorFinishEvent;
import br.saintmc.gladiator.event.GladiatorStartEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ArenaListener implements Listener {

    private World gladiatorWorld;
    private int radius = 8;
    private int height = 12;

    public ArenaListener() {
        WorldCreator worldCreator = new WorldCreator("gladiator");

        worldCreator.type(WorldType.FLAT);
        worldCreator.generatorSettings("0;0");
        worldCreator.generateStructures(false);

        this.gladiatorWorld = BukkitMain.getPlugin().getServer().createWorld(worldCreator);
        this.gladiatorWorld.setAutoSave(false);
        this.gladiatorWorld.setGameRuleValue("doDaylightCycle", "false");
        this.gladiatorWorld.setGameRuleValue("naturalRegeneration", "false");
    }

    @EventHandler
    public void onGladiatorStart(GladiatorStartEvent event) {
        Location[] location = handleArena(event.getChallenge().getBlockList());

        Location firstLocation = location[0];
        firstLocation.setYaw(135.0F);
        event.getChallenge().getEnimy().teleport(firstLocation);
        event.getChallenge().getEnimy().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 5));
        Location secondLocation = location[1];
        secondLocation.setYaw(315.0F);
        event.getChallenge().getPlayer().teleport(secondLocation);
        event.getChallenge().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 5));
        Location mainLocation = location[2];
        event.getChallenge().setMainLocation(mainLocation);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGladiatorFinish(GladiatorFinishEvent event) {
        Challenge challenge = event.getChallenge();
        for (Block block : challenge.getBlockList()) {
            block.setType(Material.AIR);
        }
        for (Block block : challenge.getPlayerBlockList()) {
            block.setType(Material.AIR);
        }
        handleClear(challenge.getMainLocation(), null);
    }


    @EventHandler
    public void onGladiatorClear(GladiatorClearEvent event) { handleClear(event.getChallenge().getMainLocation(), null); }

    private Location[] handleArena(List<Block> blockList) {
        Location loc = new Location(this.gladiatorWorld, 0.0D, 0.0D, 0.0D);
        boolean hasGladi = true;

        while (hasGladi) {
            hasGladi = false;
            boolean stop = false; double x;
            for (x = -8.0D; x <= 8.0D; x++) {
                double z; for (z = -8.0D; z <= 8.0D; z++) {
                    double y; for (y = 0.0D; y <= 10.0D; y++) {
                        Location l = new Location(loc.getWorld(), loc.getX() + x, y, loc.getZ() + z);
                        if (l.getBlock().getType() != Material.AIR) {
                            hasGladi = true;
                            loc = loc.clone().add(Constants.RANDOM.nextBoolean() ? 100.0D : -100.0D, 0.0D, Constants.RANDOM.nextBoolean() ? 100.0D : -100.0D);
                            stop = true;
                        }
                        if (stop) {
                            break;
                        }
                    }
                    if (stop) {
                        break;
                    }
                }
                if (stop) {
                    break;
                }
            }
        }

        Block mainBlock = loc.getBlock();
        double x;
        for (x = -this.radius; x <= this.radius; x++) {
            double z; for (z = -this.radius; z <= this.radius; z++) {
                double y; for (y = 0.0D; y <= this.height; y++) {
                    Location l = new Location(mainBlock.getWorld(), mainBlock.getX() + x, y, mainBlock.getZ() + z);
                    l.getBlock().setType(Material.GLASS);
                    blockList.add(l.getBlock());
                }
            }
        }

        handleClear(mainBlock.getLocation(), blockList);

        return new Location[] { new Location(mainBlock
                .getWorld(), mainBlock.getX() + 7.5D, 2.5D, mainBlock.getZ() + 7.5D), new Location(mainBlock
                .getWorld(), mainBlock.getX() - 6.5D, 2.5D, mainBlock.getZ() - 6.5D), mainBlock
                .getLocation() };
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (General.getInstance().getChallengeManager().containsKey(player)) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(player);
            challenge.addBlock(event.getBlock());
            return;
        }
        BukkitMember member = (BukkitMember) Commons.getMemberManager().getMember(player.getUniqueId());
        if (member.isBuildEnabled()) {
            if (member.hasGroupPermission(Group.DONO))
                event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (General.getInstance().getChallengeManager().containsKey(player)) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(player);
            if (challenge.getBlockList().contains(event.getBlock())) {
                event.setCancelled(true);
                return;
            }
            if (challenge.isPlayerBlock(event.getBlock()) || event.getBlock().getType() == Material.COBBLESTONE || event.getBlock().getType() == Material.OBSIDIAN) {
                event.setCancelled(false);
                challenge.removeBlock(event.getBlock());
            } else {
                event.setCancelled(true);
            }
            return;
        }
        BukkitMember member = (BukkitMember) Commons.getMemberManager().getMember(player.getUniqueId());
        if (member.isBuildEnabled()) {
            if (member.hasGroupPermission(Group.DONO))
                event.setCancelled(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (General.getInstance().getChallengeManager().containsKey(player)) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(player);
            if (challenge.getBlockList().contains(event.getBlock())) {
                Block block = event.getBlock();
                player.sendBlockChange(block.getLocation(), Material.BEDROCK, (byte)0);
                return;
            }
        }
    }

    public void handleClear(Location mainLocation, List<Block> blockList) {
        for (double x = (-this.radius + 1); x <= (this.radius - 1); x++) {
            double z; for (z = (-this.radius + 1); z <= (this.radius - 1); z++) {
                double y; for (y = 1.0D; y < this.height; y++) {
                    Location l = new Location(mainLocation.getWorld(), mainLocation.getX() + x, y, mainLocation.getZ() + z);
                    l.getBlock().setType(Material.AIR);

                    if (blockList != null)
                        blockList.remove(l.getBlock());
                }
            }
        }
    }
}