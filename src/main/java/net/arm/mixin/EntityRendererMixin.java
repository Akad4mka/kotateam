package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends net.minecraft.entity.Entity, S extends EntityRenderState> {

    @ModifyVariable(
            method = "renderLabelIfPresent",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text modifyLabelText(Text text, S state) {
        if (ArmTeamMateClient.config == null || !ArmTeamMateClient.config.nameColor || text == null || ArmTeamMateClient.config.teammates.isEmpty()) {
            return text;
        }

        String fullRawName = text.getString();
        boolean isTeammate = false;
        String matchedName = "";

        for (String teammate : ArmTeamMateClient.config.teammates) {
            if (fullRawName.toLowerCase().contains(teammate.toLowerCase())) {
                isTeammate = true;
                matchedName = teammate;
                break;
            }
        }

        if (isTeammate) {
            return arm$applyTeammateColor(text, matchedName);
        }

        return text;
    }

    @Unique
    private Text arm$applyTeammateColor(Text text, String teammateName) {
        String content = text.getLiteralString();
        MutableText modified;

        if (content != null && content.toLowerCase().contains(teammateName.toLowerCase())) {
            int index = content.toLowerCase().indexOf(teammateName.toLowerCase());
            String before = content.substring(0, index);
            String actualTeammateName = content.substring(index, index + teammateName.length());
            String after = content.substring(index + teammateName.length());

            modified = Text.empty();

            if (!before.isEmpty()) {
                modified.append(Text.literal(before).setStyle(text.getStyle()));
            }

            int currentHex = ArmTeamMateClient.config.teamColor;
            TextColor dynamicColor = TextColor.fromRgb(currentHex);
            modified.append(Text.literal(actualTeammateName).setStyle(text.getStyle().withColor(dynamicColor).withBold(true)));

            if (!after.isEmpty()) {
                modified.append(Text.literal(after).setStyle(text.getStyle()));
            }
        } else {
            modified = text.copyContentOnly().setStyle(text.getStyle());
        }

        for (Text sibling : text.getSiblings()) {
            modified.append(arm$applyTeammateColor(sibling, teammateName));
        }

        return modified;
    }
}