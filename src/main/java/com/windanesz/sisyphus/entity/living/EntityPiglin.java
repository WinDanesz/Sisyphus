package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.ai.EntityAIAvoidBlocks;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityPiglin extends EntityMob {

    private static final Set<EntityWitherSkeleton> HUNTERS = Collections.newSetFromMap(new WeakHashMap<>());

    private static final DataParameter<Boolean> BABY = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ADMIRING = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> AGGRESSIVE = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);

    private static final int ADMIRE_TIME = 120;
    private static final int ANGER_TIME = 600;
    private static final int CONVERSION_TIME = 300;
    private static List<Block> soulFire;
    private EntityAIAvoidBlocks avoid;
    private int avoidingCreatures;
    private boolean immuneToZombification;
    private int timeInOverworld;
    private int admireTicks;
    private int angerTicks;
    private int huntCooldown;
    @Nullable
    private UUID barterer;
    @Nullable
    private UUID angryAt;

    public EntityPiglin(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
        this.experienceValue = 5;
        this.setCanPickUpLoot(true);
    }

    private static boolean isLoved(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.GOLD_INGOT || item == Items.GOLDEN_APPLE || item == Items.GOLDEN_CARROT || item == Items.SPECKLED_MELON || item == Items.CLOCK
                || item == Items.GOLDEN_HORSE_ARMOR || item == Item.getItemFromBlock(Blocks.GOLD_BLOCK)
                || item == Item.getItemFromBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {
            return true;
        }
        return (item instanceof ItemArmor && ((ItemArmor) item).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD)
                || (item instanceof ItemTool && "GOLD".equals(((ItemTool) item).getToolMaterialName()))
                || (item instanceof ItemSword && "GOLD".equals(((ItemSword) item).getToolMaterialName()));
    }

    private static boolean isSoulFire(IBlockState state) {
        if (soulFire == null) {
            List<Block> found = new ArrayList<>();
            for (String name : new String[]{"soul_fire", "soul_torch", "soul_lantern", "soul_campfire"}) {
                Block soul = ModBlocks.get(name);
                if (soul != Blocks.AIR) {
                    found.add(soul);
                }
            }
            soulFire = found;
        }
        return soulFire.contains(state.getBlock());
    }

    public static boolean wearsGold(EntityLivingBase entity) {
        for (ItemStack stack : entity.getArmorInventoryList()) {
            if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD) {
                return true;
            }
        }
        return false;
    }

    public static void angerNearby(EntityPlayer player, boolean onlyIfSeen) {
        if (player.isSpectator()) {
            return;
        }
        AxisAlignedBB box = player.getEntityBoundingBox().grow(16.0D);
        for (EntityPiglin piglin : player.world.getEntitiesWithinAABB(EntityPiglin.class, box)) {
            if (!onlyIfSeen || piglin.canEntityBeSeen(player)) {
                piglin.angerAt(player);
            }
        }
    }

    private static boolean isGuarded(Block block) {
        if (block == Blocks.GOLD_BLOCK || block == Blocks.GOLD_ORE || block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return true;
        }
        for (String name : new String[]{"nether_gold_ore", "gilded_blackstone"}) {
            Block modded = ModBlocks.get(name);
            if (modded != Blocks.AIR && block == modded) {
                return true;
            }
        }
        return isChest(block);
    }

    private static boolean isChest(Block block) {
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
            return true;
        }
        Block barrel = ModBlocks.get("barrel");
        return barrel != Blocks.AIR && block == barrel;
    }

    @SubscribeEvent
    public static void onBreakGuarded(BlockEvent.BreakEvent event) {
        if (!Settings.mobs.enablePiglins || event.getWorld().isRemote || event.getPlayer() == null) {
            return;
        }
        if (isGuarded(event.getState().getBlock())) {
            EntityPiglin.angerNearby(event.getPlayer(), false);
        }
    }

    @SubscribeEvent
    public static void onOpenChest(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.mobs.enablePiglins || event.getWorld().isRemote || event.isCanceled()) {
            return;
        }
        if (isChest(event.getWorld().getBlockState(event.getPos()).getBlock()) && (!event.getEntityPlayer().isSneaking() || event.getItemStack().isEmpty())) {
            EntityPiglin.angerNearby(event.getEntityPlayer(), true);
        }
    }

    @SubscribeEvent
    public static void onWitherSkeletonJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityWitherSkeleton) || !Settings.mobs.enablePiglins) {
            return;
        }
        EntityWitherSkeleton skeleton = (EntityWitherSkeleton) event.getEntity();
        if (HUNTERS.add(skeleton)) {
            skeleton.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPiglin>(skeleton, EntityPiglin.class, true));
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BABY, false);
        this.dataManager.register(ADMIRING, false);
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(AGGRESSIVE, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new AIAdmire());
        this.tasks.addTask(1, this.avoiding(EntityPigZombie.class));
        this.tasks.addTask(1, this.avoiding(EntityZoglin.class));
        this.avoid = new EntityAIAvoidBlocks(this, EntityPiglin::isSoulFire, 8, 1.0D, piglin -> true);
        this.tasks.addTask(2, this.avoid);
        this.tasks.addTask(3, new AICrossbow());
        this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false) {
            @Override
            public boolean shouldExecute() {
                return !EntityPiglin.this.isChild() && !EntityPiglin.this.holdsCrossbow() && super.shouldExecute();
            }
        });
        this.tasks.addTask(5, new AISeekGold());
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10, true, false, this::shouldAttack));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityWitherSkeleton>(this, EntityWitherSkeleton.class, 10, true, false, skeleton -> !this.isChild()));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityHoglin>(this, EntityHoglin.class, 120, true, false,
                hoglin -> !this.isChild() && hoglin != null && !hoglin.isChild() && this.huntCooldown <= 0 && !this.isAdmiring()));
    }

    private <T extends Entity> EntityAIAvoidEntity<T> avoiding(Class<T> type) {
        return new EntityAIAvoidEntity<T>(this, type, 8.0F, 1.0D, 1.0D) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityPiglin.this.avoidingCreatures++;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityPiglin.this.avoidingCreatures = Math.max(0, EntityPiglin.this.avoidingCreatures - 1);
            }
        };
    }

    private boolean playerNearbyHoldsGold() {
        EntityPlayer player = this.world.getClosestPlayerToEntity(this, 16.0D);
        return player != null && (isLoved(player.getHeldItemMainhand()) || isLoved(player.getHeldItemOffhand())) && this.canEntityBeSeen(player);
    }

    @Override
    public boolean isChild() {
        return this.dataManager.get(BABY);
    }

    public void setChild(boolean child) {
        this.dataManager.set(BABY, child);
        this.setSize(child ? 0.3F : 0.6F, child ? 0.975F : 1.95F);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (BABY.equals(key)) {
            this.setSize(this.isChild() ? 0.3F : 0.6F, this.isChild() ? 0.975F : 1.95F);
        }
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? 0.93F : 1.79F;
    }

    public boolean isAdmiring() {
        return this.dataManager.get(ADMIRING);
    }

    public boolean isChargingCrossbow() {
        return this.dataManager.get(CHARGING);
    }

    public boolean isAggressive() {
        return this.dataManager.get(AGGRESSIVE);
    }

    public boolean holdsCrossbow() {
        Item crossbow = ModItems.get("crossbow");
        return crossbow != null && crossbow != Items.AIR && this.getHeldItemMainhand().getItem() == crossbow;
    }

    private boolean shouldAttack(@Nullable EntityPlayer player) {
        return player != null && !this.isChild() && (player.getUniqueID().equals(this.angryAt) || !wearsGold(player));
    }

    /**
     * For thirty seconds, during which gold armor does not help.
     */
    public void angerAt(EntityPlayer player) {
        if (this.isChild() || this.isDead) {
            return;
        }
        this.angryAt = player.getUniqueID();
        this.angerTicks = ANGER_TIME;
        if (this.getAttackTarget() == null) {
            this.setAttackTarget(player);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            return;
        }
        this.dataManager.set(AGGRESSIVE, this.getAttackTarget() != null && !this.isChild());
        if (this.huntCooldown > 0) {
            this.huntCooldown--;
        }
        if (this.angerTicks > 0) {
            this.angerTicks--;
        } else if (this.getAttackTarget() instanceof EntityPlayer && wearsGold(this.getAttackTarget())) {
            this.setAttackTarget(null);
            this.angryAt = null;
        }
        if (this.admireTicks > 0 && --this.admireTicks == 0) {
            this.finishBarter();
        }
        this.tickZombification();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL && !this.isNoDespawnRequired()) {
            this.setDead();
        }
    }

    private void tickZombification() {
        int dimension = this.world.provider.getDimension();
        if (!this.immuneToZombification && !this.isAIDisabled() && (dimension == 0 || dimension == 1)) {
            if (++this.timeInOverworld > CONVERSION_TIME) {
                this.zombify();
            }
        } else {
            this.timeInOverworld = 0;
        }
    }

    private void zombify() {
        EntityPigZombie zombie = new EntityPigZombie(this.world);
        zombie.copyLocationAndAnglesFrom(this);
        zombie.setChild(this.isChild());
        zombie.setNoAI(this.isAIDisabled());
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            zombie.setItemStackToSlot(slot, this.getItemStackFromSlot(slot).copy());
        }
        if (zombie.getHeldItemMainhand().isEmpty() || this.holdsCrossbow()) {
            zombie.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        }
        if (this.hasCustomName()) {
            zombie.setCustomNameTag(this.getCustomNameTag());
            zombie.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
        }
        zombie.setCanPickUpLoot(this.canPickUpLoot());
        zombie.enablePersistence();
        this.world.spawnEntity(zombie);
        this.setDead();
        zombie.playSound(ModSounds.event("entity.piglin.converted_to_zombified"), 1.0F, 1.0F);
    }

    private boolean canBarter() {
        return !this.isChild() && !this.isAdmiring() && this.getAttackTarget() == null && this.getHeldItemOffhand().isEmpty() && !this.isDead;
    }

    private void startAdmiring(ItemStack ingot, @Nullable EntityPlayer from) {
        this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ingot);
        this.setDropChance(EntityEquipmentSlot.OFFHAND, 2.0F);
        this.admireTicks = ADMIRE_TIME;
        this.barterer = from == null ? null : from.getUniqueID();
        this.dataManager.set(ADMIRING, true);
        this.getNavigator().clearPath();
        this.playSound(ModSounds.event("entity.piglin.admiring_item"), 1.0F, 1.0F);
    }

    private void finishBarter() {
        this.dataManager.set(ADMIRING, false);
        this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
        EntityPlayer player = this.barterer == null ? null : this.world.getPlayerEntityByUUID(this.barterer);
        this.barterer = null;
        this.throwLoot(PiglinBartering.roll(this.rand), player != null && this.getDistanceSq(player) < 256.0D ? player : null);
    }

    private void throwLoot(ItemStack loot, @Nullable EntityLivingBase toward) {
        double y = this.posY + this.getEyeHeight() - 0.3D;
        EntityItem item = new EntityItem(this.world, this.posX, y, this.posZ, loot);
        item.setDefaultPickupDelay();
        Vec3d direction = toward == null ? this.getLookVec()
                : new Vec3d(toward.posX - this.posX, toward.posY + toward.getEyeHeight() - y, toward.posZ - this.posZ).normalize();
        item.motionX = direction.x * 0.3D;
        item.motionY = direction.y * 0.3D + 0.1D;
        item.motionZ = direction.z * 0.3D;
        this.world.spawnEntity(item);
    }

    private void stopAdmiring() {
        if (this.isAdmiring()) {
            ItemStack ingot = this.getHeldItemOffhand();
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
            if (!ingot.isEmpty()) {
                this.entityDropItem(ingot, 0.5F);
            }
            this.admireTicks = 0;
            this.barterer = null;
            this.dataManager.set(ADMIRING, false);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.GOLD_INGOT && this.canBarter()) {
            if (!this.world.isRemote) {
                ItemStack ingot = held.splitStack(1);
                if (player.capabilities.isCreativeMode) {
                    held.grow(1);
                }
                this.startAdmiring(ingot, player);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.getItem() == Items.GOLD_INGOT) {
            if (this.canBarter()) {
                ItemStack ingot = stack.splitStack(1);
                itemEntity.setItem(stack);
                if (stack.isEmpty()) {
                    itemEntity.setDead();
                }
                this.onItemPickup(itemEntity, 1);
                EntityPlayer thrower = itemEntity.getThrower() == null ? null : this.world.getPlayerEntityByName(itemEntity.getThrower());
                this.startAdmiring(ingot, thrower);
                if (thrower instanceof EntityPlayerMP) {
                    AdvancementHelper.grant((EntityPlayerMP) thrower, "oh_shiny");
                }
            }
            return;
        }
        boolean golden = (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD)
                || stack.getItem() == Items.GOLDEN_SWORD;
        if (golden && !this.isChild()) {
            super.updateEquipmentIfNeeded(itemEntity);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt && !this.world.isRemote) {
            this.stopAdmiring();
            Entity attacker = source.getTrueSource();
            if (attacker instanceof EntityPlayer) {
                angerNearby((EntityPlayer) attacker, false);
            }
        }
        return hurt;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        super.setAttackTarget(target);
        if (target instanceof EntityPlayer && this.angerTicks <= 0 && !this.isChild()) {
            this.angryAt = target.getUniqueID();
            this.angerTicks = ANGER_TIME;
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (this.isChild()) {
            return false;
        }
        return super.attackEntityAsMob(target);
    }

    @Override
    public void onKillEntity(EntityLivingBase killed) {
        super.onKillEntity(killed);
        if (killed instanceof EntityHoglin) {
            this.huntCooldown = 6000 + this.rand.nextInt(4000);
            this.playSound(ModSounds.event("entity.piglin.celebrate"), 1.0F, 1.0F);
        }
    }

    private void shoot(EntityLivingBase target) {
        EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
        double dx = target.posX - this.posX;
        double dy = target.getEntityBoundingBox().minY + target.height / 3.0F - arrow.posY;
        double dz = target.posZ - this.posZ;
        double distance = MathHelper.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + distance * 0.2D, dz, 1.6F, (float) (14 - this.world.getDifficulty().getId() * 4));
        arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
        this.world.spawnEntity(arrow);
        this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.ITEM_CROSSBOW_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, data);
        if (this.rand.nextFloat() < 0.2F) {
            this.setChild(true);
        }
        this.setEquipmentBasedOnDifficulty(difficulty);
        return result;
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        if (this.isChild()) {
            return;
        }
        Item crossbow = ModItems.get("crossbow");
        boolean useCrossbow = crossbow != null && crossbow != Items.AIR && this.rand.nextBoolean();
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(useCrossbow ? crossbow : Items.GOLDEN_SWORD));
        if (this.rand.nextFloat() < 0.1F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        }
        if (this.rand.nextFloat() < 0.1F) {
            this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        }
        if (this.rand.nextFloat() < 0.1F) {
            this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        }
        if (this.rand.nextFloat() < 0.1F) {
            this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.world.getBlockState(new BlockPos(this).down()).canEntitySpawn(this)
                && this.isNotColliding();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 4;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsBaby", this.isChild());
        compound.setBoolean("IsImmuneToZombification", this.immuneToZombification);
        compound.setInteger("TimeInOverworld", this.timeInOverworld);
        compound.setInteger("AngerTicks", this.angerTicks);
        compound.setInteger("HuntCooldown", this.huntCooldown);
        if (this.angryAt != null) {
            compound.setUniqueId("AngryAt", this.angryAt);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setChild(compound.getBoolean("IsBaby"));
        this.immuneToZombification = compound.getBoolean("IsImmuneToZombification");
        this.timeInOverworld = compound.getInteger("TimeInOverworld");
        this.angerTicks = compound.getInteger("AngerTicks");
        this.huntCooldown = compound.getInteger("HuntCooldown");
        this.angryAt = compound.hasUniqueId("AngryAt") ? compound.getUniqueId("AngryAt") : null;
        // A piglin that was admiring when it was saved gives the ingot up
        if (!this.getHeldItemOffhand().isEmpty()) {
            this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // What it does decides what it says: in the order of the brain of the game
        if (this.isAdmiring()) {
            return ModSounds.event("entity.piglin.admiring_item");
        }
        if (this.getAttackTarget() != null) {
            return ModSounds.event("entity.piglin.angry");
        }
        if (this.avoidingCreatures > 0 || (this.avoid != null && this.avoid.isRunning())) {
            return ModSounds.event("entity.piglin.retreat");
        }
        if (!this.isChild() && this.playerNearbyHoldsGold()) {
            return ModSounds.event("entity.piglin.jealous");
        }
        return ModSounds.event("entity.piglin.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("entity.piglin.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("entity.piglin.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.event("entity.piglin.step"), 0.15F, 1.0F);
    }

    private final class AIAdmire extends EntityAIBase {
        AIAdmire() {
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return EntityPiglin.this.admireTicks > 0;
        }

        @Override
        public void updateTask() {
            EntityPiglin.this.getNavigator().clearPath();
        }
    }

    private final class AISeekGold extends EntityAIBase {
        private EntityItem ingot;

        AISeekGold() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityPiglin.this.canBarter() || EntityPiglin.this.getRNG().nextInt(10) != 0) {
                return false;
            }
            List<EntityItem> items = EntityPiglin.this.world.getEntitiesWithinAABB(EntityItem.class, EntityPiglin.this.getEntityBoundingBox().grow(8.0D),
                    item -> item != null && !item.isDead && item.getItem().getItem() == Items.GOLD_INGOT);
            if (items.isEmpty()) {
                return false;
            }
            this.ingot = items.get(0);
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.ingot != null && !this.ingot.isDead && EntityPiglin.this.canBarter() && !EntityPiglin.this.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            EntityPiglin.this.getNavigator().tryMoveToEntityLiving(this.ingot, 1.0D);
        }

        @Override
        public void updateTask() {
            if (EntityPiglin.this.ticksExisted % 10 == 0 && this.ingot != null) {
                EntityPiglin.this.getNavigator().tryMoveToXYZ(this.ingot.posX, this.ingot.posY, this.ingot.posZ, 1.0D);
            }
        }

        @Override
        public void resetTask() {
            this.ingot = null;
        }
    }

    private final class AICrossbow extends EntityAIBase {
        private int charge;
        private int cooldown;

        AICrossbow() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = EntityPiglin.this.getAttackTarget();
            return target != null && target.isEntityAlive() && !EntityPiglin.this.isChild() && EntityPiglin.this.holdsCrossbow();
        }

        @Override
        public void resetTask() {
            this.charge = 0;
            EntityPiglin.this.dataManager.set(CHARGING, false);
            EntityPiglin.this.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            EntityPiglin piglin = EntityPiglin.this;
            EntityLivingBase target = piglin.getAttackTarget();
            if (target == null) {
                return;
            }
            piglin.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            double distance = piglin.getDistanceSq(target);
            boolean sees = piglin.canEntityBeSeen(target);
            if (distance < 25.0D) {
                Vec3d away = new Vec3d(piglin.posX - target.posX, 0.0D, piglin.posZ - target.posZ).normalize().scale(4.0D);
                piglin.getNavigator().tryMoveToXYZ(piglin.posX + away.x, piglin.posY, piglin.posZ + away.z, 0.75D);
            } else if (distance > 100.0D || !sees) {
                piglin.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            } else {
                piglin.getNavigator().clearPath();
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                return;
            }
            if (sees && distance < 225.0D) {
                if (this.charge == 0) {
                    piglin.world.playSound(null, piglin.posX, piglin.posY, piglin.posZ, ModSounds.ITEM_CROSSBOW_LOADING_START, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    piglin.dataManager.set(CHARGING, true);
                }
                this.charge++;
                if (this.charge == 12) {
                    piglin.world.playSound(null, piglin.posX, piglin.posY, piglin.posZ, ModSounds.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.HOSTILE, 1.0F, 1.0F);
                }
                if (this.charge >= 25) {
                    piglin.world.playSound(null, piglin.posX, piglin.posY, piglin.posZ, ModSounds.ITEM_CROSSBOW_LOADING_END, SoundCategory.HOSTILE, 1.0F, 1.0F);
                    piglin.shoot(target);
                    this.charge = 0;
                    this.cooldown = 20 + piglin.getRNG().nextInt(20);
                    piglin.dataManager.set(CHARGING, false);
                }
            } else if (this.charge > 0) {
                this.charge = 0;
                piglin.dataManager.set(CHARGING, false);
            }
        }
    }
}
