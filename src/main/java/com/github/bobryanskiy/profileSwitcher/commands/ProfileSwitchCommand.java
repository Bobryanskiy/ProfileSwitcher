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
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
            StringBuilder stringBuilder = new StringBuilder();
            executor.getActivePotionEffects().forEach(potionEffect -> stringBuilder.append(potionEffect.serialize()).append("|"));
            ProfileSwitcher.getInstance().getConfig().set(executor.getName().toLowerCase() + "." + current + ".effects", stringBuilder.toString());

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

            String ef = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".effects");
            executor.clearActivePotionEffects();
            if (ef != null && !ef.isEmpty()) {
                String[] effectsArray = (ef.substring(0, ef.length() -1)).split("\\|");
                for (String effect : effectsArray) {
                    PotionEffect potionEffect = new PotionEffect(deserializeEffect(effect));
                    executor.addPotionEffect(potionEffect);
                }
            }

            String gm = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".gamemode");
            if (gm != null) executor.setGameMode(GameMode.valueOf(gm));
            String w = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".location");
            if (w != null) {
                Map<String, Object> map = getStringObjectMap(w);
                executor.teleportAsync(Location.deserialize(map));
            }
            String isF = ProfileSwitcher.getInstance().getConfig().getString(executor.getName().toLowerCase() + "." + current + ".isFlying");
            if (isF != null) executor.setFlying(Boolean.parseBoolean(isF));
        }
    }

    private Map<String, Object> deserializeEffect(String serializedString) {
        Map<String, Object> deserializedMap = new HashMap<>();
        String[] pairs = serializedString.replace("{", "").replace("}", "").split(",");

        for (String pair : pairs) {
            String[] seperatedPairs = pair.split("=");

            String key = seperatedPairs[0].trim();
            String value = seperatedPairs[1].trim();

            switch (key) {
                case "effect":
                    deserializedMap.put(key, value);
                    break;
                case "duration", "amplifier":
                    deserializedMap.put(key, Integer.parseInt(value));
                    break;
                default:
                    deserializedMap.put(key, Boolean.parseBoolean(value));
            }
        }
        return deserializedMap;
    }

    private static Map<String, Object> getStringObjectMap(String str) {
        String jsonInput = str.replace("=", "\":\"")
                .replace(", ", "\", \"")
                .replace("{", "{\"")
                .replace("}", "\"}");
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(jsonInput, type);
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
