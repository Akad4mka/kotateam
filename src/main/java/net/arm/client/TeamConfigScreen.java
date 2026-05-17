package net.arm.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeamConfigScreen extends Screen {
    private final Screen parent;

    public TeamConfigScreen(@Nullable Screen parent) {
        super(Text.literal("Настройки Тиммейтов"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;

        int centerX = this.width / 2 - (buttonWidth / 2);
        int startY = this.height / 2 - 70;
        int spacing = 24;

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(ArmTeamMateClient.config.nameColor)
                .build(centerX, startY, buttonWidth, buttonHeight, Text.literal("Цвет Ника"), (button, value) -> {
                    ArmTeamMateClient.config.nameColor = value;
                }));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(ArmTeamMateClient.config.pvpProtect)
                .build(centerX, startY + spacing, buttonWidth, buttonHeight, Text.literal("Защита от своих"), (button, value) -> {
                    ArmTeamMateClient.config.pvpProtect = value;
                }));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(ArmTeamMateClient.config.armorReplace)
                .build(centerX, startY + (spacing * 2), buttonWidth, buttonHeight, Text.literal("Покрас брони"), (button, value) -> {
                    ArmTeamMateClient.config.armorReplace = value;
                }));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(ArmTeamMateClient.config.highlightGlow)
                .build(centerX, startY + (spacing * 3), buttonWidth, buttonHeight, Text.literal("Свечение"), (button, value) -> {
                    ArmTeamMateClient.config.highlightGlow = value;
                }));

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить тиммейта").formatted(Formatting.GREEN), button -> {
            if (this.client != null) {
                this.client.setScreen(new ChatScreen("/kotateam add ", false));
            }
        }).dimensions(centerX, startY + (spacing * 4) + 6, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), button -> {
            this.close();
        }).dimensions(centerX, startY + (spacing * 5) + 16, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int rightX = this.width / 2 - 100;
        int startY = this.height / 2 - 70 + (24 * 5) + 45;

        if (ArmTeamMateClient.config == null || ArmTeamMateClient.config.teammates == null || ArmTeamMateClient.config.teammates.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Список пуст").formatted(Formatting.GRAY), this.width / 2, startY, 0xFFFFFF);
        } else {
            List<String> currentTeammates = ArmTeamMateClient.config.teammates;
            String listText = "Тиммейты: " + String.join(", ", currentTeammates);

            if (this.textRenderer.getWidth(listText) > 300) {
                listText = this.textRenderer.trimToWidth(listText, 290) + "...";
            }
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(listText), this.width / 2, startY, 0xFFFFFF);
        }
    }

    @Override
    public void close() {
        ArmTeamMateClient.config.save();

        if (this.client != null && this.client.world != null) {
            Scoreboard scoreboard = this.client.world.getScoreboard();
            Team team = scoreboard.getTeam("green_teammate");

            if (team != null) {
                for (PlayerEntity player : this.client.world.getPlayers()) {
                    String playerName = player.getName().getString();
                    if (!ArmTeamMateClient.config.nameColor || !ArmTeamMateClient.config.teammates.contains(playerName)) {
                        AbstractTeam currentTeam = player.getScoreboardTeam();
                        if (currentTeam != null && currentTeam.getName().equals("green_teammate")) {
                            scoreboard.removeScoreHolderFromTeam(playerName, team);
                        }
                    }
                }
            }
        }

        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}