package com.windanesz.sisyphus.worldgen;

/**
 * Two noises, one large and one smaller, stretched so that the caves are wider than high. A place is open where they add up to more than a
 * bar.
 */
final class CavernShape {

    private static final int TAPER = 7;

    private static Quantiles quantiles;

    private final CavernNoise large;
    private final CavernNoise small;

    CavernShape(long seed) {
        this.large = new CavernNoise(seed ^ 0x6C75736843617665L);
        this.small = new CavernNoise(seed * 31L + 0x2545F491L);
    }

    static double baseBar(double openness) {
        if (quantiles == null) {
            CavernShape shape = new CavernShape(7L);
            double[] samples = new double[60 * 60 * 30];
            int i = 0;
            for (int x = 0; x < 60; x++) {
                for (int z = 0; z < 60; z++) {
                    for (int y = 0; y < 30; y++) {
                        samples[i++] = shape.density(x * 7, y * 5, z * 7, 1.0D);
                    }
                }
            }
            quantiles = new Quantiles(samples);
        }
        return quantiles.top(openness);
    }

    static double bar(double base, int y, int bottom, int top) {
        double bar = base;
        if (y < bottom + TAPER) {
            double t = (bottom + TAPER - y) / (double) TAPER;
            bar += 0.7D * t * t;
        }
        if (y > top - TAPER) {
            double t = (y - (top - TAPER)) / (double) TAPER;
            bar += 0.7D * t * t;
        }
        return bar;
    }

    double density(int x, int y, int z, double size) {
        double sx = x / size;
        double sy = y / size;
        double sz = z / size;
        return this.large.get(sx / 34.0D, sy / 17.0D, sz / 34.0D) + 0.5D * this.small.get(sx / 13.0D, sy / 8.0D, sz / 13.0D);
    }
}
