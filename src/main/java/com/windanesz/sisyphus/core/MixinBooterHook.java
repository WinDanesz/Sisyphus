package com.windanesz.sisyphus.core;

import org.spongepowered.asm.mixin.Mixins;

final class MixinBooterHook {

    private MixinBooterHook() {
    }

    static void queue(String config) {
        Mixins.addConfiguration(config);
    }
}
