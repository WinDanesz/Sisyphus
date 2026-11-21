package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockWitherRose extends BlockFlower {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!Settings.plants.enableFlowers) {
            return;
        }
        EntityLivingBase victim = event.getEntityLiving();
        World world = victim.world;
        if (world.isRemote || !(event.getSource().getTrueSource() instanceof EntityWither)
                || victim.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
            return;
        }
        Block rose = ModBlocks.get("wither_rose");
        BlockPos pos = new BlockPos(victim);
        boolean placed = false;
        if (world.getGameRules().getBoolean("mobGriefing") && world.isAirBlock(pos) && rose.canPlaceBlockAt(world, pos)) {
            placed = world.setBlockState(pos, rose.getDefaultState());
        }
        if (!placed) {
            world.spawnEntity(new EntityItem(world, victim.posX, victim.posY, victim.posZ, new ItemStack(rose)));
        }
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return super.canSustainBush(state) || state.getBlock() == Blocks.NETHERRACK || state.getBlock() == Blocks.SOUL_SAND;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && world.getDifficulty() != EnumDifficulty.PEACEFUL && entity instanceof EntityLivingBase
                && !(entity instanceof EntityWither) && !(entity instanceof EntityWitherSkeleton)) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.WITHER, 40));
        }
    }
}
