package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

    @ModifyVariable(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/equipment/EquipmentModel$Layer;usePlayerTexture()Z"),
            ordinal = 1
    )
    private int overrideArmorLayerColor(int originalColor) {
        if (ArmTeamMateClient.isTeammateRendering && ArmTeamMateClient.config != null && ArmTeamMateClient.config.armorReplace) {
            int teamColor = ArmTeamMateClient.config.teamColor;

            int r = (teamColor >> 16) & 0xFF;
            int g = (teamColor >> 8) & 0xFF;
            int b = teamColor & 0xFF;

            int alpha = 0xA0;

            return (alpha << 24) | (r << 16) | (g << 8) | b;
        }
        return originalColor;
    }
}