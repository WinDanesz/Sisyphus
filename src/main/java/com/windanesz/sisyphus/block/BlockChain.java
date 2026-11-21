package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockChain extends BlockRotatedPillar {

    private static final AxisAlignedBB X = new AxisAlignedBB(0.0D, 6.5D / 16.0D, 6.5D / 16.0D, 1.0D, 9.5D / 16.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB Y = new AxisAlignedBB(6.5D / 16.0D, 0.0D, 6.5D / 16.0D, 9.5D / 16.0D, 1.0D, 9.5D / 16.0D);
    private static final AxisAlignedBB Z = new AxisAlignedBB(6.5D / 16.0D, 6.5D / 16.0D, 0.0D, 9.5D / 16.0D, 9.5D / 16.0D, 1.0D);

    public BlockChain() {
        super(Material.IRON, MapColor.IRON);
        // Standing, as the chain is placed by default: the first axis of the block state is x, which lies on its side
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
        this.setHardness(5.0F);
        this.setResistance(6.0F);
        this.setSoundType(ModSounds.type("chain"));
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AXIS)) {
            case X:
                return X;
            case Z:
                return Z;
            default:
                return Y;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
