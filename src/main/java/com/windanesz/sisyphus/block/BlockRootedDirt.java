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
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockRootedDirt extends Block implements IGrowable {

    public BlockRootedDirt() {
        super(Material.GROUND, MapColor.DIRT);
        this.setHardness(0.5F);
        this.setResistance(0.5F);
        this.setSoundType(ModSounds.type("rooted_dirt"));
        this.setHarvestLevel("shovel", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    private static boolean rootsCanGrowBelow(World world, BlockPos pos) {
        return ModBlocks.get("hanging_roots") != Blocks.AIR && world.isAirBlock(pos.down());
    }

    @SubscribeEvent
    public static void onHoe(UseHoeEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (!Settings.plants.enableLushPlants || !(world.getBlockState(pos).getBlock() instanceof BlockRootedDirt)) {
            return;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
            world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            Block.spawnAsEntity(world, pos.up(), new ItemStack(ModBlocks.get("hanging_roots")));
        }
        event.setResult(Event.Result.ALLOW);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return rootsCanGrowBelow(world, pos);
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        if (rootsCanGrowBelow(world, pos)) {
            world.setBlockState(pos.down(), ModBlocks.get("hanging_roots").getDefaultState(), 3);
        }
    }
}
