package net.arm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.lwjgl.glfw.GLFW;

import net.arm.client.gui.TeamConfigScreen;

public class ArmTeamMateClient implements ClientModInitializer {

    public static TeamModConfig config;
    private static KeyBinding configKeyBinding;
    public static final String ARM_CATEGORY = "category.armteammate.main";

    
    public static final TextColor TEAMMATE_COLOR = TextColor.fromRgb(0x00B8AA);

    @Override
    public void onInitializeClient() {
        config = TeamModConfig.load();

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
                client.setScreen(new TeamConfigScreen(Text.literal("Настройки")));
            }
        });

        
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (config == null || !config.nameColor || config.teammates.isEmpty()) {
                return message;
            }
            return modifyTeammateStyles(message);
        });
    }

    /**
     * Исправленный рекурсивный метод. Теперь он проверяет, содержит ли кусок текста ник тиммейта,
     * что позволяет красить ники даже при наличии серверных префиксов/рангов в одной строке.
     */
    private Text modifyTeammateStyles(Text text) {
        String content = text.getLiteralString();
        MutableText modified;

        if (content != null && !content.isEmpty()) {
            boolean foundTeammate = false;
            String matchedName = "";

            
            for (String teammate : config.teammates) {
                if (content.contains(teammate)) {
                    foundTeammate = true;
                    matchedName = teammate;
                    break;
                }
            }

            if (foundTeammate) {
                
                int index = content.indexOf(matchedName);
                String before = content.substring(0, index);
                String after = content.substring(index + matchedName.length());

                modified = Text.empty();
                if (!before.isEmpty()) modified.append(Text.literal(before).setStyle(text.getStyle()));

                
                int currentHex = (config != null) ? config.teamColor : 0x00B8AA;
                TextColor dynamicColor = TextColor.fromRgb(currentHex);

                modified.append(Text.literal(matchedName).setStyle(text.getStyle().withColor(dynamicColor).withBold(true)));

                if (!after.isEmpty()) modified.append(Text.literal(after).setStyle(text.getStyle()));
            } else {
                modified = text.copyContentOnly().setStyle(text.getStyle());
            }
        } else {
            modified = text.copyContentOnly().setStyle(text.getStyle());
        }

        for (Text sibling : text.getSiblings()) {
            modified.append(modifyTeammateStyles(sibling));
        }

        return modified;
    }
}