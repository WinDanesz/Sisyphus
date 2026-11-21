package com.windanesz.sisyphus.client;

import java.awt.image.BufferedImage;

/**
 * Sign textures for the woods of other mods, recoloured from the oak sign by their planks.
 */
public final class WoodTextures {

    private static final double MIN_SPREAD = 0.5D;
    private static final double MAX_SPREAD = 2.0D;

    private WoodTextures() {
    }

    static double[][] colours(BufferedImage image) {
        double[] sum = new double[3];
        double[] squares = new double[3];
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                count++;
                for (int c = 0; c < 3; c++) {
                    int value = (argb >> (16 - 8 * c)) & 255;
                    sum[c] += value;
                    squares[c] += value * value;
                }
            }
        }
        double[] mean = new double[3];
        double[] spread = new double[3];
        for (int c = 0; c < 3 && count > 0; c++) {
            mean[c] = sum[c] / count;
            spread[c] = Math.sqrt(Math.max(0.0D, squares[c] / count - mean[c] * mean[c]));
        }
        return new double[][]{mean, spread};
    }

    /**
     * Moves each colour from one wood's mean and spread to the other's. Transparent pixels stay as they are.
     */
    public static BufferedImage recolor(BufferedImage picture, BufferedImage from, BufferedImage to) {
        double[][] old = colours(from);
        double[][] wanted = colours(to);
        double[] scale = new double[3];
        for (int c = 0; c < 3; c++) {
            scale[c] = old[1][c] < 0.5D ? 1.0D : Math.max(MIN_SPREAD, Math.min(MAX_SPREAD, wanted[1][c] / old[1][c]));
        }
        BufferedImage out = new BufferedImage(picture.getWidth(), picture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < picture.getHeight(); y++) {
            for (int x = 0; x < picture.getWidth(); x++) {
                int argb = picture.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    out.setRGB(x, y, argb);
                    continue;
                }
                int result = argb & 0xFF000000;
                for (int c = 0; c < 3; c++) {
                    int shift = 16 - 8 * c;
                    double value = (((argb >> shift) & 255) - old[0][c]) * scale[c] + wanted[0][c];
                    result |= Math.max(0, Math.min(255, (int) Math.round(value))) << shift;
                }
                out.setRGB(x, y, result);
            }
        }
        return out;
    }
}
