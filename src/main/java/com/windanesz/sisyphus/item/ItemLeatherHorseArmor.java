package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class ItemLeatherHorseArmor extends Item {

    public static final int DEFAULT_COLOR = 0xA06540;
    private static final String DYE_KEY = "SisyphusBackportDye";
    private static final int PROTECTION = 3;

    @Nullable
    private static HorseArmorType armorType;

    public ItemLeatherHorseArmor() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);
    }

    // Added the first time it is needed
    public static synchronized HorseArmorType getArmorType() {
        if (armorType == null) {
            armorType = EnumHelper.addHorseArmor("SISYPHUS_LEATHER", SisyphusBackport.MODID + ":textures/entity/horse/armor/horse_armor_leather.png", PROTECTION);
        }
        return armorType;
    }

    @Nullable
    public static EnumDyeColor getDye(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(DYE_KEY)) {
            return null;
        }
        return EnumDyeColor.byDyeDamage(tag.getInteger(DYE_KEY));
    }

    public static void setDye(ItemStack stack, @Nullable EnumDyeColor color) {
        if (color == null) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                tag.removeTag(DYE_KEY);
                if (tag.isEmpty()) {
                    stack.setTagCompound(null);
                }
            }
            return;
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(DYE_KEY, color.getDyeDamage());
    }

    public static int getColor(ItemStack stack) {
        EnumDyeColor dye = getDye(stack);
        return dye == null ? DEFAULT_COLOR : ItemDye.DYE_COLORS[dye.getDyeDamage()];
    }

    @SubscribeEvent
    public static void onCauldron(PlayerInteractEvent.RightClickBlock event) {
        if (!Settings.items.enableLeatherHorseArmor) {
            return;
        }
        World world = event.getWorld();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemLeatherHorseArmor) || ItemLeatherHorseArmor.getDye(stack) == null) {
            return;
        }
        IBlockState state = world.getBlockState(event.getPos());
        if (state.getBlock() != Blocks.CAULDRON) {
            return;
        }
        int level = state.getValue(BlockCauldron.LEVEL);
        if (level <= 0) {
            return;
        }
        if (!world.isRemote) {
            ItemLeatherHorseArmor.setDye(stack, null);
            ((BlockCauldron) Blocks.CAULDRON).setWaterLevel(world, event.getPos(), state, level - 1);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    @Override
    public HorseArmorType getHorseArmorType(ItemStack stack) {
        return getArmorType();
    }

    @Override
    public String getHorseArmorTexture(EntityLiving wearer, ItemStack stack) {
        EnumDyeColor dye = getDye(stack);
        return SisyphusBackport.MODID + ":textures/entity/horse/armor/horse_armor_leather" + (dye == null ? "" : "_" + dye.getName()) + ".png";
    }
}
