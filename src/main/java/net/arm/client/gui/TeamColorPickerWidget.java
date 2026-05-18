package net.arm.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class TeamColorPickerWidget {
    public static boolean isOpen = false;

    private float hue = 0.5f;
    private float saturation = 1.0f;
    private float value = 1.0f;

    private boolean draggingSV = false;
    private boolean draggingHue = false;

    // --- Масштабирование размеров в 1.6 раза ---
    private static final int REF_W = 3840;

    // Новые увеличенные размеры (в 1.6 раза больше оригинальных)
    private final int bgW = 416; // Было 260
    private final int bgH = 400; // Было 250

    // Смещение bgX вправо на 156 пикселей, чтобы левая граница сдвинулась, а правая ушла дальше
    // Оригинальный расчет: REF_W - 260 - 1232. Добавляем 156 -> -260 + 156 = -104
    private final int bgX = REF_W - 104 - 1400;

    // Смещение bgY вверх на 150 пикселей, чтобы компенсировать рост высоты вниз
    // Оригинальное значение: 990. Вычитаем 150 -> 840
    private final int bgY = 840;

    // Внутренние элементы палитры
    private final int pad = 32;
    private final int svSize = 256;
    private final int hueWidth = 38;

    public void setColor(int rgb) {
        float[] hsv = new float[3];
        java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public int getCurrentColor() {
        return MathHelper.hsvToRgb(hue, saturation, value) | 0xFF000000;
    }

    private int getLighterColor(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = Math.min(255, (int)(r + (255 - r) * factor));
        g = Math.min(255, (int)(g + (255 - g) * factor));
        b = Math.min(255, (int)(b + (255 - b) * factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int modifyAlpha(int color, float alpha) {
        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 255;
        return (color & 0x00FFFFFF) | ((int) (a * alpha) << 24);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float alpha) {
        if (!isOpen) return;

        int currentColor = getCurrentColor();
        int dynamicBorderColor = getLighterColor(currentColor, 0.35f);

        int svX = bgX + pad;
        int svY = bgY + pad;
        int hueX = svX + svSize + pad;

        RenderSystem.disableDepthTest();

        drawSVBox(context, svX, svY, svSize, svSize, hue, alpha);
        drawHueSlider(context, hueX, svY, hueWidth, svSize, alpha);

        RenderSystem.enableDepthTest();

        int cursorX = svX + (int) (saturation * svSize);
        int cursorY = svY + (int) ((1.0f - value) * svSize);
        context.fill(cursorX - 2, cursorY - 2, cursorX + 2, cursorY + 2, modifyAlpha(0xFFFFFFFF, alpha));
        context.fill(cursorX - 1, cursorY - 1, cursorX + 1, cursorY + 1, modifyAlpha(0xFF000000, alpha));

        int hueCursorY = svY + (int) (hue * svSize);
        context.fill(hueX - 2, hueCursorY - 2, hueX + hueWidth + 2, hueCursorY + 2, modifyAlpha(0xFFFFFFFF, alpha));
        context.fill(hueX, hueCursorY - 1, hueX + hueWidth, hueCursorY + 1, modifyAlpha(0xFF000000, alpha));

        // Смещаем превью вниз с учетом увеличенного размера (15 * 1.6 ≈ 24)
        int previewY = svY + svSize + 24;
        String hexText = String.format("#%06X", (currentColor & 0xFFFFFF));

        TextRenderer textRenderer = net.minecraft.client.MinecraftClient.getInstance().textRenderer;

        context.getMatrices().push();
        context.getMatrices().translate(svX, previewY, 0);
        // Текст hex-кода также можно слегка увеличить или оставить прежним (сейчас scale 3.0f)
        context.getMatrices().scale(3.0f, 3.0f, 1.0f);
        context.drawText(textRenderer, hexText, 0, 0, modifyAlpha(0xFFFFFFFF, alpha), false);
        context.getMatrices().pop();

        // Пропорционально увеличили размер кастомной кнопки (44*1.6 ≈ 70, 34*1.6 ≈ 54)
        drawCustomButton(context, hueX - 20, previewY - 5, 70, 54, dynamicBorderColor, currentColor, alpha);
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!isOpen) return false;

        if (mouseX >= bgX && mouseX <= bgX + bgW && mouseY >= bgY && mouseY <= bgY + bgH) {
            int svX = bgX + pad;
            int svY = bgY + pad;
            int hueX = svX + svSize + pad;

            if (mouseX >= svX && mouseX <= svX + svSize && mouseY >= svY && mouseY <= svY + svSize) {
                draggingSV = true;
                updateSV(mouseX, mouseY, svX, svY);
                return true;
            }

            if (mouseX >= hueX && mouseX <= hueX + hueWidth && mouseY >= svY && mouseY <= svY + svSize) {
                draggingHue = true;
                updateHue(mouseY, svY);
                return true;
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!isOpen) return false;
        int svX = bgX + pad;
        int svY = bgY + pad;

        if (draggingSV) {
            updateSV(mouseX, mouseY, svX, svY);
            return true;
        }
        if (draggingHue) {
            updateHue(mouseY, svY);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        draggingSV = false;
        draggingHue = false;
    }

    private void updateSV(double mouseX, double mouseY, int svX, int svY) {
        this.saturation = MathHelper.clamp((float) (mouseX - svX) / svSize, 0f, 1f);
        this.value = MathHelper.clamp(1.0f - (float) (mouseY - svY) / svSize, 0f, 1f);
    }

    private void updateHue(double mouseY, int svY) {
        this.hue = MathHelper.clamp((float) (mouseY - svY) / svSize, 0f, 1f);
    }

    private void drawSVBox(DrawContext context, int x, int y, int w, int h, float hue, float alpha) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        int pureHue = MathHelper.hsvToRgb(hue, 1.0f, 1.0f);
        float[] cHue = getRgb(pureHue);

        BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0).color(1f, 1f, 1f, alpha);
        buffer.vertex(matrix, x, y + h, 0).color(0f, 0f, 0f, alpha);
        buffer.vertex(matrix, x + w, y + h, 0).color(0f, 0f, 0f, alpha);
        buffer.vertex(matrix, x + w, y, 0).color(cHue[0], cHue[1], cHue[2], alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawHueSlider(DrawContext context, int x, int y, int w, int h, float alpha) {
        int[] colors = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        float segmentH = (float) h / 6.0f;

        for (int i = 0; i < 6; i++) {
            float topY = y + i * segmentH;
            drawGradientRect(context, x, topY, w, segmentH, colors[i], colors[i + 1], alpha);
        }
    }

    private void drawGradientRect(DrawContext context, float x, float y, float w, float h, int colorTop, int colorBot, float alpha) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        float[] cT = getRgb(colorTop);
        float[] cB = getRgb(colorBot);

        BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0).color(cT[0], cT[1], cT[2], alpha);
        buffer.vertex(matrix, x, y + h, 0).color(cB[0], cB[1], cB[2], alpha);
        buffer.vertex(matrix, x + w, y + h, 0).color(cB[0], cB[1], cB[2], alpha);
        buffer.vertex(matrix, x + w, y, 0).color(cT[0], cT[1], cT[2], alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private float[] getRgb(int color) {
        return new float[]{((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f};
    }

    private void drawCustomButton(DrawContext context, int x, int y, int width, int height, int borderColor, int fillColor, float alpha) {
        borderColor = modifyAlpha(borderColor, alpha);
        fillColor = modifyAlpha(fillColor, alpha);

        // Увеличил срезы (cut) и толщину (thick) под новый размер кнопки
        int cut = 12;   // было 8
        int thick = 6;  // было 4
        context.fill(x + cut, y, x + width - cut, y + height, borderColor);
        context.fill(x, y + cut, x + width, y + height - cut, borderColor);
        int fillOffset = cut + thick;
        context.fill(x + fillOffset, y + thick, x + width - fillOffset, y + height - thick, fillColor);
        context.fill(x + thick, y + fillOffset, x + width - thick, y + height - fillOffset, fillColor);
    }
}