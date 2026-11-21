package com.windanesz.sisyphus.registry;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.potion.PotionSlowFalling;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreIngredient;

@Mod.EventBusSubscriber
public final class ModPotions {

    public static final Potion SLOW_FALLING = new PotionSlowFalling();
    private static final int DURATION = 1800;
    // The long type shares the name of the normal one, which is what the potion names in the language file are based on
    public static final PotionType SLOW_FALLING_TYPE = new PotionType("slow_falling", new PotionEffect(SLOW_FALLING, DURATION))
            .setRegistryName(SisyphusBackport.MODID, "slow_falling");
    private static final int LONG_DURATION = 4800;
    public static final PotionType LONG_SLOW_FALLING_TYPE = new PotionType("slow_falling", new PotionEffect(SLOW_FALLING, LONG_DURATION))
            .setRegistryName(SisyphusBackport.MODID, "long_slow_falling");

    private ModPotions() {
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        if (Settings.items.enableSlowFalling) {
            event.getRegistry().register(SLOW_FALLING);
        }
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        if (Settings.items.enableSlowFalling) {
            event.getRegistry().registerAll(SLOW_FALLING_TYPE, LONG_SLOW_FALLING_TYPE);
        }
    }

    /**
     * Phantoms are not in this mod, so the ingredient is the {@code membranePhantom} ore dictionary entry.
     */
    public static void registerBrewing() {
        if (!Settings.items.enableSlowFalling) {
            return;
        }
        // A vanilla mix rather than a Forge brewing recipe: Forge's recipes only compare the bottle item, so any potion would
        // turn into Slow Falling, and splash and lingering awkward potions would not
        PotionHelper.addMix(PotionTypes.AWKWARD, new OreIngredient("membranePhantom"), SLOW_FALLING_TYPE);
        PotionHelper.addMix(SLOW_FALLING_TYPE, Items.REDSTONE, LONG_SLOW_FALLING_TYPE);
    }
}
