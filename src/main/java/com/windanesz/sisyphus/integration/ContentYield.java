package com.windanesz.sisyphus.integration;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraftforge.fml.common.Loader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Gives way to another mod that has some of the same content, so that there are not two of each.
 */
public final class ContentYield {

    private final String modId;
    private final String modName;
    private final BooleanSupplier wanted;
    private final Map<String, BooleanSupplier> getters = new LinkedHashMap<>();
    private final Map<String, Consumer<Boolean>> setters = new LinkedHashMap<>();

    private Map<String, Boolean> saved = null;

    public ContentYield(String modId, String modName, BooleanSupplier wanted) {
        this.modId = modId;
        this.modName = modName;
        this.wanted = wanted;
    }

    public ContentYield with(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.getters.put(name, getter);
        this.setters.put(name, setter);
        return this;
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(this.getters.keySet());
    }

    public boolean isInstalled() {
        return Loader.isModLoaded(this.modId);
    }

    public boolean isYielding() {
        return this.saved != null;
    }

    /**
     * For tests.
     */
    public boolean isOn(String name) {
        return this.getters.get(name).getAsBoolean();
    }

    /**
     * Returns the old values, for {@link #restore}.
     */
    public Map<String, Boolean> turnOff() {
        Map<String, Boolean> before = new LinkedHashMap<>();
        for (Map.Entry<String, BooleanSupplier> option : this.getters.entrySet()) {
            before.put(option.getKey(), option.getValue().getAsBoolean());
            this.setters.get(option.getKey()).accept(false);
        }
        return before;
    }

    public void restore(Map<String, Boolean> before) {
        for (Map.Entry<String, Boolean> option : before.entrySet()) {
            this.setters.get(option.getKey()).accept(option.getValue());
        }
    }

    /**
     * Called before the blocks are registered.
     */
    public void apply() {
        this.apply(this.isInstalled());
    }

    void apply(boolean installed) {
        if (this.saved == null && installed && this.wanted.getAsBoolean()) {
            this.saved = this.turnOff();
            if (SisyphusBackport.logger != null) {
                SisyphusBackport.logger.info("{} is installed, so its version of these is used and not ours: {}", this.modName, this.names());
            }
        }
    }

    /**
     * Called before the config is read or saved again.
     */
    public void undo() {
        if (this.saved != null) {
            this.restore(this.saved);
            this.saved = null;
        }
    }
}
