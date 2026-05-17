package net.arm.mixin;

import net.arm.client.ArmTeamMateClient;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorColorMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void substituteAndRender(MatrixStack matrices, OrderedRenderCommandQueue queue, ItemStack stack, EquipmentSlot slot, int light, BipedEntityRenderState state, CallbackInfo ci) {

        if (ArmTeamMateClient.config.armorReplace && state.displayName != null &&
                ArmTeamMateClient.config.teammates.contains(state.displayName.getString())) {
            if (stack.isEmpty()) return;

            ItemStack leatherStack = switch (slot) {
                case HEAD -> new ItemStack(Items.LEATHER_HELMET);
                case CHEST -> new ItemStack(Items.LEATHER_CHESTPLATE);
                case LEGS -> new ItemStack(Items.LEATHER_LEGGINGS);
                case FEET -> new ItemStack(Items.LEATHER_BOOTS);
                default -> null;
            };

            if (leatherStack != null) {
                int greenColor = 0xFF00FF00;
                leatherStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(greenColor));

                if (stack.hasEnchantments()) {
                    leatherStack.set(DataComponentTypes.ENCHANTMENTS, stack.getEnchantments());
                }

            }
        }
    }

    // САМЫЙ РАБОЧИЙ ВАРИАНТ ДЛЯ 1.21.1:
    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "renderArmor",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ItemStack swapStack(ItemStack originalStack, MatrixStack matrices, OrderedRenderCommandQueue queue, ItemStack stack, EquipmentSlot slot, int light, BipedEntityRenderState state) {
        if (ArmTeamMateClient.config.armorReplace && state.displayName != null &&
                ArmTeamMateClient.config.teammates.contains(state.displayName.getString()) && !originalStack.isEmpty()) {

            ItemStack leatherStack = switch (slot) {
                case HEAD -> new ItemStack(Items.LEATHER_HELMET);
                case CHEST -> new ItemStack(Items.LEATHER_CHESTPLATE);
                case LEGS -> new ItemStack(Items.LEATHER_LEGGINGS);
                case FEET -> new ItemStack(Items.LEATHER_BOOTS);
                default -> originalStack;
            };

            if (leatherStack != originalStack) {
                leatherStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFF00FF00));

                if (originalStack.hasEnchantments()) {
                    leatherStack.set(DataComponentTypes.ENCHANTMENTS, originalStack.getEnchantments());
                }
                return leatherStack;
            }
        }
        return originalStack;
    }
}