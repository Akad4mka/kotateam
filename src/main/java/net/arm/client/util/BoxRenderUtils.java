package net.arm.client.util;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BoxRenderUtils {

    public static void drawThickOutline(VertexConsumer buffer, Matrix4f matrix, Box box, float t, float r, float g, float b, float a) {
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.minY-t, box.minZ-t, box.maxX+t, box.minY+t, box.minZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.minY-t, box.maxZ-t, box.maxX+t, box.minY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.minY-t, box.minZ-t, box.minX+t, box.minY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.maxX-t, box.minY-t, box.minZ-t, box.maxX+t, box.minY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.maxY-t, box.minZ-t, box.maxX+t, box.maxY+t, box.minZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.maxY-t, box.maxZ-t, box.maxX+t, box.maxY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.maxY-t, box.minZ-t, box.minX+t, box.maxY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.maxX-t, box.maxY-t, box.minZ-t, box.maxX+t, box.maxY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.minY-t, box.minZ-t, box.minX+t, box.maxY+t, box.minZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.maxX-t, box.minY-t, box.minZ-t, box.maxX+t, box.maxY+t, box.minZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.minX-t, box.minY-t, box.maxZ-t, box.minX+t, box.maxY+t, box.maxZ+t), r, g, b, a);
        drawFilledBox(buffer, matrix, new Box(box.maxX-t, box.minY-t, box.maxZ-t, box.maxX+t, box.maxY+t, box.maxZ+t), r, g, b, a);
    }

    public static void drawFilledBox(VertexConsumer buffer, Matrix4f matrix, Box box, float r, float g, float b, float a) {
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);

        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
    }

    public static Box getInterpolatedBox(Entity entity, float tickDelta, Vec3d cameraPos, double expansion) {
        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - cameraPos.x;
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - cameraPos.y;
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - cameraPos.z;

        Box baseBox = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        return baseBox.offset(x, y, z).expand(expansion);
    }
}