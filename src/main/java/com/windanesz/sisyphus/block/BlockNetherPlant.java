package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockNetherPlant extends BlockBush {

    private final AxisAlignedBB shape;
    private final boolean offset;

    public BlockNetherPlant(AxisAlignedBB shape, boolean offset, SoundType sound) {
        super(Material.PLANTS);
        this.shape = shape;
        this.offset = offset;
        this.setSoundType(sound);
        this.setHardness(0.0F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public static boolean canGrowOn(IBlockState soil) {
        Block block = soil.getBlock();
        return block instanceof BlockNylium || block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND || block == Blocks.MYCELIUM
                || ModBlocks.isSoulSoil(block);
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return canGrowOn(state);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canGrowOn(world.getBlockState(pos.down()));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.shape;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return this.offset ? EnumOffsetType.XZ : EnumOffsetType.NONE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
