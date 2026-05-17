package net.arm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ArmTeamMateClient implements ClientModInitializer {

    private static final String TEAM_NAME = "green_teammate";
    public static TeamModConfig config;

    private static KeyBinding configKeyBinding;
    public static final KeyBinding.Category ARM_CATEGORY = KeyBinding.Category.create(Identifier.of("armteammate", "main"));

    @Override
    public void onInitializeClient() {
        config = TeamModConfig.load();
        MenuDecorationHandler.init();
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.armteammate.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                ARM_CATEGORY
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            KotateamCommand.register(dispatcher);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            while (configKeyBinding.wasPressed()) {
                client.setScreen(new TeamConfigScreen(null));
            }

            Scoreboard scoreboard = client.world.getScoreboard();
            for (PlayerEntity player : client.world.getPlayers()) {
                String playerName = player.getName().getString();

                if (config.nameColor && config.teammates.contains(playerName)) {
                    setupTeam(scoreboard, player);
                } else {
                    Team team = scoreboard.getTeam(TEAM_NAME);
                    if (team != null) {
                        AbstractTeam currentTeam = player.getScoreboardTeam();
                        if (currentTeam != null && currentTeam.getName().equals(TEAM_NAME)) {
                            scoreboard.removeScoreHolderFromTeam(playerName, team);
                        }
                    }
                }
            }
        });
    }

    private void setupTeam(Scoreboard scoreboard, PlayerEntity player) {
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addTeam(TEAM_NAME);
            team.setColor(Formatting.GREEN);
        }

        AbstractTeam currentTeam = player.getScoreboardTeam();
        if (currentTeam == null || !currentTeam.getName().equals(TEAM_NAME)) {
            scoreboard.addScoreHolderToTeam(player.getName().getString(), team);
        }
    }
}