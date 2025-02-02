package com.github.bobryanskiy.profileSwitcher;

import com.github.bobryanskiy.profileSwitcher.commands.ProfileSwitchCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ProfileSwitcher getInstance() {
        return instance;
    }
}
