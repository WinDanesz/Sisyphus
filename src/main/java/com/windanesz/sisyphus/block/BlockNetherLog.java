package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;

public class BlockNetherLog extends BlockWoodLog {

    public BlockNetherLog() {
        super();
        this.setSoundType(ModSounds.type("stem"));
    }
}
