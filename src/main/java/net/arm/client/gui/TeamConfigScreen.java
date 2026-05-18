package net.arm.client.gui;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static net.arm.client.gui.TeamColorPickerWidget.isOpen;

public class TeamConfigScreen extends Screen {

    private static final int REF_W = 3840;
    private static final int REF_H = 2160;

    private final TeamColorPickerWidget colorPicker = new TeamColorPickerWidget();

    private long lastRenderTime = 0;
    private final List<ToggleButton> toggleButtons = new ArrayList<>();

    private String selectedTeammate = null;
    private final List<TeammateClickZone> teammateClickZones = new ArrayList<>();
    private final List<TeammateColorAnimation> colorAnimations = new ArrayList<>();

    private float buttonStateProgress = 0.0f;

    private boolean isClosing = false;
    private float fadeProgress = 0.0f;
    private Screen pendingScreen = null;

    private float scrollX = 0.0f;
    private boolean draggingScroll = false;

    public TeamConfigScreen(MutableText настройки) {
        super(Text.literal("Team Config"));
    }

    @Override
    protected void init() {
        toggleButtons.clear();
        int startY = 864;
        int stepY = 71;

        toggleButtons.add(new ToggleButton(startY, () -> ArmTeamMateClient.config.armorReplace, (val) -> ArmTeamMateClient.config.armorReplace = val));
        toggleButtons.add(new ToggleButton(startY + stepY, () -> ArmTeamMateClient.config.highlightGlow, (val) -> ArmTeamMateClient.config.highlightGlow = val));
        toggleButtons.add(new ToggleButton(startY + stepY * 2, () -> ArmTeamMateClient.config.nameColor, (val) -> ArmTeamMateClient.config.nameColor = val));

        for (ToggleButton btn : toggleButtons) {
            btn.initProgress();
        }

        if (ArmTeamMateClient.config != null) {
            colorPicker.setColor(ArmTeamMateClient.config.teamColor);
        }
    }

    private int modifyAlpha(int color, float alpha) {
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 255;
        int newAlpha = (int) (a * alpha);
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }

