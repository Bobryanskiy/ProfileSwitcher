package com.github.bobryanskiy.profileSwitcher.commands;

import com.github.bobryanskiy.profileSwitcher.ProfileSwitcher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class ProfileSwitchCommand implements BasicCommand {

    private static final File pluginDir = ProfileSwitcher.getInstance().getDataFolder();
    private static final File playerdataDir = new File(pluginDir.getAbsoluteFile().getParentFile().getParentFile().toString() + "/" + Bukkit.getWorlds().getFirst().getName() + "/playerdata");


    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] strings) {
        if (commandSourceStack.getExecutor() instanceof Player executor) {
            executor.saveData();
            String current = "/first/";
            String string = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + ".current");
            if (string != null) {
                current = string;
            }
            File file1 = new File(playerdataDir, executor.getUniqueId() + ".dat");
            File file2 = new File(playerdataDir, executor.getUniqueId() + ".dat_old");


            File file1To = new File(pluginDir +  "/" + executor.getName() + current, executor.getUniqueId() + ".dat");
            File file2To = new File(pluginDir + "/" + executor.getName() + current, executor.getUniqueId() + ".dat_old");

            if (string == null) {
                copyFiles(file1, file2, file1To, file2To);
            }
            else moveFiles(file1, file2, file1To, file2To);

            ProfileSwitcher.getInstance().getConfig().set(executor.getName().toLowerCase() + "." + current + ".isFlying", executor.isFlying());
            ProfileSwitcher.getInstance().getConfig().set(executor.getName().toLowerCase() + "." + current + ".gamemode", executor.getGameMode().name());
            ProfileSwitcher.getInstance().getConfig().set(executor.getName().toLowerCase() + "." + current + ".location", executor.getLocation().serialize().toString());

            if (current.equals("/first/")) current = "/second/";
            else current = "/first/";

            executor.sendRichMessage("Вы на " + current  + " профиле");

            ProfileSwitcher.getInstance().getConfig().set(executor.getName().toLowerCase() + ".current", current);
            ProfileSwitcher.getInstance().saveConfig();

            if (string != null) {
                file1 = new File(pluginDir +  "/" + executor.getName() + current, executor.getUniqueId() + ".dat");
                file2 = new File(pluginDir + "/" + executor.getName() + current, executor.getUniqueId() + ".dat_old");

                file1To = new File(playerdataDir, executor.getUniqueId() + ".dat");
                file2To = new File(playerdataDir, executor.getUniqueId() + ".dat_old");

                moveFiles(file1, file2, file1To, file2To);
            }

            executor.loadData();

            String gm = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".gamemode");
            if (gm != null) executor.setGameMode(GameMode.valueOf(gm));
            String w = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".location");
            if (w != null) {
                String jsonInput = w.replace("=", "\":\"")
                        .replace(", ", "\", \"")
                        .replace("{", "{\"")
                        .replace("}", "\"}");
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> map = gson.fromJson(jsonInput, type);
                executor.teleportAsync(Location.deserialize(map));
            }
            String isF = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".isFlying");
            if (isF != null) executor.setFlying(Boolean.parseBoolean(isF));
        }
    }

    private void copyFiles(File file1, File file2, File file1To, File file2To) {
        if (!file1To.getParentFile().exists()) {
            file1To.getParentFile().mkdirs();
        }

        try {
            if (file1.exists()) Files.copy(Paths.get(file1.toURI()), Paths.get(file1To.toURI()), StandardCopyOption.REPLACE_EXISTING);
            if (file2.exists()) Files.copy(Paths.get(file2.toURI()), Paths.get(file2To.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveFiles(File file1, File file2, File file1To, File file2To) {
        if (!file1To.getParentFile().exists()) {
            file1To.getParentFile().mkdirs();
        }

        try {
            if (file1.exists()) Files.move(Paths.get(file1.toURI()), Paths.get(file1To.toURI()), StandardCopyOption.REPLACE_EXISTING);
            if (file2.exists()) Files.move(Paths.get(file2.toURI()), Paths.get(file2To.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canUse(CommandSender sender) {
        if (sender instanceof Player) {
            return sender.isOp();
        }
        return false;
    }
}
