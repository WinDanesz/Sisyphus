package com.windanesz.sisyphus.recipe;

import com.google.common.base.Optional;
import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class StrippingRecipes {

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<Block, Block> AUTO_CACHE = new HashMap<>();
    private static boolean defaultsRegistered = false;

    private StrippingRecipes() {
    }

    public static void register(Block log, Block stripped) {
        register(state -> state.getBlock() == log, stripped);
    }

    public static void register(Predicate<IBlockState> matcher, Block stripped) {
        ENTRIES.add(new Entry(matcher, stripped));
    }

    /**
     * Called once after all blocks have been registered.
     */
    public static void registerDefaults() {
        if (defaultsRegistered) {
            return;
        }
        defaultsRegistered = true;

        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            String name = type.getName();
            Block strippedLog = ModBlocks.getOrNull("stripped_" + name + "_log");
            Block wood = ModBlocks.getOrNull(name + "_wood");
            Block strippedWood = ModBlocks.getOrNull("stripped_" + name + "_wood");

            if (strippedLog != null) {
                if (type == BlockPlanks.EnumType.ACACIA || type == BlockPlanks.EnumType.DARK_OAK) {
                    register(state -> state.getBlock() == Blocks.LOG2 && state.getValue(BlockNewLog.VARIANT) == type, strippedLog);
                } else {
                    register(state -> state.getBlock() == Blocks.LOG && state.getValue(BlockOldLog.VARIANT) == type, strippedLog);
                }
            }
            if (wood != null && strippedWood != null) {
                register(wood, strippedWood);
            }
        }

        for (String line : Settings.buildingBlocks.strippableBlocks) {
            String[] parts = line.split("=");
            if (parts.length != 2) {
                SisyphusBackport.logger.warn("Ignoring invalid strippable block entry '{}', expected 'modid:log=modid:stripped_log'", line);
                continue;
            }
            Block log = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[0].trim()));
            Block stripped = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[1].trim()));
            if (log == null || log == Blocks.AIR || stripped == null || stripped == Blocks.AIR) {
                SisyphusBackport.logger.warn("Ignoring strippable block entry '{}', one of the blocks does not exist", line);
                continue;
            }
            register(log, stripped);
        }
    }

    @Nullable
    public static IBlockState getStripped(IBlockState state) {
        registerDefaults();

        for (Entry entry : ENTRIES) {
            if (entry.matcher.test(state)) {
                return copyAxis(state, entry.stripped.getDefaultState());
            }
        }

        Block auto = findByName(state.getBlock());
        return auto == null ? null : copyAxis(state, auto.getDefaultState());
    }

    @Nullable
    private static Block findByName(Block block) {
        if (AUTO_CACHE.containsKey(block)) {
            return AUTO_CACHE.get(block);
        }
        Block result = null;
        ResourceLocation name = block.getRegistryName();
        if (name != null && !name.getPath().startsWith("stripped_") && !"minecraft".equals(name.getNamespace())) {
            Block candidate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name.getNamespace(), "stripped_" + name.getPath()));
            if (candidate != null && candidate != Blocks.AIR && candidate != block) {
                result = candidate;
            }
        }
        AUTO_CACHE.put(block, result);
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static IBlockState copyAxis(IBlockState from, IBlockState to) {
        for (IProperty fromProperty : from.getPropertyKeys()) {
            if (!"axis".equals(fromProperty.getName())) {
                continue;
            }
            for (IProperty toProperty : to.getPropertyKeys()) {
                if ("axis".equals(toProperty.getName())) {
                    Optional parsed = toProperty.parseValue(fromProperty.getName(from.getValue(fromProperty)));
                    if (parsed.isPresent()) {
                        return to.withProperty(toProperty, (Comparable) parsed.get());
                    }
                }
            }
        }
        return to;
    }

    @SubscribeEvent
    public static void onStrip(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.buildingBlocks.enableStrippedWood) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !isAxe(stack)) {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer player = event.getEntityPlayer();

        IBlockState stripped = StrippingRecipes.getStripped(world.getBlockState(pos));
        if (stripped == null || !player.canPlayerEdit(pos, event.getFace(), stack)) {
            return;
        }

        if (!world.isRemote) {
            world.setBlockState(pos, stripped, 11);
            world.playSound(null, pos, ModSounds.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            stack.damageItem(1, player);
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    private static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof ItemAxe || stack.getItem().getToolClasses(stack).contains("axe");
    }

    private static final class Entry {
        final Predicate<IBlockState> matcher;
        final Block stripped;

        Entry(Predicate<IBlockState> matcher, Block stripped) {
            this.matcher = matcher;
            this.stripped = stripped;
        }
    }
}
