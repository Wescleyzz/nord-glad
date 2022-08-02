package br.saintmc.gladiator;

import br.saintmc.commons.bukkit.command.BukkitCommandFramework;
import br.saintmc.gladiator.command.SpectatorCommand;
import br.saintmc.gladiator.gamer.manager.GamerManager;
import br.saintmc.gladiator.listener.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Gladiator extends JavaPlugin {

    @Getter
    private static Gladiator plugin;

    private General general;

    private boolean oldMode;

    private GamerManager gamerManager;

    @Override
    public void onLoad() {
        plugin = this;
        general = new General();
        general.onLoad();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        oldMode = getConfig().getBoolean("oldMode", false);
        loadListeners();
        general.onEnable();
        gamerManager = new GamerManager();
        BukkitCommandFramework.INSTANCE.registerCommands(new SpectatorCommand());
    }

    @Override
    public void onDisable() {
        plugin = null;
        general.onDisable();
    }

    private void loadListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ArenaListener(), this);
        pluginManager.registerEvents(new GladiatorListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new CombatLogListener(general.getCombatLogManager()), this);
        pluginManager.registerEvents(new ScoreboardListener(), this);
        pluginManager.registerEvents(new SpectatorListener(), this);
        pluginManager.registerEvents(new StatusListener(), this);
        pluginManager.registerEvents(new WorldListener(), this);
        pluginManager.registerEvents(new SoupListener(), this);
        pluginManager.registerEvents(new DamageFixer(), this);
    }
}
