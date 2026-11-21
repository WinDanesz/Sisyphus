package com.windanesz.sisyphus.entity.item;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBambooRaft extends EntityBoat {

    public EntityBambooRaft(World world) {
        super(world);
    }

    public EntityBambooRaft(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    private static Item item(String name) {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, name));
    }

    @Override
    public Item getItemBoat() {
        return item("bamboo_raft");
    }

    @Override
    public double getMountedYOffset() {
        return 0.25D;
    }

    @Override
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        if (stack.getItem() == Item.getItemFromBlock(Blocks.PLANKS)) {
            Block planks = Block.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "bamboo_planks"));
            stack = new ItemStack(planks, stack.getCount());
        }
        return super.entityDropItem(stack, offsetY);
    }
}
