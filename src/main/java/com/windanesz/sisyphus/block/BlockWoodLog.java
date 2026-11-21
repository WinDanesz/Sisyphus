package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockWoodLog extends BlockLog {

    public BlockWoodLog() {
        super();
        this.setDefaultState(this.blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    public BlockWoodLog(SoundType sound) {
        this();
        this.setSoundType(sound);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        switch (meta & 3) {
            case 0:
                state = state.withProperty(LOG_AXIS, EnumAxis.Y);
                break;
            case 1:
                state = state.withProperty(LOG_AXIS, EnumAxis.X);
                break;
            case 2:
                state = state.withProperty(LOG_AXIS, EnumAxis.Z);
                break;
            default:
                state = state.withProperty(LOG_AXIS, EnumAxis.NONE);
                break;
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return 1;
            case Z:
                return 2;
            case NONE:
                return 3;
            default:
                return 0;
        }
    }
}
