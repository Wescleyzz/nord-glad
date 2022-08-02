package br.saintmc.gladiator.listener;

import br.saintmc.commons.Commons;
import br.saintmc.commons.Constants;
import br.saintmc.commons.core.account.status.StatusType;
import br.saintmc.commons.core.account.status.types.normal.NormalStatus;
import br.saintmc.gladiator.event.GladiatorFinishEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class StatusListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerWarpDeath(GladiatorFinishEvent event) {
        Player player = event.getLoser();
        Player killer = event.getWinner();

        if (killer == null) {
            player.sendMessage("§c§l> §fVocê §cmorreu§f!");

            return;
        }
        NormalStatus playerStatus = (NormalStatus) Commons.getStatusManager().loadStatus(player.getUniqueId(), StatusType.GLADIATOR, NormalStatus.class);

        NormalStatus killerStatus = (NormalStatus) Commons.getStatusManager().loadStatus(killer.getUniqueId(), StatusType.GLADIATOR, NormalStatus.class);
        int winnerXp = 10;
        int lostXp = Constants.RANDOM.nextInt(8) + 1;
        player.sendMessage("§c§l> §fVocê §cmorreu§f para o §c" + killer.getName() + "§f!");
        player.sendMessage("§c§l> §fVocê perdeu §c" + lostXp + " §fde XP!");

        if (playerStatus.getKillstreak() >= 10) {
            Bukkit.broadcastMessage("§6§lKILLSTREAK §8§l> §fO jogador §e" + player
                    .getName() + "§f perdeu o seu killstreak de §b" + playerStatus
                    .getKillstreak() + "§f para o §c" + killer.getName() + "§f!");
        }
        playerStatus.addDeath();
        playerStatus.resetKillstreak();

        killer.sendMessage("§a§l> §fVocê matou o §a" + player.getName() + "§f!");
        killer.sendMessage("§a§l> §fVocê ganhou §a" + winnerXp + " xp§f!");

        killerStatus.addKill();
        killerStatus.addKillstreak();

        if (killerStatus.getKillstreak() % 5 == 0) {
            Bukkit.broadcastMessage("§6§lKILLSTREAK §8§l> §fO jogador §e" + killer.getName() + "§f está com killstreak de §b" + killerStatus.getKillstreak() + "§f!");
        }
        Commons.getMemberManager().getMember(killer.getUniqueId()).addXp(winnerXp);
        Commons.getMemberManager().getMember(player.getUniqueId()).removeXp(lostXp);
    }
}
