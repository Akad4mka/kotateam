package net.arm.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
            at = @At("HEAD")
    )
    private void applyTeammateArmorTint(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
                                        BipedEntityRenderState bipedEntityRenderState, float f, float g, CallbackInfo ci) {

        if (ArmTeamMateClient.config != null && ArmTeamMateClient.config.armorReplace && bipedEntityRenderState.displayName != null) {
            String rawName = bipedEntityRenderState.displayName.getString();

            if (ArmTeamMateClient.config.teammates.contains(rawName)) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                int color = ArmTeamMateClient.config.teamColor;
                float r = ((color >> 16) & 0xFF) / 255.0f;
                float gg = ((color >> 8) & 0xFF) / 255.0f;
                float b = (color & 0xFF) / 255.0f;

                RenderSystem.setShaderColor(r, gg, b, 0.3f);
            }
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
            at = @At("RETURN")
    )
    private void resetArmorTint(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
                                BipedEntityRenderState bipedEntityRenderState, float f, float g, CallbackInfo ci) {

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}