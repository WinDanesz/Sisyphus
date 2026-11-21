package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.client.particle.BubbleColumnParticles;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Without waterlogging, the column is a water block of its own. Soul sand and magma blocks make and keep it through their mixins, and it
 * turns back into water when what holds it is gone.
 */
public class BlockBubbleColumn extends BlockLiquid {

    /**
     * True pulls down, over a magma block; false pushes up, over soul sand.
     */
    public static final PropertyBool DRAG = PropertyBool.create("drag");

    public static final int SOURCE_DELAY = 20;

    private static final int TICK_RATE = 5;

    /**
     * Set while this block places columns itself, so that they do not each check the column again.
     */
    private static boolean placing = false;

    @Nullable
    private static BlockBubbleColumn instance;

    public BlockBubbleColumn() {
        super(Material.WATER);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0).withProperty(DRAG, true));
        this.setHardness(100.0F);
        this.setLightOpacity(3);
        this.disableStats();
    }

    @Nullable
    public static BlockBubbleColumn get() {
        if (instance == null) {
            Block block = ModBlocks.get("bubble_column");
            instance = block instanceof BlockBubbleColumn ? (BlockBubbleColumn) block : null;
        }
        return instance;
    }

    public static boolean isEnabled() {
        return Settings.gameplay.enableBubbleColumns && get() != null;
    }

    /**
     * True pulls down, false pushes up, null makes no column.
     */
    @Nullable
    public static Boolean dragAbove(IBlockState below) {
        Block block = below.getBlock();
        if (block instanceof BlockBubbleColumn) {
            return below.getValue(DRAG);
        }
        if (block == Blocks.MAGMA) {
            return Boolean.TRUE;
        }
        if (block == Blocks.SOUL_SAND) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static boolean isWaterSource(IBlockState state) {
        return (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && state.getValue(LEVEL) == 0;
    }

    public static void refresh(World world, BlockPos start) {
        BlockBubbleColumn column = get();
        if (column == null || world.isRemote) {
            return;
        }
        Boolean drag = Settings.gameplay.enableBubbleColumns ? dragAbove(world.getBlockState(start.down())) : null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start);
        placing = true;
        try {
            while (pos.getY() < world.getHeight()) {
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() == column) {
                    if (drag == null) {
                        // A water source again, which flows where it can, as the column would have if it were water
                        world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 2);
                        world.scheduleUpdate(pos.toImmutable(), Blocks.FLOWING_WATER, Blocks.FLOWING_WATER.tickRate(world));
                    } else if (state.getValue(DRAG) != drag) {
                        world.setBlockState(pos, state.withProperty(DRAG, drag), 2);
                    }
                } else if (drag != null && isWaterSource(state)) {
                    world.setBlockState(pos, column.getDefaultState().withProperty(DRAG, drag), 2);
                } else {
                    break;
                }
                pos.move(EnumFacing.UP);
            }
        } finally {
            placing = false;
        }
    }

    private static boolean isBlocked(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block instanceof BlockDoor || block == Blocks.STANDING_SIGN || block == Blocks.WALL_SIGN || block == Blocks.LADDER || block == Blocks.REEDS) {
            return true;
        }
        return material == Material.PORTAL || material == Material.STRUCTURE_VOID || material.blocksMovement();
    }

    private static boolean isFlying(Entity entity) {
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying;
    }

    private static void onEnterWithAirAbove(Entity entity, boolean down) {
        if (isFlying(entity)) {
            return;
        }
        entity.motionY = down ? Math.max(-0.9D, entity.motionY - 0.03D) : Math.min(1.8D, entity.motionY + 0.1D);
    }

    private static void onEnter(Entity entity, boolean down) {
        if (isFlying(entity)) {
            return;
        }
        entity.motionY = down ? Math.max(-0.3D, entity.motionY - 0.03D) : Math.min(0.7D, entity.motionY + 0.06D);
        entity.fallDistance = 0.0F;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!placing) {
            // Placed by something else, such as a command: checks that it stands on what makes a column
            world.scheduleUpdate(pos, this, TICK_RATE);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        // Not the water of the game, which turns into flowing water here; only lava next to it does anything at once
        this.checkForMixing(world, pos, state);
        world.scheduleUpdate(pos, this, TICK_RATE);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        refresh(world, pos);
        if (world.getBlockState(pos).getBlock() == this) {
            this.spread(world, pos);
        }
    }

    @Override
    public int tickRate(World world) {
        return TICK_RATE;
    }

    private void spread(World world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            BlockPos next = pos.offset(side);
            IBlockState state = world.getBlockState(next);
            if (state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA || isBlocked(state)) {
                continue;
            }
            if (state.getMaterial() != Material.AIR && state.getBlock() != Blocks.SNOW_LAYER) {
                state.getBlock().dropBlockAsItem(world, next, state, 0);
            }
            world.setBlockState(next, Blocks.FLOWING_WATER.getDefaultState().withProperty(LEVEL, 1), 3);
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        boolean down = state.getValue(DRAG);
        if (world.isAirBlock(pos.up())) {
            // A boat on top rocks and is then thrown up or pulled under, which its mixin does
            if (!(entity instanceof EntityBoat)) {
                onEnterWithAirAbove(entity, down);
            }
            if (!world.isRemote) {
                WorldServer server = (WorldServer) world;
                for (int i = 0; i < 2; i++) {
                    server.spawnParticle(EnumParticleTypes.WATER_SPLASH, pos.getX() + world.rand.nextDouble(), pos.getY() + 1, pos.getZ() + world.rand.nextDouble(),
                            1, 0.0D, 0.0D, 0.0D, 1.0D);
                    server.spawnParticle(EnumParticleTypes.WATER_BUBBLE, pos.getX() + world.rand.nextDouble(), pos.getY() + 1, pos.getZ() + world.rand.nextDouble(),
                            1, 0.0D, 0.01D, 0.0D, 0.2D);
                }
            }
        } else {
            onEnter(entity, down);
        }
        if (entity instanceof EntityLivingBase && pos.equals(new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ))) {
            // The bubbles give back air, four a tick as in the game, after the one the water took this tick
            EntityLivingBase living = (EntityLivingBase) entity;
            living.setAir(Math.min(300, living.getAir() + 5));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (state.getValue(DRAG)) {
            BubbleColumnParticles.currentDown(world, x + 0.5D, y + 0.8D, z);
            if (rand.nextInt(200) == 0) {
                world.playSound(x, y, z, ModSounds.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F,
                        0.9F + rand.nextFloat() * 0.15F, false);
            }
        } else {
            BubbleColumnParticles.bubbleUp(world, x + 0.5D, y, z + 0.5D);
            BubbleColumnParticles.bubbleUp(world, x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat());
            if (rand.nextInt(200) == 0) {
                world.playSound(x, y, z, ModSounds.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F,
                        0.9F + rand.nextFloat() * 0.15F, false);
            }
            if (world.isAirBlock(pos.up())) {
                // The bubbles pop where the column comes out of the water
                BubbleColumnParticles.pop(world, x + rand.nextFloat(), y + 0.9D + rand.nextFloat() * 0.1D, z + rand.nextFloat());
                if (rand.nextInt(20) == 0) {
                    world.playSound(x + 0.5D, y + 1.0D, z + 0.5D, ModSounds.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS,
                            1.0F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
                }
            }
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(DRAG, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DRAG) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL, DRAG);
    }
}
