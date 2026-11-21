package com.windanesz.sisyphus.entity.living;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ItemSuspiciousStew;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class EntityBrownMooshroom extends EntityMooshroom {

    private static final String STEW_EFFECT = "StewEffectId";
    private static final String STEW_DURATION = "StewEffectDuration";

    private int stewEffectId = -1;
    private int stewEffectDuration;

    public EntityBrownMooshroom(World world) {
        super(world);
    }

    @SubscribeEvent
    public static void onLightning(EntityStruckByLightningEvent event) {
        if (!Settings.mobs.enableBrownMooshroom) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity.world.isRemote) {
            return;
        }
        EntityMooshroom replacement;
        if (entity instanceof EntityBrownMooshroom) {
            replacement = new EntityMooshroom(entity.world);
        } else if (entity instanceof EntityMooshroom) {
            replacement = new EntityBrownMooshroom(entity.world);
        } else {
            return;
        }
        // Copy everything but the identity over to the other kind
        NBTTagCompound tag = entity.writeToNBT(new NBTTagCompound());
        tag.removeTag("UUIDMost");
        tag.removeTag("UUIDLeast");
        tag.removeTag("id");
        replacement.readFromNBT(tag);
        replacement.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        entity.world.spawnEntity(replacement);
        entity.setDead();
        event.setCanceled(true);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Feeding a flower
        if (!this.isChild() && this.stewEffectId < 0 && !stack.isEmpty()) {
            Object[] effect = ItemSuspiciousStew.effectOf(stack);
            if (effect != null) {
                this.stewEffectId = Potion.getIdFromPotion((Potion) effect[0]);
                this.stewEffectDuration = (Integer) effect[1];
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                for (int i = 0; i < 4; i++) {
                    this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
                            this.posY + this.height / 2.0D + this.rand.nextDouble() * 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width, 0.5D, 0.5D, 0.0D);
                }
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0F, 1.0F);
                return true;
            }
        }

        // Milking with a bowl
        if (stack.getItem() == Items.BOWL && this.getGrowingAge() >= 0 && !player.capabilities.isCreativeMode) {
            ItemStack result;
            if (this.stewEffectId >= 0) {
                result = new ItemStack(ModItems.get("suspicious_stew"));
                ItemSuspiciousStew.addEffect(result, Potion.getPotionById(this.stewEffectId), this.stewEffectDuration);
                this.stewEffectId = -1;
            } else {
                result = new ItemStack(Items.MUSHROOM_STEW);
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setHeldItem(hand, result);
            } else if (!player.inventory.addItemStackToInventory(result)) {
                player.dropItem(result, false);
            }
            return true;
        }

        // Shearing turns it into a cow and drops brown mushrooms
        if (stack.getItem() == Items.SHEARS && this.getGrowingAge() >= 0) {
            this.setDead();
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + this.height / 2.0F, this.posZ, 0.0D, 0.0D, 0.0D);
            if (!this.world.isRemote) {
                EntityCow cow = new EntityCow(this.world);
                cow.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                cow.setHealth(this.getHealth());
                cow.renderYawOffset = this.renderYawOffset;
                if (this.hasCustomName()) {
                    cow.setCustomNameTag(this.getCustomNameTag());
                }
                this.world.spawnEntity(cow);
                for (int i = 0; i < 5; i++) {
                    this.world.spawnEntity(new EntityItem(this.world, this.posX, this.posY + this.height, this.posZ, new ItemStack(Blocks.BROWN_MUSHROOM)));
                }
                stack.damageItem(1, player);
                this.playSound(SoundEvents.ENTITY_MOOSHROOM_SHEAR, 1.0F, 1.0F);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public EntityMooshroom createChild(EntityAgeable other) {
        return new EntityMooshroom(this.world);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger(STEW_EFFECT, this.stewEffectId);
        compound.setInteger(STEW_DURATION, this.stewEffectDuration);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.stewEffectId = compound.hasKey(STEW_EFFECT) ? compound.getInteger(STEW_EFFECT) : -1;
        this.stewEffectDuration = compound.getInteger(STEW_DURATION);
    }
}
