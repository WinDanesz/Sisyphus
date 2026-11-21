package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class RuinedPortalPiece extends StructureComponent {

    private static final float[] SPREAD = {1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
    private static final int MIN_Y = 15;
    private String template;
    private BlockPos origin;
    private Rotation rotation;
    private Mirror mirror;
    private Placement placement;
    private boolean airPocket;
    private float mossiness;
    private boolean overgrown;
    private boolean vines;
    private boolean canBeCold;
    private boolean blackstone;
    private long seed;
    private boolean cold;
    private boolean built;
    public RuinedPortalPiece() {
    }

    public RuinedPortalPiece(String template, BlockPos origin, Rotation rotation, Mirror mirror, MapGenRuinedPortals.Setup setup, boolean airPocket, long seed) {
        super(0);
        this.template = template;
        this.origin = origin;
        this.rotation = rotation;
        this.mirror = mirror;
        this.placement = setup.placement;
        this.airPocket = airPocket;
        this.mossiness = setup.mossiness;
        this.overgrown = setup.overgrown;
        this.vines = setup.vines;
        this.canBeCold = setup.canBeCold;
        this.blackstone = setup.blackstone;
        this.seed = seed;
        this.boundingBox = reach(this.getTemplateBox());
    }

    public static BlockPos transform(BlockPos pos, Mirror mirror, Rotation rotation, BlockPos pivot) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean mirrored = true;
        switch (mirror) {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                mirrored = false;
        }
        int px = pivot.getX();
        int pz = pivot.getZ();
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(px - pz + z, y, px + pz - x);
            case CLOCKWISE_90:
                return new BlockPos(px + pz - z, y, pz - px + x);
            case CLOCKWISE_180:
                return new BlockPos(px + px - x, y, pz + pz - z);
            default:
                return mirrored ? new BlockPos(x, y, z) : pos;
        }
    }

    static StructureBoundingBox reach(StructureBoundingBox box) {
        int centerX = box.minX + (box.maxX - box.minX + 1) / 2;
        int centerZ = box.minZ + (box.maxZ - box.minZ + 1) / 2;
        int radius = SPREAD.length - 1;
        return new StructureBoundingBox(Math.min(box.minX, centerX - radius), box.minY - 9, Math.min(box.minZ, centerZ - radius),
                Math.max(box.maxX, centerX + radius), box.maxY, Math.max(box.maxZ, centerZ + radius));
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static int withinInterval(Random random, int min, int max) {
        return min < max ? between(random, min, max) : max;
    }

    static boolean isGround(IBlockState state, boolean oceanFloor) {
        Material material = state.getMaterial();
        if (material.isLiquid() || material == Material.ICE) {
            return !oceanFloor;
        }
        return material != Material.AIR && material != Material.LEAVES && material != Material.WOOD && material != Material.PLANTS
                && material != Material.VINE && material != Material.SNOW && material != Material.CACTUS && material != Material.GOURD
                && material != Material.WEB && material != Material.CARPET;
    }

    static int surfaceY(World world, int x, int z, Placement placement) {
        boolean oceanFloor = placement == Placement.ON_OCEAN_FLOOR;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        int top = world.getChunk(pos).getTopFilledSegment() + 16;
        for (int y = top; y > 0; y--) {
            if (isGround(world.getBlockState(pos.setPos(x, y, z)), oceanFloor)) {
                return y;
            }
        }
        return 0;
    }

    private static IBlockState randomStairs(Random random) {
        return Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.HORIZONTALS[random.nextInt(4)])
                .withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.values()[random.nextInt(2)]);
    }

    private static IBlockState stoneSlab(BlockStoneSlab.EnumType variant) {
        return Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, variant).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
    }

    static boolean isProtected(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.MOB_SPAWNER || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.END_PORTAL_FRAME) {
            return true;
        }
        Block reinforced = ModBlocks.get("reinforced_deepslate");
        return reinforced != Blocks.AIR && block == reinforced;
    }

    /**
     * Stone brick walls are cobblestone walls here, so every wall becomes a polished blackstone brick wall.
     */
    static IBlockState toBlackstone(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.IRON_BARS) {
            Block chain = ModBlocks.get("chain");
            return chain != Blocks.AIR ? chain.getDefaultState() : state;
        }
        if (ModBlocks.get("blackstone") == Blocks.AIR) {
            return state;
        }
        if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE) {
            return mod("blackstone", state);
        }
        if (block == Blocks.STONE) {
            return state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE ? mod("polished_blackstone", state) : state;
        }
        if (block == Blocks.STONEBRICK) {
            switch (state.getValue(BlockStoneBrick.VARIANT)) {
                case CRACKED:
                    return mod("cracked_polished_blackstone_bricks", state);
                case CHISELED:
                    return mod("chiseled_polished_blackstone", state);
                default:
                    return mod("polished_blackstone_bricks", state);
            }
        }
        if (block == Blocks.STONE_STAIRS) {
            return stairs("blackstone_stairs", state);
        }
        if (block == Blocks.STONE_BRICK_STAIRS) {
            return stairs("polished_blackstone_brick_stairs", state);
        }
        if (block == Blocks.STONE_SLAB || block == Blocks.DOUBLE_STONE_SLAB) {
            String prefix;
            switch (state.getValue(BlockStoneSlab.VARIANT)) {
                case STONE:
                    prefix = "polished_blackstone";
                    break;
                case COBBLESTONE:
                    prefix = "blackstone";
                    break;
                case SMOOTHBRICK:
                    prefix = "polished_blackstone_brick";
                    break;
                default:
                    return state;
            }
            if (block == Blocks.DOUBLE_STONE_SLAB) {
                return mod(prefix + "_double_slab", state);
            }
            IBlockState slab = mod(prefix + "_slab", state);
            return slab.getPropertyKeys().contains(BlockSlab.HALF) ? slab.withProperty(BlockSlab.HALF, state.getValue(BlockSlab.HALF)) : slab;
        }
        if (block == Blocks.COBBLESTONE_WALL) {
            return mod("polished_blackstone_brick_wall", state);
        }
        return state;
    }

    private static IBlockState mod(String name, IBlockState fallback) {
        Block block = ModBlocks.get(name);
        return block != Blocks.AIR ? block.getDefaultState() : fallback;
    }

    private static IBlockState stairs(String name, IBlockState state) {
        IBlockState stairs = mod(name, state);
        if (stairs == state || !(stairs.getBlock() instanceof BlockStairs)) {
            return state;
        }
        return stairs.withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING)).withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF));
    }

    public RuinTemplate getTemplate() {
        return RuinTemplate.get(this.template);
    }

    public Placement getPlacement() {
        return this.placement;
    }

    public boolean hasAirPocket() {
        return this.airPocket;
    }

    // Finding the height

    public boolean isBuilt() {
        return this.built;
    }

    private BlockPos pivot() {
        BlockPos size = this.getTemplate().getSize();
        return new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
    }

    private BlockPos worldPos(BlockPos templatePos) {
        return this.origin.add(transform(templatePos, this.mirror, this.rotation, this.pivot()));
    }

    public StructureBoundingBox getTemplateBox() {
        BlockPos size = this.getTemplate().getSize();
        BlockPos a = this.worldPos(BlockPos.ORIGIN);
        BlockPos b = this.worldPos(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        return new StructureBoundingBox(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    @Override
    public void offset(int x, int y, int z) {
        super.offset(x, y, z);
        this.origin = this.origin.add(x, y, z);
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        tag.setString("Template", this.template);
        tag.setInteger("TX", this.origin.getX());
        tag.setInteger("TY", this.origin.getY());
        tag.setInteger("TZ", this.origin.getZ());
        tag.setString("Rot", this.rotation.name());
        tag.setString("Mirror", this.mirror.name());
        tag.setString("Placement", this.placement.name());
        tag.setBoolean("AirPocket", this.airPocket);
        tag.setFloat("Mossiness", this.mossiness);
        tag.setBoolean("Overgrown", this.overgrown);
        tag.setBoolean("Vines", this.vines);
        tag.setBoolean("CanBeCold", this.canBeCold);
        tag.setBoolean("Blackstone", this.blackstone);
        tag.setLong("Seed", this.seed);
        tag.setBoolean("Cold", this.cold);
        tag.setBoolean("Built", this.built);
    }

    // Building

    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
        this.template = tag.getString("Template");
        this.origin = new BlockPos(tag.getInteger("TX"), tag.getInteger("TY"), tag.getInteger("TZ"));
        this.rotation = Rotation.valueOf(tag.getString("Rot"));
        this.mirror = Mirror.valueOf(tag.getString("Mirror"));
        this.placement = Placement.valueOf(tag.getString("Placement"));
        this.airPocket = tag.getBoolean("AirPocket");
        this.mossiness = tag.getFloat("Mossiness");
        this.overgrown = tag.getBoolean("Overgrown");
        this.vines = tag.getBoolean("Vines");
        this.canBeCold = tag.getBoolean("CanBeCold");
        this.blackstone = tag.getBoolean("Blackstone");
        this.seed = tag.getLong("Seed");
        this.cold = tag.getBoolean("Cold");
        this.built = tag.getBoolean("Built");
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // The portal is built whole by the start, once the ground around it is there
        return true;
    }

    public boolean buildIfReady(World world, StructureBoundingBox box) {
        if (this.built || !this.boundingBox.intersectsWith(box.minX, box.minZ, box.maxX, box.maxZ)) {
            return false;
        }
        for (int chunkX = this.boundingBox.minX >> 4; chunkX <= this.boundingBox.maxX >> 4; chunkX++) {
            for (int chunkZ = this.boundingBox.minZ >> 4; chunkZ <= this.boundingBox.maxZ >> 4; chunkZ++) {
                if (!world.isChunkGeneratedAt(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        Random random = new Random(this.seed);
        this.settle(world, random);
        this.build(world, random);
        this.built = true;
        return true;
    }

    private void settle(World world, Random random) {
        StructureBoundingBox box = this.getTemplateBox();
        int centerX = box.minX + (box.maxX - box.minX + 1) / 2;
        int centerZ = box.minZ + (box.maxZ - box.minZ + 1) / 2;
        int height = surfaceY(world, centerX, centerZ, this.placement);
        int y = this.findSuitableY(world, random, height, box.maxY - box.minY + 1, box);
        this.offset(0, y - this.origin.getY(), 0);
        if (this.canBeCold) {
            this.cold = world.getBiome(this.origin).getTemperature(this.origin) < 0.15F;
        }
    }

    private int findSuitableY(World world, Random random, int height, int ySpan, StructureBoundingBox box) {
        int start;
        switch (this.placement) {
            case IN_NETHER:
                if (this.airPocket) {
                    start = between(random, 32, 100);
                } else if (random.nextFloat() < 0.5F) {
                    start = between(random, 27, 29);
                } else {
                    start = between(random, 29, 100);
                }
                break;
            case IN_MOUNTAIN:
                start = withinInterval(random, 70, height - ySpan);
                break;
            case UNDERGROUND:
                start = withinInterval(random, MIN_Y, height - ySpan);
                break;
            case PARTLY_BURIED:
                start = height - ySpan + between(random, 2, 8);
                break;
            default:
                start = height;
        }
        boolean oceanFloor = this.placement == Placement.ON_OCEAN_FLOOR;
        int[][] corners = {{box.minX, box.minZ}, {box.maxX, box.minZ}, {box.minX, box.maxZ}, {box.maxX, box.maxZ}};
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int y;
        for (y = start; y > MIN_Y; y--) {
            int ground = 0;
            for (int[] corner : corners) {
                if (isGround(world.getBlockState(pos.setPos(corner[0], y, corner[1])), oceanFloor) && ++ground == 3) {
                    return y;
                }
            }
        }
        return y;
    }

    private void build(World world, Random random) {
        RuinTemplate template = this.getTemplate();
        for (RuinTemplate.BlockInfo info : template.getBlocks()) {
            BlockPos pos = this.worldPos(info.pos);
            IBlockState state = this.process(world, random, pos, template.getState(info.state));
            if (state != null) {
                world.setBlockState(pos, RuinTemplate.rotate(state.withMirror(this.mirror), this.rotation), 2);
            }
        }
        for (RuinTemplate.Marker marker : template.getMarkers()) {
            if (marker.metadata.startsWith("loot ")) {
                TileEntity tile = world.getTileEntity(this.worldPos(marker.pos));
                if (tile instanceof TileEntityChest) {
                    ((TileEntityChest) tile).setLootTable(new ResourceLocation(SisyphusBackport.MODID, marker.metadata.substring("loot ".length())), random.nextLong());
                }
            }
        }
        StructureBoundingBox box = this.getTemplateBox();
        this.spreadNetherrack(world, random, box);
        this.addDripColumnsBelow(world, random, box);
        if (this.vines || this.overgrown) {
            for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
                if (this.vines) {
                    this.maybeAddVines(world, random, pos);
                }
                if (this.overgrown) {
                    this.maybeAddLeavesAbove(world, random, pos);
                }
            }
        }
    }

    private IBlockState process(World world, Random random, BlockPos pos, IBlockState state) {
        if (state.getMaterial() == Material.AIR && !this.airPocket) {
            return null;
        }
        state = this.replace(random, state);
        IBlockState aged = this.age(random, state);
        if (aged != null) {
            state = aged;
        }
        IBlockState current = world.getBlockState(pos);
        if (isProtected(current)) {
            return null;
        }
        if (current.getMaterial() == Material.LAVA && !state.isFullCube()) {
            state = Blocks.LAVA.getDefaultState();
        }
        return this.blackstone ? toBlackstone(state) : state;
    }

    private IBlockState replace(Random random, IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.GOLD_BLOCK) {
            return random.nextFloat() < 0.3F ? Blocks.AIR.getDefaultState() : state;
        }
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            if (this.placement == Placement.ON_OCEAN_FLOOR) {
                return Blocks.MAGMA.getDefaultState();
            }
            if (this.cold) {
                return Blocks.NETHERRACK.getDefaultState();
            }
            return random.nextFloat() < 0.2F ? Blocks.MAGMA.getDefaultState() : state;
        }
        if (block == Blocks.NETHERRACK && !this.cold && random.nextFloat() < 0.07F) {
            return Blocks.MAGMA.getDefaultState();
        }
        return state;
    }

    /**
     * Mossy stone brick stairs, slabs and stone brick walls do not exist here, so the plain ones and the mossy cobblestone wall stand in.
     */
    private IBlockState age(Random random, IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONEBRICK && state.getValue(BlockStoneBrick.VARIANT) != BlockStoneBrick.EnumType.CRACKED
                && state.getValue(BlockStoneBrick.VARIANT) != BlockStoneBrick.EnumType.MOSSY
                || block == Blocks.STONE && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) {
            if (random.nextFloat() >= 0.5F) {
                return null;
            }
            IBlockState[] plain = {Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), randomStairs(random)};
            IBlockState[] mossy = {Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY), randomStairs(random)};
            return this.pick(random, plain, mossy);
        }
        if (block instanceof BlockStairs) {
            if (random.nextFloat() >= 0.5F) {
                return null;
            }
            IBlockState[] plain = {stoneSlab(BlockStoneSlab.EnumType.STONE), stoneSlab(BlockStoneSlab.EnumType.SMOOTHBRICK)};
            IBlockState[] mossy = {Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING))
                    .withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF)), stoneSlab(BlockStoneSlab.EnumType.SMOOTHBRICK)};
            return this.pick(random, plain, mossy);
        }
        if (block instanceof BlockSlab) {
            return random.nextFloat() < this.mossiness ? stoneSlab(BlockStoneSlab.EnumType.SMOOTHBRICK) : null;
        }
        if (block instanceof BlockWall) {
            return random.nextFloat() < this.mossiness ? Blocks.COBBLESTONE_WALL.getDefaultState().withProperty(BlockWall.VARIANT, BlockWall.EnumType.MOSSY) : null;
        }
        if (block == Blocks.OBSIDIAN) {
            Block crying = ModBlocks.get("crying_obsidian");
            return crying != Blocks.AIR && random.nextFloat() < 0.15F ? crying.getDefaultState() : null;
        }
        return null;
    }

    private IBlockState pick(Random random, IBlockState[] plain, IBlockState[] mossy) {
        IBlockState[] states = random.nextFloat() < this.mossiness ? mossy : plain;
        return states[random.nextInt(states.length)];
    }

    private void spreadNetherrack(World world, Random random, StructureBoundingBox box) {
        boolean onSurface = this.placement == Placement.ON_LAND_SURFACE || this.placement == Placement.ON_OCEAN_FLOOR;
        int centerX = box.minX + (box.maxX - box.minX + 1) / 2;
        int centerZ = box.minZ + (box.maxZ - box.minZ + 1) / 2;
        int size = ((box.maxX - box.minX + 1) + (box.maxZ - box.minZ + 1)) / 2;
        int shift = random.nextInt(Math.max(1, 8 - size / 2));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = centerX - SPREAD.length; x <= centerX + SPREAD.length; x++) {
            for (int z = centerZ - SPREAD.length; z <= centerZ + SPREAD.length; z++) {
                int distance = Math.max(0, Math.abs(x - centerX) + Math.abs(z - centerZ) + shift);
                if (distance >= SPREAD.length || random.nextDouble() >= SPREAD[distance]) {
                    continue;
                }
                int surface = surfaceY(world, x, z, this.placement);
                int y = onSurface ? surface : Math.min(box.minY, surface);
                pos.setPos(x, y, z);
                if (Math.abs(y - box.minY) <= 3 && this.canBecomeNetherrack(world.getBlockState(pos))) {
                    BlockPos at = pos.toImmutable();
                    this.placeNetherrackOrMagma(world, random, at);
                    if (this.overgrown) {
                        this.maybeAddLeavesAbove(world, random, at);
                    }
                    this.addDripColumn(world, random, at.down());
                }
            }
        }
    }

    private boolean canBecomeNetherrack(IBlockState state) {
        return state.getMaterial() != Material.AIR && state.getBlock() != Blocks.OBSIDIAN && !isProtected(state)
                && (this.placement == Placement.IN_NETHER || state.getMaterial() != Material.LAVA);
    }

    private void addDripColumnsBelow(World world, Random random, StructureBoundingBox box) {
        for (int x = box.minX + 1; x < box.maxX; x++) {
            for (int z = box.minZ + 1; z < box.maxZ; z++) {
                BlockPos pos = new BlockPos(x, box.minY, z);
                if (world.getBlockState(pos).getBlock() == Blocks.NETHERRACK) {
                    this.addDripColumn(world, random, pos.down());
                }
            }
        }
    }

    private void addDripColumn(World world, Random random, BlockPos pos) {
        this.placeNetherrackOrMagma(world, random, pos);
        BlockPos at = pos;
        for (int left = 8; left > 0 && random.nextFloat() < 0.5F; left--) {
            at = at.down();
            this.placeNetherrackOrMagma(world, random, at);
        }
    }

    private void placeNetherrackOrMagma(World world, Random random, BlockPos pos) {
        if (pos.getY() <= 0) {
            return;
        }
        if (!this.cold && random.nextFloat() < 0.07F) {
            world.setBlockState(pos, Blocks.MAGMA.getDefaultState(), 2);
        } else {
            world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), 2);
        }
    }

    private void maybeAddVines(World world, Random random, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getMaterial() == Material.AIR || state.getBlock() == Blocks.VINE) {
            return;
        }
        EnumFacing side = EnumFacing.HORIZONTALS[random.nextInt(4)];
        BlockPos next = pos.offset(side);
        if (world.isAirBlock(next) && state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID) {
            world.setBlockState(next, Blocks.VINE.getDefaultState().withProperty(BlockVine.getPropertyFor(side.getOpposite()), true), 2);
        }
    }

    private void maybeAddLeavesAbove(World world, Random random, BlockPos pos) {
        if (random.nextFloat() < 0.5F && world.getBlockState(pos).getBlock() == Blocks.NETHERRACK && world.isAirBlock(pos.up())) {
            world.setBlockState(pos.up(), Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
                    .withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, false), 2);
        }
    }

    public enum Placement {
        ON_LAND_SURFACE, PARTLY_BURIED, ON_OCEAN_FLOOR, IN_MOUNTAIN, UNDERGROUND, IN_NETHER
    }
}
