package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.arm.client.util.TintedVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {

    @Unique
    private static boolean arm$isTeammateActive = false;
    @Unique
    private static int arm$currentTeamColor = 0xFFFFFF;

    @Inject(method = "render", at = @At("HEAD"))
    private void checkTeammateStatus(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
                                     BipedEntityRenderState bipedEntityRenderState, float f, float g, CallbackInfo ci) {
        arm$isTeammateActive = false;

        if (ArmTeamMateClient.config != null && ArmTeamMateClient.config.armorReplace
                && bipedEntityRenderState.displayName != null && !ArmTeamMateClient.config.teammates.isEmpty()) {

            String rawName = bipedEntityRenderState.displayName.getString();
            for (String teammate : ArmTeamMateClient.config.teammates) {
                if (rawName.contains(teammate)) {
                    arm$isTeammateActive = true;
                    arm$currentTeamColor = ArmTeamMateClient.config.teamColor;
                    break;
                }
            }
        }
    }

    @ModifyVariable(method = "renderArmor", at = @At("HEAD"), argsOnly = true)
    private VertexConsumerProvider wrapVertexConsumer(VertexConsumerProvider original) {
        if (!arm$isTeammateActive || original == null) {
            return original;
        }

        final float red = ((arm$currentTeamColor >> 16) & 0xFF) / 255.0f;
        final float green = ((arm$currentTeamColor >> 8) & 0xFF) / 255.0f;
        final float blue = (arm$currentTeamColor & 0xFF) / 255.0f;
        final float alpha = 0.55f;

        return new VertexConsumerProvider() {
            @Override
            public VertexConsumer getBuffer(RenderLayer layer) {
                VertexConsumer originalConsumer = original.getBuffer(layer);
                return new TintedVertexConsumer(originalConsumer, red, green, blue, alpha);
            }
        };
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void clearTeammateStatus(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
                                     BipedEntityRenderState bipedEntityRenderState, float f, float g, CallbackInfo ci) {
        arm$isTeammateActive = false;
    }
}