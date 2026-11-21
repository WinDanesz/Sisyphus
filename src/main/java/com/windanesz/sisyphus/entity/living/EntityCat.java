package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ItemFlowerDye;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityCat extends EntityOcelot {

    public static final int VILLAGE_CATS = 5;
    public static final int VILLAGE_RADIUS = 48;
    public static final String[] TYPES = {"tabby", "black", "red", "siamese", "british_shorthair", "calico", "persian", "ragdoll", "white", "jellie", "all_black"};
    private static final int CAT_SPAWN_INTERVAL = 1200;
    private static final int CAT_MIN_DISTANCE = 8;
    private static final int CAT_MAX_DISTANCE = 32;
    private static final String TAMED_KEY = "SisyphusBackportCatsTamed";
    private static final ItemStack[] GIFTS = {new ItemStack(Items.CHICKEN), new ItemStack(Items.FEATHER), new ItemStack(Items.RABBIT_HIDE),
            new ItemStack(Items.RABBIT_FOOT), new ItemStack(Items.ROTTEN_FLESH)};
    private static final DataParameter<Integer> CAT_TYPE = EntityDataManager.createKey(EntityCat.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> COLLAR = EntityDataManager.createKey(EntityCat.class, DataSerializers.VARINT);

    public EntityCat(World world) {
        super(world);
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!Settings.mobs.enableCats || player.world.isRemote || !event.updateWorld()) {
            return;
        }
        List<EntityCat> cats = player.world.getEntitiesWithinAABB(EntityCat.class, new AxisAlignedBB(player.getPosition()).grow(10.0D));
        for (EntityCat cat : cats) {
            if (cat.isTamed() && cat.isOwner(player) && cat.getRNG().nextFloat() < 0.7F) {
                ItemStack gift = GIFTS[cat.getRNG().nextInt(GIFTS.length)].copy();
                player.world.spawnEntity(new EntityItem(player.world, cat.posX, cat.posY + 0.5D, cat.posZ, gift));
            }
        }
    }

    /**
     * Cats are tamed by the ocelot code, so taming is detected here instead.
     */
    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (!Settings.mobs.enableCats || !(event.getAnimal() instanceof EntityCat) || !(event.getTamer() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getTamer();
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted = data.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();
        int mask = persisted.getInteger(TAMED_KEY) | (1 << ((EntityCat) event.getAnimal()).getCatType());
        persisted.setInteger(TAMED_KEY, mask);
        data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        if (mask == (1 << TYPES.length) - 1) {
            AdvancementHelper.grant(player, "complete_catalogue");
        }
    }

    public static boolean needsCats(int catsAround) {
        return catsAround < VILLAGE_CATS;
    }

    /**
     * The world generator only knows villages once a new chunk has generated since load, so village doors are checked too.
     */
    public static boolean isInVillage(World world, BlockPos pos) {
        if (world.getVillageCollection() != null && world.getVillageCollection().getNearestVillage(pos, 0) != null) {
            return true;
        }
        return world.getChunkProvider() instanceof ChunkProviderServer
                && ((ChunkProviderServer) world.getChunkProvider()).chunkGenerator.isInsideStructure(world, "Village", pos);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote || !Settings.mobs.enableCats || world.playerEntities.isEmpty()
                || world.getTotalWorldTime() % CAT_SPAWN_INTERVAL != 0 || !world.getGameRules().getBoolean("doMobSpawning")
                || !(world instanceof WorldServer) || !((WorldServer) world).getMinecraftServer().getCanSpawnAnimals()) {
            return;
        }
        Random rand = world.rand;
        EntityPlayer player = world.playerEntities.get(rand.nextInt(world.playerEntities.size()));
        if (player.isSpectator()) {
            return;
        }
        BlockPos pos = world.getHeight(new BlockPos(player).add(catOffset(rand), 0, catOffset(rand)));
        if (!world.isAreaLoaded(pos, 10) || !WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, pos)
                || !isInVillage(world, pos)) {
            return;
        }
        AxisAlignedBB around = new AxisAlignedBB(pos).grow(VILLAGE_RADIUS, 8.0D, VILLAGE_RADIUS);
        if (!needsCats(world.getEntitiesWithinAABB(EntityCat.class, around).size())) {
            return;
        }
        EntityCat cat = new EntityCat(world);
        cat.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
        if (cat.isNotColliding()) {
            cat.onInitialSpawn(world.getDifficultyForLocation(pos), null);
            world.spawnEntity(cat);
        }
    }

    static int catOffset(Random rand) {
        int distance = CAT_MIN_DISTANCE + rand.nextInt(CAT_MAX_DISTANCE - CAT_MIN_DISTANCE);
        return rand.nextBoolean() ? distance : -distance;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CAT_TYPE, 0);
        this.dataManager.register(COLLAR, EnumDyeColor.RED.getMetadata());
    }

    public int getCatType() {
        return this.dataManager.get(CAT_TYPE);
    }

    public void setCatType(int type) {
        this.dataManager.set(CAT_TYPE, Math.max(0, Math.min(type, TYPES.length - 1)));
    }

    public EnumDyeColor getCollarColor() {
        return EnumDyeColor.byMetadata(this.dataManager.get(COLLAR));
    }

    public void setCollarColor(EnumDyeColor color) {
        this.dataManager.set(COLLAR, color.getMetadata());
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(SisyphusBackport.MODID, "textures/entity/cat/" + TYPES[this.getCatType()] + ".png");
    }

    @Override
    public void setTameSkin(int skin) {
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        this.setCatType(this.rand.nextInt(TYPES.length));
        return data;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EnumDyeColor color = ItemFlowerDye.colorOf(stack);
        if (color != null && this.isTamed() && this.isOwner(player)) {
            if (color != this.getCollarColor()) {
                this.setCollarColor(color);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public EntityCat createChild(EntityAgeable other) {
        EntityCat kitten = new EntityCat(this.world);
        int type = other instanceof EntityCat && this.rand.nextBoolean() ? ((EntityCat) other).getCatType() : this.getCatType();
        kitten.setCatType(type);
        if (this.isTamed()) {
            kitten.setOwnerId(this.getOwnerId());
            kitten.setTamed(true);
        }
        return kitten;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(SisyphusBackport.MODID, "entities/cat");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("CatType", this.getCatType());
        compound.setByte("CollarColor", (byte) this.getCollarColor().getMetadata());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCatType(compound.getInteger("CatType"));
        if (compound.hasKey("CollarColor", 99)) {
            this.setCollarColor(EnumDyeColor.byMetadata(compound.getByte("CollarColor")));
        }
    }
}
