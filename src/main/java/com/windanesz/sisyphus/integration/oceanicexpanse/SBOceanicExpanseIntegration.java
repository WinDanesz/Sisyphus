package com.windanesz.sisyphus.integration.oceanicexpanse;

import com.windanesz.sisyphus.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public final class SBOceanicExpanseIntegration {

    public static final String OCEANIC_EXPANSE_MOD_ID = "oe";

    private static final ResourceLocation DROWNED = new ResourceLocation(OCEANIC_EXPANSE_MOD_ID, "drowned");

    private static boolean loaded;

    private SBOceanicExpanseIntegration() {
    } // no instances

    public static void init() {
        loaded = Loader.isModLoaded(OCEANIC_EXPANSE_MOD_ID);
    }

    public static boolean enabled() {
        return Settings.compatibility.oceanicExpanseIntegration && loaded;
    }

    public static void spawnDrowned(World world, BlockPos pos) {
        if (!enabled()) {
            return;
        }
        Entity entity = EntityList.createEntityByIDFromName(DROWNED, world);
        if (!(entity instanceof EntityLiving)) {
            return;
        }
        EntityLiving drowned = (EntityLiving) entity;
        drowned.enablePersistence();
        drowned.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.rand.nextFloat() * 360.0F, 0.0F);
        drowned.onInitialSpawn(world.getDifficultyForLocation(pos), null);
        world.spawnEntity(drowned);
    }
}
