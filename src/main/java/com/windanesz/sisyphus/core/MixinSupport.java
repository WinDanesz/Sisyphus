package com.windanesz.sisyphus.core;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MixinSupport {

    public static final String CONFIG = "mixins.sisyphus.json";

    private static final Logger LOGGER = LogManager.getLogger("SisyphusBackport Core");

    private MixinSupport() {
    }

    public static Loader choose(boolean mixinBooter, boolean fermiumBooter) {
        if (mixinBooter) {
            return Loader.MIXINBOOTER;
        }
        return fermiumBooter ? Loader.FERMIUMBOOTER : Loader.NONE;
    }

    private static boolean isPresent(String resource) {
        return Launch.classLoader != null && Launch.classLoader.getResource(resource) != null;
    }

    public static Loader detect() {
        boolean mixinBooter = isPresent("zone/rong/mixinbooter/MixinBooterPlugin.class") || isPresent("zone/rong/mixinbooter/IEarlyMixinLoader.class");
        return choose(mixinBooter, isPresent("fermiumbooter/FermiumRegistryAPI.class"));
    }

    static void queueEarly() {
        if (detect() == Loader.FERMIUMBOOTER) {
            try {
                FermiumBooterHook.queue(CONFIG);
                LOGGER.info("Queued {} with FermiumBooter.", CONFIG);
            } catch (Throwable t) {
                LOGGER.error("Could not queue the mixins with FermiumBooter, what needs them is left as it is.", t);
            }
        }
    }

    static void queueWhenDataIsInjected() {
        Loader loader = detect();
        if (loader == Loader.MIXINBOOTER) {
            try {
                MixinBooterHook.queue(CONFIG);
                LOGGER.info("Queued {} with MixinBooter.", CONFIG);
            } catch (Throwable t) {
                LOGGER.error("Could not queue the mixins with MixinBooter, what needs them is left as it is.", t);
            }
        } else if (loader == Loader.NONE) {
            LOGGER.info("Neither MixinBooter nor FermiumBooter is installed. What needs a mixin is left as it is.");
        }
    }

    public enum Loader {
        NONE,
        MIXINBOOTER,
        FERMIUMBOOTER
    }
}
