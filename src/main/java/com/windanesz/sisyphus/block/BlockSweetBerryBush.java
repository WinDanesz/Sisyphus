package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockSweetBerryBush extends BlockBush implements IGrowable {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    public static final DamageSource DAMAGE = new DamageSource("sweetBerryBush");

    private static final AxisAlignedBB SAPLING_BOX = new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.5D, 0.8125D);
    private static final AxisAlignedBB GROWN_BOX = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    public BlockSweetBerryBush() {
        super(Material.PLANTS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, Math.min(meta, 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(AGE) == 0 ? SAPLING_BOX : GROWN_BOX;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        int age = state.getValue(AGE);
        if (age < 3 && world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(5) == 0) {
            world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        // Slows down everything that is inside the bush
        entity.motionX *= 0.8D;
        entity.motionY *= 0.75D;
        entity.motionZ *= 0.8D;
        entity.fallDistance = 0.0F;

        if (!world.isRemote && state.getValue(AGE) > 0 && (entity.lastTickPosX != entity.posX || entity.lastTickPosZ != entity.posZ)) {
            double dx = Math.abs(entity.posX - entity.lastTickPosX);
            double dz = Math.abs(entity.posZ - entity.lastTickPosZ);
            if (dx >= 0.003D || dz >= 0.003D) {
                entity.attackEntityFrom(DAMAGE, 1.0F);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        int age = state.getValue(AGE);
        boolean bonemeal = player.getHeldItem(hand).getItem() == Items.DYE
                && player.getHeldItem(hand).getMetadata() == 15;
        if (age <= 1 || (bonemeal && age < 3)) {
            return false;
        }

        if (!world.isRemote) {
            int count = 1 + world.rand.nextInt(2) + (age == 3 ? 1 : 0);
            spawnAsEntity(world, pos, new ItemStack(ModItems.get("sweet_berries"), count));
            world.playSound(null, pos, ModSounds.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
            world.setBlockState(pos, state.withProperty(AGE, 1), 2);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int age = state.getValue(AGE);
        if (age > 1) {
            Random rand = world instanceof World ? ((World) world).rand : RANDOM;
            drops.add(new ItemStack(ModItems.get("sweet_berries"), 1 + rand.nextInt(2) + (age == 3 ? 1 : 0)));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(ModItems.get("sweet_berries"));
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(AGE, Math.min(3, state.getValue(AGE) + 1)), 2);
    }
}
