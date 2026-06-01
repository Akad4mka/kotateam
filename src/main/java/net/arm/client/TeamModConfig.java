package net.arm.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TeamModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("arm_teammates.json");

    public boolean highlightGlow = true;
    public boolean armorReplace = true;
    public boolean nameColor = true;

    public int teamColor = 0x00B8AA;

    public List<String> teammates = new ArrayList<>();



    public void save() {
        try (var writer = Files.newBufferedWriter(FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static TeamModConfig load() {
        if (Files.exists(FILE)) {
            try (var reader = Files.newBufferedReader(FILE)) {
                TeamModConfig loaded = GSON.fromJson(reader, TeamModConfig.class);
                if (loaded != null ){
                    if (loaded.teammates == null) {
                        loaded.teammates = new java.util.ArrayList<>();
                    }
                    return loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new TeamModConfig();
    }
}