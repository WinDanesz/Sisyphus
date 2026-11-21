package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.client.particle.ParticleObsidianTear;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockCryingObsidian extends Block {

    public BlockCryingObsidian() {
        super(Material.ROCK, MapColor.BLACK);
        this.setHardness(50.0F);
        this.setResistance(1200.0F);
        this.setLightLevel(10.0F / 15.0F);
        this.setHarvestLevel("pickaxe", 3);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return !(entity instanceof EntityDragon) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(5) != 0) {
            return;
        }
        EnumFacing side = EnumFacing.values()[rand.nextInt(6)];
        if (side == EnumFacing.UP) {
            return;
        }
        BlockPos next = pos.offset(side);
        IBlockState neighbor = world.getBlockState(next);
        if (neighbor.isSideSolid(world, next, side.getOpposite())) {
            return;
        }
        double x = pos.getX() + (side.getXOffset() == 0 ? rand.nextDouble() : 0.5D + side.getXOffset() * 0.6D);
        double y = pos.getY() + (side.getYOffset() == 0 ? rand.nextDouble() : 0.5D + side.getYOffset() * 0.6D);
        double z = pos.getZ() + (side.getZOffset() == 0 ? rand.nextDouble() : 0.5D + side.getZOffset() * 0.6D);
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleObsidianTear(world, x, y, z));
    }
}
