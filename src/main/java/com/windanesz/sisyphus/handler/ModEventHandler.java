package com.windanesz.sisyphus.handler;

import com.google.common.collect.ImmutableSet;
import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ItemFlowerDye;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ModEventHandler {

    public static final String SIGN_COLOR_KEY = "SisyphusBackportSignColor";
    private static final String[] NETHERITE_ARMOR = {"netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots"};
    private static final Set<EntityLiving> TEMPTED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<EntityLiving> ILLAGERS = Collections.newSetFromMap(new WeakHashMap<>());

    private ModEventHandler() {
    } // no instances

    @Nullable
    private static SoundEvent instrumentFor(Block block) {
        if (block == Blocks.IRON_BLOCK) {
            return ModSounds.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
        } else if (block == Blocks.SOUL_SAND) {
            return ModSounds.BLOCK_NOTE_BLOCK_COW_BELL;
        } else if (block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN) {
            return ModSounds.BLOCK_NOTE_BLOCK_DIDGERIDOO;
        } else if (block == Blocks.EMERALD_BLOCK) {
            return ModSounds.BLOCK_NOTE_BLOCK_BIT;
        } else if (block == Blocks.HAY_BLOCK) {
            return ModSounds.BLOCK_NOTE_BLOCK_BANJO;
        } else if (block == Blocks.GLOWSTONE) {
            return ModSounds.BLOCK_NOTE_BLOCK_PLING;
        }
        return null;
    }

    @SubscribeEvent
    public static void onNotePlay(NoteBlockEvent.Play event) {
        if (!Settings.functionalBlocks.enableNoteBlockSounds) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        SoundEvent sound = instrumentFor(world.getBlockState(pos.down()).getBlock());
        if (sound == null) {
            return;
        }

        // Replace the vanilla instrument. The sound only reaches players from the server, the particle only the client
        int note = event.getVanillaNoteId();
        float pitch = (float) Math.pow(2.0D, (note - 12) / 12.0D);
        world.playSound(null, pos, sound, SoundCategory.RECORDS, 3.0F, pitch);
        world.spawnParticle(EnumParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, note / 24.0D, 0.0D, 0.0D);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDyeSign(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.buildingBlocks.enableSigns) {
            return;
        }
        ItemStack stack = event.getItemStack();
        EnumDyeColor color = ItemFlowerDye.colorOf(stack);
        if (color == null) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntitySign)) {
            return;
        }

        if (!world.isRemote) {
            TileEntitySign sign = (TileEntitySign) tile;
            if (!sign.getTileData().hasKey(SIGN_COLOR_KEY) || sign.getTileData().getInteger(SIGN_COLOR_KEY) != color.getMetadata()) {
                sign.getTileData().setInteger(SIGN_COLOR_KEY, color.getMetadata());
                sign.markDirty();
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    public static boolean needsMending(ItemStack stack) {
        return !stack.isEmpty() && stack.isItemDamaged() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0;
    }

    public static List<ItemStack> wornMendingItems(EntityLivingBase entity) {
        List<ItemStack> worn = new ArrayList<>();
        for (ItemStack stack : Enchantments.MENDING.getEntityEquipment(entity)) {
            if (needsMending(stack)) {
                worn.add(stack);
            }
        }
        return worn;
    }

    private static int roundAverage(float value, Random rand) {
        int floor = (int) Math.floor(value);
        return floor + (rand.nextFloat() < value - floor ? 1 : 0);
    }

    /**
     * Returns the experience left over.
     */
    public static int repair(List<ItemStack> items, int xp, Random rand) {
        List<ItemStack> worn = new ArrayList<>(items);
        while (xp > 0 && !worn.isEmpty()) {
            ItemStack stack = worn.get(rand.nextInt(worn.size()));
            float ratio = stack.getItem().getXpRepairRatio(stack);
            int repaired = ratio > 0.0F ? Math.min(roundAverage(xp * ratio, rand), stack.getItemDamage()) : 0;
            if (repaired <= 0) {
                // Nothing to repair, or it cannot be repaired with experience: it is left out
                worn.remove(stack);
                continue;
            }
            xp -= roundAverage(repaired / ratio, rand);
            stack.setItemDamage(stack.getItemDamage() - repaired);
            if (!stack.isItemDamaged()) {
                worn.remove(stack);
            }
        }
        return xp;
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerPickupXpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!Settings.gameplay.enableMendingRepairFirst || player.world.isRemote) {
            return;
        }
        List<ItemStack> worn = wornMendingItems(player);
        if (worn.isEmpty()) {
            // Nothing to repair: the game does what it did
            return;
        }
        EntityXPOrb orb = event.getOrb();
        event.setCanceled(true);
        player.xpCooldown = 2;
        player.onItemPickup(orb, 1);
        orb.xpValue = repair(worn, orb.xpValue, player.getRNG());
        if (orb.xpValue > 0) {
            player.addExperience(orb.xpValue);
        }
        orb.setDead();
    }

    public static boolean isHoeBlock(Block block) {
        if (block instanceof BlockLeaves || block == Blocks.HAY_BLOCK || block == Blocks.SPONGE || block == Blocks.NETHER_WART_BLOCK) {
            return true;
        }
        return matches(block, ModBlocks.getOrNull("warped_wart_block")) || matches(block, ModBlocks.get("shroomlight")) || matches(block, ModBlocks.get("target"));
    }

    private static boolean matches(Block block, @Nullable Block other) {
        return other != null && other != Blocks.AIR && block == other;
    }

    public static float tierSpeed(ItemStack hoe) {
        try {
            return Item.ToolMaterial.valueOf(((ItemHoe) hoe.getItem()).getMaterialName()).getEfficiency();
        } catch (IllegalArgumentException e) {
            return Item.ToolMaterial.WOOD.getEfficiency();
        }
    }

    public static float hoeSpeed(ItemStack hoe) {
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, hoe);
        return tierSpeed(hoe) + (efficiency > 0 ? efficiency * efficiency + 1 : 0);
    }

    private static boolean hoeApplies(EntityPlayer player, Block block) {
        return Settings.gameplay.enableHoeEnchanting && player.getHeldItemMainhand().getItem() instanceof ItemHoe && isHoeBlock(block);
    }

    @SubscribeEvent
    public static void onHoeBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (hoeApplies(event.getEntityPlayer(), event.getState().getBlock())) {
            // The speed of a hand is 1, and haste, water and the air change it, so it is the speed of the hoe times those
            event.setNewSpeed(event.getNewSpeed() * hoeSpeed(event.getEntityPlayer().getHeldItemMainhand()));
        }
    }

    @SubscribeEvent
    public static void onHoeBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player != null && !player.capabilities.isCreativeMode && !event.getWorld().isRemote && hoeApplies(player, event.getState().getBlock())) {
            player.getHeldItemMainhand().damageItem(1, player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote || event.player.ticksExisted % 20 != 0
                || !(event.player instanceof EntityPlayerMP) || !Settings.extras.enableAdvancements) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        Item debris = Item.getItemFromBlock(ModBlocks.get("ancient_debris"));
        Item crying = Item.getItemFromBlock(ModBlocks.get("crying_obsidian"));
        Item blackstone = Item.getItemFromBlock(ModBlocks.get("blackstone"));
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == debris) {
                AdvancementHelper.grant(player, "hidden_in_the_depths");
            } else if (stack.getItem() == crying) {
                AdvancementHelper.grant(player, "who_is_cutting_onions");
            } else if (stack.getItem() == blackstone) {
                // Blackstone counts as stone for Stone Age
                AdvancementHelper.grantVanilla(player, "story/mine_stone", "get_stone");
            }
        }
        for (String piece : NETHERITE_ARMOR) {
            Item armor = ModItems.get(piece);
            if (armor == null || !wearing(player, armor)) {
                return;
            }
        }
        AdvancementHelper.grant(player, "cover_me_in_debris");
    }

    private static boolean wearing(EntityPlayerMP player, Item armor) {
        for (ItemStack worn : player.inventory.armorInventory) {
            if (worn.getItem() == armor) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onAnimalJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !Settings.gameplay.enableAnimalsFollowingFood || !(event.getEntity() instanceof AbstractHorse)) {
            return;
        }
        AbstractHorse animal = (AbstractHorse) event.getEntity();
        Set<Item> food;
        if (animal instanceof EntityLlama) {
            food = Food.LLAMA;
        } else if (animal instanceof EntityHorse || animal instanceof EntityDonkey || animal instanceof EntityMule) {
            food = Food.HORSE;
        } else {
            return;
        }
        if (TEMPTED.add(animal)) {
            animal.tasks.addTask(3, new FollowFood(animal, food));
        }
    }

    @SubscribeEvent
    public static void onIllagerTarget(LivingSetAttackTargetEvent event) {
        if (!Settings.gameplay.enableIllagersSparingBabies || !(event.getEntityLiving() instanceof AbstractIllager)) {
            return;
        }
        if (event.getTarget() instanceof EntityVillager && event.getTarget().isChild()) {
            ((EntityLiving) event.getEntityLiving()).setAttackTarget(null);
        }
    }

    /**
     * The nearest villager may be a baby, which an illager would keep picking over the grown ones behind it, so it also looks for grown
     * villagers.
     */
    @SubscribeEvent
    public static void onIllagerJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !Settings.gameplay.enableIllagersSparingBabies || !(event.getEntity() instanceof AbstractIllager)) {
            return;
        }
        AbstractIllager illager = (AbstractIllager) event.getEntity();
        if (ILLAGERS.add(illager)) {
            illager.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(illager, EntityVillager.class, 10, true, false,
                    villager -> villager != null && !villager.isChild()));
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!Settings.gameplay.enableDarkOnlySpawning || event.getSpawner() != null) {
            return;
        }
        EntityLivingBase mob = event.getEntityLiving();
        World world = event.getWorld();
        if (!(mob instanceof EntityMob) || mob instanceof EntityPigZombie || mob instanceof EntityBlaze
                || world.provider.getDimensionType() != DimensionType.OVERWORLD) {
            return;
        }
        if (world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(event.getX(), event.getY(), event.getZ())) > 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static final class Food {
        static final Set<Item> HORSE = ImmutableSet.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE);
        static final Set<Item> LLAMA = ImmutableSet.of(Item.getItemFromBlock(Blocks.HAY_BLOCK));
    }

    private static final class FollowFood extends EntityAITempt {

        private final EntityCreature animal;

        FollowFood(EntityCreature animal, Set<Item> food) {
            super(animal, 1.25D, false, food);
            this.animal = animal;
        }

        @Override
        public boolean shouldExecute() {
            return !this.animal.isBeingRidden() && super.shouldExecute();
        }
    }
}
