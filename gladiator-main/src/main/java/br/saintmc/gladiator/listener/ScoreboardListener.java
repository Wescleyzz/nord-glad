package br.saintmc.gladiator.listener;

import br.saintmc.commons.Commons;
import br.saintmc.commons.bukkit.api.scoreboard.Score;
import br.saintmc.commons.bukkit.api.scoreboard.Scoreboard;
import br.saintmc.commons.bukkit.api.scoreboard.impl.SimpleScoreboard;
import br.saintmc.commons.bukkit.bukkit.BukkitMember;
import br.saintmc.commons.bukkit.event.player.PlayerScoreboardStateEvent;
import br.saintmc.commons.core.account.Member;
import br.saintmc.commons.core.account.status.StatusType;
import br.saintmc.commons.core.account.status.types.normal.NormalStatus;
import br.saintmc.commons.core.utils.string.StringUtils;
import br.saintmc.gladiator.Gladiator;
import br.saintmc.gladiator.challenge.Challenge;
import br.saintmc.gladiator.event.GladiatorFinishEvent;
import br.saintmc.gladiator.event.GladiatorPulseEvent;
import br.saintmc.gladiator.event.GladiatorSpectatorEvent;
import br.saintmc.gladiator.event.GladiatorStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardListener implements Listener {

    public ScoreboardListener() {
        SCOREBOARD.blankLine(11);
        SCOREBOARD.setScore(10, new Score("§eNormal: ", "modeNormal"));
        SCOREBOARD.setScore(9, new Score(" Wins: §60", "winsNormal"));
        SCOREBOARD.setScore(8, new Score(" Losts: §60", "lostsNormal"));
        SCOREBOARD.setScore(7, new Score("§eOld: ", "modeOld"));
        SCOREBOARD.setScore(6, new Score(" Wins: §6", "winsOld"));
        SCOREBOARD.setScore(5, new Score(" Losts: §6", "lostsOld"));
        SCOREBOARD.blankLine(4);
        SCOREBOARD.setScore(3, new Score("Winstreak: §e0", "winstreak"));
        SCOREBOARD.setScore(2, new Score("Jogadores: §a0", "players"));
        SCOREBOARD.blankLine(1);
        SCOREBOARD.setScore(0, new Score("§6www.saint-mc.com", "site"));

        FIGHT_SCOREBOARD.blankLine(6);
        FIGHT_SCOREBOARD.setScore(5, new Score("§9Ninguém: §e0ms", "firstPing"));
        FIGHT_SCOREBOARD.setScore(4, new Score("§cNinguém: §e0ms", "secondPing"));
        FIGHT_SCOREBOARD.blankLine(3);
        FIGHT_SCOREBOARD.setScore(2, new Score("Tempo: §a", "time"));
        FIGHT_SCOREBOARD.blankLine(1);
        FIGHT_SCOREBOARD.setScore(0, new Score("§6www.saint-mc.com", "site"));

        QUEUE_SCOREBOARD.blankLine(8);
        QUEUE_SCOREBOARD.setScore(7, new Score("§9Ninguém: §e0ms", "firstPing"));
        QUEUE_SCOREBOARD.setScore(6, new Score("§cNinguém: §e0ms", "secondPing"));
        QUEUE_SCOREBOARD.blankLine(4);
        QUEUE_SCOREBOARD.setScore(3, new Score("Ranking: §7(§f-§7)", "rank"));
        QUEUE_SCOREBOARD.setScore(2, new Score("Jogadores: §a0", "players"));
        QUEUE_SCOREBOARD.blankLine(1);
        QUEUE_SCOREBOARD.setScore(0, new Score("§6www.saintmc.net", "site"));
    }

    private static final Scoreboard SCOREBOARD = new SimpleScoreboard("§6§lGLADIATOR");

    @EventHandler
    public void onPlayerWarpJoin(final PlayerScoreboardStateEvent event) {
        if (event.isScoreboardEnabled())
            (new BukkitRunnable() {

                public void run() {
                    ScoreboardListener.this.loadScoreboard(event.getPlayer());
                }
            }).runTaskLater(Gladiator.getPlugin(), 5L);
    }
    private static final Scoreboard FIGHT_SCOREBOARD = new SimpleScoreboard("§6§lGLADIATOR"); private static final Scoreboard QUEUE_SCOREBOARD = new SimpleScoreboard("§6§lGLADIATOR");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) { (new BukkitRunnable()
    {
        public void run()
        {
            ScoreboardListener.this.loadScoreboard(event.getPlayer());
        }
    }).runTaskLater(Gladiator.getPlugin(), 7L); }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerQuitEvent event) {
        SCOREBOARD.removeViewer((BukkitMember) Commons.getMemberManager()
                .getMember(event.getPlayer().getUniqueId()));
        QUEUE_SCOREBOARD.removeViewer((BukkitMember)Commons.getMemberManager()
                .getMember(event.getPlayer().getUniqueId()));
        FIGHT_SCOREBOARD.removeViewer((BukkitMember)Commons.getMemberManager()
                .getMember(event.getPlayer().getUniqueId()));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) { (new BukkitRunnable()
    {
        public void run()
        {
            SCOREBOARD.updateScore(new Score("Jogadores: §a" + Bukkit.getOnlinePlayers().size(), "players"));
        }
    }).runTaskLater(Gladiator.getPlugin(), 7L); }


    @EventHandler
    public void onGladiatorStart(GladiatorStartEvent event) {
        Player player = event.getChallenge().getPlayer();
        Player enimy = event.getChallenge().getEnimy();

        SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(player.getUniqueId()));
        QUEUE_SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(player.getUniqueId()));

        SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(enimy.getUniqueId()));
        QUEUE_SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(enimy.getUniqueId()));

        FIGHT_SCOREBOARD.createScoreboard(player);
        FIGHT_SCOREBOARD.createScoreboard(enimy);

        updateScore(player, event.getChallenge());
        updateScore(enimy, event.getChallenge());
    }

    @EventHandler
    public void onGladiatorSpectator(GladiatorSpectatorEvent event) {
        if (event.getAction() == GladiatorSpectatorEvent.Action.JOIN) {
            SCOREBOARD.removeViewer((BukkitMember) Commons.getMemberManager()
                    .getMember(event.getPlayer().getUniqueId()));
            QUEUE_SCOREBOARD.removeViewer((BukkitMember)Commons.getMemberManager()
                    .getMember(event.getPlayer().getUniqueId()));

            FIGHT_SCOREBOARD.createScoreboard(event.getPlayer());
            updateScore(event.getPlayer(), event.getChallenge());
        } else {
            loadScoreboard(event.getPlayer());
        }
    }

    @EventHandler
    public void onGladiatorPulse(GladiatorPulseEvent event) {
        updateScore(event.getChallenge().getEnimy(), event.getChallenge());
        updateScore(event.getChallenge().getPlayer(), event.getChallenge());

        event.getChallenge().getSpectatorSet().forEach(player -> updateScore(player, event.getChallenge()));
    }

    @EventHandler
    public void onGladiatorFinish(GladiatorFinishEvent event) {
        loadScoreboard(event.getChallenge().getEnimy());
        loadScoreboard(event.getChallenge().getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerWarpDeath(GladiatorFinishEvent event) {
        boolean updatePlayer = true;
        boolean updateKiller = (event.getWinner() != null);

        if (updatePlayer) {
            Player player = event.getLoser();
            NormalStatus playerStatus = (NormalStatus)Commons.getStatusManager().loadStatus(player.getUniqueId(), StatusType.GLADIATOR, NormalStatus.class);
            NormalStatus playerStatusOld = (NormalStatus) Commons.getStatusManager().loadStatus(player.getUniqueId(), StatusType.GLADIATOR_OLD, NormalStatus.class);

            SCOREBOARD.updateScore(player, new Score(" Wins: §6" + playerStatus.getKills(), "winsNormal"));
            SCOREBOARD.updateScore(player, new Score(" Losts: §6" + playerStatus.getDeaths(), "lostsNormal"));
            SCOREBOARD.updateScore(player, new Score(" Wins: §6" + playerStatusOld.getKills(), "winsOld"));
            SCOREBOARD.updateScore(player, new Score(" Losts: §6" + playerStatusOld.getDeaths(), "lostsOld"));
            SCOREBOARD.updateScore(player, new Score("Winstreak: §e" + playerStatusOld.getKillstreak() + playerStatus.getKillstreak(), "winstreak"));
        }

        if (updateKiller) {
            Player killer = event.getWinner();
            NormalStatus killerStatus = (NormalStatus)Commons.getStatusManager().loadStatus(killer.getUniqueId(), StatusType.GLADIATOR, NormalStatus.class);
            NormalStatus killerStatusOld = (NormalStatus) Commons.getStatusManager().loadStatus(killer.getUniqueId(), StatusType.GLADIATOR_OLD, NormalStatus.class);

            SCOREBOARD.updateScore(killer, new Score(" Wins: §6" + killerStatus.getKills(), "winsNormal"));
            SCOREBOARD.updateScore(killer, new Score(" Losts: §6" + killerStatus.getDeaths(), "lostsNormal"));
            SCOREBOARD.updateScore(killer, new Score(" Wins: §6" + killerStatusOld.getKills(), "winsOld"));
            SCOREBOARD.updateScore(killer, new Score(" Losts: §6" + killerStatusOld.getDeaths(), "lostsOld"));
            SCOREBOARD.updateScore(killer, new Score("Winstreak: §e" + killerStatus.getKillstreak() + killerStatusOld.getKillstreak(), "winstreak"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)

    public void loadScoreboard(Player player) {
        FIGHT_SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(player.getUniqueId()));
        QUEUE_SCOREBOARD.removeViewer(
                (BukkitMember)Commons.getMemberManager().getMember(player.getUniqueId()));

        SCOREBOARD.createScoreboard(player);
        updateScore(player);
    }

    public void updateScore(Player player) {
        Member member = Commons.getMemberManager().getMember(player.getUniqueId());
        NormalStatus playerStatus = (NormalStatus)Commons.getStatusManager().loadStatus(player.getUniqueId(), StatusType.GLADIATOR, NormalStatus.class);
        NormalStatus playerStatusOld = (NormalStatus) Commons.getStatusManager().loadStatus(player.getUniqueId(), StatusType.GLADIATOR_OLD, NormalStatus.class);

        SCOREBOARD.updateScore(player, new Score(" Wins: §6" + playerStatus.getKills(), "winsNormal"));
        SCOREBOARD.updateScore(player, new Score(" Losts: §6" + playerStatus.getDeaths(), "lostsNormal"));
        SCOREBOARD.updateScore(player, new Score("Winstreak: §e" + playerStatus.getKillstreak(), "winstreak"));

        SCOREBOARD.updateScore(player, new Score(" Wins: §6" + playerStatusOld.getKills(), "winsOld"));
        SCOREBOARD.updateScore(player, new Score(" Losts: §6" + playerStatusOld.getDeaths(), "lostsOld"));
        SCOREBOARD.updateScore(player, new Score("Winstreak: §e" + playerStatusOld.getKillstreak(), "winstreak"));

        SCOREBOARD.updateScore(new Score("Jogadores: §a" + Bukkit.getOnlinePlayers().size(), "players"));

    }

    private void updateScore(Player player, Challenge challenge) {
        Player enemy = (challenge.getPlayer() == player) ? challenge.getEnimy() : challenge.getPlayer();
        Player target = (challenge.getPlayer() == player) ? challenge.getPlayer() : challenge.getEnimy();

        FIGHT_SCOREBOARD.updateScore(player, new Score("§9" + target.getName() + ": §e" + (
                ((((CraftPlayer)target).getHandle()).ping >= 1000) ? "1000+" : Integer.valueOf((((CraftPlayer)target).getHandle()).ping)) + "ms", "firstPing"));

        FIGHT_SCOREBOARD.updateScore(player, new Score("§c" + enemy.getName() + ": §e" + (
                ((((CraftPlayer)enemy).getHandle()).ping >= 1000) ? "1000+" : Integer.valueOf((((CraftPlayer)enemy).getHandle()).ping)) + "ms", "secondPing"));

        FIGHT_SCOREBOARD.updateScore(player, new Score("Tempo: §a" + StringUtils.format(challenge.getTime()), "time"));

        Member member = Commons.getMemberManager().getMember(player.getUniqueId());
    }
}
