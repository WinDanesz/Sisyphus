package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockRespawnAnchor extends Block {

    public static final PropertyInteger CHARGES = PropertyInteger.create("charges", 0, 4);

    public static final String SPAWN_KEY = "SisyphusBackportRespawnAnchor";

    public BlockRespawnAnchor() {
        super(Material.ROCK, MapColor.BLACK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CHARGES, 0));
        this.setHardness(50.0F);
        this.setResistance(1200.0F);
        this.setHarvestLevel("pickaxe", 3);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE);
    }

    public static void charge(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(CHARGES, state.getValue(CHARGES) + 1), 3);
        world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ModSounds.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static void explode(World world, BlockPos pos) {
        world.setBlockToAir(pos);
        world.newExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, true);
    }

    public static boolean isEnabled() {
        return Settings.functionalBlocks.enableRespawnAnchor;
    }

    @SubscribeEvent
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote && event.getNewSpawn() != null) {
            NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persisted.removeTag(BlockRespawnAnchor.SPAWN_KEY);
            player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote || !(player instanceof EntityPlayerMP) || !Settings.functionalBlocks.enableRespawnAnchor) {
            return;
        }
        NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        int[] at = persisted.getIntArray(BlockRespawnAnchor.SPAWN_KEY);
        if (at.length != 3) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        // Done a moment later, when the new player is in the world
        server.addScheduledTask(() -> moveToAnchor(server, (EntityPlayerMP) player, new BlockPos(at[0], at[1], at[2])));
    }

    private static void moveToAnchor(MinecraftServer server, EntityPlayerMP player, BlockPos anchor) {
        WorldServer nether = server.getWorld(-1);
        IBlockState state = nether.getBlockState(anchor);
        BlockPos spot = findSpot(nether, anchor);
        if (!(state.getBlock() instanceof BlockRespawnAnchor) || state.getValue(BlockRespawnAnchor.CHARGES) == 0 || spot == null) {
            NBTTagCompound persisted = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persisted.removeTag(BlockRespawnAnchor.SPAWN_KEY);
            player.sendStatusMessage(new TextComponentTranslation("block.sisyphus.spawn_not_valid"), false);
            return;
        }
        nether.setBlockState(anchor, state.withProperty(BlockRespawnAnchor.CHARGES, state.getValue(BlockRespawnAnchor.CHARGES) - 1), 3);
        nether.playSound(null, anchor.getX() + 0.5D, anchor.getY() + 0.5D, anchor.getZ() + 0.5D, ModSounds.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        final BlockPos target = spot;
        server.getPlayerList().transferPlayerToDimension(player, -1, new ITeleporter() {
            @Override
            public void placeEntity(World world, Entity entity, float yaw) {
                entity.setLocationAndAngles(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, yaw, 0.0F);
            }
        });
    }

    @Nullable
    private static BlockPos findSpot(World world, BlockPos anchor) {
        BlockPos[] candidates = {anchor.up(), anchor.north(), anchor.south(), anchor.east(), anchor.west(), anchor.north().up(), anchor.south().up(),
                anchor.east().up(), anchor.west().up()};
        for (BlockPos pos : candidates) {
            if (world.isAirBlock(pos) && world.isAirBlock(pos.up()) && world.getBlockState(pos.down()).isTopSolid()) {
                return pos;
            }
        }
        return null;
    }

    public static boolean dispenseCharge(IBlockSource source, World world, BlockPos front, ItemStack stack) {
        IBlockState state = world.getBlockState(front);
        if (!(state.getBlock() instanceof BlockRespawnAnchor) || state.getValue(BlockRespawnAnchor.CHARGES) >= 4 || !BlockRespawnAnchor.isFuel(stack)) {
            return false;
        }
        if (!world.isRemote) {
            BlockRespawnAnchor.charge(world, front, state);
        }
        stack.shrink(1);
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHARGES);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(CHARGES, Math.min(meta, 4));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CHARGES);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return (int) Math.floor(state.getValue(CHARGES) / 4.0F * 15.0F);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return !(entity instanceof EntityDragon) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return (int) Math.floor(state.getValue(CHARGES) / 4.0F * 15.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        int charges = state.getValue(CHARGES);

        // Glowstone charges it
        if (isFuel(held) && charges < 4) {
            if (!world.isRemote) {
                charge(world, pos, state);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                if (charges + 1 == 4 && player instanceof EntityPlayerMP) {
                    AdvancementHelper.grant((EntityPlayerMP) player, "not_quite_nine_lives");
                }
            }
            return true;
        }
        if (hand != EnumHand.MAIN_HAND || charges == 0) {
            return false;
        }
        if (!world.provider.isNether()) {
            if (!world.isRemote) {
                explode(world, pos);
            }
            return true;
        }
        if (!world.isRemote) {
            NBTTagCompound data = player.getEntityData();
            NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persisted.setIntArray(SPAWN_KEY, new int[]{pos.getX(), pos.getY(), pos.getZ()});
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
            world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ModSounds.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.sendStatusMessage(new TextComponentTranslation("block.sisyphus.set_spawn"), true);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (state.getValue(CHARGES) == 0) {
            return;
        }
        if (rand.nextInt(100) == 0) {
            world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ModSounds.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
        double x = pos.getX() + 0.5D + (0.5D - rand.nextDouble());
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.5D + (0.5D - rand.nextDouble());
        world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0.0D, rand.nextFloat() * 0.04D, 0.0D);
    }
}
