package com.windanesz.sisyphus.entity.projectile;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.block.BlockTarget;
import com.windanesz.sisyphus.item.ItemThrowingDisc;
import com.windanesz.sisyphus.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityThrowingDisc extends Entity {

    public static final int OFF_HAND = -1;

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityThrowingDisc.class, DataSerializers.ITEM_STACK);

    private static final double MAX_SPEED = 2.4D;
    private static final double VELOCITY_BONUS = 0.2D;
    private static final double BOUNCE_SPEEDUP = 1.15D;
    private static final double RICOCHET_SPEEDUP = 0.05D;
    private static final double REACH_RANGE = 2.0D;
    private static final double BOUNCE_RANGE = 5.0D;
    private static final int RICOCHET_BOUNCES = 2;
    private static final float[] REBOUND_DAMAGE = {0.0F, 0.5F, 0.75F};
    private static final double RETURN_SPEED = 1.0D;
    /**
     * Slows down over these ticks before turning, so it does not turn around at full speed.
     */
    private static final double EASE_TICKS = 6.0D;
    private static final double MIN_EASE = 0.15D;
    private static final double TURN_SPEED = 0.3D;
    private static final int LIFETIME = 600;
    private static final String CANDLE_SLOWED = "enchantedcandles:candle_slowed";
    private static final String CANDLE_SLOW_TAKEN = "sisyphus:candle_slow_taken";
    private final Set<Integer> hit = new HashSet<>();
    private final Set<Integer> hitOnTheWayOut = new HashSet<>();
    @Nullable
    private UUID ownerId;
    private int slot;
    private double rangeLeft = Settings.items.throwingDiscs.range;
    private int bounces;
    private double speed = Settings.items.throwingDiscs.speed;
    private boolean returning;
    private double returnSpeed;

    public EntityThrowingDisc(World world) {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.isImmuneToFire = true;
    }

    public EntityThrowingDisc(World world, EntityPlayer thrower, ItemStack stack, int slot) {
        this(world);
        this.ownerId = thrower.getUniqueID();
        this.slot = slot;
        this.setItem(stack);
        this.setPosition(thrower.posX, thrower.posY + thrower.getEyeHeight() - 0.1D - this.height / 2.0D, thrower.posZ);
        this.speed = Settings.items.throwingDiscs.speed * this.velocityFactor();
        this.rangeLeft = Settings.items.throwingDiscs.range + REACH_RANGE * ModEnchantments.level(ModEnchantments.REACH, stack);
        Vec3d look = thrower.getLookVec();
        this.motionX = look.x * this.speed;
        this.motionY = look.y * this.speed;
        this.motionZ = look.z * this.speed;
        this.updateRotation();
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    private static boolean isSoft(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.CLOTH || material == Material.CARPET || material == Material.LEAVES || material == Material.SPONGE
                || material == Material.SNOW || material == Material.CRAFTED_SNOW || material == Material.CAKE || material == Material.GOURD
                || material == Material.CACTUS;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return this.dataManager.get(ITEM);
    }

    private void setItem(ItemStack stack) {
        this.dataManager.set(ITEM, stack);
    }

    @Nullable
    private EntityPlayer getOwner() {
        return this.ownerId == null ? null : this.world.getPlayerEntityByUUID(this.ownerId);
    }

    private double velocityFactor() {
        return 1.0D + VELOCITY_BONUS * ModEnchantments.level(ModEnchantments.VELOCITY, this.getItem());
    }

    private Vec3d center() {
        return new Vec3d(this.posX, this.posY + this.height / 2.0D, this.posZ);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            // The server says where it is every tick; in between it carries on the same way
            this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            this.updateRotation();
            return;
        }
        if (this.getItem().isEmpty() || this.ticksExisted > LIFETIME) {
            this.drop();
            return;
        }
        // A Projectile Protection candle of the Enchanted Candles mod halves the motion of what it marks; the speeds the disc flies by
        // for the rest of its flight, there and back, are its own
        NBTTagCompound data = this.getEntityData();
        if (data.getBoolean(CANDLE_SLOWED) && !data.getBoolean(CANDLE_SLOW_TAKEN)) {
            data.setBoolean(CANDLE_SLOW_TAKEN, true);
            this.speed *= 0.5D;
            this.returnSpeed *= 0.5D;
        }
        EntityPlayer owner = this.getOwner();
        if (this.returning) {
            this.flyHome(owner);
        } else {
            this.flyOut(owner);
        }
        this.updateRotation();
    }

    private void flyOut(@Nullable EntityPlayer owner) {
        if (this.isInsideBlock()) {
            this.turnBack(0.0D);
            return;
        }
        this.easeOff();
        Vec3d start = this.center();
        Vec3d end = start.add(this.motionX, this.motionY, this.motionZ);
        RayTraceResult block = this.world.rayTraceBlocks(start, end, false, true, false);
        Vec3d reached = block == null ? end : block.hitVec;
        this.hitEntities(start, reached, owner);
        if (this.isDead) {
            return;
        }
        this.rangeLeft -= start.distanceTo(reached);
        if (block != null && block.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.hitBlock(block, end, owner);
        } else {
            this.moveTo(end);
        }
        if (this.rangeLeft <= 0.0D && !this.returning && !this.isDead) {
            this.turnBack(this.currentSpeed());
        }
    }

    private void easeOff() {
        double brakingDistance = this.speed * EASE_TICKS / 2.0D;
        double ease = MathHelper.clamp(Math.sqrt(Math.max(0.0D, this.rangeLeft) / brakingDistance), MIN_EASE, 1.0D);
        this.setSpeed(this.speed * ease);
    }

    private double currentSpeed() {
        return Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
    }

    private void setSpeed(double newSpeed) {
        double scale = newSpeed / Math.max(1.0E-4D, this.currentSpeed());
        this.motionX *= scale;
        this.motionY *= scale;
        this.motionZ *= scale;
    }

    private void flyHome(@Nullable EntityPlayer owner) {
        if (owner == null || !owner.isEntityAlive() || owner.world != this.world || this.getDistanceSq(owner) > 64.0D * 64.0D) {
            this.drop();
            return;
        }
        Vec3d start = this.center();
        Vec3d target = new Vec3d(owner.posX, owner.posY + owner.getEyeHeight() * 0.8D, owner.posZ);
        Vec3d way = target.subtract(start);
        double distance = way.length();
        // It picks up speed as steadily as it lost it, reaching full speed in EASE_TICKS ticks
        this.returnSpeed = Math.min(this.speed, this.returnSpeed + this.speed / EASE_TICKS);
        if (distance < 1.0D + this.returnSpeed) {
            this.catchBy(owner);
            return;
        }
        Vec3d step = way.scale(this.returnSpeed / distance);
        this.motionX = step.x;
        this.motionY = step.y;
        this.motionZ = step.z;
        Vec3d end = start.add(step);
        this.hitEntities(start, end, owner);
        if (!this.isDead) {
            this.moveTo(end);
        }
    }

    private void moveTo(Vec3d center) {
        this.setPosition(center.x, center.y - this.height / 2.0D, center.z);
    }

    private boolean isInsideBlock() {
        BlockPos pos = new BlockPos(this.center());
        IBlockState state = this.world.getBlockState(pos);
        AxisAlignedBB box = state.getCollisionBoundingBox(this.world, pos);
        return box != null && box != Block.NULL_AABB && box.offset(pos).contains(this.center());
    }

    private void turnBack(double startSpeed) {
        this.returning = true;
        this.noClip = true;
        this.speed = Math.max(this.speed, RETURN_SPEED);
        this.returnSpeed = Math.min(startSpeed, this.speed);
        this.hit.clear();
    }

    private void hitEntities(Vec3d start, Vec3d end, @Nullable EntityPlayer owner) {
        AxisAlignedBB sweep = new AxisAlignedBB(start, end).grow(1.0D);
        List<Entity> entities = this.world.getEntitiesInAABBexcluding(this, sweep, EntitySelectors.NOT_SPECTATING);
        for (Entity entity : entities) {
            if (entity == owner || !entity.canBeCollidedWith() || this.hit.contains(entity.getEntityId())
                    || (owner != null && entity.isRidingOrBeingRiddenBy(owner))) {
                continue;
            }
            if (this.returning && this.hitOnTheWayOut.contains(entity.getEntityId()) && this.reboundDamage() <= 0.0F) {
                continue;
            }
            AxisAlignedBB box = entity.getEntityBoundingBox().grow(0.3D);
            if (box.contains(start) || box.calculateIntercept(start, end) != null) {
                this.hitEntity(entity, owner);
                if (this.isDead) {
                    return;
                }
            }
        }
    }

    private float reboundDamage() {
        int level = ModEnchantments.level(ModEnchantments.REBOUND, this.getItem());
        return REBOUND_DAMAGE[MathHelper.clamp(level, 0, REBOUND_DAMAGE.length - 1)];
    }

    private void hitEntity(Entity entity, @Nullable EntityPlayer owner) {
        int id = entity.getEntityId();
        this.hit.add(id);
        ItemStack stack = this.getItem();
        float damage = stack.getItem() instanceof ItemThrowingDisc ? ((ItemThrowingDisc) stack.getItem()).getDamage() : 1.0F;
        if (this.returning) {
            if (this.hitOnTheWayOut.contains(id)) {
                damage *= this.reboundDamage();
            }
        } else {
            this.hitOnTheWayOut.add(id);
        }
        DamageSource source = DamageSource.causeThrownDamage(this, owner == null ? this : owner);
        if (!entity.attackEntityFrom(source, damage)) {
            return;
        }
        if (entity instanceof EntityLivingBase) {
            double horizontal = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (horizontal > 0.0D) {
                entity.addVelocity(this.motionX / horizontal * 0.3D, 0.05D, this.motionZ / horizontal * 0.3D);
            }
            if (owner != null) {
                owner.setLastAttackedEntity(entity);
            }
        }
        this.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 0.8F,
                1.2F + this.rand.nextFloat() * 0.2F);
        this.wear(owner, 1);
    }

    private void hitBlock(RayTraceResult block, Vec3d end, @Nullable EntityPlayer owner) {
        BlockPos pos = block.getBlockPos();
        ForgeEventFactory.onProjectileImpact(this, block);
        IBlockState state = this.world.getBlockState(pos);
        if (state.getBlock() instanceof BlockTarget) {
            ((BlockTarget) state.getBlock()).onHit(this.world, pos, state, block.sideHit, block.hitVec, this);
        }
        if (state.getCollisionBoundingBox(this.world, pos) == Block.NULL_AABB) {
            // It broke what it hit, a decorated pot, and flies on
            this.moveTo(end);
            return;
        }
        EnumFacing side = block.sideHit;
        Vec3d off = block.hitVec.add(side.getXOffset() * 0.05D, side.getYOffset() * 0.05D, side.getZOffset() * 0.05D);
        this.moveTo(off);
        if (isSoft(state)) {
            this.world.playSound(null, pos, state.getBlock().getSoundType(state, this.world, pos, this).getHitSound(), SoundCategory.PLAYERS,
                    0.8F, 1.0F);
            this.turnBack(0.0D);
            return;
        }
        switch (side.getAxis()) {
            case X:
                this.motionX = -this.motionX;
                break;
            case Y:
                this.motionY = -this.motionY;
                break;
            default:
                this.motionZ = -this.motionZ;
        }
        int ricochet = ModEnchantments.level(ModEnchantments.RICOCHET, this.getItem());
        double newSpeed = Math.min(Math.max(MAX_SPEED, Settings.items.throwingDiscs.speed) * this.velocityFactor(), this.speed * (BOUNCE_SPEEDUP + RICOCHET_SPEEDUP * ricochet));
        this.setSpeed(newSpeed);
        this.speed = newSpeed;
        this.rangeLeft += BOUNCE_RANGE;
        this.hit.clear();
        this.world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.2F, 1.8F + this.rand.nextFloat() * 0.2F);
        this.wear(owner, Settings.items.throwingDiscs.bounceWear);
        if (!this.isDead && ++this.bounces >= Settings.items.throwingDiscs.bounces + RICOCHET_BOUNCES * ricochet) {
            this.turnBack(TURN_SPEED);
        }
    }

    private void wear(@Nullable EntityPlayer owner, int amount) {
        ItemStack stack = this.getItem().copy();
        if (owner != null) {
            stack.damageItem(amount, owner);
        } else if (stack.attemptDamageItem(amount, this.rand, null)) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8F, 1.0F);
            this.setDead();
        } else {
            this.setItem(stack);
        }
    }

    private void catchBy(EntityPlayer owner) {
        ItemStack stack = this.getItem().copy();
        // A creative player kept the disc in their hand when they threw it
        if (!owner.capabilities.isCreativeMode && !this.putBack(owner, stack)) {
            owner.dropItem(stack, false);
        }
        owner.onItemPickup(this, 1);
        this.world.playSound(null, owner.posX, owner.posY, owner.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.4F,
                1.6F + this.rand.nextFloat() * 0.3F);
        this.setDead();
    }

    private boolean putBack(EntityPlayer owner, ItemStack stack) {
        if (this.slot == OFF_HAND) {
            if (owner.inventory.offHandInventory.get(0).isEmpty()) {
                owner.inventory.offHandInventory.set(0, stack);
                return true;
            }
        } else if (this.slot >= 0 && this.slot < 9 && owner.inventory.mainInventory.get(this.slot).isEmpty()) {
            owner.inventory.mainInventory.set(this.slot, stack);
            return true;
        }
        return owner.inventory.addItemStackToInventory(stack);
    }

    private void drop() {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            this.entityDropItem(stack.copy(), 0.0F);
        }
        this.setDead();
    }

    private void updateRotation() {
        double horizontal = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180.0D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(this.motionY, horizontal) * (180.0D / Math.PI));
        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }
        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }
    }

    public boolean isReturning() {
        return this.returning;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double size = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D * getRenderDistanceWeight();
        return distance < size * size * 64.0D;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setItem(new ItemStack(compound.getCompoundTag("Item")));
        this.ownerId = compound.hasUniqueId("Owner") ? compound.getUniqueId("Owner") : null;
        this.slot = compound.getInteger("Slot");
        this.rangeLeft = compound.getDouble("Range");
        this.bounces = compound.getInteger("Bounces");
        this.speed = compound.hasKey("Speed") ? compound.getDouble("Speed") : Settings.items.throwingDiscs.speed;
        this.returning = compound.getBoolean("Returning");
        this.returnSpeed = compound.hasKey("ReturnSpeed") ? compound.getDouble("ReturnSpeed") : this.speed;
        this.noClip = this.returning;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
        if (this.ownerId != null) {
            compound.setUniqueId("Owner", this.ownerId);
        }
        compound.setInteger("Slot", this.slot);
        compound.setDouble("Range", this.rangeLeft);
        compound.setInteger("Bounces", this.bounces);
        compound.setDouble("Speed", this.speed);
        compound.setBoolean("Returning", this.returning);
        compound.setDouble("ReturnSpeed", this.returnSpeed);
    }
}
