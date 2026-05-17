package net.arm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arm.client.util.BoxRenderUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class ArmClientHitboxes implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            
            if (ArmTeamMateClient.config == null || !ArmTeamMateClient.config.highlightGlow) return;

            Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
            var cameraPos = context.camera().getPos();

            
            float tickDelta = context.tickCounter().getTickDelta(true);

            setupRenderState();

            
            for (Entity entity : client.world.getEntities()) {
                if (!(entity instanceof PlayerEntity player)) continue;

                
                if (player == client.cameraEntity && client.options.getPerspective().isFirstPerson()) continue;

                String playerName = player.getName().getString();

                
                if (ArmTeamMateClient.config.teammates.contains(playerName)) {
                    
                    Box visualBox = BoxRenderUtils.getInterpolatedBox(player, tickDelta, cameraPos, 0.0);
                    int color = ArmTeamMateClient.config.teamColor;
                    float r = ((color >> 16) & 0xFF) / 255.0f;
                    float g = ((color >> 8) & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;

                    renderTeammateBox(matrix, visualBox, r, g, b);
                }
            }

            resetRenderState();
        });
    }

    private void renderTeammateBox(Matrix4f matrix, Box visualBox, float r, float g, float b) {
        Tessellator tessellator = Tessellator.getInstance();

        
        RenderSystem.depthMask(false);
        BufferBuilder fillBuf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        BoxRenderUtils.drawFilledBox(fillBuf, matrix, visualBox, r, g, b, 0.15f); 
        BufferRenderer.drawWithGlobalProgram(fillBuf.end());

        
        RenderSystem.depthMask(true);
        BufferBuilder lineBuf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        BoxRenderUtils.drawThickOutline(lineBuf, matrix, visualBox, 0.005f, r, g, b, 1.0f); 
        BufferRenderer.drawWithGlobalProgram(lineBuf.end());
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
    }

    private void resetRenderState() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}