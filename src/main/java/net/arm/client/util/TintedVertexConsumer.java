package net.arm.client.util;

import net.minecraft.client.render.VertexConsumer;

public class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public TintedVertexConsumer(VertexConsumer delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int newR = (int) (r * this.r);
        int newG = (int) (g * this.g);
        int newB = (int) (b * this.b);
        int newA = (int) (a * this.a);
        delegate.color(newR, newG, newB, newA);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }
}