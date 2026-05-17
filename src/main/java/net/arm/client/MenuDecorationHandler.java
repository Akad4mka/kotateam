package net.arm.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class MenuDecorationHandler {

    private static final Identifier LEFT_TEXTURE = Identifier.of("teammate", "textures/left.png");
    private static final Identifier RIGHT_TEXTURE = Identifier.of("teammate", "textures/right.png");
    private static final Identifier LEFT_TEXTURE_ON = Identifier.of("teammate", "textures/left_on.png");
    private static final Identifier RIGHT_TEXTURE_ON = Identifier.of("teammate", "textures/right_on.png");
    private static final Identifier LOGO_TEXTURE = Identifier.of("teammate", "textures/logo.png");

    private static final int ORIG_W = 1080;
    private static final int ORIG_H = 2100;
    private static final float ASPECT_RATIO = (float) ORIG_W / ORIG_H;

    private static final int LOGO_ORIG_SIZE = 1080;

    private static float leftHoverProgress = 0f;
    private static float rightHoverProgress = 0f;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                ScreenEvents.afterRender(screen).register((scr, drawContext, mouseX, mouseY, tickDelta) -> {
                    renderMenuElements(drawContext, scr.width, scr.height, mouseX, mouseY, tickDelta);
                });

                ScreenMouseEvents.afterMouseClick(screen).register((scr, context, consumed) -> {
                    double mx = context.x();
                    double my = context.y();
                    int button = context.button();

                    if (button == 0) {
                        if (isMouseOverLeft(scr.width, scr.height, mx, my) ||
                                isMouseOverRight(scr.width, scr.height, mx, my)) {

                            Text title = Text.literal("Перейти в ")
                                    .append(Text.literal("Telegram").formatted(Formatting.AQUA))
                                    .append("-бота с бесплатными модами?");

                            Text yesText = Text.literal("Перейти").formatted(Formatting.GREEN);
                            Text noText = Text.literal("Остаться").formatted(Formatting.RED);

                            MinecraftClient.getInstance().setScreen(new ConfirmScreen(
                                    (confirmed) -> {
                                        if (confirmed) {
                                            Util.getOperatingSystem().open("https://t.me/kotamods_bot");
                                        }
                                        MinecraftClient.getInstance().setScreen(screen);
                                    },
                                    title,
                                    Text.empty(),
                                    yesText,
                                    noText
                            ));

                            return true;
                        }
                    }
                    return false;
                });
            }
        });
    }

    private static void renderMenuElements(DrawContext context, int screenWidth, int screenHeight, double mouseX, double mouseY, float tickDelta) {
        int targetHeight = 168;
        int targetWidth = (int) (targetHeight * ASPECT_RATIO);
        int yDecor = (screenHeight / 4) + 6;
        int centerX = screenWidth / 2;
        int buttonHalfWidth = 105;

        int xLeft = centerX - buttonHalfWidth - targetWidth;
        int xRight = centerX + buttonHalfWidth;

        int logoSize = targetHeight / 3;
        int xLogo = centerX - (logoSize / 2);
        int yLogo = yDecor - logoSize - 2;
        drawGenericTexture(context, LOGO_TEXTURE, xLogo, yLogo, logoSize, logoSize, LOGO_ORIG_SIZE, LOGO_ORIG_SIZE, -1);

        float speed = 0.15f;
        boolean leftHovered = mouseX >= xLeft && mouseX <= xLeft + targetWidth && mouseY >= yDecor && mouseY <= yDecor + targetHeight;
        leftHoverProgress = MathHelper.lerp(speed * tickDelta, leftHoverProgress, leftHovered ? 1.0f : 0.0f);
        boolean hoveredRight = mouseX >= xRight && mouseX <= xRight + targetWidth && mouseY >= yDecor && mouseY <= yDecor + targetHeight;
        rightHoverProgress = MathHelper.lerp(speed * tickDelta, rightHoverProgress, hoveredRight ? 1.0f : 0.0f);

        drawGenericTexture(context, LEFT_TEXTURE, xLeft, yDecor, targetWidth, targetHeight, ORIG_W, ORIG_H, -1);
        if (leftHoverProgress > 0) {
            int alpha = (int) (leftHoverProgress * 255);
            drawGenericTexture(context, LEFT_TEXTURE_ON, xLeft, yDecor, targetWidth, targetHeight, ORIG_W, ORIG_H, ColorHelper.getArgb(alpha, 255, 255, 255));
        }
        drawGenericTexture(context, RIGHT_TEXTURE, xRight, yDecor, targetWidth, targetHeight, ORIG_W, ORIG_H, -1);
        if (rightHoverProgress > 0) {
            int alpha = (int) (rightHoverProgress * 255);
            drawGenericTexture(context, RIGHT_TEXTURE_ON, xRight, yDecor, targetWidth, targetHeight, ORIG_W, ORIG_H, ColorHelper.getArgb(alpha, 255, 255, 255));
        }
    }

    private static boolean isMouseOverLeft(int sw, int sh, double mx, double my) {
        int th = 168;
        int tw = (int) (th * ASPECT_RATIO);
        int x = (sw / 2) - 105 - tw;
        int y = (sh / 4) + 6;
        return mx >= x && mx <= (x + tw) && my >= y && my <= (y + th);
    }

    private static boolean isMouseOverRight(int sw, int sh, double mx, double my) {
        int th = 168;
        int tw = (int) (th * ASPECT_RATIO);
        int x = (sw / 2) + 105;
        int y = (sh / 4) + 6;
        return mx >= x && mx <= (x + tw) && my >= y && my <= (y + th);
    }

    private static void drawGenericTexture(DrawContext context, Identifier texture, int x, int y, int w, int h, int origW, int origH, int color) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y,
                0f, 0f,
                w, h,
                origW, origH,
                origW, origH,
                color
        );
    }
}