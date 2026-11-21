package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EntityWanderingTrader extends EntityAgeable implements INpc, IMerchant {

    public static final int STAY_TICKS = 48000;

    private static final DataParameter<Boolean> UNHAPPY = EntityDataManager.createKey(EntityWanderingTrader.class, DataSerializers.BOOLEAN);

    private int unhappyTicks;
    private float gait;

    @Nullable
    private EntityPlayer customer;
    @Nullable
    private MerchantRecipeList offers;
    private int despawnDelay;
    private boolean needsLlamas;
    @Nullable
    private BlockPos wanderTarget;

    public EntityWanderingTrader(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
        this.setGrowingAge(0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(UNHAPPY, false);
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

    public boolean isUnhappy() {
        return this.dataManager.get(UNHAPPY);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        // At night the trader drinks a potion of invisibility, in the morning he drinks milk and is seen again
        this.tasks.addTask(0, new AIUseItem(this, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.INVISIBILITY),
                ModSounds.ENTITY_WANDERING_TRADER_DRINK_POTION, ModSounds.ENTITY_WANDERING_TRADER_DISAPPEARED,
                trader -> !trader.world.isDaytime() && !trader.isInvisible()));
        this.tasks.addTask(0, new AIUseItem(this, new ItemStack(Items.MILK_BUCKET), ModSounds.ENTITY_WANDERING_TRADER_DRINK_MILK,
                ModSounds.ENTITY_WANDERING_TRADER_REAPPEARED, trader -> trader.world.isDaytime() && trader.isInvisible()));
        this.tasks.addTask(1, new AITradeWithPlayer(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityZombie>(this, EntityZombie.class, 8.0F, 0.5D, 0.5D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityEvoker>(this, EntityEvoker.class, 12.0F, 0.5D, 0.5D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityVindicator>(this, EntityVindicator.class, 8.0F, 0.5D, 0.5D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityVex>(this, EntityVex.class, 8.0F, 0.5D, 0.5D));
        this.tasks.addTask(1, new EntityAIPanic(this, 0.5D));
        this.tasks.addTask(1, new AILookAtCustomer(this));
        this.tasks.addTask(4, new AIWanderToTarget(this, 0.35D));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 0.35D));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLlama.class, 8.0F));
        this.tasks.addTask(11, new EntityAILookIdle(this));
    }

    @Nullable
    @Override
    public EntityPlayer getCustomer() {
        return this.customer;
    }

    @Override
    public void setCustomer(@Nullable EntityPlayer player) {
        this.customer = player;
    }

    public boolean isTrading() {
        return this.customer != null;
    }

    @Nullable
    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (this.offers == null) {
            this.offers = WanderingTraderTrades.generate(this.rand);
        }
        return this.offers;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setRecipes(@Nullable MerchantRecipeList recipes) {
    }

    @Override
    public void useRecipe(MerchantRecipe recipe) {
        recipe.incrementToolUses();
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound(ModSounds.ENTITY_WANDERING_TRADER_TRADE, this.getSoundVolume(), this.getSoundPitch());
        if (!this.world.isRemote) {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, 3 + this.rand.nextInt(4)));
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack) {
        if (!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
            this.livingSoundTime = -this.getTalkInterval();
            this.playSound(stack.isEmpty() ? ModSounds.ENTITY_WANDERING_TRADER_NO : ModSounds.ENTITY_WANDERING_TRADER_YES, this.getSoundVolume(), this.getSoundPitch());
            if (stack.isEmpty()) {
                this.unhappyTicks = 40;
                this.dataManager.set(UNHAPPY, true);
            }
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? super.getDisplayName() : new TextComponentTranslation("entity.sisyphus:wandering_trader.name");
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public BlockPos getPos() {
        return new BlockPos(this);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.SPAWN_EGG || !this.isEntityAlive() || this.isTrading() || this.isChild()) {
            return super.processInteract(player, hand);
        }
        MerchantRecipeList recipes = this.getRecipes(player);
        if (recipes == null || recipes.isEmpty()) {
            return super.processInteract(player, hand);
        }
        if (!this.world.isRemote) {
            this.setCustomer(player);
            player.displayVillagerTradeGui(this);
        }
        return true;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    public void setDespawnDelay(int ticks) {
        this.despawnDelay = ticks;
    }

    @Nullable
    public BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    public void setWanderTarget(@Nullable BlockPos pos) {
        this.wanderTarget = pos;
        if (pos != null) {
            this.setHomePosAndDistance(pos, 16);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            double raw = this.groundSpeed();
            this.gait += ((raw < 0.4D ? 0.0F : (float) raw) - this.gait) * 0.25F;
            return;
        }
        if (this.needsLlamas && this.world instanceof WorldServer) {
            this.needsLlamas = false;
            WanderingTraderManager.spawnLlamas((WorldServer) this.world, this);
        }
        // A shopper that wandered off or closed the screen no longer holds the trader
        if (this.customer != null && (this.customer.isDead || !(this.customer.openContainer instanceof ContainerMerchant))) {
            this.customer = null;
        }
        if (this.unhappyTicks > 0 && --this.unhappyTicks == 0) {
            this.dataManager.set(UNHAPPY, false);
        }
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.setDead();
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        this.needsLlamas = true;
        return super.onInitialSpawn(difficulty, data);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        return null;
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? 0.81F : 1.62F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? ModSounds.ENTITY_WANDERING_TRADER_TRADE : ModSounds.ENTITY_WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_WANDERING_TRADER_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("DespawnDelay", this.despawnDelay);
        if (this.offers != null) {
            compound.setTag("Offers", this.offers.getRecipiesAsTags());
        }
        if (this.wanderTarget != null) {
            compound.setInteger("WanderTargetX", this.wanderTarget.getX());
            compound.setInteger("WanderTargetY", this.wanderTarget.getY());
            compound.setInteger("WanderTargetZ", this.wanderTarget.getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.despawnDelay = compound.getInteger("DespawnDelay");
        if (compound.hasKey("Offers", 10)) {
            this.offers = new MerchantRecipeList(compound.getCompoundTag("Offers"));
        }
        if (compound.hasKey("WanderTargetX")) {
            this.setWanderTarget(new BlockPos(compound.getInteger("WanderTargetX"), compound.getInteger("WanderTargetY"), compound.getInteger("WanderTargetZ")));
        }
        this.setGrowingAge(Math.max(0, this.getGrowingAge()));
    }

    static class AIUseItem extends EntityAIBase {

        private final EntityWanderingTrader trader;
        private final ItemStack stack;
        private final SoundEvent startSound;
        private final SoundEvent finishSound;
        private final Predicate<EntityWanderingTrader> condition;

        AIUseItem(EntityWanderingTrader trader, ItemStack stack, SoundEvent startSound, SoundEvent finishSound, Predicate<EntityWanderingTrader> condition) {
            this.trader = trader;
            this.stack = stack;
            this.startSound = startSound;
            this.finishSound = finishSound;
            this.condition = condition;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return !this.trader.isTrading() && this.condition.test(this.trader);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.trader.isHandActive();
        }

        @Override
        public void startExecuting() {
            this.trader.getNavigator().clearPath();
            this.trader.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, this.stack.copy());
            this.trader.setActiveHand(EnumHand.MAIN_HAND);
            this.trader.world.playSound(null, this.trader.posX, this.trader.posY, this.trader.posZ, this.startSound,
                    SoundCategory.NEUTRAL, 1.0F, this.trader.rand.nextFloat() * 0.2F + 0.9F);
        }

        @Override
        public void resetTask() {
            this.trader.stopActiveHand();
            this.trader.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!this.condition.test(this.trader)) {
                this.trader.world.playSound(null, this.trader.posX, this.trader.posY, this.trader.posZ, this.finishSound,
                        SoundCategory.NEUTRAL, 1.0F, this.trader.rand.nextFloat() * 0.2F + 0.9F);
            }
        }
    }

    static class AITradeWithPlayer extends EntityAIBase {

        private final EntityWanderingTrader trader;

        AITradeWithPlayer(EntityWanderingTrader trader) {
            this.trader = trader;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return this.trader.isEntityAlive() && this.trader.isTrading() && !this.trader.isInWater() && this.trader.onGround && !this.trader.velocityChanged
                    && this.trader.getCustomer() != null && this.trader.getDistanceSq(this.trader.getCustomer()) <= 16.0D;
        }

        @Override
        public void startExecuting() {
            this.trader.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            this.trader.setCustomer(null);
        }
    }

    static class AILookAtCustomer extends EntityAIWatchClosest {

        private final EntityWanderingTrader trader;

        AILookAtCustomer(EntityWanderingTrader trader) {
            super(trader, EntityPlayer.class, 8.0F);
            this.trader = trader;
        }

        @Override
        public boolean shouldExecute() {
            if (this.trader.isTrading()) {
                this.closestEntity = this.trader.getCustomer();
                return true;
            }
            return false;
        }
    }

    static class AIWanderToTarget extends EntityAIBase {

        private final EntityWanderingTrader trader;
        private final double speed;

        AIWanderToTarget(EntityWanderingTrader trader, double speed) {
            this.trader = trader;
            this.speed = speed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            BlockPos target = this.trader.getWanderTarget();
            return target != null && !this.trader.isTrading() && target.distanceSq(this.trader.posX, this.trader.posY, this.trader.posZ) > 16.0D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute() && !this.trader.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            BlockPos target = this.trader.getWanderTarget();
            if (target != null) {
                this.trader.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, this.speed);
            }
        }
    }
}
