package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.item.ItemLodestoneCompass;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLodestone extends Block {

    public BlockLodestone() {
        super(Material.IRON, MapColor.IRON);
        this.setHardness(3.5F);
        this.setResistance(3.5F);
        this.setSoundType(ModSounds.type("lodestone"));
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        boolean compass = held.getItem() == Items.COMPASS || held.getItem() instanceof ItemLodestoneCompass;
        if (!compass || ItemLodestoneCompass.INSTANCE == null) {
            return false;
        }
        if (!world.isRemote) {
            ItemStack locked = new ItemStack(ItemLodestoneCompass.INSTANCE);
            if (held.hasTagCompound()) {
                locked.setTagCompound(held.getTagCompound().copy());
            }
            ItemLodestoneCompass.lock(locked, world, pos);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            if (held.isEmpty() && !player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, locked);
            } else if (!player.inventory.addItemStackToInventory(locked)) {
                player.dropItem(locked, false);
            }
            world.playSound(null, pos, ModSounds.ITEM_LODESTONE_COMPASS_LOCK,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            if (player instanceof EntityPlayerMP) {
                AdvancementHelper.grant((EntityPlayerMP) player, "country_lode_take_me_home");
            }
        }
        return true;
    }
}
