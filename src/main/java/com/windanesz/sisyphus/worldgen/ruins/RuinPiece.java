package com.windanesz.sisyphus.worldgen.ruins;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.integration.oceanicexpanse.SBOceanicExpanseIntegration;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.worldgen.ArchaeologySites;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.*;

public class RuinPiece extends StructureComponent {

    public static final String HOUSES = "trail_ruins_houses_archaeology";
    public static final String ROADS = "trail_ruins_roads_archaeology";
    public static final String TOWER_TOP = "trail_ruins_tower_top_archaeology";
    public static final String OCEAN_COLD = "ocean_ruin_cold";
    public static final String OCEAN_WARM = "ocean_ruin_warm";

    private String template;
    private BlockPos origin;
    private Rotation rotation;
    private String processors;
    private float integrity = 1.0F;
    private boolean large;
    private boolean levelled;
    private boolean built;

    private transient Map<BlockPos, String> suspicious;

    public RuinPiece() {
    }

    public RuinPiece(String template, BlockPos origin, Rotation rotation, String processors, float integrity, boolean large) {
        super(0);
        this.template = template;
        this.origin = origin;
        this.rotation = rotation;
        this.processors = processors;
        this.integrity = integrity;
        this.large = large;
        this.boundingBox = this.getTemplate().getBoundingBox(origin, rotation);
    }

    /**
     * The seed of MathHelper.getPositionRandom, which is client only and crashed dedicated servers.
     */
    private static long positionSeed(BlockPos pos) {
        long seed = (long) (pos.getX() * 3129871) ^ (long) pos.getZ() * 116129781L ^ (long) pos.getY();
        return seed * seed * 42317861L + seed * 11L;
    }

