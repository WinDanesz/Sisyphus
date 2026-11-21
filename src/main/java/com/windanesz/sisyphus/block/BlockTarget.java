package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public class BlockTarget extends Block {

    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);

    public BlockTarget() {
        super(Material.GRASS, MapColor.QUARTZ);
        this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, 0));
        this.setHardness(0.5F);
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    public static int strengthOf(EnumFacing side, Vec3d hit) {
        double dx = Math.abs(frac(hit.x) - 0.5D);
        double dy = Math.abs(frac(hit.y) - 0.5D);
        double dz = Math.abs(frac(hit.z) - 0.5D);
        double distance;
        switch (side.getAxis()) {
            case Y:
                distance = Math.max(dx, dz);
                break;
            case Z:
                distance = Math.max(dx, dy);
                break;
            default:
                distance = Math.max(dy, dz);
        }
        return Math.max(1, MathHelper.ceil(15.0D * MathHelper.clamp((0.5D - distance) / 0.5D, 0.0D, 1.0D)));
    }

    private static double frac(double value) {
        return value - Math.floor(value);
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent.Arrow event) {
        hitTarget(event.getEntity(), event.getRayTraceResult());
    }

    @SubscribeEvent
    public static void onThrowableImpact(ProjectileImpactEvent.Throwable event) {
        hitTarget(event.getEntity(), event.getRayTraceResult());
    }

    private static void hitTarget(Entity projectile, RayTraceResult hit) {
        World world = projectile.world;
        if (world.isRemote || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        IBlockState state = world.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof BlockTarget)) {
            return;
        }
        int strength = ((BlockTarget) state.getBlock()).onHit(world, hit.getBlockPos(), state, hit.sideHit, hit.hitVec, projectile);
        // Hitting the middle from 30 blocks away or more
        if (strength == 15 && projectile instanceof EntityArrow && ((EntityArrow) projectile).shootingEntity instanceof EntityPlayerMP) {
            EntityPlayerMP shooter = (EntityPlayerMP) ((EntityArrow) projectile).shootingEntity;
            if (shooter.getDistanceSq(hit.getBlockPos()) >= 900.0D) {
                AdvancementHelper.grant(shooter, "bullseye");
            }
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWER);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWER, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWER);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWER);
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(POWER);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(POWER) != 0) {
            world.setBlockState(pos, state.withProperty(POWER, 0), 3);
        }
    }

    public int onHit(World world, BlockPos pos, IBlockState state, EnumFacing side, Vec3d hit, Entity projectile) {
        int strength = strengthOf(side, hit);
        int duration = projectile instanceof EntityArrow ? 20 : 8;
        if (state.getValue(POWER) == 0) {
            world.setBlockState(pos, state.withProperty(POWER, strength), 3);
        }
        world.scheduleUpdate(pos, this, duration);
        return strength;
    }
}
