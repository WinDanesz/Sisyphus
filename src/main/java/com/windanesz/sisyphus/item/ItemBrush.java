package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockSuspicious;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityBrushable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemBrush extends Item {

    public static final int USE_DURATION = 200;

    public ItemBrush() {
        this.setMaxStackSize(1);
        this.setMaxDamage(64);
        this.setCreativeTab(CreativeTabs.TOOLS);
        // Picks the model of the brush that is tilted for the stroke, which the third person view shows
        this.addPropertyOverride(new ResourceLocation("brushing"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return entity != null && entity.getActiveItemStack() == stack ? (entity.getItemInUseCount() % 10) / 10.0F : 0.0F;
            }
        });
    }

    private static void spawnDustParticles(World world, RayTraceResult hit, IBlockState state, Vec3d look, EnumHandSide arm) {
        int side = arm == EnumHandSide.RIGHT ? 1 : -1;
        Random rand = world.rand;
        int count = 7 + rand.nextInt(5);
        EnumFacing face = hit.sideHit;
        double dx;
        double dz;
        switch (face) {
            case NORTH:
                dx = 1.0D;
                dz = -0.1D;
                break;
            case SOUTH:
                dx = -1.0D;
                dz = 0.1D;
                break;
            case WEST:
                dx = -0.1D;
                dz = -1.0D;
                break;
            case EAST:
                dx = 0.1D;
                dz = 1.0D;
                break;
            default:
                dx = look.z;
                dz = -look.x;
        }
        Vec3d at = hit.hitVec;
        int id = Block.getStateId(state);
        for (int i = 0; i < count; i++) {
            world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, at.x - (face == EnumFacing.WEST ? 1.0E-6D : 0.0D), at.y, at.z - (face == EnumFacing.NORTH ? 1.0E-6D : 0.0D),
                    dx * side * 3.0D * rand.nextDouble(), 0.0D, dz * side * 3.0D * rand.nextDouble(), id);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        RayTraceResult hit = this.rayTrace(world, player, false);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        if (count < 0 || !(entity instanceof EntityPlayer)) {
            entity.stopActiveHand();
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        World world = player.world;
        RayTraceResult hit = this.rayTrace(world, player, false);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            player.stopActiveHand();
            return;
        }
        int used = this.getMaxItemUseDuration(stack) - count + 1;
        if (used % 10 != 5) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        EnumHandSide arm = player.getActiveHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        if (world.isRemote) {
            spawnDustParticles(world, hit, state, player.getLook(0.0F), arm);
        }
        Block block = state.getBlock();
        SoundEvent sound = block instanceof BlockSuspicious ? ((BlockSuspicious) block).getBrushSound() : ModSounds.event("item.brush.brushing.generic");
        world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityBrushable && ((TileEntityBrushable) tile).brush(world.getTotalWorldTime(), player, hit.sideHit)) {
                stack.damageItem(1, player);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // The durability it loses while it is held does not swap it out and in again
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
