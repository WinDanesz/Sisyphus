package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class BlockCherryLeaves extends BlockLeaves {

    public static final int REACH = 6;

    public BlockCherryLeaves() {
        super();
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setSoundType(ModSounds.type("cherry_leaves"));
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CHECK_DECAY, true).withProperty(DECAYABLE, true));
        this.leavesFancy = true;
    }

    static boolean isNearLog(World world, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        ArrayDeque<Integer> steps = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        steps.add(0);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int step = steps.poll();
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos next = pos.offset(side);
                if (!seen.add(next) || !world.isBlockLoaded(next)) {
                    continue;
                }
                IBlockState state = world.getBlockState(next);
                Block block = state.getBlock();
                if (block.canSustainLeaves(state, world, next)) {
                    return true;
                }
                if (step + 1 < REACH && block.isLeaves(state, world, next)) {
                    queue.add(next);
                    steps.add(step + 1);
                }
            }
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DECAYABLE, (meta & 4) == 0).withProperty(CHECK_DECAY, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(DECAYABLE) ? 0 : 4) | (state.getValue(CHECK_DECAY) ? 8 : 0);
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote || !state.getValue(CHECK_DECAY) || !state.getValue(DECAYABLE)) {
            return;
        }
        if (isNearLog(world, pos)) {
            world.setBlockState(pos, state.withProperty(CHECK_DECAY, false), 4);
        } else {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.get("cherry_sapling"));
    }

    @Override
    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        if (rand.nextInt(Math.max(10, 50 - fortune * 10)) == 0) {
            drops.add(new ItemStack(Items.STICK, 1 + rand.nextInt(2)));
        }
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !this.leavesFancy;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return this.leavesFancy ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        if (rand.nextInt(10) != 0) {
            return;
        }
        BlockPos below = pos.down();
        IBlockState under = world.getBlockState(below);
        if (under.getBlockFaceShape(world, below, EnumFacing.UP) != BlockFaceShape.SOLID) {
            SisyphusBackport.proxy.spawnCherryPetal(world, pos.getX() + rand.nextDouble(), pos.getY() - 0.05D, pos.getZ() + rand.nextDouble());
        }
    }
}
