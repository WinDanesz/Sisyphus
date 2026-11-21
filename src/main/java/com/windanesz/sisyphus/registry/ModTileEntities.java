package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.tileentity.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModTileEntities {

    private ModTileEntities() {
    } // No instances!

    public static void register() {
        if (Settings.functionalBlocks.enableLectern) {
            register(TileEntityLectern.class, "lectern");
        }
        if (Settings.functionalBlocks.enableBell) {
            register(TileEntityBell.class, "bell");
        }
        if (Settings.functionalBlocks.enableCampfire) {
            register(TileEntityCampfire.class, "campfire");
        }
        if (Settings.functionalBlocks.enableBarrel) {
            register(TileEntityBarrel.class, "barrel");
        }
        if (Settings.functionalBlocks.enableWorkstations && (Settings.items.enableNetherite || Settings.items.enableArmorTrims)) {
            register(TileEntitySmithingTable.class, "smithing_table");
        }
        if (Settings.functionalBlocks.enableFurnaces) {
            register(TileEntityBlastFurnace.class, "blast_furnace");
            register(TileEntitySmoker.class, "smoker");
        }
        if (Settings.buildingBlocks.enableCandles) {
            register(TileEntityCandle.class, "candle");
        }
        if (Settings.plants.enableLushPlants) {
            register(TileEntityGlowLichen.class, "glow_lichen");
        }
        if (Settings.isContentEnabled("hanging_signs")) {
            register(TileEntityHangingSign.class, "hanging_sign");
        }
        if (Settings.archaeology.enableArchaeology) {
            register(TileEntityBrushable.class, "brushable_block");
            register(TileEntityDecoratedPot.class, "decorated_pot");
        }
    }

    private static void register(Class<? extends TileEntity> type, String name) {
        GameRegistry.registerTileEntity(type, new ResourceLocation(SisyphusBackport.MODID, name));
    }
}
