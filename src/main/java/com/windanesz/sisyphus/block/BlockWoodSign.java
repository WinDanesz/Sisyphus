package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.integration.ModdedWoodSigns;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public final class BlockWoodSign {

    private BlockWoodSign() {
    }

    @Nullable
    public static String woodOf(Block block) {
        ResourceLocation name = block.getRegistryName();
        if (name == null || !SisyphusBackport.MODID.equals(name.getNamespace())) {
            return null;
        }
        for (String suffix : new String[]{"_standing_sign", "_wall_sign"}) {
            if (name.getPath().endsWith(suffix)) {
                String wood = name.getPath().substring(0, name.getPath().length() - suffix.length());
                return ModBlocks.signWoods().contains(wood) || ModdedWoodSigns.all().containsKey(wood) ? wood : null;
            }
        }
        return null;
    }

    private static final class NetherSignSounds {
        static boolean isNetherWood(String wood) {
            return wood.equals("crimson") || wood.equals("warped");
        }
    }

    public static class Standing extends BlockStandingSign {

        private final String wood;

        public Standing(String wood) {
            this.wood = wood;
            if (NetherSignSounds.isNetherWood(wood)) {
                this.setSoundType(ModSounds.type("nether_wood"));
            } else if (wood.equals("cherry")) {
                this.setSoundType(ModSounds.type("cherry_wood"));
            } else if (wood.equals("bamboo")) {
                this.setSoundType(ModSounds.type("bamboo_wood"));
            }
        }

        public Standing(String wood, SoundType sound) {
            this.wood = wood;
            this.setSoundType(sound);
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return ModItems.get(this.wood + "_sign");
        }

        @Override
        public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
            return new ItemStack(ModItems.get(this.wood + "_sign"));
        }
    }

    public static class Wall extends BlockWallSign {

        private final String wood;

        public Wall(String wood) {
            this.wood = wood;
            if (NetherSignSounds.isNetherWood(wood)) {
                this.setSoundType(ModSounds.type("nether_wood"));
            } else if (wood.equals("cherry")) {
                this.setSoundType(ModSounds.type("cherry_wood"));
            } else if (wood.equals("bamboo")) {
                this.setSoundType(ModSounds.type("bamboo_wood"));
            }
        }

        public Wall(String wood, SoundType sound) {
            this.wood = wood;
            this.setSoundType(sound);
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return ModItems.get(this.wood + "_sign");
        }

        @Override
        public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
            return new ItemStack(ModItems.get(this.wood + "_sign"));
        }
    }
}
