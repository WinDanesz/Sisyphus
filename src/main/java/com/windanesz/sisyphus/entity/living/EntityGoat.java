package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.ai.GoatAILongJump;
import com.windanesz.sisyphus.entity.ai.GoatAIRam;
import com.windanesz.sisyphus.item.ItemGoatHorn;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityGoat extends EntityAnimal {

    public static final double SCREAMING_CHANCE = 0.02D;
    public static final float MISSING_HORN_CHANCE = 0.1F;
    private static final DataParameter<Boolean> SCREAMING = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOWERING_HEAD = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFT_HORN = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_HORN = EntityDataManager.createKey(EntityGoat.class, DataSerializers.BOOLEAN);
    private static final ResourceLocation CHERRY_LOG = new ResourceLocation(SisyphusBackport.MODID, "cherry_log");

    private int ramCooldown;
    private int longJumpCooldown;

    public EntityGoat(World world) {
        super(world);
        this.setSize(0.9F, 1.3F);
        this.ramCooldown = ramCooldown(this.rand, false);
        this.longJumpCooldown = longJumpCooldown(this.rand);
    }

    /**
     * 30-300 seconds, 5-15 for a screaming goat.
     */
    public static int ramCooldown(Random random, boolean screaming) {
        return screaming ? 100 + random.nextInt(201) : 600 + random.nextInt(5401);
    }

    /**
     * 30-60 seconds.
     */
    public static int longJumpCooldown(Random random) {
        return 600 + random.nextInt(601);
    }

    public static boolean screamingOffspring(Random random, boolean parentScreaming) {
        return parentScreaming || random.nextDouble() < SCREAMING_CHANCE;
    }

    /**
     * {@code effectLevels} is the Speed levels minus the Slowness levels.
     */
    public static float ramKnockback(double speed, int effectLevels, boolean child, boolean blocked) {
        double strength = MathHelper.clamp(speed * 1.65D, 0.2D, 3.0D) + 0.25D * effectLevels;
        return (float) ((blocked ? 0.5D : 1.0D) * strength * (child ? 1.0D : 2.5D));
    }

    public static boolean isSpawnableOn(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE) {
            return state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE;
        }
        return block == Blocks.GRASS || block == Blocks.SNOW_LAYER || block == Blocks.SNOW || block == Blocks.PACKED_ICE || block == Blocks.GRAVEL;
    }

    public static boolean snapsHorn(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE) {
            return state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE;
        }
        return block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.PACKED_ICE || block == Blocks.COAL_ORE || block == Blocks.IRON_ORE
                || block == Blocks.EMERALD_ORE || CHERRY_LOG.equals(block.getRegistryName());
    }

    public static boolean hasGoat(Entity vehicle) {
        for (Entity passenger : vehicle.getPassengers()) {
            if (passenger instanceof EntityGoat) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote || event.player.ticksExisted % 20 != 0
                || !(event.player instanceof EntityPlayerMP) || !Settings.mobs.enableGoats) {
            return;
        }
        Entity vehicle = event.player.getRidingEntity();
        if (vehicle instanceof EntityBoat && hasGoat(vehicle)) {
            AdvancementHelper.grant((EntityPlayerMP) event.player, "whatever_floats_your_goat");
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SCREAMING, false);
        this.dataManager.register(LOWERING_HEAD, false);
        this.dataManager.register(LEFT_HORN, true);
        this.dataManager.register(RIGHT_HORN, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        this.tasks.addTask(2, new GoatAIRam(this));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.25D, Items.WHEAT, false));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(6, new GoatAILongJump(this));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
    }

    public boolean isScreaming() {
        return this.dataManager.get(SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        this.dataManager.set(SCREAMING, screaming);
    }

    public boolean isLoweringHead() {
        return this.dataManager.get(LOWERING_HEAD);
    }

    public void setLoweringHead(boolean lowering) {
        this.dataManager.set(LOWERING_HEAD, lowering);
    }

    public boolean hasLeftHorn() {
        return this.dataManager.get(LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return this.dataManager.get(RIGHT_HORN);
    }

    private void setHorns(boolean left, boolean right) {
        this.dataManager.set(LEFT_HORN, left);
        this.dataManager.set(RIGHT_HORN, right);
    }

    /**
     * False if it had no horn to lose.
     */
    public boolean dropHorn() {
        boolean left = this.hasLeftHorn();
        boolean right = this.hasRightHorn();
        ItemGoatHorn item = ModItems.goatHorn();
        if (item == null || !left && !right) {
            return false;
        }
        if (left && right ? this.rand.nextBoolean() : left) {
            this.setHorns(false, right);
        } else {
            this.setHorns(left, false);
        }
        ItemStack horn = item.create(ItemGoatHorn.instrumentFor(this.getUniqueID(), this.isScreaming()));
        EntityItem drop = new EntityItem(this.world, this.posX, this.posY, this.posZ, horn);
        drop.motionX = this.rand.nextFloat() * 0.4F - 0.2F;
        drop.motionY = 0.3F + this.rand.nextFloat() * 0.4F;
        drop.motionZ = this.rand.nextFloat() * 0.4F - 0.2F;
        drop.setDefaultPickupDelay();
        this.world.spawnEntity(drop);
        return true;
    }

    @Override
    protected void onGrowingAdult() {
        super.onGrowingAdult();
        this.setHorns(true, true);
    }

    public int getRamCooldown() {
        return this.ramCooldown;
    }

    public void setRamCooldown(int ticks) {
        this.ramCooldown = ticks;
    }

    public int getLongJumpCooldown() {
        return this.longJumpCooldown;
    }

    public void setLongJumpCooldown(int ticks) {
        this.longJumpCooldown = ticks;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            if (this.ramCooldown > 0) {
                this.ramCooldown--;
            }
            if (this.longJumpCooldown > 0) {
                this.longJumpCooldown--;
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsScreamingGoat", this.isScreaming());
        compound.setBoolean("HasLeftHorn", this.hasLeftHorn());
        compound.setBoolean("HasRightHorn", this.hasRightHorn());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setScreaming(compound.getBoolean("IsScreamingGoat"));
        // Goats from before the horns could be lost have both of them, unless they are kids
        boolean grown = !this.isChild();
        this.setHorns(compound.hasKey("HasLeftHorn") ? compound.getBoolean("HasLeftHorn") : grown,
                compound.hasKey("HasRightHorn") ? compound.getBoolean("HasRightHorn") : grown);
        this.ramCooldown = ramCooldown(this.rand, this.isScreaming());
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (this.rand.nextDouble() < SCREAMING_CHANCE) {
            this.setScreaming(true);
        }
        this.ramCooldown = ramCooldown(this.rand, this.isScreaming());
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        if (this.isChild()) {
            this.setHorns(false, false);
        } else if (this.rand.nextFloat() < MISSING_HORN_CHANCE) {
            boolean left = this.rand.nextBoolean();
            this.setHorns(!left, left);
        }
        return data;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.WHEAT;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        EntityGoat kid = new EntityGoat(this.world);
        EntityAgeable parent = this.rand.nextBoolean() ? this : other;
        kid.setScreaming(screamingOffspring(this.rand, parent instanceof EntityGoat && ((EntityGoat) parent).isScreaming()));
        kid.ramCooldown = ramCooldown(kid.rand, kid.isScreaming());
        kid.setHorns(false, false);
        return kid;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !this.isChild()) {
            this.playSound(this.sound("milk"), 1.0F, 1.0F);
            held.shrink(1);
            if (held.isEmpty()) {
                player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
            } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
            }
            return true;
        }
        boolean food = this.isBreedingItem(held);
        boolean handled = super.processInteract(player, hand);
        if (handled && food) {
            this.playSound(this.sound("eat"), 1.0F, this.getSoundPitch());
        }
        return handled;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return isSpawnableOn(this.world.getBlockState(pos.down())) ? 10.0F : this.world.getLightBrightness(pos) - 0.5F;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        IBlockState ground = this.world.getBlockState(pos.down());
        return isSpawnableOn(ground) && this.world.getLight(pos) > 8 && ground.canEntitySpawn(this) && this.getBlockPathWeight(pos) >= 0.0F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        super.fall(Math.max(0.0F, distance - 10.0F), damageMultiplier);
    }

    private SoundEvent sound(String kind) {
        return ModSounds.event("entity.goat." + (this.isScreaming() ? "screaming." : "") + kind);
    }

    public void playPrepareRamSound() {
        this.playSound(this.sound("prepare_ram"), 1.0F, 1.0F);
    }

    public void playRamImpactSound() {
        this.playSound(this.sound("ram_impact"), 1.0F, 1.0F);
    }

    public void playHornBreakSound() {
        this.playSound(ModSounds.event("entity.goat.horn_break"), 1.0F, 1.0F);
    }

    public void playLongJumpSound() {
        this.playSound(this.sound("long_jump"), 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.sound("ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.sound("hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.sound("death");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.event("entity.goat.step"), 0.15F, 1.0F);
    }

    public static final class GoatJump {

        private static final double GRAVITY = 0.08D;
        private static final double VERTICAL_DRAG = 0.98D;
        private static final double HORIZONTAL_DRAG = 0.91D;
        private static final int MAX_TICKS = 60;
        private static final double CLEARANCE = 1.0D;

        private GoatJump() {
        }

        /**
         * Null if the jump needs more than {@code maxSpeed}.
         */
        public static Launch plan(double dx, double dy, double dz, double maxSpeed) {
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < 0.5D) {
                return null;
            }
            Launch best = null;
            for (double vy = 0.3D; vy <= maxSpeed; vy += 0.01D) {
                int ticks = flightTime(vy, dy);
                if (ticks < 0) {
                    continue;
                }
                // the horizontal speeds of the ticks add up to v * (1 - drag^ticks) / (1 - drag)
                double horizontal = distance * (1.0D - HORIZONTAL_DRAG) / (1.0D - Math.pow(HORIZONTAL_DRAG, ticks));
                Launch launch = new Launch(horizontal * dx / distance, vy, horizontal * dz / distance, ticks);
                if (launch.speed() <= maxSpeed && (best == null || launch.speed() < best.speed())) {
                    best = launch;
                }
            }
            return best;
        }

        /**
         * -1 if it never clears the higher point by a block, or stays airborne too long.
         */
        static int flightTime(double vy, double dy) {
            double y = 0.0D;
            double speed = vy;
            double apex = 0.0D;
            for (int tick = 1; tick <= MAX_TICKS; tick++) {
                y += speed;
                apex = Math.max(apex, y);
                speed = (speed - GRAVITY) * VERTICAL_DRAG;
                if (speed < 0.0D && y <= dy) {
                    return apex >= Math.max(dy, 0.0D) + CLEARANCE ? tick : -1;
                }
            }
            return -1;
        }

        public static final class Launch {
            public final double motionX;
            public final double motionY;
            public final double motionZ;
            public final int ticks;

            Launch(double motionX, double motionY, double motionZ, int ticks) {
                this.motionX = motionX;
                this.motionY = motionY;
                this.motionZ = motionZ;
                this.ticks = ticks;
            }

            /**
             * Returns {x, y, z}.
             */
            public double[] offsetAfter(int tick) {
                double x = 0.0D;
                double y = 0.0D;
                double z = 0.0D;
                double vx = this.motionX;
                double vy = this.motionY;
                double vz = this.motionZ;
                for (int i = 0; i < tick; i++) {
                    x += vx;
                    y += vy;
                    z += vz;
                    vy = (vy - GRAVITY) * VERTICAL_DRAG;
                    vx *= HORIZONTAL_DRAG;
                    vz *= HORIZONTAL_DRAG;
                }
                return new double[]{x, y, z};
            }

            public double speed() {
                return Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            }
        }
    }
}
