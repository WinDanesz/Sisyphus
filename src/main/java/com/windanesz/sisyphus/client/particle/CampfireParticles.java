package com.windanesz.sisyphus.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public final class CampfireParticles {

    private CampfireParticles() {
    }

    public static boolean isSignalFire(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() == Blocks.HAY_BLOCK;
    }

    public static void makeSmoke(World world, BlockPos pos) {
        Random rand = world.rand;
        double x = pos.getX() + 0.5D + rand.nextDouble() / 3.0D * (rand.nextBoolean() ? 1 : -1);
        double y = pos.getY() + rand.nextDouble() + rand.nextDouble();
        double z = pos.getZ() + 0.5D + rand.nextDouble() / 3.0D * (rand.nextBoolean() ? 1 : -1);
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCampfireSmoke(world, x, y, z, 0.0D, 0.07D, 0.0D));
    }
}