    @Override
    public void close() {
        if (!isClosing) {
            isClosing = true;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        long currentTime = System.currentTimeMillis();
        if (lastRenderTime == 0) lastRenderTime = currentTime;
        float elapsedSec = (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (elapsedSec > 0.1f) elapsedSec = 0.1f;

        float fadeSpeed = 5.0f;
        if (isClosing) {
            fadeProgress = Math.max(0.0f, fadeProgress - elapsedSec * fadeSpeed);
            if (fadeProgress <= 0.0f) {
                if (this.client != null && pendingScreen != null) {
                    this.client.setScreen(pendingScreen);
                } else {
                    super.close();
                }
                return;
            }
        } else {
            fadeProgress = Math.min(1.0f, fadeProgress + elapsedSec * fadeSpeed);
        }

        for (ToggleButton btn : toggleButtons) {
            btn.updateAnimation(elapsedSec);
        }

        float speed = 5.0f;
        if (selectedTeammate != null) {
            if (buttonStateProgress < 1.0f) buttonStateProgress = Math.min(1.0f, buttonStateProgress + elapsedSec * speed);
        } else {
            if (buttonStateProgress > 0.0f) buttonStateProgress = Math.max(0.0f, buttonStateProgress - elapsedSec * speed);
        }

        for (TeammateColorAnimation anim : colorAnimations) {
            if (anim.name.equals(selectedTeammate)) {
                if (anim.colorProgress < 1.0f) anim.colorProgress = Math.min(1.0f, anim.colorProgress + elapsedSec * speed);
            } else {
                if (anim.colorProgress > 0.0f) anim.colorProgress = Math.max(0.0f, anim.colorProgress - elapsedSec * speed);
            }
        }

        float scale = Math.min((float) this.width / REF_W, (float) this.height / REF_H);
        float offsetX = (this.width - (REF_W * scale)) / 2.0f;
        float offsetY = (this.height - (REF_H * scale)) / 2.0f;
        double vMouseX = (mouseX - offsetX) / scale;
        double vMouseY = (mouseY - offsetY) / scale;

        context.getMatrices().push();
        context.getMatrices().translate(offsetX, offsetY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        int academyX = REF_W - 766 - 3042;
        boolean hoveredAcademy = vMouseX >= academyX && vMouseX <= academyX + 766 && vMouseY >= 32 && vMouseY <= 32 + 52;
        int academyBorder = hoveredAcademy ? 0xFF3A3F4B : 0xFF1E2127;
        drawCustomButton(context, academyX, 32, 766, 75, academyBorder, 0xFF111316);
        drawCustomText(context, "Сделано учениками Kota Academy", REF_W - 3770, 54, 0xFFFFFFFF, 4.0f, false);

        drawCustomButton(context, REF_W - 850 - 1495, 774, 850, 380, 0xFF1E2127, 0xFF111316);
        drawCustomButton(context, REF_W - 850 - 1495, 1166, 850, 155, 0xFF1E2127, 0xFF111316);

        if (isOpen) {
            drawCustomButton(context, REF_W - 359 - 1125, 849, 359, 366, 0xFF1E2127, 0xFF111316);
        }

        int settingsX = REF_W - 2313;
        int settingsY = 806;
        drawCustomText(context, "Настройки", settingsX, settingsY, 0xFFFFFFFF, 4.0f, false);

        drawCustomText(context, "Покрас брони:", REF_W - 2313, 877, 0xFFC0C0C0, 4.0f, false);
        drawCustomText(context, "ХитБокс:", REF_W - 2313, 948, 0xFFC0C0C0, 4.0f, false);
        drawCustomText(context, "Цвет ника:", REF_W - 2313, 1019, 0xFFC0C0C0, 4.0f, false);
        drawCustomText(context, "Выбор цвета:", REF_W - 2313, 1090, 0xFFC0C0C0, 4.0f, false);

        int teammatesX = REF_W - 2313;
        int teammatesY = 1198;
        drawCustomText(context, "Тиммейты", teammatesX, teammatesY, 0xFFFFFFFF, 4.0f, false);

        teammateClickZones.clear();
        int buttonCenterX = 1495 + (850 / 2);

        if (ArmTeamMateClient.config == null || ArmTeamMateClient.config.teammates == null || ArmTeamMateClient.config.teammates.isEmpty()) {
            String emptyStr = "Пусто";
            int renderedTextWidth = this.textRenderer.getWidth(emptyStr) * 4;
            drawCustomText(context, emptyStr, buttonCenterX - (renderedTextWidth / 2), 1261, 0xFF676767, 4.0f, false);
        } else {
            List<String> list = ArmTeamMateClient.config.teammates;
            int totalWidth = 0;
            for (int i = 0; i < list.size(); i++) {
                totalWidth += this.textRenderer.getWidth(list.get(i)) * 4;
                if (i < list.size() - 1) totalWidth += this.textRenderer.getWidth(", ") * 4;
            }

            int boxX = REF_W - 850 - 1495;
            int boxW = 850;
            int padding = 25;
            int allowedWidth = boxW - (padding * 2);

            int currentX;
            if (totalWidth > allowedWidth) {
                int maxScrollOffset = totalWidth - allowedWidth;
                currentX = (boxX + padding) - (int) (scrollX * maxScrollOffset);
            } else {
                currentX = buttonCenterX - (totalWidth / 2);
                scrollX = 0.0f;
            }

            int textY = 1261;

            context.enableScissor(boxX + padding, 1166, boxX + boxW - padding, 1166 + 155);

            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i);
                int nameWidth = this.textRenderer.getWidth(name) * 4;

                TeammateColorAnimation anim = colorAnimations.stream()
                        .filter(a -> a.name.equals(name))
                        .findFirst()
                        .orElseGet(() -> {
                            TeammateColorAnimation newAnim = new TeammateColorAnimation(name);
                            colorAnimations.add(newAnim);
                            return newAnim;
                        });

                int nameColor = lerpColor(0xFF676767, 0xFFFFFFFF, anim.colorProgress);
                drawCustomText(context, name, currentX, textY, nameColor, 4.0f, false);

                teammateClickZones.add(new TeammateClickZone(name, currentX, textY, currentX + nameWidth, textY + 40));

                currentX += nameWidth;
                if (i < list.size() - 1) {
                    String comma = ", ";
                    int commaWidth = this.textRenderer.getWidth(comma) * 4;
                    drawCustomText(context, comma, currentX, textY, 0xFF676767, 4.0f, false);
                    currentX += commaWidth;
                }
            }

            context.disableScissor();

            if (totalWidth > allowedWidth) {
                int barY = textY + 50;
                int barH = 3;
                int trackX = boxX + padding;
                int trackW = allowedWidth;

                int thumbW = Math.max(40, (int) ((float) trackW / totalWidth * trackW));
                int maxThumbTravel = trackW - thumbW;
                int thumbX = trackX + (int) (scrollX * maxThumbTravel);

                int trackColor = modifyAlpha(0xFF32363F, fadeProgress);
                context.fill(trackX, barY, trackX + trackW, barY + barH, trackColor);

                int thumbColor = modifyAlpha(0xFF8E929C, fadeProgress);
                context.fill(thumbX, barY, thumbX + thumbW, barY + barH, thumbColor);
            }
        }

        for (ToggleButton btn : toggleButtons) {
            btn.render(context);
        }

        int selectedRGB = colorPicker.getCurrentColor();
        int btnFill = selectedRGB;
        int btnBorder = (selectedRGB & 0x00FFFFFF) | 0x99000000;
        drawCustomButton(context, REF_W - 104 - 1527, 1080, 104, 52, btnBorder, btnFill);

        int addButtonX = (int) (1495 + (431 * buttonStateProgress));
        int addButtonWidth = (int) (850 - (431 * buttonStateProgress));
        int addTextX = (int) (1824 + (216 * buttonStateProgress));

        drawCustomButton(context, addButtonX, 1333, addButtonWidth, 75, 0xFF1E2127, 0xFF111316);
        drawCustomText(context, "Добавить", addTextX, 1355, 0xFFFFFFFF, 4.0f, false);

        if (buttonStateProgress > 0.01f) {
            int deleteButtonX = 1495;
            int deleteTextX = 1621;

            context.getMatrices().push();
            int alpha = (int) (buttonStateProgress * 255) << 24;
            int deleteBorder = (0xFFE73D4B & 0x00FFFFFF) | alpha;
            int deleteFill = (0xFFA91925 & 0x00FFFFFF) | alpha;
            int deleteTextColor = (0xFFFFFFFF & 0x00FFFFFF) | alpha;

            drawCustomButton(context, deleteButtonX, 1333, 419, 75, deleteBorder, deleteFill);
            drawCustomText(context, "Удалить", deleteTextX, 1355, deleteTextColor, 4.0f, false);
            context.getMatrices().pop();
        }

        colorPicker.render(context, (int) vMouseX, (int) vMouseY, fadeProgress);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = Math.min((float) this.width / REF_W, (float) this.height / REF_H);
        float offsetX = (this.width - (REF_W * scale)) / 2.0f;
        float offsetY = (this.height - (REF_H * scale)) / 2.0f;

        double vMouseX = (mouseX - offsetX) / scale;
        double vMouseY = (mouseY - offsetY) / scale;

        if (button == 0) {
            if (colorPicker.mouseClicked(vMouseX, vMouseY)) {
                if (ArmTeamMateClient.config != null) {
                    ArmTeamMateClient.config.teamColor = colorPicker.getCurrentColor() & 0xFFFFFF;
                    ArmTeamMateClient.config.save();
                }
                return true;
            }

            int boxX = REF_W - 850 - 1495;
            int padding = 25;
            int trackX = boxX + padding;
            int trackW = boxWFromCode(boxX);
            int barY = 1261 + 50;

            if (vMouseX >= trackX && vMouseX <= trackX + trackW && vMouseY >= barY - 5 && vMouseY <= barY + 10) {
                draggingScroll = true;
                updateScrollFromMouse(vMouseX, trackX, trackW);
                return true;
            }

            int academyX = REF_W - 850 - 2953;
            if (vMouseX >= academyX && vMouseX <= academyX + 850 && vMouseY >= 32 && vMouseY <= 32 + 75) {
                playClickSound();
                net.minecraft.util.Util.getOperatingSystem().open("https://t.me/kotaacademy");
                return true;
            }

            for (ToggleButton btn : toggleButtons) {
                if (btn.checkClick(vMouseX, vMouseY)) return true;
            }

            int colorBtnX = REF_W - 104 - 1527;
            if (vMouseX >= colorBtnX && vMouseX <= colorBtnX + 104 && vMouseY >= 1080 && vMouseY <= 1080 + 52) {
                isOpen = !isOpen;
                playClickSound();
                return true;
            }

            for (TeammateClickZone zone : teammateClickZones) {
                if (vMouseX >= zone.x1 && vMouseX <= zone.x2 && vMouseY >= zone.y1 && vMouseY <= zone.y2) {
                    selectedTeammate = zone.name.equals(selectedTeammate) ? null : zone.name;
                    playClickSound();
                    return true;
                }
            }

            int currentAddX = (int) (1495 + (431 * buttonStateProgress));
            int addButtonWidth = (int) (850 - (431 * buttonStateProgress));
            if (vMouseX >= currentAddX && vMouseX <= currentAddX + addButtonWidth && vMouseY >= 1333 && vMouseY <= 1333 + 75) {
                playClickSound();
                this.pendingScreen = new net.minecraft.client.gui.screen.ChatScreen("/kotateam add ");
                this.close();
                return true;
            }

            if (selectedTeammate != null && buttonStateProgress >= 0.9f) {
                int deleteX = 1495;
                if (vMouseX >= deleteX && vMouseX <= deleteX + 419 && vMouseY >= 1333 && vMouseY <= 1333 + 75) {
                    if (ArmTeamMateClient.config != null && ArmTeamMateClient.config.teammates != null) {
                        ArmTeamMateClient.config.teammates.remove(selectedTeammate);
                        ArmTeamMateClient.config.save();
                        colorAnimations.removeIf(a -> a.name.equals(selectedTeammate));
                        selectedTeammate = null;
                    }
                    playClickSound();
                    return true;
                }
            }

            if (vMouseX >= 1495 && vMouseX <= 1495 + 850 && vMouseY >= 1166 && vMouseY <= 1166 + 155) {
                selectedTeammate = null;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int boxWFromCode(int boxX) {
        return 850 - (25 * 2);
    }

    private void updateScrollFromMouse(double vMouseX, int trackX, int trackW) {
        List<String> list = ArmTeamMateClient.config.teammates;
        if (list == null || list.isEmpty()) return;

        int totalWidth = 0;
        for (int i = 0; i < list.size(); i++) {
            totalWidth += this.textRenderer.getWidth(list.get(i)) * 4;
            if (i < list.size() - 1) totalWidth += this.textRenderer.getWidth(", ") * 4;
        }

        int thumbW = Math.max(40, (int) ((float) trackW / totalWidth * trackW));
        int maxThumbTravel = trackW - thumbW;

        if (maxThumbTravel <= 0) return;

        double mousePosInTravel = vMouseX - trackX - (thumbW / 2.0);
        scrollX = (float) (mousePosInTravel / maxThumbTravel);
        scrollX = MathHelper.clamp(scrollX, 0.0f, 1.0f);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float scale = Math.min((float) this.width / REF_W, (float) this.height / REF_H);
        float offsetX = (this.width - (REF_W * scale)) / 2.0f;
        float offsetY = (this.height - (REF_H * scale)) / 2.0f;
        double vMouseX = (mouseX - offsetX) / scale;
        double vMouseY = (mouseY - offsetY) / scale;

        if (draggingScroll) {
            int boxX = REF_W - 850 - 1495;
            int padding = 25;
            updateScrollFromMouse(vMouseX, boxX + padding, boxWFromCode(boxX));
            return true;
        }

        if (colorPicker.mouseDragged(vMouseX, vMouseY)) {
            if (ArmTeamMateClient.config != null) {
                ArmTeamMateClient.config.teamColor = colorPicker.getCurrentColor() & 0xFFFFFF;
                ArmTeamMateClient.config.save();
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingScroll = false;
        }
        colorPicker.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawCustomButton(DrawContext context, int x, int y, int width, int height, int borderColor, int fillColor) {
        borderColor = modifyAlpha(borderColor, fadeProgress);
        fillColor = modifyAlpha(fillColor, fadeProgress);

        int cut = 8;
        int thick = 4;
        context.fill(x + cut, y, x + width - cut, y + height, borderColor);
        context.fill(x, y + cut, x + width, y + height - cut, borderColor);
        int fillOffset = cut + thick;
        context.fill(x + fillOffset, y + thick, x + width - fillOffset, y + height - thick, fillColor);
        context.fill(x + thick, y + fillOffset, x + width - thick, y + height - fillOffset, fillColor);
    }

    private void drawCustomText(DrawContext context, String text, int x, int y, int color, float scale, boolean shadow) {
        color = modifyAlpha(color, fadeProgress);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawText(this.textRenderer, text, 0, 0, color, shadow);
        context.getMatrices().pop();
    }

    private int lerpColor(int startColor, int endColor, float fraction) {
        int startA = (startColor >> 24) & 0xFF;
        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endA = (endColor >> 24) & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int r = (int) (startR + fraction * (endR - startR));
        int g = (int) (startG + fraction * (endG - startG));
        int b = (int) (startB + fraction * (endB - startB));
        int a = (int) (startA + fraction * (endA - startA));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private class ToggleButton {
        private final int y;
        private final java.util.function.Supplier<Boolean> getter;
        private final java.util.function.Consumer<Boolean> setter;
        private float progress = 0.0f;
        private static final int textYOffset = 10;

        public ToggleButton(int y, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
            this.y = y;
            this.getter = getter;
            this.setter = setter;
        }

        public void initProgress() {
            this.progress = getter.get() ? 1.0f : 0.0f;
        }

        public void updateAnimation(float elapsedSec) {
            float target = getter.get() ? 1.0f : 0.0f;
            float speed = 6.0f;
            if (progress < target) progress = Math.min(target, progress + elapsedSec * speed);
            else if (progress > target) progress = Math.max(target, progress - elapsedSec * speed);
        }

        public void render(DrawContext context) {
            int bgX = REF_W - 104 - 1527;
            int bgBorderColor = lerpColor(0xFF7E161F, 0xFF00746B, progress);
            int bgFillColor = lerpColor(0xFF3A1115, 0xFF07524D, progress);
            int knobBorderColor = lerpColor(0xFFE73D4B, 0xFF00E3D2, progress);
            int knobFillColor = lerpColor(0xFFA91925, 0xFF00B8AA, progress);

            int startKnobX = REF_W - 52 - 1579;
            int knobX = startKnobX + (int) (52 * progress);

            drawCustomButton(context, bgX, y, 104, 52, bgBorderColor, bgFillColor);
            drawCustomButton(context, knobX, y, 52, 52, knobBorderColor, knobFillColor);

            String symbol = progress > 0.5f ? "o" : "x";
            int textX = knobX + (progress > 0.5f ? 17 : 16);
            drawCustomText(context, symbol, textX, y + textYOffset, 0xFFFFFFFF, 4.0f, false);
        }

        public boolean checkClick(double vMouseX, double vMouseY) {
            int bgX = REF_W - 104 - 1527;
            if (vMouseX >= bgX && vMouseX <= bgX + 104 && vMouseY >= y && vMouseY <= y + 52) {
                setter.accept(!getter.get());
                playClickSound();
                return true;
            }
            return false;
        }
    }

    private static class TeammateClickZone {
        String name; int x1, y1, x2, y2;
        public TeammateClickZone(String name, int x1, int y1, int x2, int y2) {
            this.name = name; this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
        }
    }

    private static class TeammateColorAnimation {
        String name; float colorProgress = 0.0f;
        public TeammateColorAnimation(String name) { this.name = name; }
    }

    private void playClickSound() {
        if (this.client != null && this.client.player != null) {
            this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }
}