package br.saintmc.gladiator.command;

import br.saintmc.commons.bukkit.command.BukkitCommandArgs;
import br.saintmc.commons.core.account.group.Group;
import br.saintmc.commons.core.command.CommandClass;
import br.saintmc.commons.core.command.CommandFramework;
import br.saintmc.gladiator.General;
import br.saintmc.gladiator.challenge.Challenge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpectatorCommand implements CommandClass {

    @CommandFramework.Command(name = "spectator", aliases = {"espectar", "spec"}, groupToUse = Group.LIGHT)
    public void spectatorCommand(BukkitCommandArgs cmdArgs) {
        if (!cmdArgs.isPlayer()) {
            return;
        }
        Player player = cmdArgs.getPlayer();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            player.sendMessage(" §e* §fUse §a/" + cmdArgs.getLabel() + " <playerName>§f para espectar um duelo de gladiator!");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            return;
        }

        if (General.getInstance().getChallengeManager().containsKey(target)) {
            Challenge challenge = (Challenge) General.getInstance().getChallengeManager().getValue(target);
            challenge.spectate(player);
        } else {
            player.sendMessage("§cO jogador " + target.getName() + " não está em combate!");
        }
    }
}
