package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityAxolotl extends EntityAnimal {

    public static final DamageSource DRY_OUT = new DamageSource("dryout");
    public static final int MAX_MOISTURE = 6000;
    public static final int REHYDRATE = 1800;
    public static final int PLAY_DEAD_TICKS = 200;
    public static final int HUNTING_COOLDOWN = 2400;
    public static final int RARE_VARIANT_CHANCE = 1200;
    public static final double PLAYER_REGEN_RANGE = 20.0D;
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityAxolotl.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> PLAYING_DEAD = EntityDataManager.createKey(EntityAxolotl.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityAxolotl.class, DataSerializers.BOOLEAN);
    private static final double HUNT_RANGE = 8.0D;

    private static final double LAND_SPEED = 0.15D;
    private static final float WATER_SPEED = 0.1F;

    private static final String[] NAMES = {"lucy", "wild", "gold", "cyan", "blue"};
    private final PathNavigate landNavigator;
    private final PathNavigate waterNavigator;
    private int moisture = MAX_MOISTURE;
    private int playDeadTicks;
    private int huntingCooldown;
    private EntityLivingBase lastTarget;
    private int movingTicks;
    /**
     * The look helper levels every mob out each tick, so the swim pitch is put back after it.
     */
    private float swimPitch;
    public EntityAxolotl(World world) {
        super(world);
        this.setSize(0.75F, 0.42F);
        this.stepHeight = 1.0F;
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.landNavigator = this.navigator;
        this.waterNavigator = new PathNavigateSwimmer(this, world);
        this.moveHelper = new MoveHelper(this);
    }

    private static boolean isAlwaysHostile(Entity entity) {
        return entity instanceof EntityGuardian;
    }

    private static boolean isHuntTarget(Entity entity) {
        return entity instanceof EntitySquid || entity instanceof EntityFishBase;
    }

    public static void spawnFromBucket(World world, BlockPos pos, ItemStack bucket) {
        EntityAxolotl axolotl = new EntityAxolotl(world);
        NBTTagCompound tag = bucket.getTagCompound();
        if (tag != null) {
            axolotl.setVariant(Variant.byId(tag.getInteger("Variant")));
            axolotl.setGrowingAge(tag.getInteger("Age"));
            if (tag.hasKey("Health")) {
                axolotl.setHealth(tag.getFloat("Health"));
            }
            axolotl.huntingCooldown = tag.getInteger("HuntingCooldown");
            axolotl.setNoAI(tag.getBoolean("NoAI"));
            axolotl.setSilent(tag.getBoolean("Silent"));
        }
        if (bucket.hasDisplayName()) {
            axolotl.setCustomNameTag(bucket.getDisplayName());
        }
        axolotl.setFromBucket(true);
        axolotl.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        world.spawnEntity(axolotl);
    }

    static boolean isUnderground(World world, BlockPos pos) {
        BlockPos.MutableBlockPos up = new BlockPos.MutableBlockPos(pos);
        for (int y = pos.getY() + 1; y < world.getHeight(); y++) {
            up.setY(y);
            IBlockState state = world.getBlockState(up);
            if (state.isOpaqueCube()) {
                return true;
            }
            if (y > world.getSeaLevel() && world.canSeeSky(up)) {
                return false;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityAxolotl && ((EntityAxolotl) event.getTarget()).isPlayingDead()
                && event.getEntityLiving() instanceof EntityLiving) {
            ((EntityLiving) event.getEntityLiving()).setAttackTarget(null);
        }
    }

    /**
     * Runs last, so a bottle that another mod cancels does not wet them.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionImpact(ProjectileImpactEvent.Throwable event) {
        if (!(event.getThrowable() instanceof EntityPotion) || event.getThrowable().world.isRemote) {
            return;
        }
        EntityPotion potion = (EntityPotion) event.getThrowable();
        ItemStack stack = potion.getPotion();
        if (PotionUtils.getPotionFromItem(stack) != PotionTypes.WATER || !PotionUtils.getEffectsFromStack(stack).isEmpty()) {
            return;
        }
        for (EntityAxolotl axolotl : potion.world.getEntitiesWithinAABB(EntityAxolotl.class, potion.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D))) {
            axolotl.rehydrate();
        }
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        PathNavigateGround navigator = new PathNavigateGround(this, world);
        navigator.setCanSwim(true);
        return navigator;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(PLAYING_DEAD, false);
        this.dataManager.register(FROM_BUCKET, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AIPlayDead(this));
        this.tasks.addTask(1, new EntityAIMate(this, 0.2D));
        this.tasks.addTask(2, new AITempt(this));
        this.tasks.addTask(2, new EntityAIFollowParent(this, 0.6D));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 0.6D, true));
        this.tasks.addTask(4, new AIFindWater(this));
        this.tasks.addTask(5, new AIWander(this));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 10, true, false,
                target -> target != null && this.canHunt(target)));
    }

    public Variant getVariant() {
        return Variant.byId(this.dataManager.get(VARIANT));
    }

    public void setVariant(Variant variant) {
        this.dataManager.set(VARIANT, variant.ordinal());
    }

    public boolean isPlayingDead() {
        return this.dataManager.get(PLAYING_DEAD);
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    /**
     * Client side only.
     */
    public boolean isMovingForAnimation() {
        return this.movingTicks > 0;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (!this.world.isRemote && this.isEntityAlive() && !this.isAIDisabled()) {
            if (this.isWet()) {
                this.moisture = MAX_MOISTURE;
            } else if (--this.moisture <= -20) {
                // As the air of a drowning mob: once it has dried out, it is hurt every second
                this.moisture = 0;
                this.attackEntityFrom(DRY_OUT, 2.0F);
            }
        }
    }

    public void rehydrate() {
        this.moisture = Math.min(this.moisture + REHYDRATE, MAX_MOISTURE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) {
            double dx = this.posX - this.prevPosX;
            double dy = this.posY - this.prevPosY;
            double dz = this.posZ - this.prevPosZ;
            boolean turned = Math.abs(MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw)) > 1.0F
                    || Math.abs(this.rotationPitch - this.prevRotationPitch) > 1.0F;
            if (dx * dx + dy * dy + dz * dz > 1.0E-4D || turned) {
                this.movingTicks = 4;
            } else if (this.movingTicks > 0) {
                this.movingTicks--;
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote) {
            // Swim with the swimming path finder in water, walk with the ground one on land
            PathNavigate wanted = this.isInWater() ? this.waterNavigator : this.landNavigator;
            if (this.navigator != wanted) {
                this.navigator.clearPath();
                this.navigator = wanted;
            }
        }
        super.onLivingUpdate();
        if (!this.world.isRemote && this.isInWater()) {
            this.rotationPitch = this.swimPitch;
        }
        if (this.isPlayingDead()) {
            // It does not look around while it plays dead
            this.rotationYawHead = this.renderYawOffset;
        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (this.playDeadTicks > 0) {
            this.playDeadTicks--;
        }
        this.dataManager.set(PLAYING_DEAD, this.playDeadTicks > 0);
        if (this.huntingCooldown > 0) {
            this.huntingCooldown--;
        }

        EntityLivingBase target = this.getAttackTarget();
        if (this.lastTarget != null && target != this.lastTarget) {
            this.onStopAttacking(this.lastTarget);
            if (target == null) {
                this.huntingCooldown = HUNTING_COOLDOWN;
            }
        }
        this.lastTarget = target;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isServerWorld() && this.isInWater()) {
            // No gravity in water: it hovers where it stops, as it does in the game
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    boolean canHunt(EntityLivingBase target) {
        return !this.isPlayingDead() && !this.isInLove() && target.isInWater() && this.getDistanceSq(target) <= HUNT_RANGE * HUNT_RANGE
                && (isAlwaysHostile(target) || this.huntingCooldown <= 0 && isHuntTarget(target));
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (hit) {
            this.applyEnchantments(this, target);
            this.playSound(ModSounds.event("entity.axolotl.attack"), 1.0F, 1.0F);
        }
        return hit;
    }

    private void onStopAttacking(EntityLivingBase target) {
        if (target.isEntityAlive() && target.getHealth() > 0.0F) {
            return;
        }
        DamageSource last = target.getLastDamageSource();
        if (last != null && last.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) last.getTrueSource();
            if (this.getEntityBoundingBox().grow(PLAYER_REGEN_RANGE).intersects(player.getEntityBoundingBox())) {
                this.applySupportingEffects(player);
            }
        }
    }

    public void applySupportingEffects(EntityPlayer player) {
        PotionEffect regeneration = player.getActivePotionEffect(MobEffects.REGENERATION);
        if (regeneration == null || regeneration.getDuration() <= 2399) {
            int left = regeneration != null ? regeneration.getDuration() : 0;
            player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, Math.min(2400, 100 + left), 0));
            if (player instanceof EntityPlayerMP) {
                AdvancementHelper.grant((EntityPlayerMP) player, "the_healing_power_of_friendship");
            }
        }
        player.removePotionEffect(MobEffects.MINING_FATIGUE);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        float health = this.getHealth();
        if (!this.world.isRemote && !this.isAIDisabled() && this.rand.nextInt(3) == 0
                && (this.rand.nextInt(3) < amount || health / this.getMaxHealth() < 0.5F) && amount < health && this.isInWater()
                && (source.getTrueSource() != null || source.getImmediateSource() != null) && !this.isPlayingDead()) {
            this.playDeadTicks = PLAY_DEAD_TICKS;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH && stack.getMetadata() == ItemFishFood.FishType.CLOWNFISH.getMetadata();
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        EntityAxolotl baby = new EntityAxolotl(this.world);
        Variant variant;
        if (this.rand.nextInt(RARE_VARIANT_CHANCE) == 0) {
            variant = Variant.BLUE;
        } else {
            variant = this.rand.nextBoolean() || !(mate instanceof EntityAxolotl) ? this.getVariant() : ((EntityAxolotl) mate).getVariant();
        }
        baby.setVariant(variant);
        baby.enablePersistence();
        return baby;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(ModSounds.event("item.bucket.fill_axolotl"), 1.0F, 1.0F);
            if (!this.world.isRemote) {
                ItemStack bucket = this.toBucket();
                if (player.capabilities.isCreativeMode) {
                    if (!player.inventory.hasItemStack(bucket)) {
                        player.inventory.addItemStackToInventory(bucket);
                    }
                } else {
                    held.shrink(1);
                    if (held.isEmpty()) {
                        player.setHeldItem(hand, bucket);
                    } else if (!player.inventory.addItemStackToInventory(bucket)) {
                        player.dropItem(bucket, false);
                    }
                }
                if (player instanceof EntityPlayerMP) {
                    AdvancementHelper.grant((EntityPlayerMP) player, "the_cutest_predator");
                }
                this.setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    public ItemStack toBucket() {
        ItemStack bucket = new ItemStack(Item.getByNameOrId("sisyphus:axolotl_bucket"));
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Variant", this.getVariant().ordinal());
        tag.setInteger("Age", this.getGrowingAge());
        tag.setFloat("Health", this.getHealth());
        tag.setInteger("HuntingCooldown", this.huntingCooldown);
        if (this.isAIDisabled()) {
            tag.setBoolean("NoAI", true);
        }
        if (this.isSilent()) {
            tag.setBoolean("Silent", true);
        }
        bucket.setTagCompound(tag);
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        return bucket;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(difficulty, data);
        GroupData group = data instanceof GroupData ? (GroupData) data : new GroupData(Variant.common(this.rand), Variant.common(this.rand));
        if (group.size >= 2) {
            this.setGrowingAge(-24000);
        }
        group.size++;
        this.setVariant(group.variants[this.rand.nextInt(group.variants.length)]);
        return group;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        return this.world.provider.isSurfaceWorld() && pos.getY() < this.world.getSeaLevel()
                && this.world.getBlockState(pos).getMaterial() == Material.WATER && isUnderground(this.world, pos);
    }

    @Override
    public boolean isNotColliding() {
        // As other water mobs: the water it spawns in is not in the way
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant().ordinal());
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setInteger("Moisture", this.moisture);
        compound.setInteger("PlayDeadTicks", this.playDeadTicks);
        compound.setInteger("HuntingCooldown", this.huntingCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(Variant.byId(compound.getInteger("Variant")));
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.moisture = compound.hasKey("Moisture") ? compound.getInteger("Moisture") : MAX_MOISTURE;
        this.playDeadTicks = compound.getInteger("PlayDeadTicks");
        this.huntingCooldown = compound.getInteger("HuntingCooldown");
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isPlayingDead()) {
            return null;
        }
        return ModSounds.event(this.isInWater() ? "entity.axolotl.idle_water" : "entity.axolotl.idle_air");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("entity.axolotl.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("entity.axolotl.death");
    }

    @Override
    protected SoundEvent getSwimSound() {
        return ModSounds.event("entity.axolotl.swim");
    }

    @Override
    protected SoundEvent getSplashSound() {
        return ModSounds.event("entity.axolotl.splash");
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    public enum Variant {
        LUCY, WILD, GOLD, CYAN, BLUE;

        public static Variant byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : LUCY;
        }

        static Variant common(Random random) {
            return values()[random.nextInt(4)];
        }

        public String getName() {
            return NAMES[this.ordinal()];
        }

        public boolean isCommon() {
            return this != BLUE;
        }
    }

    private static final class GroupData implements IEntityLivingData {
        final Variant[] variants;
        int size;

        GroupData(Variant... variants) {
            this.variants = variants;
        }
    }

    static class MoveHelper extends EntityMoveHelper {

        private final EntityAxolotl axolotl;

        MoveHelper(EntityAxolotl axolotl) {
            super(axolotl);
            this.axolotl = axolotl;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.axolotl.isPlayingDead()) {
                this.action = Action.WAIT;
                this.stop();
                return;
            }
            if (!this.axolotl.isInWater()) {
                double speed = this.speed;
                this.speed = speed * LAND_SPEED;
                super.onUpdateMoveHelper();
                this.speed = speed;
                return;
            }
            if (this.action != Action.MOVE_TO || this.axolotl.getNavigator().noPath()) {
                this.stop();
                // Level out again while it hovers
                this.axolotl.swimPitch = this.limitAngle(this.axolotl.swimPitch, 0.0F, 5.0F);
                return;
            }
            this.action = Action.WAIT;
            double dx = this.posX - this.axolotl.posX;
            double dy = this.posY - this.axolotl.posY;
            double dz = this.posZ - this.axolotl.posZ;
            if (dx * dx + dy * dy + dz * dz < 2.5E-7D) {
                this.stop();
                return;
            }
            float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            this.axolotl.rotationYaw = this.limitAngle(this.axolotl.rotationYaw, yaw, 10.0F);
            this.axolotl.renderYawOffset = this.axolotl.rotationYaw;
            this.axolotl.rotationYawHead = this.axolotl.rotationYaw;
            float speed = (float) (this.speed * this.axolotl.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dy) > 1.0E-5D || horizontal > 1.0E-5D) {
                float pitch = -(float) (MathHelper.atan2(dy, horizontal) * (180D / Math.PI));
                pitch = MathHelper.clamp(MathHelper.wrapDegrees(pitch), -85.0F, 85.0F);
                this.axolotl.swimPitch = this.limitAngle(this.axolotl.swimPitch, pitch, 5.0F);
            }
            float radians = this.axolotl.swimPitch * 0.017453292F;
            this.axolotl.setAIMoveSpeed(speed * WATER_SPEED);
            this.axolotl.moveForward = MathHelper.cos(radians) * speed;
            this.axolotl.moveVertical = -MathHelper.sin(radians) * speed;
        }

        private void stop() {
            this.axolotl.setAIMoveSpeed(0.0F);
            this.axolotl.moveForward = 0.0F;
            this.axolotl.moveVertical = 0.0F;
            this.axolotl.moveStrafing = 0.0F;
        }
    }

    static class AIPlayDead extends EntityAIBase {

        private final EntityAxolotl axolotl;

        AIPlayDead(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return this.axolotl.playDeadTicks > 0 && this.axolotl.isInWater();
        }

        @Override
        public void startExecuting() {
            this.axolotl.getNavigator().clearPath();
            this.axolotl.setAttackTarget(null);
            this.axolotl.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, PLAY_DEAD_TICKS, 0));
            List<EntityLiving> around = this.axolotl.world.getEntitiesWithinAABB(EntityLiving.class, this.axolotl.getEntityBoundingBox().grow(32.0D),
                    mob -> mob != null && mob.getAttackTarget() == this.axolotl);
            for (EntityLiving mob : around) {
                mob.setAttackTarget(null);
            }
        }

        @Override
        public void updateTask() {
            this.axolotl.getNavigator().clearPath();
        }
    }

    static class AITempt extends EntityAITempt {

        private final EntityAxolotl axolotl;

        AITempt(EntityAxolotl axolotl) {
            super(axolotl, 0.5D, false, Collections.singleton(Items.FISH));
            this.axolotl = axolotl;
        }

        @Override
        protected boolean isTempting(ItemStack stack) {
            return this.axolotl.isBreedingItem(stack);
        }
    }

    static class AIFindWater extends EntityAIBase {

        private final EntityAxolotl axolotl;
        private BlockPos water;

        AIFindWater(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.axolotl.isInWater() || !this.axolotl.onGround || this.axolotl.getRNG().nextInt(10) != 0) {
                return false;
            }
            this.water = this.findWater();
            return this.water != null;
        }

        @Nullable
        private BlockPos findWater() {
            BlockPos origin = new BlockPos(this.axolotl);
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-6, -2, -6), origin.add(6, 2, 6))) {
                if (this.axolotl.world.getBlockState(pos).getMaterial() == Material.WATER) {
                    double distance = pos.distanceSq(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.toImmutable();
                    }
                }
            }
            return best;
        }

        @Override
        public void startExecuting() {
            this.axolotl.getNavigator().tryMoveToXYZ(this.water.getX() + 0.5D, this.water.getY(), this.water.getZ() + 0.5D, 0.5D);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.axolotl.isInWater() && !this.axolotl.getNavigator().noPath();
        }
    }

    static class AIWander extends EntityAIBase {

        private final EntityAxolotl axolotl;
        private Vec3d target;

        AIWander(EntityAxolotl axolotl) {
            this.axolotl = axolotl;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.axolotl.isBeingRidden() || this.axolotl.getRNG().nextInt(this.axolotl.isInWater() ? 40 : 120) != 0) {
                return false;
            }
            this.target = this.axolotl.isInWater() ? this.findSwimTarget() : RandomPositionGenerator.findRandomTarget(this.axolotl, 10, 7);
            return this.target != null;
        }

        @Nullable
        private Vec3d findSwimTarget() {
            for (int i = 0; i < 10; i++) {
                BlockPos pos = new BlockPos(this.axolotl).add(this.axolotl.getRNG().nextInt(21) - 10, this.axolotl.getRNG().nextInt(15) - 7,
                        this.axolotl.getRNG().nextInt(21) - 10);
                if (pos.getY() > 0 && this.axolotl.world.getBlockState(pos).getMaterial() == Material.WATER) {
                    return new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                }
            }
            return null;
        }

        @Override
        public void startExecuting() {
            this.axolotl.getNavigator().tryMoveToXYZ(this.target.x, this.target.y, this.target.z, 0.5D);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.axolotl.getNavigator().noPath();
        }
    }
}
