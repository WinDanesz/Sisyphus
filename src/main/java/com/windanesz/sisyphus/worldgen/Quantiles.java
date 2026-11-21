package com.windanesz.sisyphus.worldgen;

import java.util.Arrays;

final class Quantiles {

    private final double[] sorted;

    Quantiles(double[] samples) {
        this.sorted = samples.clone();
        Arrays.sort(this.sorted);
    }

    double top(double share) {
        if (share <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        if (share >= 1.0D) {
            return Double.NEGATIVE_INFINITY;
        }
        int i = (int) Math.round((1.0D - share) * (this.sorted.length - 1));
        return this.sorted[i];
    }
}
