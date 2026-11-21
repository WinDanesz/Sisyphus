package com.windanesz.sisyphus.enchantment;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.particle.SoulParticles;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EnchantmentSoulSpeed extends Enchantment {

    private static final UUID BOOST_ID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
    private static final Set<EntityLivingBase> ON_SAND = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public EnchantmentSoulSpeed() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.ARMOR_FEET, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET});
    }

    private static boolean isSoulBlock(Block block) {
        return block == Blocks.SOUL_SAND || ModBlocks.isSoulSoil(block);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (ModEnchantments.SOUL_SPEED == null) {
            return;
        }
        EntityLivingBase entity = event.getEntityLiving();
        IAttributeInstance speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        AttributeModifier current = speed.getModifier(BOOST_ID);
        int level = EnchantmentHelper.getMaxEnchantmentLevel(ModEnchantments.SOUL_SPEED, entity);
        Block below = null;
        if (level > 0 && entity.onGround) {
            Block block = entity.world.getBlockState(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.5000001D, entity.posZ)).getBlock();
            if (isSoulBlock(block)) {
                below = block;
            }
        }
        if (below == null) {
            if (current != null) {
                speed.removeModifier(BOOST_ID);
            }
            ON_SAND.remove(entity);
            return;
        }

        double amount = 0.03D * (1.0D + level * 0.35D);
        if (current == null || current.getAmount() != amount) {
            if (current != null) {
                speed.removeModifier(BOOST_ID);
            }
            speed.applyModifier(new AttributeModifier(BOOST_ID, "Soul speed boost", amount, 0).setSaved(false));
        }

        if (below == Blocks.SOUL_SAND) {
            // soul sand slows down whatever stands on it to 40% in every tick, which is undone here
            if (ON_SAND.contains(entity)) {
                entity.motionX /= 0.4D;
                entity.motionZ /= 0.4D;
            }
            ON_SAND.add(entity);
        } else {
            ON_SAND.remove(entity);
        }

        boolean moving = entity.motionX * entity.motionX + entity.motionZ * entity.motionZ > 1.0E-4D;
        if (moving) {
            if (entity.world.isRemote) {
                SoulParticles.spawn(entity);
            } else if (entity.getRNG().nextFloat() < 0.04F) {
                ItemStack boots = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                if (!boots.isEmpty()) {
                    boots.damageItem(1, entity);
                }
            }
        }
    }

    @Override
    public int getMinEnchantability(int level) {
        return level * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }
}
