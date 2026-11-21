package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLantern extends Block {

    public static final PropertyBool HANGING = PropertyBool.create("hanging");

    public static final SoundType LANTERN = new SoundType(1.0F, 1.0F, ModSounds.BLOCK_LANTERN_BREAK, ModSounds.BLOCK_LANTERN_STEP,
            ModSounds.BLOCK_LANTERN_PLACE, ModSounds.BLOCK_LANTERN_HIT, ModSounds.BLOCK_LANTERN_FALL);

    private static final AxisAlignedBB STANDING_BOX = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.5625D, 0.6875D);
    private static final AxisAlignedBB HANGING_BOX = new AxisAlignedBB(0.3125D, 0.0625D, 0.3125D, 0.6875D, 0.625D, 0.6875D);

    public BlockLantern() {
        this(1.0F);
    }

    public BlockLantern(float light) {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HANGING, false));
        this.setHardness(3.5F);
        this.setSoundType(LANTERN);
        this.setLightLevel(light);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    private static boolean canStandOn(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    private static boolean canHangFrom(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), EnumFacing.DOWN);
    }

    private static boolean isSupported(IBlockAccess world, BlockPos pos, boolean hanging) {
        return hanging ? canHangFrom(world, pos) : canStandOn(world, pos);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HANGING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HANGING, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HANGING) ? 1 : 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? HANGING_BOX : STANDING_BOX;
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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && (canStandOn(world, pos) || canHangFrom(world, pos));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        // Clicking the underside of a block hangs the lantern, otherwise it stands if it can, and hangs if not
        boolean hanging = facing == EnumFacing.DOWN ? canHangFrom(world, pos) : !canStandOn(world, pos);
        return this.getDefaultState().withProperty(HANGING, hanging);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!isSupported(world, pos, state.getValue(HANGING))) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
