package com.windanesz.sisyphus.integration;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockWoodSign;
import com.windanesz.sisyphus.item.ItemWoodSign;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = SisyphusBackport.MODID)
public final class ModdedWoodSigns {

    private static final String LOG = "_log";
    private static final String PLANKS = "_planks";

    private static final Set<String> SIGN_WORDS = new HashSet<>(Arrays.asList("sign", "signs", "standing", "wall", "floor", "post", "block"));

    private static final Set<String> WOOD_WORDS = new HashSet<>(Arrays.asList("planks", "plank", "log", "logs", "stem", "stems", "wood", "block"));
    private static final Map<String, Wood> WOODS = new LinkedHashMap<>();

    private ModdedWoodSigns() {
    }

    public static boolean isEnabled() {
        return Settings.buildingBlocks.enableSigns && Settings.buildingBlocks.enableModdedWoodSigns;
    }

    public static Map<String, Wood> all() {
        return Collections.unmodifiableMap(WOODS);
    }

    /**
     * Includes the mod id, as two mods may have a wood of the same name.
     */
    public static String woodName(ResourceLocation planks) {
        String path = planks.getPath();
        return planks.getNamespace() + "_" + path.substring(0, path.length() - PLANKS.length());
    }

    static String clean(String name) {
        String cleaned = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "_").replaceAll("_+", "_");
        return cleaned.replaceAll("^_|_$", "");
    }

    static String withoutWoodWords(String path) {
        List<String> rest = new ArrayList<>();
        for (String word : clean(path.replace('/', '_')).split("_")) {
            if (!word.isEmpty() && !WOOD_WORDS.contains(word)) {
                rest.add(word);
            }
        }
        return String.join("_", rest);
    }

    /**
     * {@code modid:log=modid:planks[@meta][=name]}, or null if the line is not written that way.
     */
    @Nullable
    static Entry parse(String line) {
        String[] parts = line.trim().split("=");
        if (parts.length < 2 || parts.length > 3 || !parts[0].contains(":")) {
            return null;
        }
        String planksPart = parts[1].trim();
        int meta = -1;
        int at = planksPart.indexOf('@');
        if (at >= 0) {
            try {
                meta = Integer.parseInt(planksPart.substring(at + 1).trim());
            } catch (NumberFormatException e) {
                return null;
            }
            if (meta < 0 || meta > 15) {
                return null;
            }
            planksPart = planksPart.substring(0, at).trim();
        }
        if (!planksPart.contains(":")) {
            return null;
        }
        ResourceLocation log = new ResourceLocation(parts[0].trim());
        ResourceLocation planks = new ResourceLocation(planksPart);
        String name = parts.length == 3 ? clean(parts[2]) : "";
        if (name.isEmpty()) {
            name = withoutWoodWords(planks.getPath());
            if (name.isEmpty()) {
                name = withoutWoodWords(log.getPath());
            }
            if (meta >= 0) {
                name = name.isEmpty() ? String.valueOf(meta) : name + "_" + meta;
            }
            if (name.isEmpty()) {
                name = "wood";
            }
        }
        return new Entry(log, planks, meta, planks.getNamespace() + "_" + name);
    }

    static boolean isSignOf(String path, String wood) {
        List<String> rest = new ArrayList<>();
        boolean sign = false;
        for (String word : path.split("_")) {
            if (SIGN_WORDS.contains(word)) {
                sign |= word.startsWith("sign");
            } else {
                rest.add(word);
            }
        }
        return sign && String.join("_", rest).equals(wood);
    }

    /**
     * Sorted, so the result does not depend on the order of the blocks.
     */
    public static List<ResourceLocation> findPlanks(Collection<ResourceLocation> blocks) {
        Map<String, Set<String>> byMod = new HashMap<>();
        for (ResourceLocation block : blocks) {
            byMod.computeIfAbsent(block.getNamespace(), mod -> new TreeSet<>()).add(block.getPath());
        }
        byMod.remove("minecraft");
        byMod.remove(SisyphusBackport.MODID);
        List<ResourceLocation> found = new ArrayList<>();
        for (String mod : new TreeSet<>(byMod.keySet())) {
            Set<String> paths = byMod.get(mod);
            for (String path : paths) {
                if (!path.endsWith(LOG) || path.length() == LOG.length()) {
                    continue;
                }
                String wood = path.substring(0, path.length() - LOG.length());
                if (paths.contains(wood + PLANKS) && paths.stream().noneMatch(other -> isSignOf(other, wood))) {
                    found.add(new ResourceLocation(mod, wood + PLANKS));
                }
            }
        }
        return found;
    }

    private static Map<String, Wood> findWoods(IForgeRegistry<Block> registry) {
        Map<String, Wood> woods = new LinkedHashMap<>();
        for (String line : Settings.buildingBlocks.moddedSignWoods) {
            if (line.trim().isEmpty()) {
                continue;
            }
            Entry entry = parse(line);
            if (entry == null) {
                SisyphusBackport.logger.warn("Ignoring invalid modded sign wood entry '{}', expected 'modid:log=modid:planks', "
                        + "optionally with '@meta' after the planks and '=name' at the end", line);
                continue;
            }
            if (!registry.containsKey(entry.log) || !registry.containsKey(entry.planks)) {
                SisyphusBackport.logger.warn("Ignoring modded sign wood entry '{}', one of the blocks does not exist", line);
                continue;
            }
            if (woods.containsKey(entry.wood)) {
                SisyphusBackport.logger.warn("Ignoring modded sign wood entry '{}', another entry already makes the sign {}_sign; give it a "
                        + "name of its own with '=name' at the end", line, entry.wood);
                continue;
            }
            woods.put(entry.wood, new Wood(registry.getValue(entry.planks), entry.meta));
        }
        if (Settings.buildingBlocks.detectModdedWoods) {
            for (ResourceLocation id : findPlanks(new ArrayList<>(registry.getKeys()))) {
                Block planks = registry.getValue(id);
                boolean listed = woods.values().stream().anyMatch(wood -> wood.planks == planks);
                if (planks != null && !listed) {
                    woods.putIfAbsent(woodName(id), new Wood(planks, -1));
                }
            }
        }
        return woods;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (!isEnabled()) {
            return;
        }
        IForgeRegistry<Block> registry = event.getRegistry();
        for (Map.Entry<String, Wood> entry : findWoods(registry).entrySet()) {
            String wood = entry.getKey();
            if (registry.containsKey(new ResourceLocation(SisyphusBackport.MODID, wood + "_standing_sign"))) {
                continue;
            }
            // The sign sounds like the planks it is made of
            @SuppressWarnings("deprecation")
            SoundType sound = entry.getValue().planks.getSoundType();
            Block standing = new BlockWoodSign.Standing(wood, sound);
            standing.setHardness(1.0F);
            Block wall = new BlockWoodSign.Wall(wood, sound);
            wall.setHardness(1.0F);
            ModBlocks.registerBlock(registry, wood + "_standing_sign", standing);
            ModBlocks.registerBlock(registry, wood + "_wall_sign", wall);
            WOODS.put(wood, entry.getValue());
        }
        if (!WOODS.isEmpty()) {
            SisyphusBackport.logger.info("Made signs of {} woods of other mods: {}", WOODS.size(), String.join(", ", WOODS.keySet()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Map.Entry<String, Wood> entry : WOODS.entrySet()) {
            String wood = entry.getKey();
            ModItems.registerItem(event.getRegistry(), SisyphusBackport.MODID, wood + "_sign",
                    new SignItem(ModBlocks.get(wood + "_standing_sign"), ModBlocks.get(wood + "_wall_sign"), entry.getValue()), false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        for (Map.Entry<String, Wood> entry : WOODS.entrySet()) {
            ItemStack planks = entry.getValue().stack(1);
            Item sign = ModItems.get(entry.getKey() + "_sign");
            if (planks.isEmpty() || sign == null || sign == Items.AIR) {
                SisyphusBackport.logger.warn("The planks {} have no item, so their sign has no recipe", entry.getValue().planks.getRegistryName());
                continue;
            }
            IRecipe recipe = new ShapedOreRecipe(new ResourceLocation("sign"), new ItemStack(sign, 3), "PPP", "PPP", " S ", 'P', planks, 'S', Items.STICK);
            event.getRegistry().register(recipe.setRegistryName(new ResourceLocation(SisyphusBackport.MODID, entry.getKey() + "_sign")));
        }
    }

    /**
     * "Fir Planks" gives "Fir". Falls back to the registry name where the planks are not named that way.
     */
    static String woodDisplayName(String planksName, String planksWord, String wood) {
        String name = planksName.trim();
        if (!planksWord.isEmpty() && name.length() > planksWord.length()
                && name.toLowerCase(Locale.ROOT).endsWith(planksWord.toLowerCase(Locale.ROOT))) {
            String stripped = name.substring(0, name.length() - planksWord.length()).trim();
            if (!stripped.isEmpty()) {
                return stripped;
            }
        }
        StringBuilder words = new StringBuilder();
        for (String word : wood.split("_")) {
            if (!word.isEmpty()) {
                words.append(words.length() == 0 ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return words.toString();
    }

    public static final class Wood {
        public final Block planks;
        public final int meta;

        Wood(Block planks, int meta) {
            this.planks = planks;
            this.meta = meta;
        }

        public ItemStack stack(int count) {
            return new ItemStack(this.planks, count, Math.max(0, this.meta));
        }

        @SuppressWarnings("deprecation")
        public IBlockState state() {
            return this.meta < 0 ? this.planks.getDefaultState() : this.planks.getStateFromMeta(this.meta);
        }
    }

    static final class Entry {
        final ResourceLocation log;
        final ResourceLocation planks;
        final int meta;
        final String wood;

        Entry(ResourceLocation log, ResourceLocation planks, int meta, String wood) {
            this.log = log;
            this.planks = planks;
            this.meta = meta;
            this.wood = wood;
        }
    }

    public static class SignItem extends ItemWoodSign {

        private final Wood wood;

        public SignItem(Block standing, Block wall, Wood wood) {
            super(standing, wall);
            this.wood = wood;
        }

        @Override
        @SuppressWarnings("deprecation")
        public String getItemStackDisplayName(ItemStack stack) {
            ItemStack planks = this.wood.stack(1);
            String fallback = withoutWoodWords(this.wood.planks.getRegistryName().getPath());
            String name = woodDisplayName(planks.isEmpty() ? "" : planks.getDisplayName(),
                    I18n.translateToLocal("sisyphus.wood_sign.planks"), fallback.isEmpty() ? "wood" : fallback);
            return I18n.translateToLocalFormatted("sisyphus.wood_sign", name);
        }
    }
}
