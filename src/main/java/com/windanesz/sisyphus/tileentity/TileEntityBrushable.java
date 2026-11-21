package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.block.BlockSuspicious;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TileEntityBrushable extends TileEntity {

    private static final int BRUSH_COOLDOWN_TICKS = 10;
    private static final int BRUSH_RESET_TICKS = 40;
    private static final int REQUIRED_BRUSHES_TO_BREAK = 10;

    private int brushCount;
    private long brushCountResetsAtTick;
    private long coolDownEndsAtTick;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private EnumFacing hitDirection;
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    public ItemStack getItem() {
        return this.item;
    }

    @Nullable
    public EnumFacing getHitDirection() {
        return this.hitDirection;
    }

    /**
     * A seed of 0 picks a random one.
     */
    public void setLootTable(ResourceLocation table, long seed) {
        this.lootTable = table;
        this.lootTableSeed = seed;
        this.markDirty();
    }

    @Nullable
    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    /**
     * True on the last stroke, which uncovers the item.
     */
    public boolean brush(long gameTime, EntityPlayer player, EnumFacing side) {
        if (this.hitDirection == null) {
            this.hitDirection = side;
        }
        this.brushCountResetsAtTick = gameTime + BRUSH_RESET_TICKS;
        if (gameTime < this.coolDownEndsAtTick || !(this.world instanceof WorldServer)) {
            return false;
        }
        this.coolDownEndsAtTick = gameTime + BRUSH_COOLDOWN_TICKS;
        this.unpackLootTable(player);
        int before = this.getCompletionState();
        if (++this.brushCount >= REQUIRED_BRUSHES_TO_BREAK) {
            this.brushingCompleted(player);
            return true;
        }
        this.world.scheduleUpdate(this.pos, this.getBlockType(), BRUSH_RESET_TICKS);
        int after = this.getCompletionState();
        if (before != after) {
            this.setDusted(after);
        } else {
            this.sync();
        }
        return false;
    }

    public void unpackLootTable(@Nullable EntityPlayer player) {
        if (this.lootTable == null || !(this.world instanceof WorldServer)) {
            return;
        }
        WorldServer server = (WorldServer) this.world;
        LootTable table = server.getLootTableManager().getLootTableFromLocation(this.lootTable);
        LootContext.Builder builder = new LootContext.Builder(server);
        if (player != null) {
            builder.withLuck(player.getLuck()).withPlayer(player);
        }
        Random random = this.lootTableSeed == 0L ? new Random() : new Random(this.lootTableSeed);
        List<ItemStack> items = table.generateLootForPools(random, builder.build());
        this.item = items.isEmpty() ? ItemStack.EMPTY : items.get(0);
        this.lootTable = null;
        this.markDirty();
        if (player instanceof EntityPlayerMP) {
            // The first half of "Respecting the Remnants": brushing a suspicious block. The second is having a sherd
            AdvancementHelper.grant((EntityPlayerMP) player, "respecting_the_remnants");
        }
    }

    private void brushingCompleted(EntityPlayer player) {
        if (this.world == null) {
            return;
        }
        this.dropContent(player);
        IBlockState state = this.world.getBlockState(this.pos);
        Block block = state.getBlock();
        if (!(block instanceof BlockSuspicious)) {
            return;
        }
        BlockSuspicious suspicious = (BlockSuspicious) block;
        // The sound of the last stroke and the bits of the block, as when it breaks, but without the sound of breaking
        this.world.playSound(null, this.pos, suspicious.getBrushCompletedSound(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D,
                    40, 0.25D, 0.25D, 0.25D, 0.05D, Block.getStateId(state));
        }
        this.world.setBlockState(this.pos, suspicious.getTurnsInto(), 3);
    }

    private void dropContent(EntityPlayer player) {
        this.unpackLootTable(player);
        if (this.item.isEmpty()) {
            return;
        }
        // Just outside the brushed side, standing still, as in the game
        double width = 0.25D;
        double offset = 1.0D - width;
        EnumFacing side = this.hitDirection == null ? EnumFacing.UP : this.hitDirection;
        BlockPos at = this.pos.offset(side);
        double x = at.getX() + 0.5D * offset + width / 2.0D;
        double y = at.getY() + 0.5D + width / 2.0D;
        double z = at.getZ() + 0.5D * offset + width / 2.0D;
        EntityItem entity = new EntityItem(this.world, x, y, z, this.item.splitStack(this.world.rand.nextInt(21) + 10));
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        this.world.spawnEntity(entity);
        this.item = ItemStack.EMPTY;
    }

    public void checkReset() {
        if (this.world == null) {
            return;
        }
        long gameTime = this.world.getTotalWorldTime();
        if (this.brushCount != 0 && gameTime >= this.brushCountResetsAtTick) {
            int before = this.getCompletionState();
            this.brushCount = Math.max(0, this.brushCount - 2);
            int after = this.getCompletionState();
            if (before != after) {
                this.setDusted(after);
            }
            this.brushCountResetsAtTick = gameTime + 4;
        }
        if (this.brushCount == 0) {
            this.hitDirection = null;
            this.brushCountResetsAtTick = 0L;
            this.coolDownEndsAtTick = 0L;
            this.sync();
        } else {
            this.world.scheduleUpdate(this.pos, this.getBlockType(), (int) (this.brushCountResetsAtTick - gameTime));
        }
    }

    private int getCompletionState() {
        if (this.brushCount == 0) {
            return 0;
        } else if (this.brushCount < 3) {
            return 1;
        } else {
            return this.brushCount < 6 ? 2 : 3;
        }
    }

    private void setDusted(int dusted) {
        IBlockState state = this.world.getBlockState(this.pos);
        if (state.getBlock() instanceof BlockSuspicious) {
            // The tile entity stays, as shouldRefresh keeps it for the same block, and the client is told about both
            this.world.setBlockState(this.pos, state.withProperty(BlockSuspicious.DUSTED, dusted), 3);
        }
        this.sync();
    }

    private void sync() {
        this.markDirty();
        if (this.world != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("LootTable", 8)) {
            this.lootTable = new ResourceLocation(compound.getString("LootTable"));
            this.lootTableSeed = compound.getLong("LootTableSeed");
        } else {
            this.lootTable = null;
        }
        this.item = compound.hasKey("item", 10) ? new ItemStack(compound.getCompoundTag("item")) : ItemStack.EMPTY;
        this.hitDirection = compound.hasKey("hit_direction", 99) ? EnumFacing.byIndex(compound.getInteger("hit_direction")) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.lootTable != null) {
            compound.setString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                compound.setLong("LootTableSeed", this.lootTableSeed);
            }
        }
        if (!this.item.isEmpty()) {
            compound.setTag("item", this.item.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // The client needs the item and the side to draw the item coming out, but not the loot table
        NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
        if (this.hitDirection != null) {
            compound.setInteger("hit_direction", this.hitDirection.getIndex());
        }
        if (!this.item.isEmpty()) {
            compound.setTag("item", this.item.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        NBTTagCompound compound = packet.getNbtCompound();
        this.item = compound.hasKey("item", 10) ? new ItemStack(compound.getCompoundTag("item")) : ItemStack.EMPTY;
        this.hitDirection = compound.hasKey("hit_direction", 99) ? EnumFacing.byIndex(compound.getInteger("hit_direction")) : null;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.item = compound.hasKey("item", 10) ? new ItemStack(compound.getCompoundTag("item")) : ItemStack.EMPTY;
        this.hitDirection = compound.hasKey("hit_direction", 99) ? EnumFacing.byIndex(compound.getInteger("hit_direction")) : null;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
