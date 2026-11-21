package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.client.particle.ParticleSoulFlame;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockSoulTorch extends BlockTorch {

    public BlockSoulTorch() {
        super();
        this.setHardness(0.0F);
        this.setLightLevel(10.0F / 15.0F);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        EnumFacing facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        if (facing.getAxis().isHorizontal()) {
            EnumFacing opposite = facing.getOpposite();
            x += 0.27D * opposite.getXOffset();
            y += 0.22D;
            z += 0.27D * opposite.getZOffset();
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D);
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSoulFlame(world, x, y, z, 0.0D, 0.0D, 0.0D));
    }
}
