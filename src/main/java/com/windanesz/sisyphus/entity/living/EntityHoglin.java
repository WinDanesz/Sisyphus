package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.ai.EntityAIAvoidBlocks;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityHoglin extends EntityAnimal implements IMob, HoglinLike {

    private static final int CONVERSION_TIME = 300;
    private static boolean resolved;
    private static Block warpedFungus;
    private static Block respawnAnchor;
    private EntityAIAvoidBlocks avoid;
    private boolean immuneToZombification;
    private int timeInOverworld;
    private int attackTicks;

    public EntityHoglin(World world) {
        super(world);
        this.setSize(1.3964844F, 1.4F);
        this.experienceValue = 5;
    }

    private static boolean isRepellent(IBlockState state) {
        if (!resolved) {
            warpedFungus = ModBlocks.getOrNull("warped_fungus");
            Block anchor = ModBlocks.get("respawn_anchor");
            respawnAnchor = anchor == Blocks.AIR ? null : anchor;
            resolved = true;
        }
        Block block = state.getBlock();
        return block == Blocks.PORTAL || (warpedFungus != null && block == warpedFungus) || (respawnAnchor != null && block == respawnAnchor);
    }

    @Nullable
    private static Item breedingItem() {
        Block fungus = ModBlocks.getOrNull("crimson_fungus");
        return fungus == null ? null : Item.getItemFromBlock(fungus);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.avoid = new EntityAIAvoidBlocks(this, EntityHoglin::isRepellent, 8, 1.0D, hoglin -> true);
        this.tasks.addTask(1, this.avoid);
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityHoglin.this.isChild() && super.shouldExecute();
            }
        });
        this.tasks.addTask(3, new EntityAIMate(this, 0.6D));
        Item fungus = breedingItem();
        if (fungus != null) {
            this.tasks.addTask(4, new EntityAITempt(this, 0.8D, fungus, false));
        }
        this.tasks.addTask(5, new EntityAIFollowParent(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.4D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10, true, false, player -> !this.isChild()));
    }

    @Override
    public int getAttackTicks() {
        return this.attackTicks;
    }

    @Override
    public boolean isBaby() {
        return this.isChild();
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == HoglinCombat.HEADBUTT) {
            this.attackTicks = 10;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.attackTicks > 0) {
            this.attackTicks--;
        }
        if (!this.world.isRemote) {
            this.tickZombification();
        }
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
        EntityZoglin zoglin = new EntityZoglin(this.world);
        zoglin.copyLocationAndAnglesFrom(this);
        zoglin.setChild(this.isChild());
        zoglin.setNoAI(this.isAIDisabled());
        if (this.hasCustomName()) {
            zoglin.setCustomNameTag(this.getCustomNameTag());
            zoglin.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
        }
        zoglin.enablePersistence();
        this.world.spawnEntity(zoglin);
        this.setDead();
        zoglin.playSound(ModSounds.event("entity.hoglin.converted_to_zombified"), 1.0F, 1.0F);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("IsImmuneToZombification", this.immuneToZombification);
        compound.setInteger("TimeInOverworld", this.timeInOverworld);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.immuneToZombification = compound.getBoolean("IsImmuneToZombification");
        this.timeInOverworld = compound.getInteger("TimeInOverworld");
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        if (this.rand.nextFloat() < 0.2F) {
            this.setGrowingAge(-24000);
        }
        return super.onInitialSpawn(difficulty, data);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Item fungus = breedingItem();
        return fungus != null && stack.getItem() == fungus;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable other) {
        return new EntityHoglin(this.world);
    }

    @Override
    public void setInLove(@Nullable EntityPlayer player) {
        super.setInLove(player);
        this.enablePersistence();
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
    protected boolean canDespawn() {
        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (this.isChild()) {
            return false;
        }
        return HoglinCombat.headbutt(this, target, (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(),
                ModSounds.event("entity.hoglin.attack"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.avoid != null && this.avoid.isRunning()) {
            return ModSounds.event("entity.hoglin.retreat");
        }
        return ModSounds.event(this.getAttackTarget() != null ? "entity.hoglin.angry" : "entity.hoglin.ambient");
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.event("entity.hoglin.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.event("entity.hoglin.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, Block block) {
        this.playSound(ModSounds.event("entity.hoglin.step"), 0.15F, 1.0F);
    }

    @Override
    protected boolean canDropLoot() {
        return true;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return this.isChild() ? 0 : 5;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/hoglin");
    }
}
