package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
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
        if (ArmTeamMateClient.config == null || !ArmTeamMateClient.config.nameColor || state.displayName == null) {
            return;
        }

        String rawName = state.displayName.getString();

        if (ArmTeamMateClient.config.teammates.contains(rawName)) {
            int currentHex = ArmTeamMateClient.config.teamColor;

            state.displayName = Text.literal(rawName).setStyle(state.displayName.getStyle().withColor(TextColor.fromRgb(currentHex)).withBold(true));
        }
    }
}