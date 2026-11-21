package com.windanesz.sisyphus.worldgen.ruins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class TrailRuinsAssembler {

    private static final int MAX_DEPTH = 7;
    private static final int MAX_DISTANCE = 80;
    private static final String START_POOL = "trail_ruins/tower";

    private static volatile Map<String, List<Element>> pools;

    private TrailRuinsAssembler() {
    }

    public static Map<String, List<Element>> pools() {
        if (pools == null) {
            String resource = "/assets/" + SisyphusBackport.MODID + "/structures/trail_ruins/pools.json";
            try (InputStream input = TrailRuinsAssembler.class.getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IllegalStateException("Missing " + resource);
                }
                JsonObject json = new JsonParser().parse(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                Map<String, List<Element>> read = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    List<Element> elements = new ArrayList<>();
                    JsonArray array = entry.getValue().getAsJsonArray();
                    for (JsonElement element : array) {
                        JsonObject object = element.getAsJsonObject();
                        elements.add(new Element(object.get("location").getAsString(), object.get("weight").getAsInt(), object.get("processors").getAsString()));
                    }
                    read.put(entry.getKey(), Collections.unmodifiableList(elements));
                }
                pools = Collections.unmodifiableMap(read);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return pools;
    }

    private static List<Element> shuffled(List<Element> pool, Random random) {
        List<Element> list = new ArrayList<>();
        for (Element element : pool) {
            for (int i = 0; i < element.weight; i++) {
                list.add(element);
            }
        }
        Collections.shuffle(list, random);
        return list;
    }

    private static List<Rotation> shuffledRotations(Random random) {
        List<Rotation> list = new ArrayList<>(Arrays.asList(Rotation.values()));
        Collections.shuffle(list, random);
        return list;
    }

    public static List<RuinPiece> assemble(Random random, int x, int z) {
        Map<String, List<Element>> pools = pools();
        List<Element> starts = shuffled(pools.get(START_POOL), random);
        Element startElement = starts.get(0);
        RuinPiece start = new RuinPiece(startElement.location, new BlockPos(x, 0, z), Rotation.values()[random.nextInt(4)], startElement.processors, 1.0F, false);
        StructureBoundingBox startBox = start.getBoundingBox();
        int centerX = (startBox.minX + startBox.maxX) / 2;
        int centerZ = (startBox.minZ + startBox.maxZ) / 2;
        int centerY = (startBox.minY + startBox.maxY) / 2;
        StructureBoundingBox limit = new StructureBoundingBox(centerX - MAX_DISTANCE, centerY - MAX_DISTANCE, centerZ - MAX_DISTANCE,
                centerX + MAX_DISTANCE, centerY + MAX_DISTANCE, centerZ + MAX_DISTANCE);

        List<Placed> placed = new ArrayList<>();
        Deque<Placed> queue = new ArrayDeque<>();
        Placed first = new Placed(start, 0);
        placed.add(first);
        queue.add(first);
        while (!queue.isEmpty()) {
            Placed parent = queue.poll();
            if (parent.depth >= MAX_DEPTH) {
                continue;
            }
            RuinTemplate template = parent.piece.getTemplate();
            List<RuinTemplate.Jigsaw> jigsaws = new ArrayList<>(template.getJigsaws());
            Collections.shuffle(jigsaws, random);
            for (RuinTemplate.Jigsaw jigsaw : jigsaws) {
                List<Element> pool = pools.get(jigsaw.pool);
                if (pool == null || pool.isEmpty()) {
                    continue;
                }
                Placed child = attach(parent, jigsaw, pool, placed, limit, random);
                if (child != null) {
                    placed.add(child);
                    queue.add(child);
                }
            }
        }
        List<RuinPiece> pieces = new ArrayList<>();
        for (Placed p : placed) {
            pieces.add(p.piece);
        }
        return pieces;
    }

    private static Placed attach(Placed parent, RuinTemplate.Jigsaw jigsaw, List<Element> pool, List<Placed> placed, StructureBoundingBox limit, Random random) {
        RuinPiece piece = parent.piece;
        BlockPos from = piece.getOrigin().add(RuinTemplate.transform(jigsaw.pos, piece.getRotation()));
        EnumFacing front = RuinTemplate.transform(jigsaw.front, piece.getRotation());
        EnumFacing top = RuinTemplate.transform(jigsaw.top, piece.getRotation());
        BlockPos target = from.offset(front);
        boolean inside = piece.getBoundingBox().isVecInside(target);
        for (Element element : shuffled(pool, random)) {
            RuinTemplate candidate = RuinTemplate.get(element.location);
            for (Rotation rotation : shuffledRotations(random)) {
                List<RuinTemplate.Jigsaw> connectors = new ArrayList<>(candidate.getJigsaws());
                Collections.shuffle(connectors, random);
                for (RuinTemplate.Jigsaw connector : connectors) {
                    EnumFacing childFront = RuinTemplate.transform(connector.front, rotation);
                    EnumFacing childTop = RuinTemplate.transform(connector.top, rotation);
                    if (childFront != front.getOpposite() || (!jigsaw.rollable && childTop != top) || !jigsaw.target.equals(connector.name)) {
                        continue;
                    }
                    BlockPos origin = target.subtract(RuinTemplate.transform(connector.pos, rotation));
                    StructureBoundingBox box = candidate.getBoundingBox(origin, rotation);
                    if (fits(box, parent, inside, placed, limit)) {
                        if (inside) {
                            parent.inside.add(box);
                        }
                        return new Placed(new RuinPiece(element.location, origin, rotation, element.processors, 1.0F, false), parent.depth + 1);
                    }
                }
            }
        }
        return null;
    }

    private static boolean fits(StructureBoundingBox box, Placed parent, boolean inside, List<Placed> placed, StructureBoundingBox limit) {
        if (inside) {
            if (!contains(parent.piece.getBoundingBox(), box)) {
                return false;
            }
            for (StructureBoundingBox other : parent.inside) {
                if (other.intersectsWith(box)) {
                    return false;
                }
            }
            return true;
        }
        if (!contains(limit, box)) {
            return false;
        }
        for (Placed other : placed) {
            if (other.piece.getBoundingBox().intersectsWith(box)) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(StructureBoundingBox outer, StructureBoundingBox inner) {
        return inner.minX >= outer.minX && inner.maxX <= outer.maxX && inner.minY >= outer.minY && inner.maxY <= outer.maxY
                && inner.minZ >= outer.minZ && inner.maxZ <= outer.maxZ;
    }

    public static final class Element {
        public final String location;
        public final int weight;
        public final String processors;

        Element(String location, int weight, String processors) {
            this.location = location;
            this.weight = weight;
            this.processors = processors;
        }
    }

    private static final class Placed {
        final RuinPiece piece;
        final int depth;
        final List<StructureBoundingBox> inside = new ArrayList<>();

        Placed(RuinPiece piece, int depth) {
            this.piece = piece;
            this.depth = depth;
        }
    }
}
