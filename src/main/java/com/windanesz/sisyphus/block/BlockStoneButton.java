package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockButtonStone;
import net.minecraft.block.SoundType;

public class BlockStoneButton extends BlockButtonStone {

    public BlockStoneButton() {
        super();
        this.setHardness(0.5F);
        this.setSoundType(SoundType.STONE);
    }
}
