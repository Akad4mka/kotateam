package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class GlowVisibilityMixin {

    @Redirect(
            method = "hasOutline",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isGlowing()Z")
    )
    private boolean toggleGlowBasedOnVisibility(Entity entity) {
        if (ArmTeamMateClient.config.highlightGlow && entity instanceof PlayerEntity player &&
                ArmTeamMateClient.config.teammates.contains(player.getName().getString())) {
            return true;
        }

        return entity.isGlowing();
    }
}