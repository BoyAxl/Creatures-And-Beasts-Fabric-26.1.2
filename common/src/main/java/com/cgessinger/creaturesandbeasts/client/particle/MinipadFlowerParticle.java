package com.cgessinger.creaturesandbeasts.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class MinipadFlowerParticle extends SimpleAnimatedParticle {
    public MinipadFlowerParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.0F);
        this.xd = motionX;
        this.yd = motionY * 0.2F;
        this.zd = motionZ;
        this.quadSize *= 0.5F;
        this.lifetime = 10;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float tick) {
        int i = super.getLightCoords(tick);
        int k = i >> 16 & 255;
        return 240 | k << 16;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel levelIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new MinipadFlowerParticle(levelIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
