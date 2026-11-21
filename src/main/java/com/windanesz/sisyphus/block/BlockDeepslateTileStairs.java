package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

public class BlockDeepslateTileStairs extends BlockStairs {

    public BlockDeepslateTileStairs(IBlockState modelState) {
        this(modelState, false);
    }

    public BlockDeepslateTileStairs(IBlockState modelState, boolean isBricks) {
        this(modelState, isBricks ? ModSounds.type("deepslate_bricks") : ModSounds.type("deepslate"));
    }

    public BlockDeepslateTileStairs(IBlockState modelState, SoundType soundType) {
        super(modelState);
        this.setSoundType(soundType);
    }
}
