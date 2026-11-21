package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class ItemGoatHorn extends Item {

    /**
     * Also how long the horn cannot be played again.
     */
    public static final int USE_DURATION = 140;
    /**
     * A sound is heard sixteen blocks for each step of its volume.
     */
    public static final float RANGE = 256.0F;
    private static final String TAG = "instrument";
    public ItemGoatHorn() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.TOOLS);
        // Picks the model of the horn that is raised to the mouth
        this.addPropertyOverride(new ResourceLocation("tooting"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    /**
     * Picked by UUID, so both horns of a goat match.
     */
    public static Instrument instrumentFor(UUID goat, boolean screaming) {
        int index = new Random(goat.hashCode()).nextInt(4);
        return Instrument.values()[screaming ? index + 4 : index];
    }

    @Nullable
    public static Instrument getInstrument(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? null : Instrument.byId(tag.getString(TAG));
    }

    public static boolean isTooting(EntityLivingBase entity) {
        return entity.isHandActive() && entity.getActiveItemStack().getItem() instanceof ItemGoatHorn;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    public ItemStack create(Instrument instrument) {
        ItemStack stack = new ItemStack(this);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(TAG, instrument.id());
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (Instrument instrument : Instrument.values()) {
                items.add(this.create(instrument));
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        Instrument instrument = getInstrument(stack);
        if (instrument == null) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        // The player hears their own call at once, and the others hear it from the server
        world.playSound(player, player.posX, player.posY, player.posZ, ModSounds.event("item.goat_horn.sound." + instrument.ordinal()),
                SoundCategory.RECORDS, RANGE / 16.0F, 1.0F);
        player.getCooldownTracker().setCooldown(this, USE_DURATION);
        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        Instrument instrument = getInstrument(stack);
        if (instrument != null) {
            tooltip.add(TextFormatting.GRAY + I18n.format("instrument.sisyphus." + instrument.id()));
        }
    }

    /**
     * In the order of their sounds, {@code item.goat_horn.sound.0} to {@code 7}.
     */
    public enum Instrument {
        PONDER, SING, SEEK, FEEL, ADMIRE, CALL, YEARN, DREAM;

        @Nullable
        public static Instrument byId(String id) {
            for (Instrument instrument : values()) {
                if (instrument.id().equals(id)) {
                    return instrument;
                }
            }
            return null;
        }

        public String id() {
            return this.name().toLowerCase(Locale.ROOT) + "_goat_horn";
        }

        /**
         * The last four are the screaming calls.
         */
        public boolean isScreaming() {
            return this.ordinal() >= 4;
        }
    }
}
