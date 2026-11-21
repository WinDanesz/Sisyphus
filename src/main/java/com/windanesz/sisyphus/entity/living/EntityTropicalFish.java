package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityTropicalFish extends EntitySchoolingFish {

    public static final String[] PATTERNS = {"kob", "sunstreak", "snooper", "dasher", "brinely", "spotty", "flopper", "stripey", "glitter", "blockfish", "betty", "clayfish"};
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityTropicalFish.class, DataSerializers.VARINT);
    private static final int[][] COMMON_VARIANTS = {
            variantSpec(7, EnumDyeColor.ORANGE, EnumDyeColor.GRAY),
            variantSpec(6, EnumDyeColor.GRAY, EnumDyeColor.GRAY),
            variantSpec(6, EnumDyeColor.GRAY, EnumDyeColor.BLUE),
            variantSpec(11, EnumDyeColor.WHITE, EnumDyeColor.GRAY),
            variantSpec(1, EnumDyeColor.BLUE, EnumDyeColor.GRAY),
            variantSpec(0, EnumDyeColor.ORANGE, EnumDyeColor.WHITE),
            variantSpec(5, EnumDyeColor.PINK, EnumDyeColor.LIGHT_BLUE),
            variantSpec(9, EnumDyeColor.PURPLE, EnumDyeColor.YELLOW),
            variantSpec(11, EnumDyeColor.WHITE, EnumDyeColor.RED),
            variantSpec(5, EnumDyeColor.WHITE, EnumDyeColor.YELLOW),
            variantSpec(8, EnumDyeColor.WHITE, EnumDyeColor.GRAY),
            variantSpec(11, EnumDyeColor.WHITE, EnumDyeColor.ORANGE),
            variantSpec(3, EnumDyeColor.CYAN, EnumDyeColor.PINK),
            variantSpec(4, EnumDyeColor.LIME, EnumDyeColor.LIGHT_BLUE),
            variantSpec(10, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variantSpec(2, EnumDyeColor.GRAY, EnumDyeColor.RED),
            variantSpec(9, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variantSpec(6, EnumDyeColor.WHITE, EnumDyeColor.YELLOW),
            variantSpec(0, EnumDyeColor.RED, EnumDyeColor.WHITE),
            variantSpec(1, EnumDyeColor.GRAY, EnumDyeColor.WHITE),
            variantSpec(3, EnumDyeColor.CYAN, EnumDyeColor.YELLOW),
            variantSpec(6, EnumDyeColor.YELLOW, EnumDyeColor.YELLOW)
    };

    private boolean isSchool = true;

    public EntityTropicalFish(World world) {
        super(world);
        this.setSize(0.5F, 0.4F);
    }

    private static int[] variantSpec(int patternId, EnumDyeColor base, EnumDyeColor pattern) {
        return new int[]{patternId, base.getMetadata(), pattern.getMetadata()};
    }

    public static int packVariant(int patternId, int baseColor, int patternColor) {
        int shape = patternId / 6;
        int index = patternId % 6;
        return shape | index << 8 | (baseColor & 255) << 16 | (patternColor & 255) << 24;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, packVariant(0, EnumDyeColor.WHITE.getMetadata(), EnumDyeColor.WHITE.getMetadata()));
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    /**
     * 0 for the small body shape, 1 for the large one.
     */
    public int getShape() {
        return this.getVariant() & 255;
    }

    /**
     * 0-5 within the body shape.
     */
    public int getPatternIndex() {
        return (this.getVariant() >> 8) & 255;
    }

    public EnumDyeColor getBaseColor() {
        return EnumDyeColor.byMetadata((this.getVariant() >> 16) & 255);
    }

    public EnumDyeColor getPatternColor() {
        return EnumDyeColor.byMetadata((this.getVariant() >> 24) & 255);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.setVariant(tag.getInteger("Variant"));
    }

    @Override
    protected String getLootName() {
        return "tropical_fish";
    }

    @Override
    public int getMaxSchoolSize() {
        return 9;
    }

    @Override
    public boolean canBeFollowed() {
        return this.isSchool && super.canBeFollowed();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        data = super.onInitialSpawn(difficulty, data);
        if (data instanceof TropicalFishSpawnData) {
            this.setVariant(((TropicalFishSpawnData) data).variant);
        } else if (this.rand.nextFloat() < 0.9F) {
            int[] spec = COMMON_VARIANTS[this.rand.nextInt(COMMON_VARIANTS.length)];
            int variant = packVariant(spec[0], spec[1], spec[2]);
            this.setVariant(variant);
            data = new TropicalFishSpawnData(this, variant);
        } else {
            this.isSchool = false;
            this.setVariant(packVariant(this.rand.nextInt(PATTERNS.length), this.rand.nextInt(16), this.rand.nextInt(16)));
        }
        return data;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return ModSounds.ENTITY_TROPICAL_FISH_FLOP;
    }

    public static class TropicalFishSpawnData extends SchoolSpawnData {

        public final int variant;

        public TropicalFishSpawnData(EntitySchoolingFish leader, int variant) {
            super(leader);
            this.variant = variant;
        }
    }
}
