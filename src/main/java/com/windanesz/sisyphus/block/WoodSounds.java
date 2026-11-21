package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.SoundType;
import net.minecraft.util.SoundEvent;

public final class WoodSounds {

    public final SoundType type;
    final SoundEvent doorOpen;
    final SoundEvent doorClose;
    final SoundEvent trapdoorOpen;
    final SoundEvent trapdoorClose;
    final SoundEvent gateOpen;
    final SoundEvent gateClose;
    final SoundEvent buttonOn;
    final SoundEvent buttonOff;
    final SoundEvent plateOn;
    final SoundEvent plateOff;

    private WoodSounds(SoundType type, SoundEvent doorOpen, SoundEvent doorClose, SoundEvent trapdoorOpen, SoundEvent trapdoorClose,
                       SoundEvent gateOpen, SoundEvent gateClose, SoundEvent buttonOn, SoundEvent buttonOff, SoundEvent plateOn, SoundEvent plateOff) {
        this.type = type;
        this.doorOpen = doorOpen;
        this.doorClose = doorClose;
        this.trapdoorOpen = trapdoorOpen;
        this.trapdoorClose = trapdoorClose;
        this.gateOpen = gateOpen;
        this.gateClose = gateClose;
        this.buttonOn = buttonOn;
        this.buttonOff = buttonOff;
        this.plateOn = plateOn;
        this.plateOff = plateOff;
    }

    public static WoodSounds nether() {
        return new WoodSounds(ModSounds.type("nether_wood"), ModSounds.BLOCK_NETHER_WOOD_DOOR_OPEN, ModSounds.BLOCK_NETHER_WOOD_DOOR_CLOSE,
                ModSounds.BLOCK_NETHER_WOOD_TRAPDOOR_OPEN, ModSounds.BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE, ModSounds.BLOCK_NETHER_WOOD_FENCE_GATE_OPEN,
                ModSounds.BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE, ModSounds.BLOCK_NETHER_WOOD_BUTTON_CLICK_ON, ModSounds.BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF,
                ModSounds.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_ON, ModSounds.BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF);
    }

    public static WoodSounds cherry() {
        return new WoodSounds(ModSounds.type("cherry_wood"), ModSounds.event("block.cherry_wood_door.open"),
                ModSounds.event("block.cherry_wood_door.close"), ModSounds.event("block.cherry_wood_trapdoor.open"),
                ModSounds.event("block.cherry_wood_trapdoor.close"), ModSounds.event("block.cherry_wood_fence_gate.open"),
                ModSounds.event("block.cherry_wood_fence_gate.close"), ModSounds.event("block.cherry_wood_button.click_on"),
                ModSounds.event("block.cherry_wood_button.click_off"), ModSounds.event("block.cherry_wood_pressure_plate.click_on"),
                ModSounds.event("block.cherry_wood_pressure_plate.click_off"));
    }

    public static WoodSounds bamboo() {
        return new WoodSounds(ModSounds.type("bamboo_wood"), ModSounds.event("block.bamboo_wood_door.open"),
                ModSounds.event("block.bamboo_wood_door.close"), ModSounds.event("block.bamboo_wood_trapdoor.open"),
                ModSounds.event("block.bamboo_wood_trapdoor.close"), ModSounds.event("block.bamboo_wood_fence_gate.open"),
                ModSounds.event("block.bamboo_wood_fence_gate.close"), ModSounds.event("block.bamboo_wood_button.click_on"),
                ModSounds.event("block.bamboo_wood_button.click_off"), ModSounds.event("block.bamboo_wood_pressure_plate.click_on"),
                ModSounds.event("block.bamboo_wood_pressure_plate.click_off"));
    }
}
