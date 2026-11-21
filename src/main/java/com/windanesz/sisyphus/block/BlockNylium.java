package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockNylium extends Block implements IGrowable {

    private static final String[] CRIMSON_PLANTS = {"crimson_roots", "crimson_roots", "crimson_roots", "crimson_fungus"};
    private static final String[] WARPED_PLANTS = {"warped_roots", "warped_roots", "nether_sprouts", "nether_sprouts", "warped_fungus", "twisting_vines"};

    private final boolean warped;

    public BlockNylium(boolean warped) {
        super(Material.ROCK, warped ? MapColor.CYAN : MapColor.NETHERRACK);
        this.warped = warped;
        this.setHardness(0.4F);
        this.setResistance(0.4F);
        this.setHarvestLevel("pickaxe", 0);
        this.setSoundType(ModSounds.type("nylium"));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @SubscribeEvent
    public static void onBoneMealNetherrack(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (!Settings.plants.enableNetherFungi || stack.getItem() != Items.DYE || stack.getMetadata() != EnumDyeColor.WHITE.getDyeDamage()
                || world.getBlockState(pos).getBlock() != Blocks.NETHERRACK || !world.isAirBlock(pos.up())) {
            return;
        }
        boolean crimson = false;
        boolean warped = false;
        for (BlockPos near : BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            Block block = world.getBlockState(near).getBlock();
            if (block instanceof BlockNylium) {
                if (((BlockNylium) block).isWarped()) {
                    warped = true;
                } else {
                    crimson = true;
                }
            }
        }
        if (!crimson && !warped) {
            return;
        }
        if (!world.isRemote) {
            boolean useWarped = warped && (!crimson || world.rand.nextBoolean());
            world.setBlockState(pos, ModBlocks.getOrNull(useWarped ? "warped_nylium" : "crimson_nylium").getDefaultState(), 3);
            world.playEvent(2005, pos.up(), 0);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    public boolean isWarped() {
        return this.warped;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.NETHERRACK);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }
        BlockPos up = pos.up();
        if (world.getBlockState(up).getLightOpacity(world, up) > 2) {
            world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState());
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return world.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        String[] plants = this.warped ? WARPED_PLANTS : CRIMSON_PLANTS;
        for (int i = 0; i < 16; i++) {
            BlockPos target = pos.add(rand.nextInt(7) - 3, rand.nextInt(3) - 1, rand.nextInt(7) - 3);
            if (world.isAirBlock(target) && world.getBlockState(target.down()).getBlock() == this) {
                Block plant = ModBlocks.getOrNull(plants[rand.nextInt(plants.length)]);
                if (plant != null && plant.canPlaceBlockAt(world, target)) {
                    world.setBlockState(target, plant.getDefaultState(), 3);
                }
            }
        }
    }
}
