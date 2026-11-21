package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.projectile.EntityCrossbowFirework;
import com.windanesz.sisyphus.entity.projectile.EntityPiercingArrow;
import com.windanesz.sisyphus.registry.ModEnchantments;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class ItemCrossbow extends Item {

    public static final String FROM_CROSSBOW = "FromCrossbow";

    private static final String KILLED = "KilledTypes";

    private static final String CHARGED = "Charged";
    private static final String PROJECTILES = "ChargedProjectiles";

    public ItemCrossbow() {
        this.setMaxStackSize(1);
        this.setMaxDamage(Settings.items.crossbow.durability);
        this.setCreativeTab(CreativeTabs.COMBAT);

        this.addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) -> {
            if (entity == null || isCharged(stack)) {
                return 0.0F;
            }
            return (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / (float) getChargeTime(stack);
        });
        this.addPropertyOverride(new ResourceLocation("pulling"), (stack, world, entity) ->
                entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack && !isCharged(stack) ? 1.0F : 0.0F);
        this.addPropertyOverride(new ResourceLocation("charged"), (stack, world, entity) -> isCharged(stack) ? 1.0F : 0.0F);
        this.addPropertyOverride(new ResourceLocation("firework"), (stack, world, entity) ->
                isCharged(stack) && containsProjectile(stack, Items.FIREWORKS) ? 1.0F : 0.0F);
    }

    public static boolean isCharged(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(CHARGED);
    }

    private static void setCharged(ItemStack stack, boolean charged) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        tag.setBoolean(CHARGED, charged);
        stack.setTagCompound(tag);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = new ArrayList<>();
        if (crossbow.hasTagCompound()) {
            NBTTagList tags = crossbow.getTagCompound().getTagList(PROJECTILES, 10);
            for (int i = 0; i < tags.tagCount(); i++) {
                ItemStack stack = new ItemStack(tags.getCompoundTagAt(i));
                if (!stack.isEmpty()) {
                    list.add(stack);
                }
            }
        }
        return list;
    }

    private static void setProjectiles(ItemStack crossbow, List<ItemStack> projectiles) {
        NBTTagCompound tag = crossbow.hasTagCompound() ? crossbow.getTagCompound() : new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (ItemStack projectile : projectiles) {
            list.appendTag(projectile.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag(PROJECTILES, list);
        crossbow.setTagCompound(tag);
    }

    private static boolean containsProjectile(ItemStack crossbow, Item item) {
        for (ItemStack projectile : getProjectiles(crossbow)) {
            if (projectile.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static int getChargeTime(ItemStack stack) {
        int level = ModEnchantments.QUICK_CHARGE == null ? 0 : EnchantmentHelper.getEnchantmentLevel(ModEnchantments.QUICK_CHARGE, stack);
        int reload = Settings.items.crossbow.reloadTime;
        return Math.max(1, reload - reload * level / 5);
    }

    private static float getPower(int useTicks, ItemStack stack) {
        return Math.min(1.0F, (float) useTicks / (float) getChargeTime(stack));
    }

    private static boolean isProjectile(ItemStack stack) {
        return stack.getItem() instanceof ItemArrow || stack.getItem() == Items.FIREWORKS;
    }

    private static ItemStack findAmmo(EntityPlayer player, ItemStack crossbow) {
        for (EnumHand hand : EnumHand.values()) {
            ItemStack held = player.getHeldItem(hand);
            if (held != crossbow && isProjectile(held)) {
                return held;
            }
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (isProjectile(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void playLoadSound(EntityLivingBase entity, SoundEvent sound, float volume) {
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.PLAYERS, volume, 1.0F);
    }

    private static boolean load(EntityPlayer player, ItemStack crossbow) {
        boolean multishot = ModEnchantments.MULTISHOT != null && EnchantmentHelper.getEnchantmentLevel(ModEnchantments.MULTISHOT, crossbow) > 0;
        int count = multishot ? 3 : 1;
        boolean creative = player.capabilities.isCreativeMode;

        ItemStack ammo = findAmmo(player, crossbow);
        if (ammo.isEmpty() && creative) {
            ammo = new ItemStack(Items.ARROW);
        }
        if (ammo.isEmpty()) {
            return false;
        }

        List<ItemStack> projectiles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ItemStack single = ammo.copy();
            single.setCount(1);
            if (i > 0) {
                // The extra projectiles of Multishot cannot be picked up again
                NBTTagCompound tag = single.hasTagCompound() ? single.getTagCompound() : new NBTTagCompound();
                tag.setBoolean("Extra", true);
                single.setTagCompound(tag);
            }
            projectiles.add(single);
        }

        if (!creative) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.inventory.deleteStack(ammo);
            }
        }
        setProjectiles(crossbow, projectiles);
        return true;
    }

    private static void shoot(World world, EntityPlayer player, EnumHand hand, ItemStack crossbow) {
        List<ItemStack> projectiles = getProjectiles(crossbow);
        Random random = player.getRNG();
        float[] pitches = {1.0F, 0.9F, 1.1F};
        float[] angles = {0.0F, -10.0F, 10.0F};
        boolean firework = false;

        for (int i = 0; i < projectiles.size(); i++) {
            ItemStack projectile = projectiles.get(i);
            boolean extra = projectile.hasTagCompound() && projectile.getTagCompound().getBoolean("Extra");
            if (projectile.hasTagCompound()) {
                projectile.getTagCompound().removeTag("Extra");
                if (projectile.getTagCompound().getKeySet().isEmpty()) {
                    projectile.setTagCompound(null);
                }
            }
            float angle = projectiles.size() > 1 ? angles[i % 3] : 0.0F;

            if (projectile.getItem() == Items.FIREWORKS) {
                firework = true;
                Vec3d look = player.getLook(1.0F).rotateYaw((float) Math.toRadians(-angle));
                EntityCrossbowFirework rocket = new EntityCrossbowFirework(world, player.posX, player.posY + player.getEyeHeight() - 0.15D,
                        player.posZ, projectile, look.scale(1.6D));
                world.spawnEntity(rocket);
            } else if (projectile.getItem() instanceof ItemArrow) {
                int pierce = ModEnchantments.PIERCING == null ? 0 : EnchantmentHelper.getEnchantmentLevel(ModEnchantments.PIERCING, crossbow);
                EntityArrow arrow;
                if (pierce > 0 && !(projectile.getItem() instanceof ItemSpectralArrow)) {
                    // Arrows that go through mobs are an entity of their own, so the client knows about them too
                    EntityPiercingArrow piercing = new EntityPiercingArrow(world, player);
                    piercing.setPotionEffect(projectile);
                    piercing.setPierceLevel(pierce);
                    arrow = piercing;
                } else {
                    arrow = ((ItemArrow) projectile.getItem()).createArrow(world, projectile, player);
                }
                arrow.shoot(player, player.rotationPitch, player.rotationYaw + angle, 0.0F, 3.15F, 1.0F);
                arrow.setIsCritical(true);
                arrow.setDamage(arrow.getDamage() * Settings.items.crossbow.damageMultiplier);
                if (extra || player.capabilities.isCreativeMode) {
                    arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                }
                arrow.getEntityData().setBoolean(FROM_CROSSBOW, true);
                world.spawnEntity(arrow);
            }
            world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F,
                    pitches[i % 3] / (random.nextFloat() * 0.5F + 1.8F) + 0.5F);
        }

        setProjectiles(crossbow, new ArrayList<>());
        setCharged(crossbow, false);
        crossbow.damageItem(firework ? 3 : 1, player);
        if (player instanceof EntityPlayerMP) {
            AdvancementHelper.grant((EntityPlayerMP) player, "ol_betsy");
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!Settings.items.crossbow.enabled || event.getEntityLiving().world.isRemote
                || !(event.getSource().getImmediateSource() instanceof EntityArrow)) {
            return;
        }
        EntityArrow arrow = (EntityArrow) event.getSource().getImmediateSource();
        NBTTagCompound data = arrow.getEntityData();
        if (!data.getBoolean(FROM_CROSSBOW)) {
            return;
        }
        ResourceLocation key = EntityList.getKey(event.getEntityLiving());
        if (key == null) {
            return;
        }
        NBTTagList list = data.getTagList(KILLED, 8);
        Set<String> types = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            types.add(list.getStringTagAt(i));
        }
        if (types.add(key.toString())) {
            list.appendTag(new NBTTagString(key.toString()));
            data.setTag(KILLED, list);
        }
        if (types.size() >= 5 && arrow.shootingEntity instanceof EntityPlayerMP) {
            AdvancementHelper.grant((EntityPlayerMP) arrow.shootingEntity, "arbalistic");
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (isCharged(stack)) {
            if (!world.isRemote) {
                shoot(world, player, hand, stack);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (!findAmmo(player, stack).isEmpty() || player.capabilities.isCreativeMode) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        if (entity.world.isRemote || isCharged(stack)) {
            return;
        }
        int used = this.getMaxItemUseDuration(stack) - count;
        int chargeTime = getChargeTime(stack);
        if (used == 0) {
            playLoadSound(entity, ModSounds.ITEM_CROSSBOW_LOADING_START, 0.5F);
        } else if (used == chargeTime / 2 && chargeTime > 4) {
            playLoadSound(entity, ModSounds.ITEM_CROSSBOW_LOADING_MIDDLE, 0.5F);
        }
        // Loaded: stop using the item, so it cannot be held any longer
        if (entity instanceof EntityPlayer && getPower(used, stack) >= 1.0F && load((EntityPlayer) entity, stack)) {
            setCharged(stack, true);
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSounds.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0F,
                    1.0F / (itemRand.nextFloat() * 0.5F + 1.0F) + 0.2F);
            entity.stopActiveHand();
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        int used = this.getMaxItemUseDuration(stack) - timeLeft;
        if (!world.isRemote && entity instanceof EntityPlayer && getPower(used, stack) >= 1.0F && !isCharged(stack)
                && load((EntityPlayer) entity, stack)) {
            setCharged(stack, true);
            world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSounds.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0F,
                    1.0F / (itemRand.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == Items.IRON_INGOT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (isCharged(stack)) {
            for (ItemStack projectile : getProjectiles(stack)) {
                tooltip.add(I18n.format("item.crossbow.projectile") + " " + projectile.getDisplayName());
            }
        }
    }
}
