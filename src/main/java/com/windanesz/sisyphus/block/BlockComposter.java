package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockComposter extends Block {

    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 8);

    private static final AxisAlignedBB FULL_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    private static final AxisAlignedBB NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
    private static final AxisAlignedBB SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
    private static final AxisAlignedBB EAST = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final Map<Item, Float> CHANCES = new HashMap<>();
    private static boolean filled = false;

    public BlockComposter() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
        this.setHardness(0.6F);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    private static void addChance(float chance, Block... blocks) {
        for (Block block : blocks) {
            addChance(chance, Item.getItemFromBlock(block));
        }
    }

    private static void addChance(float chance, Item... items) {
        for (Item item : items) {
            if (item != null && item != Items.AIR) {
                CHANCES.put(item, chance);
            }
        }
    }

    private static void fillChances() {
        filled = true;
        addChance(0.3F, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);
        addChance(0.3F, Blocks.SAPLING, Blocks.LEAVES, Blocks.LEAVES2, Blocks.TALLGRASS);
        addChance(0.5F, Blocks.CACTUS, Blocks.VINE);
        addChance(0.5F, Items.REEDS, Items.MELON);
        addChance(0.65F, Items.APPLE, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.WHEAT, Items.NETHER_WART);
        addChance(0.65F, Blocks.YELLOW_FLOWER, Blocks.RED_FLOWER, Blocks.DOUBLE_PLANT, Blocks.WATERLILY, Blocks.PUMPKIN,
                Blocks.MELON_BLOCK, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM);
        addChance(0.85F, Items.BREAD, Items.COOKIE, Items.BAKED_POTATO, Items.POISONOUS_POTATO);
        addChance(0.85F, Blocks.HAY_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.NETHER_WART_BLOCK);
        addChance(1.0F, Items.CAKE, Items.PUMPKIN_PIE);

        // The things this mod adds
        if (Settings.plants.enableFlowers) {
            addChance(0.65F, ModBlocks.get("cornflower"), ModBlocks.get("lily_of_the_valley"), ModBlocks.get("wither_rose"));
        }
        if (Settings.plants.enableSweetBerries) {
            addChance(0.3F, ModItems.get("sweet_berries"));
        }
        if (Settings.isContentEnabled("cherry_trees")) {
            addChance(0.3F, ModBlocks.get("cherry_leaves"), ModBlocks.get("cherry_sapling"), ModBlocks.get("pink_petals"));
        }
        fillNether();
        fillCaves();
    }

    private static void fillCaves() {
        if (Settings.plants.enableLushPlants) {
            addChance(0.3F, itemOf(ModBlocks.get("hanging_roots")), itemOf(ModBlocks.get("azalea_leaves")), itemOf(ModBlocks.get("flowering_azalea_leaves")));
            addChance(0.5F, itemOf(ModBlocks.get("glow_lichen")));
            addChance(0.65F, itemOf(ModBlocks.get("azalea")), itemOf(ModBlocks.get("spore_blossom")));
            addChance(0.85F, itemOf(ModBlocks.get("flowering_azalea")));
        }
        if (Settings.plants.enableCaveVines) {
            addChance(0.3F, ModItems.get("glow_berries"));
        }
        if (Settings.plants.enableDripleaf) {
            addChance(0.65F, itemOf(ModBlocks.get("big_dripleaf")), itemOf(ModBlocks.get("small_dripleaf")));
        }
    }

    private static void fillNether() {
        if (Settings.plants.enableNetherFungi) {
            addChance(0.5F, itemOf(ModBlocks.get("nether_sprouts")), itemOf(ModBlocks.get("weeping_vines")), itemOf(ModBlocks.get("twisting_vines")));
            addChance(0.65F, itemOf(ModBlocks.get("crimson_fungus")), itemOf(ModBlocks.get("warped_fungus")),
                    itemOf(ModBlocks.get("crimson_roots")), itemOf(ModBlocks.get("warped_roots")));
            addChance(0.85F, itemOf(ModBlocks.get("warped_wart_block")));
        }
        if (Settings.buildingBlocks.enableShroomlight) {
            addChance(0.85F, itemOf(ModBlocks.get("shroomlight")));
        }
    }

    private static Item itemOf(Block block) {
        return block == null || block == Blocks.AIR ? Items.AIR : Item.getItemFromBlock(block);
    }

    public static float getChance(ItemStack stack) {
        if (!filled) {
            fillChances();
        }
        Float chance = CHANCES.get(stack.getItem());
        if (chance != null) {
            return chance;
        }
        // Cocoa beans are a dye in 1.12
        if (stack.getItem() == Items.DYE && stack.getMetadata() == 3) {
            return 0.65F;
        }
        return 0.0F;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(LEVEL, Math.min(meta, 8));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BOX;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BOTTOM);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        int level = state.getValue(LEVEL);
        ItemStack stack = player.getHeldItem(hand);

        if (level == 8) {
            // Take the finished bone meal
            if (!world.isRemote) {
                spawnAsEntity(world, pos.up(), new ItemStack(Items.DYE, 1, 15));
                world.setBlockState(pos, state.withProperty(LEVEL, 0), 3);
                world.playSound(null, pos, ModSounds.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }

        float chance = stack.isEmpty() ? 0.0F : getChance(stack);
        if (level < 7 && chance > 0.0F) {
            if (!world.isRemote) {
                boolean added = world.rand.nextFloat() < chance;
                if (added) {
                    int newLevel = level + 1;
                    world.setBlockState(pos, state.withProperty(LEVEL, newLevel), 3);
                    if (newLevel == 7) {
                        world.scheduleUpdate(pos, this, 20);
                    }
                }
                world.playSound(null, pos, added ? ModSounds.BLOCK_COMPOSTER_FILL_SUCCESS : ModSounds.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            return true;
        }
        // A full composter is just a block, items cannot be added to it
        return level == 7 && chance > 0.0F;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        // A full composter turns the contents into bone meal after a short moment
        if (!world.isRemote && state.getValue(LEVEL) == 7) {
            world.setBlockState(pos, state.withProperty(LEVEL, 8), 3);
            world.playSound(null, pos, ModSounds.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
