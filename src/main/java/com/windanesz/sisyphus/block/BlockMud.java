package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockMud extends Block {

    private static final AxisAlignedBB COLLISION = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 14.0D / 16.0D, 1.0D);

    public BlockMud() {
        super(Material.GROUND, MapColor.CYAN_STAINED_HARDENED_CLAY);
        this.setHardness(0.5F);
        this.setSoundType(ModSounds.type("mud"));
        this.setHarvestLevel("shovel", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public static boolean makesMud(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.DIRT) {
            BlockDirt.DirtType type = state.getValue(BlockDirt.VARIANT);
            return type == BlockDirt.DirtType.DIRT || type == BlockDirt.DirtType.COARSE_DIRT;
        }
        return block instanceof BlockRootedDirt;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.buildingBlocks.enableMud || event.getFace() == EnumFacing.DOWN) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != Items.POTIONITEM || PotionUtils.getPotionFromItem(stack) != PotionTypes.WATER) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (!makesMud(world.getBlockState(pos))) {
            return;
        }
        Block mud = ModBlocks.get("mud");
        if (mud == Blocks.AIR) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        EntityPlayer player = event.getEntityPlayer();
        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (world.isRemote) {
            player.swingArm(event.getHand());
            return;
        }
        player.addStat(StatList.getObjectUseStats(Items.POTIONITEM));
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) {
                player.setHeldItem(event.getHand(), bottle);
            } else if (!player.inventory.addItemStackToInventory(bottle)) {
                player.dropItem(bottle, false);
            }
        }
        if (world instanceof WorldServer) {
            for (int i = 0; i < 5; i++) {
                ((WorldServer) world).spawnParticle(EnumParticleTypes.WATER_SPLASH, pos.getX() + world.rand.nextDouble(), pos.getY() + 1.0D,
                        pos.getZ() + world.rand.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
            }
        }
        world.setBlockState(pos, mud.getDefaultState());
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return COLLISION;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }
}
