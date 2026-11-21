package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityFox extends EntityAnimal {

    private static final DataParameter<Integer> FOX_TYPE = EntityDataManager.createKey(EntityFox.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(EntityFox.class, DataSerializers.BYTE);

    private static final int SITTING = 1;
    private static final int CROUCHING = 2;
    private static final int SLEEPING = 4;
    private static final int INTERESTED = 8;
    private static final int POUNCING = 16;
    private static final int FACEPLANTED = 32;

    private UUID trustedPlayer;
    private int eatTicks;
    private int crouchTicks;
    private int faceplantTicks;
    private float gait;

    public EntityFox(World world) {
        super(world);
        this.setSize(0.6F, 0.7F);
        this.setCanPickUpLoot(true);
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        if (Settings.mobs.enableFoxes && !event.getWorld().isRemote && event.getEntity() instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) event.getEntity();
            wolf.targetTasks.addTask(6, new EntityAITargetNonTamed<EntityFox>(wolf, EntityFox.class, false, fox -> fox != null));
        }
    }

    @SubscribeEvent
    public static void onBaby(BabyEntitySpawnEvent event) {
        if (event.getChild() instanceof EntityFox) {
            EntityPlayer player = event.getCausedByPlayer();
            if (player != null) {
                ((EntityFox) event.getChild()).trust(player);
            }
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FOX_TYPE, 0);
        this.dataManager.register(FLAGS, (byte) 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AISleep(this));
        this.tasks.addTask(2, new EntityAIPanic(this, 2.2D));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        Item berries = ModItems.get("sweet_berries");
        if (berries != null && berries != Items.AIR) {
            this.tasks.addTask(4, new EntityAITempt(this, 1.2D, berries, false));
        }
        this.tasks.addTask(5, new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class,
                player -> player != null && !EntityFox.this.trusts(player) && !EntityFox.this.isChild() && !EntityFox.this.isSleeping(),
                16.0F, 1.6D, 1.4D));
        this.tasks.addTask(6, new AIPounce(this));
        this.tasks.addTask(6, new AISit(this));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new EntityAIAttackMelee(this, 1.2D, true));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityChicken>(this, EntityChicken.class, 10, true, false, chicken -> !this.isSleeping() && !this.world.isDaytime()));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityRabbit>(this, EntityRabbit.class, 10, true, false, rabbit -> !this.isSleeping() && !this.world.isDaytime()));
    }

    private boolean getFoxFlag(int flag) {
        return (this.dataManager.get(FLAGS) & flag) != 0;
    }

    private void setFoxFlag(int flag, boolean value) {
        byte current = this.dataManager.get(FLAGS);
        this.dataManager.set(FLAGS, (byte) (value ? current | flag : current & ~flag));
    }

    public boolean isSitting() {
        return this.getFoxFlag(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.setFoxFlag(SITTING, sitting);
    }

    public boolean isCrouching() {
        return this.getFoxFlag(CROUCHING);
    }

    public boolean isSleeping() {
        return this.getFoxFlag(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.setFoxFlag(SLEEPING, sleeping);
    }

    public boolean isInterested() {
        return this.getFoxFlag(INTERESTED);
    }

    public boolean isPouncing() {
        return this.getFoxFlag(POUNCING);
    }

    public boolean isFaceplanted() {
        return this.getFoxFlag(FACEPLANTED);
    }

    public boolean isFullyCrouched() {
        return this.isCrouching() && this.crouchTicks >= 15;
    }

    /**
     * In blocks per second, from the last tick.
     */
    public double groundSpeed() {
        double dx = this.posX - this.prevPosX;
        double dz = this.posZ - this.prevPosZ;
        return Math.sqrt(dx * dx + dz * dz) * 20.0D;
    }

    /**
     * Eased on the client, raw on the server.
     */
    public double gaitSpeed() {
        return this.world.isRemote ? this.gait : this.groundSpeed();
    }

    /**
     * 0 for red, 1 for white.
     */
    public int getFoxType() {
        return this.dataManager.get(FOX_TYPE);
    }

    public void setFoxType(int type) {
        this.dataManager.set(FOX_TYPE, type);
    }

    public boolean trusts(EntityPlayer player) {
        return this.trustedPlayer != null && this.trustedPlayer.equals(player.getUniqueID());
    }

    public void trust(EntityPlayer player) {
        this.trustedPlayer = player.getUniqueID();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        BlockPos pos = new BlockPos(this);
        boolean snowy = BiomeDictionary.hasType(this.world.getBiome(pos), BiomeDictionary.Type.SNOWY);
        this.setFoxType(snowy ? 1 : 0);
        return super.onInitialSpawn(difficulty, data);
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.isFaceplanted() || super.isMovementBlocked();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            double raw = this.groundSpeed();
            this.gait += ((raw < 0.4D ? 0.0F : (float) raw) - this.gait) * 0.25F;
            return;
        }
        if (!this.isEntityAlive()) {
            return;
        }

        // Sneaks up on its prey, watching it with its head to the side, and leaps once it has crouched long enough
        EntityLivingBase target = this.getAttackTarget();
        boolean stalking = target != null && !this.isPouncing() && !this.isFaceplanted() && this.getDistanceSq(target) > 9.0D && target.getDistanceSq(this) < 256.0D;
        this.setFoxFlag(CROUCHING, stalking);
        this.setFoxFlag(INTERESTED, stalking);
        this.crouchTicks = stalking ? this.crouchTicks + 1 : 0;

        if (this.isPouncing()) {
            this.updatePounce();
        }
        if (this.isFaceplanted()) {
            this.rotationPitch = 60.0F;
            if (--this.faceplantTicks <= 0) {
                this.setFoxFlag(FACEPLANTED, false);
                this.rotationPitch = 0.0F;
            }
        }

        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty()) {
            // Picks up food that lies around
            if (!this.isSleeping() && this.ticksExisted % 10 == 0) {
                for (EntityItem item : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(1.0D, 0.5D, 1.0D))) {
                    if (!item.isDead && item.getItem().getItem() instanceof ItemFood) {
                        ItemStack single = item.getItem().splitStack(1);
                        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, single);
                        if (item.getItem().isEmpty()) {
                            item.setDead();
                        }
                        this.eatTicks = 0;
                        break;
                    }
                }
            }
        } else if (++this.eatTicks > 200) {
            // Eats what it carries, which heals it
            if (held.getItem() instanceof ItemFood) {
                this.heal(((ItemFood) held.getItem()).getHealAmount(held) / 2.0F);
            }
            this.playSound(ModSounds.ENTITY_FOX_EAT, 1.0F, 1.0F);
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.eatTicks = 0;
        }
    }

    private void pounce(EntityLivingBase target) {
        this.setFoxFlag(POUNCING, true);
        this.setFoxFlag(CROUCHING, false);
        this.setFoxFlag(INTERESTED, false);
        this.crouchTicks = 0;
        this.getNavigator().clearPath();
        double dx = target.posX - this.posX;
        double dz = target.posZ - this.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 1.0E-4D) {
            dx = dx / length * 0.8D + this.motionX * 0.2D;
            dz = dz / length * 0.8D + this.motionZ * 0.2D;
        }
        this.motionX = dx;
        this.motionY = 0.5D;
        this.motionZ = dz;
        this.isAirBorne = true;
        this.playSound(ModSounds.ENTITY_FOX_AMBIENT, 1.0F, 1.5F);
    }

    private void updatePounce() {
        double horizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationPitch = (float) -(Math.atan2(this.motionY, horizontal) * 180.0D / Math.PI);
        EntityLivingBase target = this.getAttackTarget();
        if (target != null && this.getDistanceSq(target) <= 4.0D) {
            this.attackEntityAsMob(target);
        }
        if (this.onGround && this.motionY <= 0.0D) {
            boolean snow = this.world.getBlockState(this.getPosition()).getBlock() == Blocks.SNOW_LAYER
                    || this.world.getBlockState(this.getPosition().down()).getBlock() == Blocks.SNOW;
            this.setFoxFlag(POUNCING, false);
            if (snow && this.rotationPitch > 0.0F) {
                this.setFoxFlag(FACEPLANTED, true);
                this.faceplantTicks = 40;
                this.setAttackTarget(null);
                this.rotationPitch = 60.0F;
            } else {
                this.rotationPitch = 0.0F;
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isSleeping()) {
            this.setSleeping(false);
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (hit) {
            this.applyEnchantments(this, target);
            this.playSound(ModSounds.ENTITY_FOX_BITE, 1.0F, 1.0F);
        }
        return hit;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        return super.processInteract(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == ModItems.get("sweet_berries");
    }

    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        EntityFox pup = new EntityFox(this.world);
        boolean snow = other instanceof EntityFox && this.rand.nextBoolean() ? ((EntityFox) other).getFoxType() == 1 : this.getFoxType() == 1;
        pup.setFoxType(snow ? 1 : 0);
        return pup;
    }

    @Override
    protected boolean canDespawn() {
        return !this.hasCustomName() && !this.isChild();
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? ModSounds.ENTITY_FOX_SLEEP : ModSounds.ENTITY_FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_FOX_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("FoxType", this.getFoxType());
        compound.setBoolean("Sitting", this.isSitting());
        compound.setBoolean("Sleeping", this.isSleeping());
        if (this.trustedPlayer != null) {
            compound.setUniqueId("Trusted", this.trustedPlayer);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFoxType(compound.getInteger("FoxType"));
        this.setSitting(compound.getBoolean("Sitting"));
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.trustedPlayer = compound.hasUniqueId("Trusted") ? compound.getUniqueId("Trusted") : null;
    }

    static class AISit extends EntityAIBase {

        private final EntityFox fox;
        private int ticks;

        AISit(EntityFox fox) {
            this.fox = fox;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return this.fox.onGround && !this.fox.isInWater() && !this.fox.isSleeping() && this.fox.getAttackTarget() == null
                    && this.fox.getRNG().nextInt(500) == 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return --this.ticks > 0 && this.fox.getAttackTarget() == null && this.fox.world.getClosestPlayerToEntity(this.fox, 6.0D) == null;
        }

        @Override
        public void startExecuting() {
            this.ticks = 100 + this.fox.getRNG().nextInt(200);
            this.fox.getNavigator().clearPath();
            this.fox.setSitting(true);
        }

        @Override
        public void resetTask() {
            this.fox.setSitting(false);
        }
    }

    static class AISleep extends EntityAIBase {

        private final EntityFox fox;
        private int cooldown;

        AISleep(EntityFox fox) {
            this.fox = fox;
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            return Settings.mobs.enableFoxes && this.fox.world.isDaytime() && this.fox.onGround && !this.fox.isInWater()
                    && this.fox.getAttackTarget() == null && !this.fox.isChild() && this.fox.getHeldItemMainhand().isEmpty()
                    && this.fox.getRNG().nextInt(200) == 0 && this.fox.world.getClosestPlayerToEntity(this.fox, 8.0D) == null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.fox.world.isDaytime() && this.fox.getAttackTarget() == null && !this.fox.isInWater()
                    && this.fox.world.getClosestPlayerToEntity(this.fox, 6.0D) == null;
        }

        @Override
        public void startExecuting() {
            this.fox.getNavigator().clearPath();
            this.fox.setSleeping(true);
        }

        @Override
        public void resetTask() {
            this.fox.setSleeping(false);
            this.cooldown = 140;
        }
    }

    static class AIPounce extends EntityAIBase {

        private final EntityFox fox;

        AIPounce(EntityFox fox) {
            this.fox = fox;
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = this.fox.getAttackTarget();
            if (target == null || !target.isEntityAlive() || !this.fox.isFullyCrouched() || !this.fox.onGround) {
                return false;
            }
            double distance = this.fox.getDistanceSq(target);
            return distance > 6.0D && distance < 20.0D && this.fox.getEntitySenses().canSee(target);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            this.fox.pounce(this.fox.getAttackTarget());
        }
    }
}
