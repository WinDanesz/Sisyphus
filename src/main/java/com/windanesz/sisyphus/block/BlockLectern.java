package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ModGuiHandler;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntityLectern;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;
import java.util.regex.Pattern;

public class BlockLectern extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool HAS_BOOK = PropertyBool.create("has_book");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);

    public BlockLectern() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(HAS_BOOK, false).withProperty(POWERED, false));
        this.setHardness(2.5F);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    public static boolean canHold(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK) {
            return true;
        }
        if (stack.getItem() == Items.FILLED_MAP) {
            return Settings.functionalBlocks.lecternHoldsMaps;
        }
        // Blocks are left out, or "*book*" would put bookshelves on lecterns
        return !(stack.getItem() instanceof ItemBlock) && stack.getItem().getRegistryName() != null
                && matchesAny(Settings.functionalBlocks.lecternItems, stack.getItem().getRegistryName().toString());
    }

    public static boolean matchesAny(String[] patterns, String name) {
        for (String pattern : patterns) {
            String trimmed = pattern.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\*", -1);
            for (int i = 0; i < parts.length; i++) {
                parts[i] = Pattern.quote(parts[i]);
            }
            if (Pattern.compile(String.join(".*", parts), Pattern.CASE_INSENSITIVE).matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HAS_BOOK, POWERED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(HAS_BOOK, (meta & 4) != 0).withProperty(POWERED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(HAS_BOOK) ? 4 : 0) | (state.getValue(POWERED) ? 8 : 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLectern();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityLectern)) {
            return false;
        }
        TileEntityLectern lectern = (TileEntityLectern) tile;
        ItemStack held = player.getHeldItem(hand);

        if (!lectern.hasBook()) {
            if (canHold(held)) {
                if (!world.isRemote) {
                    ItemStack one = held.copy();
                    one.setCount(1);
                    lectern.setBook(one);
                    world.setBlockState(pos, state.withProperty(HAS_BOOK, true), 3);
                    world.playSound(null, pos, ModSounds.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!player.capabilities.isCreativeMode) {
                        held.shrink(1);
                    }
                }
                return true;
            }
            return false;
        }

        if (player.isSneaking() && held.isEmpty()) {
            if (!world.isRemote) {
                this.giveBook(world, pos, state, lectern, player);
            }
            return true;
        }
        if (!lectern.hasReadableBook() && !lectern.hasMap()) {
            // On both sides, as the screens of guide books are opened by the client or by the server
            this.useItem(world, lectern, player, hand);
            return true;
        }
        if (!world.isRemote) {
            player.openGui(SisyphusBackport.instance, ModGuiHandler.LECTERN, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    /**
     * The item stays on the lectern even if using it would use it up.
     */
    private void useItem(World world, TileEntityLectern lectern, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        ItemStack onLectern = lectern.getBook().copy();
        player.setHeldItem(hand, onLectern);
        ItemStack result;
        try {
            result = onLectern.getItem().onItemRightClick(world, player, hand).getResult();
        } finally {
            if (player.isHandActive()) {
                player.resetActiveHand();
            }
            player.setHeldItem(hand, held);
        }
        // What using it wrote on the item is kept, such as the page a book was left open on
        if (!world.isRemote && !result.isEmpty() && result.getItem() == lectern.getBook().getItem()) {
            lectern.updateBook(result);
        }
    }

    public void giveBook(World world, BlockPos pos, IBlockState state, TileEntityLectern lectern, EntityPlayer player) {
        ItemStack book = lectern.removeBook();
        world.setBlockState(pos, state.withProperty(HAS_BOOK, false).withProperty(POWERED, false), 3);
        if (!book.isEmpty() && !player.inventory.addItemStackToInventory(book)) {
            player.dropItem(book, false);
        }
        world.playSound(null, pos, ModSounds.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void pulse(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(POWERED, true), 3);
        world.scheduleUpdate(pos, this, 2);
        this.updateNeighbours(world, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(POWERED, false), 3);
            this.updateNeighbours(world, pos);
        }
    }

    private void updateNeighbours(World world, BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, this, false);
        world.notifyNeighborsOfStateChange(pos.down(), this, false);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP && state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityLectern ? ((TileEntityLectern) tile).getRedstoneSignal() : 0;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityLectern && ((TileEntityLectern) tile).hasBook() && !world.isRemote) {
            ItemStack book = ((TileEntityLectern) tile).getBook();
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, book.copy()));
        }
        super.breakBlock(world, pos, state);
    }
}
