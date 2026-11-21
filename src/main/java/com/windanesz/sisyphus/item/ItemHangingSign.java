package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockHangingSign;
import com.windanesz.sisyphus.packet.ModPacketHandler;
import com.windanesz.sisyphus.packet.PacketHangingSignEditor;
import com.windanesz.sisyphus.tileentity.TileEntityHangingSign;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class ItemHangingSign extends Item {

    private final Block ceiling;
    private final Block wall;

    public ItemHangingSign(Block ceiling, Block wall) {
        this.ceiling = ceiling;
        this.wall = wall;
        this.setMaxStackSize(16);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    static EnumFacing[] lookingDirections(Vec3d look) {
        EnumFacing[] directions = EnumFacing.values().clone();
        Arrays.sort(directions, Comparator.comparingDouble(
                (EnumFacing d) -> -(d.getXOffset() * look.x + d.getYOffset() * look.y + d.getZOffset() * look.z)));
        return directions;
    }

    static int ceilingRotation(float yaw, boolean attached) {
        if (attached) {
            return MathHelper.floor((yaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        }
        return EnumFacing.fromAngle(yaw).getOpposite().getHorizontalIndex() * 4;
    }

    /**
     * Not under a hanging sign facing along the look direction, so that a row of them hangs straight.
     */
    private static boolean isAttached(World world, BlockPos pos, EntityPlayer player) {
        BlockPos up = pos.up();
        IBlockState above = world.getBlockState(up);
        boolean attached = above.getBlockFaceShape(world, up, EnumFacing.DOWN) != BlockFaceShape.SOLID || player.isSneaking();
        if (!player.isSneaking() && above.getBlock() instanceof BlockHangingSign) {
            EnumFacing looking = EnumFacing.fromAngle(player.rotationYaw);
            EnumFacing facing = above.getBlock() instanceof BlockHangingSign.Wall ? above.getValue(BlockHangingSign.Wall.FACING)
                    : BlockHangingSign.Ceiling.facingOf(above.getValue(BlockHangingSign.Ceiling.ROTATION));
            if (facing != null && facing.getAxis() == looking.getAxis()) {
                attached = false;
            }
        }
        return attached;
    }

    @Nullable
    private IBlockState wallState(World world, BlockPos pos, EnumFacing clicked, EnumFacing[] directions) {
        for (EnumFacing direction : directions) {
            if (direction.getAxis().isHorizontal() && direction.getAxis() != clicked.getAxis()) {
                EnumFacing facing = direction.getOpposite();
                if (BlockHangingSign.canHangOnWall(world, pos, facing)) {
                    return this.wall.getDefaultState().withProperty(BlockHangingSign.Wall.FACING, facing);
                }
            }
        }
        return null;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos) || !player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }

        EnumFacing[] directions = lookingDirections(player.getLook(1.0F));
        IBlockState wallState = this.wallState(world, pos, facing, directions);
        IBlockState state = null;
        boolean attached = false;
        for (EnumFacing direction : directions) {
            if (direction == EnumFacing.UP) {
                if (BlockHangingSign.canHangUnder(world, pos.up())) {
                    attached = isAttached(world, pos, player);
                    state = this.ceiling.getDefaultState().withProperty(BlockHangingSign.Ceiling.ROTATION, ceilingRotation(player.rotationYaw, attached));
                    break;
                }
            } else if (direction != EnumFacing.DOWN && wallState != null) {
                state = wallState;
                break;
            }
        }
        if (state == null || !world.mayPlace(state.getBlock(), pos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        world.setBlockState(pos, state, 11);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityHangingSign) {
            TileEntityHangingSign sign = (TileEntityHangingSign) tile;
            sign.setAttached(attached);
            if (!ItemBlock.setTileEntityNBT(world, player, pos, stack) && player instanceof EntityPlayerMP) {
                sign.setPlayer(player);
                ModPacketHandler.net.sendTo(new PacketHangingSignEditor.Message(pos, ((BlockHangingSign) state.getBlock()).wood), (EntityPlayerMP) player);
            }
        }
        SoundType sound = state.getBlock().getSoundType(state, world, pos, player);
        world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
