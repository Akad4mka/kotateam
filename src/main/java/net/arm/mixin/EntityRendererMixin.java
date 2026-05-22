package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends net.minecraft.entity.Entity, S extends EntityRenderState> {

    @Inject(method = "render", at = @At("HEAD"))
    private void modifyLabelText(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ArmTeamMateClient.config == null || !ArmTeamMateClient.config.nameColor || state.displayName == null || ArmTeamMateClient.config.teammates.isEmpty()) {
            return;
        }

        // Получаем полную строку, включая все префиксы и суффиксы сервера
        String fullRawName = state.displayName.getString();
        boolean isTeammate = false;
        String matchedName = "";

        // Проверяем, содержится ли ник кого-то из тиммейтов в этой строке
        for (String teammate : ArmTeamMateClient.config.teammates) {
            if (fullRawName.contains(teammate)) {
                isTeammate = true;
                matchedName = teammate;
                break;
            }
        }

        if (isTeammate) {
            // Если тиммейт найден, рекурсивно перекрашиваем только его ник
            state.displayName = applyTeammateColor(state.displayName, matchedName);
        }
    }

    /**
     * Рекурсивный метод для сохранения структуры текста (префиксов/кланов).
     * Работает по аналогии с вашим modifyTeammateStyles для чата.
     */
    private Text applyTeammateColor(Text text, String teammateName) {
        String content = text.getLiteralString();
        MutableText modified;

        if (content != null && content.contains(teammateName)) {
            int index = content.indexOf(teammateName);
            String before = content.substring(0, index);
            String after = content.substring(index + teammateName.length());

            modified = Text.empty();

            // Добавляем то, что было ДО ника (например, префикс [Admin])
            if (!before.isEmpty()) {
                modified.append(Text.literal(before).setStyle(text.getStyle()));
            }

            // Красим сам ник в цвет команды и делаем жирным
            int currentHex = ArmTeamMateClient.config.teamColor;
            TextColor dynamicColor = TextColor.fromRgb(currentHex);
            modified.append(Text.literal(teammateName).setStyle(text.getStyle().withColor(dynamicColor).withBold(true)));

            // Добавляем то, что ПОСЛЕ ника
            if (!after.isEmpty()) {
                modified.append(Text.literal(after).setStyle(text.getStyle()));
            }
        } else {
            // Если в этом куске текста нет ника, просто копируем его как есть
            modified = text.copyContentOnly().setStyle(text.getStyle());
        }

        // Рекурсивно обрабатываем всех "детей" (siblings) этого текста
        for (Text sibling : text.getSiblings()) {
            modified.append(applyTeammateColor(sibling, teammateName));
        }

        return modified;
    }
}