    public static void removeFloatingBlocks(World world, List<BlockPos> placed) {
        Set<BlockPos> ruin = new HashSet<>();
        for (BlockPos pos : placed) {
            if (isSolid(world.getBlockState(pos))) {
                ruin.add(pos);
            }
        }
        Set<BlockPos> held = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pos : ruin) {
            BlockPos below = pos.down();
            if (!ruin.contains(below) && isSolid(world.getBlockState(below))) {
                held.add(pos);
                queue.add(pos);
            }
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos next = pos.offset(facing);
                if (ruin.contains(next) && held.add(next)) {
                    queue.add(next);
                }
            }
        }
        for (BlockPos pos : ruin) {
            if (!held.contains(pos)) {
                world.setBlockState(pos, pos.getY() > world.getSeaLevel() ? Blocks.AIR.getDefaultState() : Blocks.WATER.getDefaultState(), 2);
            }
        }
    }

    private static boolean isSolid(IBlockState state) {
        Material material = state.getMaterial();
        return material != Material.AIR && material != Material.WATER && material != Material.ICE && material != Material.PACKED_ICE;
    }

    static int floorAbove(World world, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, world.getHeight(x, z), z);
        while (pos.getY() > 1) {
            pos.setY(pos.getY() - 1);
            Material material = world.getBlockState(pos).getMaterial();
            if (material != Material.AIR && material != Material.WATER && material != Material.ICE && material != Material.PACKED_ICE) {
                return pos.getY() + 1;
            }
        }
        return 1;
    }

    public RuinTemplate getTemplate() {
        return RuinTemplate.get(this.template);
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    private boolean isOcean() {
        return OCEAN_COLD.equals(this.processors) || OCEAN_WARM.equals(this.processors);
    }

    @Override
    public void offset(int x, int y, int z) {
        super.offset(x, y, z);
        this.origin = this.origin.add(x, y, z);
        this.suspicious = null;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        tag.setString("Template", this.template);
        tag.setInteger("TX", this.origin.getX());
        tag.setInteger("TY", this.origin.getY());
        tag.setInteger("TZ", this.origin.getZ());
        tag.setString("Rot", this.rotation.name());
        tag.setString("Processors", this.processors);
        tag.setFloat("Integrity", this.integrity);
        tag.setBoolean("IsLarge", this.large);
        tag.setBoolean("Levelled", this.levelled);
        tag.setBoolean("Built", this.built);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
        this.template = tag.getString("Template");
        this.origin = new BlockPos(tag.getInteger("TX"), tag.getInteger("TY"), tag.getInteger("TZ"));
        this.rotation = Rotation.valueOf(tag.getString("Rot"));
        this.processors = tag.getString("Processors");
        this.integrity = tag.getFloat("Integrity");
        this.large = tag.getBoolean("IsLarge");
        this.levelled = tag.getBoolean("Levelled");
        this.built = tag.getBoolean("Built");
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        if (this.isOcean() && (!this.levelled || this.built)) {
            // Not on the floor yet, or already built whole by the start
            return true;
        }
        return this.place(world, random, box, null);
    }

    private boolean place(World world, Random random, StructureBoundingBox box, List<BlockPos> placed) {
        RuinTemplate template = this.getTemplate();
        Map<BlockPos, String> suspicious = this.getSuspicious(world);
        for (RuinTemplate.BlockInfo info : template.getBlocks()) {
            BlockPos pos = this.origin.add(RuinTemplate.transform(info.pos, this.rotation));
            if (!box.isVecInside(pos)) {
                continue;
            }
            IBlockState state = this.process(pos, template.getState(info.state));
            if (state == null) {
                continue;
            }
            String table = suspicious.get(pos);
            if (table != null) {
                ArchaeologySites.placeSuspicious(world, null, pos, ModBlocks.get(OCEAN_WARM.equals(this.processors) ? "suspicious_sand" : "suspicious_gravel"), table);
            } else {
                world.setBlockState(pos, RuinTemplate.rotate(state, this.rotation), 2);
            }
            if (placed != null) {
                placed.add(pos);
            }
        }
        for (RuinTemplate.Marker marker : template.getMarkers()) {
            BlockPos pos = this.origin.add(RuinTemplate.transform(marker.pos, this.rotation));
            if (box.isVecInside(pos)) {
                this.handleMarker(world, random, pos, marker.metadata);
            }
        }
        return true;
    }

    private IBlockState process(BlockPos pos, IBlockState state) {
        if (this.isOcean()) {
            if (state.getMaterial() == Material.AIR) {
                return null;
            }
            return new Random(positionSeed(pos)).nextFloat() <= this.integrity ? state : null;
        }
        if (TOWER_TOP.equals(this.processors)) {
            return state;
        }
        Random random = new Random(positionSeed(pos));
        Block block = state.getBlock();
        if (block == Blocks.GRAVEL) {
            if (random.nextFloat() < 0.2F) {
                return Blocks.DIRT.getDefaultState();
            }
            if (random.nextFloat() < 0.1F) {
                return Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
            }
        } else {
            Block bricks = ModBlocks.get("mud_bricks");
            if (bricks != Blocks.AIR && block == bricks && random.nextFloat() < 0.1F) {
                return ModBlocks.get("packed_mud").getDefaultState();
            }
        }
        return state;
    }

    private Map<BlockPos, String> getSuspicious(World world) {
        if (this.suspicious != null) {
            return this.suspicious;
        }
        Block target = OCEAN_WARM.equals(this.processors) ? Blocks.SAND : Blocks.GRAVEL;
        RuinTemplate template = this.getTemplate();
        List<BlockPos> candidates = new ArrayList<>();
        for (RuinTemplate.BlockInfo info : template.getBlocks()) {
            BlockPos pos = this.origin.add(RuinTemplate.transform(info.pos, this.rotation));
            IBlockState state = this.process(pos, template.getState(info.state));
            if (state != null && state.getBlock() == target) {
                candidates.add(pos);
            }
        }
        Collections.shuffle(candidates, new Random(world.getSeed() ^ this.origin.toLong() * 6364136223846793005L));
        Map<BlockPos, String> map = new HashMap<>();
        int common;
        int rare = 0;
        String commonTable;
        if (this.isOcean()) {
            common = 5;
            commonTable = this.processors;
        } else {
            commonTable = "trail_ruins_common";
            common = HOUSES.equals(this.processors) ? 6 : 2;
            rare = HOUSES.equals(this.processors) ? 3 : 0;
        }
        for (int i = 0; i < candidates.size() && i < common + rare; i++) {
            map.put(candidates.get(i), i < common ? commonTable : "trail_ruins_rare");
        }
        this.suspicious = map;
        return map;
    }

    private void handleMarker(World world, Random random, BlockPos pos, String metadata) {
        if ("chest".equals(metadata)) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityChest) {
                ((TileEntityChest) tile).setLootTable(new ResourceLocation(SisyphusBackport.MODID, this.large ? "chests/underwater_ruin_big" : "chests/underwater_ruin_small"),
                        random.nextLong());
            }
        } else if ("drowned".equals(metadata)) {
            // There are no drowned in 1.12: the marker is water, or air above the sea, with the drowned of Oceanic Expanse when it is there
            world.setBlockState(pos, pos.getY() > world.getSeaLevel() ? Blocks.AIR.getDefaultState() : Blocks.WATER.getDefaultState(), 2);
            SBOceanicExpanseIntegration.spawnDrowned(world, pos);
        }
    }

    /**
     * The start settles all its pieces before any is built, so that the stacked pieces of a cold ruin find the same floor.
     */
    public boolean settleIfNeeded(World world, StructureBoundingBox box) {
        if (!this.isOcean() || this.levelled || !this.boundingBox.intersectsWith(box.minX, box.minZ, box.maxX, box.maxZ)) {
            return false;
        }
        for (int chunkX = this.boundingBox.minX >> 4; chunkX <= this.boundingBox.maxX >> 4; chunkX++) {
            for (int chunkZ = this.boundingBox.minZ >> 4; chunkZ <= this.boundingBox.maxZ >> 4; chunkZ++) {
                if (!world.isChunkGeneratedAt(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        this.settleOnOceanFloor(world);
        return true;
    }

    public void buildWhole(World world, Random random, List<BlockPos> placed) {
        this.place(world, random, new StructureBoundingBox(this.boundingBox), placed);
        this.built = true;
    }

    private void settleOnOceanFloor(World world) {
        int base = floorAbove(world, this.origin.getX(), this.origin.getZ());
        int lowest = 512;
        int deeper = 0;
        for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; x++) {
            for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; z++) {
                int floor = Math.min(floorAbove(world, x, z), base) - 1;
                lowest = Math.min(lowest, floor);
                if (floor < base - 1 - 2) {
                    deeper++;
                }
            }
        }
        int width = this.boundingBox.maxX - this.boundingBox.minX;
        int y = base - 1 - lowest > 2 && deeper > width - 2 ? lowest + 1 : base;
        this.offset(0, y - this.boundingBox.minY, 0);
        this.levelled = true;
    }
}
