package com.windanesz.sisyphus.worldgen;

import java.util.Random;

/**
 * Between about -1 and 1.
 */
final class CavernNoise {

    private final int[] perm = new int[512];
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    CavernNoise(long seed) {
        Random random = new Random(seed);
        this.offsetX = random.nextDouble() * 256.0D;
        this.offsetY = random.nextDouble() * 256.0D;
        this.offsetZ = random.nextDouble() * 256.0D;
        int[] order = new int[256];
        for (int i = 0; i < 256; i++) {
            order[i] = i;
        }
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = order[i];
            order[i] = order[j];
            order[j] = swap;
        }
        for (int i = 0; i < 512; i++) {
            this.perm[i] = order[i & 255];
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    double get(double x, double y, double z) {
        x += this.offsetX;
        y += this.offsetY;
        z += this.offsetZ;
        int fx = (int) Math.floor(x);
        int fy = (int) Math.floor(y);
        int fz = (int) Math.floor(z);
        int X = fx & 255;
        int Y = fy & 255;
        int Z = fz & 255;
        x -= fx;
        y -= fy;
        z -= fz;
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        int[] p = this.perm;
        int a = p[X] + Y;
        int aa = p[a] + Z;
        int ab = p[a + 1] + Z;
        int b = p[X + 1] + Y;
        int ba = p[b] + Z;
        int bb = p[b + 1] + Z;
        return lerp(w,
                lerp(v, lerp(u, grad(p[aa], x, y, z), grad(p[ba], x - 1, y, z)),
                        lerp(u, grad(p[ab], x, y - 1, z), grad(p[bb], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(p[aa + 1], x, y, z - 1), grad(p[ba + 1], x - 1, y, z - 1)),
                        lerp(u, grad(p[ab + 1], x, y - 1, z - 1), grad(p[bb + 1], x - 1, y - 1, z - 1))));
    }
}
