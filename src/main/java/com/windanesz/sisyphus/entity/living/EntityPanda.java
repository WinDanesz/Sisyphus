package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EntityPanda extends EntityAnimal {

    public static final String[] TYPES = {"normal", "lazy", "worried", "playful", "brown", "weak", "aggressive"};
    private static final int NORMAL = 0;
    private static final int BROWN = 4;
    private static final int WEAK = 5;
    private static final int AGGRESSIVE = 6;

    private static final DataParameter<Integer> MAIN_GENE = EntityDataManager.createKey(EntityPanda.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HIDDEN_GENE = EntityDataManager.createKey(EntityPanda.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(EntityPanda.class, DataSerializers.BYTE);

    private static final int SITTING = 1;
    private static final int ON_BACK = 2;
    private static final int ROLLING = 4;
    private static final int SNEEZING = 8;
    private static final int EATING = 16;
    private static final int UNHAPPY = 32;

    private static final int ROLL_TICKS = 32;
    private static final int SNEEZE_TICKS = 20;

    private static final byte BITE = 4;
    private static final byte SWIPE_RIGHT = 5;
    private static final byte SWIPE_LEFT = 6;
    private static final String[] ATTACKS = {"bite", "swipe_right", "swipe_left"};
    private static final int[] ATTACK_TICKS = {9, 11, 11};
    private static final int[] GENE_WEIGHTS = {5, 1, 1, 1, 2, 5, 1};
    private int rollCounter;
    private Vec3d rollDelta = Vec3d.ZERO;
    private int sneezeCounter;
    private int unhappyCounter;
    private int unhappyCooldown;
    private int lieCooldown;
    private boolean didBite;
    private float gait;
    private int attack;
    private int attackTicks;

    public EntityPanda(World world) {
        super(world);
        this.setSize(1.3F, 1.25F);
        // A panda that is sitting, rolling or lying on its back does not walk
        this.moveHelper = new PandaMoveHelper(this);
    }

    private static boolean isRecessive(int gene) {
        return gene == BROWN || gene == WEAK;
    }

    private static int randomGene(Random rand) {
        int pick = rand.nextInt(16);
        for (int gene = 0; gene < GENE_WEIGHTS.length; gene++) {
            pick -= GENE_WEIGHTS[gene];
            if (pick < 0) {
                return gene;
            }
        }
        return NORMAL;
    }

    private static int inherit(EntityPanda parent, Random rand) {
        int gene = rand.nextBoolean() ? parent.getMainGene() : parent.getHiddenGene();
        return rand.nextInt(32) == 0 ? randomGene(rand) : gene;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MAIN_GENE, NORMAL);
        this.dataManager.register(HIDDEN_GENE, NORMAL);
        this.dataManager.register(FLAGS, (byte) 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIPanic(this, 2.0D) {
            @Override
            public boolean shouldExecute() {
                // Aggressive pandas do not run when they are hurt
                return !EntityPanda.this.isAggressive() && super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                return !EntityPanda.this.isSitting() && super.shouldContinueExecuting();
            }
        });
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, true) {
            @Override
            public boolean shouldExecute() {
                // Only a panda that was hurt fights back
                return EntityPanda.this.getRevengeTarget() != null && EntityPanda.this.canPerformAction() && super.shouldExecute();
            }
        });
        this.tasks.addTask(3, new AIMate(this, 1.0D));
        Item bamboo = ModItems.get("bamboo");
        if (bamboo != null && bamboo != Items.AIR) {
            this.tasks.addTask(4, new EntityAITempt(this, 1.0D, bamboo, false));
        }
        // Worried pandas keep away from players and hostile mobs
        this.tasks.addTask(4, new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 8.0F, 2.0D, 2.0D) {
            @Override
            public boolean shouldExecute() {
                return EntityPanda.this.isWorried() && EntityPanda.this.canPerformAction() && super.shouldExecute();
            }
        });
        this.tasks.addTask(4, new EntityAIAvoidEntity<EntityMob>(this, EntityMob.class, 4.0F, 2.0D, 2.0D) {
            @Override
            public boolean shouldExecute() {
                return EntityPanda.this.isWorried() && EntityPanda.this.canPerformAction() && super.shouldExecute();
            }
        });
        this.tasks.addTask(5, new AIEat(this));
        this.tasks.addTask(5, new AILieOnBack(this));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.2D));
        this.tasks.addTask(6, new AIRoll(this));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new AISneeze(this));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityPanda.this.isChild() && super.shouldExecute();
            }

            @Override
            protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
                if (creature instanceof EntityPanda && ((EntityPanda) creature).isAggressive()) {
                    super.setEntityAttackTarget(creature, target);
                }
            }
        });
    }

    public int getMainGene() {
        return this.dataManager.get(MAIN_GENE);
    }

    public int getHiddenGene() {
        return this.dataManager.get(HIDDEN_GENE);
    }

    public void setGenes(int main, int hidden) {
        this.dataManager.set(MAIN_GENE, main);
        this.dataManager.set(HIDDEN_GENE, hidden);
        this.updateTraits();
    }

    public int getVariant() {
        int main = this.getMainGene();
        int hidden = this.getHiddenGene();
        if (isRecessive(main)) {
            return main == hidden ? main : NORMAL;
        }
        return main;
    }

    public ResourceLocation getTexture() {
        int variant = this.getVariant();
        String name = variant == NORMAL ? "panda" : TYPES[variant] + "_panda";
        return new ResourceLocation(SisyphusBackport.MODID, "textures/entity/panda/" + name + ".png");
    }

    private void updateTraits() {
        boolean weak = this.getVariant() == WEAK;
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(weak ? 10.0D : 20.0D);
        // Lazy pandas are the slowest land mob there is
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.isLazy() ? 0.07D : 0.15D);
        this.setHealth(Math.min(this.getHealth(), (float) this.getMaxHealth()));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        this.setGenes(randomGene(this.rand), randomGene(this.rand));
        IEntityLivingData result = super.onInitialSpawn(difficulty, data);
        // A few of them spawn as cubs
        if (this.rand.nextFloat() < 0.05F) {
            this.setGrowingAge(-24000);
        }
        return result;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        EntityPanda cub = new EntityPanda(this.world);
        if (other instanceof EntityPanda) {
            // The cub gets one gene of each parent and mixes them up as its main and hidden gene
            EntityPanda parent = (EntityPanda) other;
            if (this.rand.nextBoolean()) {
                cub.setGenes(inherit(this, this.rand), inherit(parent, this.rand));
            } else {
                cub.setGenes(inherit(parent, this.rand), inherit(this, this.rand));
            }
        }
        return cub;
    }

    private boolean getPandaFlag(int flag) {
        return (this.dataManager.get(FLAGS) & flag) != 0;
    }

    private void setPandaFlag(int flag, boolean set) {
        int flags = this.dataManager.get(FLAGS);
        this.dataManager.set(FLAGS, (byte) (set ? flags | flag : flags & ~flag));
    }

    public boolean isSitting() {
        return this.getPandaFlag(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.setPandaFlag(SITTING, sitting);
    }

    public boolean isEating() {
        return this.getPandaFlag(EATING);
    }

    public void setEating(boolean eating) {
        this.setPandaFlag(EATING, eating);
    }

    public boolean isOnBack() {
        return this.getPandaFlag(ON_BACK);
    }

    public void setOnBack(boolean onBack) {
        this.setPandaFlag(ON_BACK, onBack);
    }

    public boolean isRolling() {
        return this.getPandaFlag(ROLLING);
    }

    public boolean isSneezing() {
        return this.getPandaFlag(SNEEZING);
    }

    public boolean isUnhappy() {
        return this.getPandaFlag(UNHAPPY);
    }

    public boolean isAggressive() {
        return this.getVariant() == AGGRESSIVE;
    }

    public boolean isLazy() {
        return this.getVariant() == 1;
    }

    public boolean isWorried() {
        return this.getVariant() == 2;
    }

    public boolean isPlayful() {
        return this.getVariant() == 3;
    }

    public boolean isWeak() {
        return this.getVariant() == WEAK;
    }

    public boolean isScared() {
        return this.isWorried() && this.world.isThundering();
    }

    public boolean canPerformAction() {
        return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
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

    private boolean canFindBamboo() {
        Block bamboo = ModBlocks.get("bamboo");
        if (bamboo == null) {
            return false;
        }
        BlockPos origin = this.getPosition();
        for (int y = 0; y < 3; y++) {
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    if (this.world.getBlockState(origin.add(x, y, z)).getBlock() == bamboo) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == ModItems.get("bamboo");
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) {
            double raw = this.groundSpeed();
            this.gait += ((raw < 0.4D ? 0.0F : (float) raw) - this.gait) * 0.25F;
            if (this.attackTicks > 0) {
                this.attackTicks--;
            }
            return;
        }
        if (!this.isEntityAlive()) {
            return;
        }

        // A worried panda sits down in a thunderstorm and stays scared until it is over
        if (this.isWorried()) {
            if (this.world.isThundering() && !this.isInWater()) {
                this.setSitting(true);
                this.setEating(false);
            } else if (!this.isEating()) {
                this.setSitting(false);
            }
        }

        // Only an aggressive panda keeps biting: the others drop their target after the first bite
        if (this.didBite && !this.isAggressive()) {
            this.setAttackTarget(null);
            this.setRevengeTarget(null);
        }
        if (this.getAttackTarget() == null) {
            this.didBite = false;
        }

        if (this.unhappyCounter > 0) {
            EntityPlayer player = this.world.getClosestPlayerToEntity(this, 8.0D);
            if (player != null) {
                this.getLookHelper().setLookPositionWithEntity(player, 90.0F, 90.0F);
            }
            if (this.unhappyCounter == 29 || this.unhappyCounter == 14) {
                this.playSound(ModSounds.ENTITY_PANDA_CANT_BREED, 1.0F, 1.0F);
            }
            this.unhappyCounter--;
            this.setPandaFlag(UNHAPPY, this.unhappyCounter > 0);
        }

        if (this.isSneezing()) {
            this.sneezeCounter++;
            if (this.sneezeCounter == 1) {
                this.playSound(ModSounds.ENTITY_PANDA_PRE_SNEEZE, 1.0F, 1.0F);
            }
            if (this.sneezeCounter > SNEEZE_TICKS) {
                this.setPandaFlag(SNEEZING, false);
                this.afterSneeze();
            }
        }

        if (this.isRolling()) {
            this.handleRoll();
        } else {
            this.rollCounter = 0;
        }
    }

    private void handleRoll() {
        if (++this.rollCounter > ROLL_TICKS) {
            this.setPandaFlag(ROLLING, false);
            return;
        }
        if (this.rollCounter == 1) {
            float yaw = this.rotationYaw * 0.017453292F;
            double speed = this.isChild() ? 0.1D : 0.2D;
            this.rollDelta = new Vec3d(-MathHelper.sin(yaw) * speed, 0.0D, MathHelper.cos(yaw) * speed);
            this.motionX = this.rollDelta.x;
            this.motionY = 0.27D;
            this.motionZ = this.rollDelta.z;
        } else if ((this.rollCounter == 7 || this.rollCounter == 15 || this.rollCounter == 23) && this.onGround) {
            this.motionX = this.rollDelta.x;
            this.motionY = 0.27D;
            this.motionZ = this.rollDelta.z;
        } else {
            this.motionX = this.rollDelta.x;
            this.motionZ = this.rollDelta.z;
        }
    }

    private void afterSneeze() {
        this.sneezeCounter = 0;
        if (this.world instanceof WorldServer) {
            float yaw = this.renderYawOffset * 0.017453292F;
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SLIME, this.posX - MathHelper.sin(yaw) * this.width * 0.5D,
                    this.posY + this.getEyeHeight() - 0.1D, this.posZ + MathHelper.cos(yaw) * this.width * 0.5D, 6, 0.1D, 0.05D, 0.1D, 0.02D);
        }
        this.playSound(ModSounds.ENTITY_PANDA_SNEEZE, 1.0F, 1.0F);
        for (EntityPanda other : this.world.getEntitiesWithinAABB(EntityPanda.class, this.getEntityBoundingBox().grow(10.0D))) {
            if (!other.isChild() && other.onGround && !other.isInWater() && other.canPerformAction()) {
                other.getJumpHelper().setJumping();
            }
        }
        if (this.rand.nextInt(700) == 0) {
            this.entityDropItem(new ItemStack(Items.SLIME_BALL), 0.0F);
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id >= BITE && id <= SWIPE_LEFT) {
            this.attack = id - BITE;
            this.attackTicks = ATTACK_TICKS[this.attack];
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * Client side only.
     */
    @Nullable
    public String currentAttack() {
        return this.attackTicks > 0 ? ATTACKS[this.attack] : null;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (hit) {
            this.applyEnchantments(this, target);
            this.playSound(ModSounds.ENTITY_PANDA_BITE, 1.0F, 1.0F);
            // Two hits in three are bites, the third a swipe of a front paw
            byte move = this.rand.nextInt(3) != 0 ? BITE : this.rand.nextBoolean() ? SWIPE_RIGHT : SWIPE_LEFT;
            this.world.setEntityState(this, move);
            if (!this.isAggressive()) {
                this.didBite = true;
            }
        }
        return hit;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/panda");
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAggressive()) {
            return ModSounds.ENTITY_PANDA_AGGRESSIVE_AMBIENT;
        }
        return this.isWorried() ? ModSounds.ENTITY_PANDA_WORRIED_AMBIENT : ModSounds.ENTITY_PANDA_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.ENTITY_PANDA_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_PANDA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PANDA_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("MainGene", this.getMainGene());
        compound.setInteger("HiddenGene", this.getHiddenGene());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setGenes(compound.getInteger("MainGene"), compound.getInteger("HiddenGene"));
    }

    static class AIEat extends EntityAIBase {

        private final EntityPanda panda;
        private EntityItem target;
        private int eatTicks;

        AIEat(EntityPanda panda) {
            this.panda = panda;
            this.setMutexBits(3);
        }

        private static boolean isFood(ItemStack stack) {
            return stack.getItem() == ModItems.get("bamboo") || stack.getItem() == Items.CAKE;
        }

        @Override
        public boolean shouldExecute() {
            if (this.panda.getRNG().nextInt(10) != 0 || this.panda.getRevengeTarget() != null || this.panda.isScared() || !this.panda.canPerformAction()
                    || this.panda.isChild() || this.panda.isWorried()) {
                return false;
            }
            List<EntityItem> items = this.panda.world.getEntitiesWithinAABB(EntityItem.class, this.panda.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D),
                    item -> item != null && !item.isDead && isFood(item.getItem()));
            if (items.isEmpty()) {
                return false;
            }
            this.target = items.get(0);
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.target != null && !this.target.isDead && (this.eatTicks > 0 || this.panda.getDistanceSq(this.target) < 100.0D);
        }

        @Override
        public void startExecuting() {
            this.eatTicks = 0;
            this.panda.getNavigator().tryMoveToEntityLiving(this.target, 1.2D);
        }

        @Override
        public void updateTask() {
            if (this.eatTicks == 0) {
                if (this.panda.getDistanceSq(this.target) < 2.5D) {
                    this.panda.getNavigator().clearPath();
                    this.panda.setSitting(true);
                    this.panda.setEating(true);
                    this.eatTicks = 1;
                } else if (this.panda.getNavigator().noPath()) {
                    this.panda.getNavigator().tryMoveToEntityLiving(this.target, 1.2D);
                }
                return;
            }

            this.eatTicks++;
            if (this.eatTicks % 10 == 0) {
                this.panda.playSound(ModSounds.ENTITY_PANDA_EAT, 1.0F, 1.0F);
                this.panda.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.panda.posX, this.panda.posY + 0.8D, this.panda.posZ + 0.4D, 0.0D, 0.1D, 0.0D,
                        Item.getIdFromItem(this.target.getItem().getItem()));
            }
            if (this.eatTicks > 60) {
                if (!this.panda.world.isRemote) {
                    this.target.getItem().shrink(1);
                    if (this.target.getItem().isEmpty()) {
                        this.target.setDead();
                    }
                }
                this.target = null;
            }
        }

        @Override
        public void resetTask() {
            if (!this.panda.isScared()) {
                this.panda.setSitting(false);
            }
            this.panda.setEating(false);
            this.target = null;
            this.eatTicks = 0;
        }
    }

    static class AIMate extends EntityAIMate {

        private final EntityPanda panda;

        AIMate(EntityPanda panda, double speed) {
            super(panda, speed);
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            if (!super.shouldExecute()) {
                return false;
            }
            if (this.panda.canFindBamboo()) {
                return true;
            }
            if (this.panda.unhappyCounter == 0 && this.panda.unhappyCooldown <= this.panda.ticksExisted) {
                this.panda.unhappyCounter = 32;
                this.panda.unhappyCooldown = this.panda.ticksExisted + 600;
                this.panda.setPandaFlag(UNHAPPY, true);
            }
            return false;
        }
    }

    static class AILieOnBack extends EntityAIBase {

        private final EntityPanda panda;

        AILieOnBack(EntityPanda panda) {
            this.panda = panda;
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return this.panda.lieCooldown < this.panda.ticksExisted && this.panda.isLazy() && this.panda.canPerformAction()
                    && this.panda.onGround && this.panda.getRNG().nextInt(400) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.panda.isInWater() && (this.panda.isLazy() || this.panda.getRNG().nextInt(600) == 1) && this.panda.getRNG().nextInt(2000) != 1;
        }

        @Override
        public void startExecuting() {
            this.panda.getNavigator().clearPath();
            this.panda.setOnBack(true);
        }

        @Override
        public void resetTask() {
            this.panda.setOnBack(false);
            this.panda.lieCooldown = this.panda.ticksExisted + 200;
        }
    }

    static class AIRoll extends EntityAIBase {

        private final EntityPanda panda;

        AIRoll(EntityPanda panda) {
            this.panda = panda;
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            if (!(this.panda.isChild() || this.panda.isPlayful()) || !this.panda.onGround || !this.panda.canPerformAction()) {
                return false;
            }
            // It needs solid ground ahead, so that it does not roll off an edge
            float yaw = this.panda.rotationYaw * 0.017453292F;
            BlockPos ahead = new BlockPos(this.panda.posX - MathHelper.sin(yaw) * 2.0D, this.panda.posY - 1.0D, this.panda.posZ + MathHelper.cos(yaw) * 2.0D);
            if (this.panda.world.isAirBlock(ahead)) {
                return false;
            }
            return this.panda.isPlayful() ? this.panda.getRNG().nextInt(60) == 1 : this.panda.getRNG().nextInt(500) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            this.panda.getNavigator().clearPath();
            this.panda.setPandaFlag(ROLLING, true);
        }
    }

    static class AISneeze extends EntityAIBase {

        private final EntityPanda panda;

        AISneeze(EntityPanda panda) {
            this.panda = panda;
        }

        @Override
        public boolean shouldExecute() {
            if (!this.panda.isChild() || !this.panda.canPerformAction()) {
                return false;
            }
            return this.panda.isWeak() ? this.panda.getRNG().nextInt(500) == 1 : this.panda.getRNG().nextInt(6000) == 1;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            this.panda.sneezeCounter = 0;
            this.panda.setPandaFlag(SNEEZING, true);
        }
    }

    static class PandaMoveHelper extends EntityMoveHelper {

        private final EntityPanda panda;

        PandaMoveHelper(EntityPanda panda) {
            super(panda);
            this.panda = panda;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.panda.canPerformAction()) {
                super.onUpdateMoveHelper();
            }
        }
    }
}
