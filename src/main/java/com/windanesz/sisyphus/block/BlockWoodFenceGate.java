package com.windanesz.sisyphus.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWoodFenceGate extends BlockFenceGate {

    private final WoodSounds sounds;

    public BlockWoodFenceGate(WoodSounds sounds) {
        super(BlockPlanks.EnumType.OAK);
        this.sounds = sounds;
        this.setHardness(2.0F);
        this.setResistance(3.0F);
        this.setSoundType(sounds.type);
    }

    private void click(World world, BlockPos pos, boolean open, @Nullable EntityPlayer player) {
        world.playSound(player, pos, open ? this.sounds.gateOpen : this.sounds.gateClose, SoundCategory.BLOCKS, 1.0F,
                world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        if (state.getValue(OPEN)) {
            state = state.withProperty(OPEN, false);
        } else {
            EnumFacing toward = EnumFacing.fromAngle(player.rotationYaw);
            if (state.getValue(FACING) == toward.getOpposite()) {
                state = state.withProperty(FACING, toward);
            }
            state = state.withProperty(OPEN, true);
        }
        world.setBlockState(pos, state, 10);
        this.click(world, pos, state.getValue(OPEN), player);
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        boolean powered = world.isBlockPowered(pos);
        if (!powered && !block.getDefaultState().canProvidePower()) {
            return;
        }
        if (powered && !state.getValue(OPEN) && !state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(OPEN, true).withProperty(POWERED, true), 2);
            this.click(world, pos, true, null);
        } else if (!powered && state.getValue(OPEN) && state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(OPEN, false).withProperty(POWERED, false), 2);
            this.click(world, pos, false, null);
        } else if (powered != state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(POWERED, powered), 2);
        }
    }
}
