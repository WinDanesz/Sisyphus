package com.windanesz.sisyphus.core;

import fermiumbooter.FermiumRegistryAPI;

final class FermiumBooterHook {

    private FermiumBooterHook() {
    }

    static void queue(String config) {
        FermiumRegistryAPI.enqueueMixin(false, config);
    }
}
