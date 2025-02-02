package com.github.bobryanskiy.profileSwitcher;

import com.github.bobryanskiy.profileSwitcher.commands.ProfileSwitchCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ProfileSwitcher extends JavaPlugin implements Listener {

    private static ProfileSwitcher instance;

    @Override
    public void onEnable() {
        instance = this;

        saveResource("config.yml", false);
        saveDefaultConfig();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register("switchme", new ProfileSwitchCommand())
        );

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello222, " + event.getPlayer().getName() + "!" + Bukkit.getWorlds().getFirst().getName()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ProfileSwitcher getInstance() {
        return instance;
    }
}
