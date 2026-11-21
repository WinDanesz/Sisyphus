package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.ai.StriderNavigator;
import com.windanesz.sisyphus.item.ItemWarpedFungusOnAStick;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityStrider extends EntityAnimal {

    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(EntityStrider.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SUFFOCATING = EntityDataManager.createKey(EntityStrider.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.createKey(EntityStrider.class, DataSerializers.VARINT);

    private static final UUID SUFFOCATING_ID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
    private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(SUFFOCATING_ID, "Strider suffocating modifier", -0.34D, 1).setSaved(false);
    private static final double LAVA_RIDE_DISTANCE = 50.0D;
    private EntityAIPanic panic;
    private EntityAITempt tempt;
    private boolean panicking;
    private boolean boosting;
    private int boostTime;
    private int totalBoostTime;
    private boolean lavaRide;
    private double lavaRideX;
    private double lavaRideZ;

    public EntityStrider(World world) {
        super(world);
        this.setSize(0.9F, 1.7F);
        this.isImmuneToFire = true;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
    }

    @Nullable
    private static Item breedingItem() {
        Block fungus = ModBlocks.getOrNull("warped_fungus");
        return fungus == null ? null : Item.getItemFromBlock(fungus);
    }

    private static boolean holdsStick(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() instanceof ItemWarpedFungusOnAStick || player.getHeldItemOffhand().getItem() instanceof ItemWarpedFungusOnAStick;
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        return new StriderNavigator(this, world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SADDLED, false);
        this.dataManager.register(SUFFOCATING, false);
        this.dataManager.register(BOOST_TIME, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.175D);
    }

    @Override
    protected void initEntityAI() {
        this.panic = new EntityAIPanic(this, 1.65D) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityStrider.this.panicking = true;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityStrider.this.panicking = false;
            }
        };
        this.tasks.addTask(1, this.panic);
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        Item fungus = breedingItem();
        if (fungus != null) {
            this.tempt = new EntityAITempt(this, 1.4D, fungus, false);
            this.tasks.addTask(3, this.tempt);
        }
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D, 60));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.dataManager.set(SADDLED, saddled);
    }

    public boolean isSuffocating() {
        return this.dataManager.get(SUFFOCATING);
    }

    private void setSuffocating(boolean suffocating) {
        if (suffocating != this.isSuffocating()) {
            this.dataManager.set(SUFFOCATING, suffocating);
            IAttributeInstance speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            speed.removeModifier(SUFFOCATING_ID);
            if (suffocating) {
                speed.applyModifier(SUFFOCATING_MODIFIER);
            }
        }
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? 0.85F : 1.6F;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.boosting && this.boostTime++ > this.totalBoostTime) {
            this.boosting = false;
        }
        if (this.world.isRemote) {
            return;
        }
        // Happy sounds when it is tempted by warped fungus, and cries when it runs away
        if (this.tempt != null && this.tempt.isRunning() && this.rand.nextInt(140) == 0) {
            this.playSound(ModSounds.event("entity.strider.happy"), 1.0F, 1.0F);
        } else if (this.panicking && this.rand.nextInt(60) == 0) {
            this.playSound(ModSounds.event("entity.strider.retreat"), 1.0F, 1.0F);
        }
        boolean warm = this.isInLava() || (this.getRidingEntity() instanceof EntityStrider && this.getRidingEntity().isInLava());
        this.setSuffocating(!warm);
        if (this.isWet()) {
            this.attackEntityFrom(DamageSource.DROWN, 1.0F);
        }
        Entity rider = this.getControllingPassenger();
        if (rider instanceof EntityPlayerMP && this.ticksExisted % 20 == 0) {
            AdvancementHelper.grant((EntityPlayerMP) rider, "this_boat_has_legs");
        }
        this.trackLavaRide(rider);
    }

    private void trackLavaRide(Entity rider) {
        if (!(rider instanceof EntityPlayerMP) || this.world.provider.getDimension() != 0 || !this.isInLava()) {
            this.lavaRide = false;
            return;
        }
        if (!this.lavaRide) {
            this.lavaRide = true;
            this.lavaRideX = this.posX;
            this.lavaRideZ = this.posZ;
            return;
        }
        double dx = this.posX - this.lavaRideX;
        double dz = this.posZ - this.lavaRideZ;
        if (dx * dx + dz * dz >= LAVA_RIDE_DISTANCE * LAVA_RIDE_DISTANCE) {
            AdvancementHelper.grant((EntityPlayerMP) rider, "feels_like_home");
            this.lavaRide = false;
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (BOOST_TIME.equals(key) && this.world.isRemote) {
            this.boosting = true;
            this.boostTime = 0;
            this.totalBoostTime = this.dataManager.get(BOOST_TIME);
        }
        super.notifyDataManagerChange(key);
    }

    /**
     * False if it is boosting already.
     */
    public boolean boost() {
        if (this.boosting) {
            return false;
        }
        this.boosting = true;
        this.boostTime = 0;
        this.totalBoostTime = this.getRNG().nextInt(841) + 140;
        this.dataManager.set(BOOST_TIME, this.totalBoostTime);
        return true;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase rider = (EntityLivingBase) this.getControllingPassenger();
            this.rotationYaw = rider.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = rider.rotationPitch * 0.5F;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.rotationYaw;
            this.stepHeight = 1.0F;
            this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
            if (this.canPassengerSteer()) {
                float speed = (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.225F;
                if (this.boosting) {
                    speed += speed * 1.15F * MathHelper.sin((float) this.boostTime / (float) this.totalBoostTime * (float) Math.PI);
                }
                this.setAIMoveSpeed(speed);
                this.moveStrider(0.0F, 0.0F, 1.0F);
            } else {
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }
            this.updateLimbs();
        } else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            this.moveStrider(strafe, vertical, forward);
        }
    }

    private void moveStrider(float strafe, float vertical, float forward) {
        if (!this.isInLava()) {
            super.travel(strafe, vertical, forward);
            return;
        }
        BlockPos feet = new BlockPos(this.posX, this.posY, this.posZ);
        boolean lavaAbove = this.world.getBlockState(feet.up()).getMaterial() == Material.LAVA;
        if (!lavaAbove && this.posY >= feet.getY() + 0.5D - 1.0E-3D) {
            float friction = 0.546F;
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed() * (0.16277136F / (friction * friction * friction)));
            this.move(MoverType.SELF, this.motionX, 0.0D, this.motionZ);
            this.motionY = 0.0D;
            this.motionX *= friction;
            this.motionZ *= friction;
            this.onGround = true;
        } else {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY = this.motionY * 0.5D + 0.05D;
            this.motionZ *= 0.5D;
        }
        this.updateLimbs();
    }

    private void updateLimbs() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double dx = this.posX - this.prevPosX;
        double dz = this.posZ - this.prevPosZ;
        float amount = MathHelper.sqrt(dx * dx + dz * dz) * 4.0F;
        if (amount > 1.0F) {
            amount = 1.0F;
        }
        this.limbSwingAmount += (amount - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    public boolean canBeSteered() {
        Entity rider = this.getControllingPassenger();
        return rider instanceof EntityPlayer && this.isSaddled() && holdsStick((EntityPlayer) rider);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public double getMountedYOffset() {
        return this.height - 0.2D;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.SADDLE && !this.isSaddled() && !this.isChild()) {
            if (!this.world.isRemote) {
                held.shrink(1);
                this.setSaddled(true);
                this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.event("entity.strider.saddle"), SoundCategory.NEUTRAL, 0.5F, 1.0F);
            }
            return true;
        }
        boolean feeding = this.isBreedingItem(held);
        if (super.processInteract(player, hand)) {
            if (feeding) {
                this.playSound(ModSounds.event("entity.strider.eat"), 1.0F, 1.0F);
            }
            return true;
        }
        if (this.isSaddled() && !this.isChild() && !this.world.isRemote && !this.isBeingRidden() && !this.isBreedingItem(held)) {
            player.startRiding(this);
            return true;
        }
        return false;
    }

    @Override
    public void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        if (this.isSaddled()) {
            this.dropItem(Items.SADDLE, 1);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Item fungus = breedingItem();
        return fungus != null && stack.getItem() == fungus;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        return new EntityStrider(this.world);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, data);
        if (!this.isChild() && !this.isBeingRidden()) {
            if (this.rand.nextInt(30) == 0) {
                EntityStrider baby = new EntityStrider(this.world);
                baby.setGrowingAge(-24000);
                baby.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                baby.onInitialSpawn(difficulty, null);
                this.world.spawnEntity(baby);
                baby.startRiding(this, true);
            } else if (this.rand.nextInt(10) == 0) {
                EntityPigZombie zombie = new EntityPigZombie(this.world);
                zombie.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                zombie.onInitialSpawn(difficulty, null);
                this.world.spawnEntity(zombie);
                zombie.startRiding(this, true);
                this.setSaddled(true);
            }
        }
        return result;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA && this.world.isAirBlock(new BlockPos(this).up());
    }

    @Override
    public boolean isNotColliding() {
        // The game's check refuses any place with liquid in it, and a strider spawns in lava
        return this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        // Lava is a good place to be, and the way to it is not wet
        return this.world.getBlockState(pos).getMaterial() == Material.LAVA ? 10.0F : 0.0F;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isSaddled();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Saddle", this.isSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSaddled(compound.getBoolean("Saddle"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // Silent while it runs away or follows the fungus, as it has other sounds for those
        return this.panicking || (this.tempt != null && this.tempt.isRunning()) ? null : ModSounds.event("entity.strider.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("entity.strider.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("entity.strider.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.event(this.isInLava() ? "entity.strider.step_lava" : "entity.strider.step"), 1.0F, 1.0F);
    }

    @Override
    protected boolean canDropLoot() {
        return true;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return this.isChild() ? 0 : 1 + this.rand.nextInt(3);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/strider");
    }
}
