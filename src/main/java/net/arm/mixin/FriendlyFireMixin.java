package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class FriendlyFireMixin {

    @Shadow @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preventTeammateAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) this.crosshairTarget).getEntity();

            if (target instanceof PlayerEntity targetPlayer) {
                if (ArmTeamMateClient.config.pvpProtect && ArmTeamMateClient.config.teammates.contains(targetPlayer.getName().getString())) {
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }
}