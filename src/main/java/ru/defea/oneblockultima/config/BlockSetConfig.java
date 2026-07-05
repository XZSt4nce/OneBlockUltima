package ru.defea.oneblockultima.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.defea.oneblockultima.NBTTagCompoundAdapter;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class BlockSetConfig
{
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(NBTTagCompound.class, new NBTTagCompoundAdapter())
            .create();

    private static BlockSetConfig instance;
    static File configFile;

    private List<BlockSetDefinition> sets = new ArrayList<>();
    private SettingsDefinition settings = new SettingsDefinition();

    private transient Map<String, BlockSetDefinition> setsById = new HashMap<>();

    public static BlockSetConfig get()
    {
        return instance;
    }

    public static File getConfigFile()
    {
        return configFile;
    }

    public static boolean saveCurrentConfig()
    {
        if (instance == null || configFile == null)
        {
            return false;
        }

        saveToFile(configFile, instance);
        return true;
    }

    public static boolean reload()
    {
        if (configFile == null)
        {
            return false;
        }

        BlockSetConfig loaded = loadFromFile(configFile);
        if (loaded == null)
        {
            // Если файл не существует, копируем конфиг по умолчанию
            copyDefaultToConfigFile(configFile);
            loaded = loadFromFile(configFile);
            if (loaded == null)
            {
                // Если всё ещё не получилось, загружаем из ресурсов
                loaded = loadDefaultFromResources();
                if (loaded == null)
                {
                    loaded = new BlockSetConfig();
                }
                saveToFile(configFile, loaded);
            }
        }

        instance = loaded;
        instance.buildIndex();
        return true;
    }

    public static void load(File configDir)
    {
        if (configDir == null)
        {
            OneBlockUltima.getLogger().error("Config directory is null, cannot load configuration");
            instance = new BlockSetConfig();
            instance.buildIndex();
            return;
        }

        configFile = new File(configDir, "oneblockultima/blocksets.json");
        reload();
    }

    private static BlockSetConfig loadFromFile(File file)
    {
        if (file == null || !file.exists())
        {
            return null;
        }

        try (FileReader reader = new FileReader(file))
        {
            return GSON.fromJson(reader, BlockSetConfig.class);
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("Failed to load blocksets.json from config directory", e);
            return null;
        }
    }

    private static void saveToFile(File file, BlockSetConfig config)
    {
        if (file == null || config == null)
        {
            return;
        }

        try (OutputStream outputStream = Files.newOutputStream(file.toPath());
             java.io.Writer writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
        {
            writer.write(GSON.toJson(config));
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("Failed to save blocksets.json to config directory", e);
        }
    }

    private static void copyDefaultToConfigFile(File file)
    {
        if (file == null)
        {
            return;
        }

        try (InputStream input = BlockSetConfig.class.getResourceAsStream("/assets/oneblockultima/blocksets.json"))
        {
            if (input == null)
            {
                return;
            }

            if (file.getParentFile() != null && !file.getParentFile().exists())
            {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }

            try (OutputStream output = Files.newOutputStream(file.toPath()))
            {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) >= 0)
                {
                    output.write(buffer, 0, read);
                }
            }
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("Failed to copy default blocksets.json to config directory", e);
        }
    }

    private static BlockSetConfig loadDefaultFromResources()
    {
        try (InputStream input = BlockSetConfig.class.getResourceAsStream("/assets/oneblockultima/blocksets.json"))
        {
            if (input == null)
            {
                return new BlockSetConfig();
            }

            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8))
            {
                return GSON.fromJson(reader, BlockSetConfig.class);
            }
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("Failed to load default blocksets.json from resources", e);
            return new BlockSetConfig();
        }
    }

    public static class BlockElementDefinition
    {
        public String registry;
        public int meta;
        public List<Integer> metas = new ArrayList<>();
        public int baseLevel = 1;
        public int baseChance = 0;
        public int currency = 0;
        String dropItem = null;
        public NBTTagCompound nbtTags = new NBTTagCompound();

        public List<Integer> getMetaValues()
        {
            if (metas != null && !metas.isEmpty())
            {
                return metas;
            }

            return Collections.singletonList(meta);
        }
    }

    public static class MobElementDefinition
    {
        public String registry;
        public int baseLevel = 1;
        public int baseChance = 0;
        public int count = 1;
        public NBTTagCompound nbtTags = new NBTTagCompound();
    }

    // internal runtime element used for unified computations of percentages
    private static class InternalElement
    {
        String registry;
        int meta;
        int baseLevel;
        int baseChance;
        int currency;
        String dropItem = null;
        int count;
        boolean isMob;
        NBTTagCompound nbtTags = new NBTTagCompound();
    }

    private void buildIndex()
    {
        setsById = new HashMap<>();
        if (sets == null)
        {
            sets = new ArrayList<>();
        }

        for (BlockSetDefinition set : sets)
        {
            if (set != null && set.id != null)
            {
                setsById.put(set.id, set);
            }
        }
    }

    public List<BlockSetDefinition> getSets()
    {
        return sets == null ? Collections.emptyList() : sets;
    }

    public BlockSetDefinition getSet(String id)
    {
        return setsById.get(id);
    }

    public String getDefaultSetId()
    {
        if (sets == null || sets.isEmpty())
        {
            return null;
        }

        for (BlockSetDefinition set : sets)
        {
            if (set != null && set.isAvailable())
            {
                return set.id;
            }
        }

        return sets.get(0).id;
    }

    public SettingsDefinition getSettings()
    {
        if (settings == null)
        {
            settings = new SettingsDefinition();
        }
        return settings;
    }

    public static class SettingsDefinition
    {
        public boolean disableFluidGeneration = false;
        public boolean disableMobGeneration = false;
        public boolean disableChestGeneration = false;
    }

    public static class UnlockConditionGroup
    {
        public String mode = "any";
        public List<UnlockConditionDefinition> conditions = new ArrayList<>();
    }

    public static class UnlockConditionDefinition
    {
        public String type = "set_level";
        public String setId;
        public int level = 0;
        public int count = 0;

        public boolean isSatisfied(IOneBlockPlayerData data)
        {
            return isSatisfied(data, null);
        }

        public boolean isSatisfied(IOneBlockPlayerData data, TileEntityOneBlockGenerator generator)
        {
            if (data == null)
            {
                return false;
            }

            switch (type == null ? "" : type.toLowerCase(Locale.ROOT))
            {
                case "broken_blocks_total":
                    return data.getBrokenBlocksCount() >= count;
                case "broken_blocks":
                    return data.getBrokenBlocksCount(setId) >= count;
                case "set_level":
                    int generatorLevel = generator == null ? data.getSetLevel(setId) : generator.getSetLevel(setId);
                    return generatorLevel >= level;
                default:
                    return false;
            }
        }
    }

    public static class BlockSetDefinition
    {
        public String id;
        public List<String> requiredMods = new ArrayList<>();
        public int unlockCost = 0;
        public UnlockConditionGroup unlockConditions;

        // New format: separate lists for block-elements and mob-elements
        public List<BlockElementDefinition> blocks = new ArrayList<>();
        public List<MobElementDefinition> mobs = new ArrayList<>();

        // transient cache for computed levels
        private transient java.util.Map<Integer, SetLevelDefinition> computedLevels = null;

        public boolean isAvailable()
        {
            if (requiredMods.isEmpty())
            {
                return true;
            }

            for (String modId : requiredMods)
            {
                if (modId == null || modId.isEmpty())
                {
                    continue;
                }
                if (!Loader.isModLoaded(modId))
                {
                    return false;
                }
            }
            return true;
        }

        public boolean hasUnlockRequirementsMet(IOneBlockPlayerData data)
        {
            return hasUnlockRequirementsMet(data, null);
        }

        public boolean hasUnlockRequirementsMet(IOneBlockPlayerData data, TileEntityOneBlockGenerator generator)
        {
            if (unlockConditions == null || unlockConditions.conditions == null || unlockConditions.conditions.isEmpty())
            {
                return true;
            }

            boolean requireAll = "all".equalsIgnoreCase(unlockConditions.mode);
            if (requireAll)
            {
                for (UnlockConditionDefinition condition : unlockConditions.conditions)
                {
                    if (condition == null || !condition.isSatisfied(data, generator))
                    {
                        return false;
                    }
                }
                return true;
            }

            for (UnlockConditionDefinition condition : unlockConditions.conditions)
            {
                if (condition != null && condition.isSatisfied(data, generator))
                {
                    return true;
                }
            }
            return false;
        }

        public SetLevelDefinition getLevel(int level)
        {
            // Ensure computedLevels built
            ensureComputedLevels();
            return computedLevels.get(level);
        }

        public int getMaxLevel()
        {
            ensureComputedLevels();
            int max = 0;
            for (Integer lvl : computedLevels.keySet()) if (lvl > max) max = lvl;
            return max;
        }

        private void ensureComputedLevels()
        {
            if (computedLevels != null) return;

            OneBlockUltima.getLogger().info("[Config] ensureComputedLevels called for set: {}", id);
            OneBlockUltima.getLogger().info("[Config] blocks size: {}", blocks.size());
            OneBlockUltima.getLogger().info("[Config] mobs size: {}", mobs.size());

            computedLevels = new java.util.HashMap<>();

            // If still no elements, nothing to compute
            if (blocks.isEmpty() && mobs.isEmpty()) return;

            // Build levels iteratively until stabilization
            java.util.List<InternalElement> elems = new ArrayList<>();
            for (BlockElementDefinition be : blocks) {
                if (be == null) continue;
                List<Integer> metaValues = be.getMetaValues();
                if (metaValues == null || metaValues.isEmpty())
                {
                    metaValues = Collections.singletonList(be.meta);
                }

                for (Integer metaValue : metaValues)
                {
                    InternalElement ie = new InternalElement();
                    ie.registry = be.registry;
                    ie.meta = metaValue == null ? 0 : metaValue;
                    ie.baseLevel = be.baseLevel;
                    ie.baseChance = be.baseChance;
                    ie.currency = be.currency;
                    ie.dropItem = be.dropItem;
                    ie.count = 1;
                    ie.isMob = false;
                    Set<String> keys = be.nbtTags.getKeySet();
                    for (String key : keys) {
                        NBTBase tag = be.nbtTags.getTag(key);
                        ie.nbtTags.setTag(key, tag.copy());
                    }
                    elems.add(ie);
                }
            }
            for (MobElementDefinition me : mobs) {
                if (me == null) continue;
                InternalElement ie = new InternalElement();
                ie.registry = me.registry;
                ie.meta = 0;
                ie.baseLevel = me.baseLevel;
                ie.baseChance = me.baseChance;
                ie.currency = 0;
                ie.dropItem = null;
                ie.count = me.count;
                ie.isMob = true;
                Set<String> keys = me.nbtTags.getKeySet();
                for (String key : keys) {
                    NBTBase tag = me.nbtTags.getTag(key);
                    ie.nbtTags.setTag(key, tag.copy());
                }
                elems.add(ie);
            }
            // determine minimal baseLevel and iterate
            int minLevel = Integer.MAX_VALUE;
            for (InternalElement e : elems) if (e.baseLevel < minLevel) minLevel = e.baseLevel;
            if (minLevel == Integer.MAX_VALUE) minLevel = 1;

            java.util.List<InternalElement> blockElems = new ArrayList<>();
            java.util.List<InternalElement> mobElems = new ArrayList<>();
            for (InternalElement e : elems)
            {
                if (e.isMob)
                {
                    mobElems.add(e);
                }
                else
                {
                    blockElems.add(e);
                }
            }

            java.util.Map<String, Double> prevPercBlocks = new java.util.HashMap<>();
            java.util.Map<String, Double> prevPercMobs = new java.util.HashMap<>();

            int level = minLevel;
            while (true)
            {
                java.util.List<InternalElement> availBlocks = new ArrayList<>();
                java.util.List<InternalElement> availMobs = new ArrayList<>();
                for (InternalElement e : blockElems) if (e.baseLevel <= level) availBlocks.add(e);
                for (InternalElement e : mobElems) if (e.baseLevel <= level) availMobs.add(e);

                if (availBlocks.isEmpty() && availMobs.isEmpty())
                {
                    level++;
                    if (level > 1000) break;
                    continue;
                }

                java.util.Map<String, Integer> proposedBlocks = computeLevelPercentages(availBlocks, prevPercBlocks, 100);
                java.util.Map<String, Integer> proposedMobs = computeLevelPercentages(availMobs, prevPercMobs, 10);

                SetLevelDefinition lvlDef = new SetLevelDefinition();
                lvlDef.level = level;
                int baseOpenLevel = minLevel <= 0 ? 1 : minLevel;
                lvlDef.upgradeCost = Math.max(0, unlockCost + 50 * (level - baseOpenLevel));
                lvlDef.blocks = new java.util.ArrayList<>();
                lvlDef.mobs = new java.util.ArrayList<>();

                for (InternalElement e : availBlocks)
                {
                    String key = elementKey(e);
                    int percent = proposedBlocks.getOrDefault(key, 0);
                    BlockEntryDefinition b = new BlockEntryDefinition();
                    b.registry = e.registry;
                    b.meta = e.meta;
                    b.chance = percent;
                    b.currency = e.currency;
                    b.dropItem = e.dropItem;
                    Set<String> keys = e.nbtTags.getKeySet();
                    for (String nbtKey : keys) {
                        NBTBase tag = e.nbtTags.getTag(nbtKey);
                        b.nbtTags.setTag(nbtKey, tag.copy());
                    }
                    lvlDef.blocks.add(b);
                }

                for (InternalElement e : availMobs)
                {
                    String key = elementKey(e);
                    int percent = proposedMobs.getOrDefault(key, 0);
                    MobEntryDefinition m = new MobEntryDefinition();
                    m.registry = e.registry;
                    m.chance = percent;
                    m.count = e.count;
                    Set<String> keys = e.nbtTags.getKeySet();
                    for (String nbtKey : keys) {
                        NBTBase tag = e.nbtTags.getTag(nbtKey);
                        m.nbtTags.setTag(nbtKey, tag.copy());
                    }
                    lvlDef.mobs.add(m);
                }

                computedLevels.put(level, lvlDef);

                boolean changed = hasSignificantChange(availBlocks, prevPercBlocks, proposedBlocks)
                        || hasSignificantChange(availMobs, prevPercMobs, proposedMobs);

                for (String k : proposedBlocks.keySet()) prevPercBlocks.put(k, proposedBlocks.get(k).doubleValue());
                for (String k : proposedMobs.keySet()) prevPercMobs.put(k, proposedMobs.get(k).doubleValue());

                level++;
                if (!changed)
                {
                    break;
                }
                if (level > 200) break;
            }
        }
    }

    private static java.util.Map<String, Integer> computeLevelPercentages(java.util.List<InternalElement> avail, java.util.Map<String, Double> prevPerc, int maximumTotal)
    {
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        if (avail == null || avail.isEmpty())
        {
            return result;
        }

        java.util.Map<String, Double> prevForThis = new java.util.HashMap<>();
        for (InternalElement e : avail)
        {
            String key = elementKey(e);
            prevForThis.put(key, prevPerc.getOrDefault(key, (double)Math.max(1, e.baseChance)));
        }

        double mean = 0.0;
        for (Double v : prevForThis.values()) mean += v;
        mean = mean / prevForThis.size();

        java.util.Map<String, Double> proposed = new java.util.HashMap<>();
        double totalChange = 0.0;
        for (InternalElement e : avail)
        {
            String key = elementKey(e);
            double prev = prevForThis.get(key);
            double alpha = 0.24 / (1.0 + e.baseLevel * 0.15) / (1.0 + (e.baseChance / 100.0));
            double next = prev + (mean - prev) * alpha;
            proposed.put(key, next);
            totalChange += Math.abs(next - prev);
        }

        boolean newAdded = false;
        for (InternalElement e : avail)
        {
            String key = elementKey(e);
            if (!prevPerc.containsKey(key)) { newAdded = true; break; }
        }

        if (!newAdded)
        {
            if (totalChange < 1.0)
            {
                double factor = totalChange <= 0.0 ? 0.0 : (1.0 / totalChange);
                for (String k : proposed.keySet())
                {
                    double adj = prevForThis.get(k) + (proposed.get(k) - prevForThis.get(k)) * factor;
                    proposed.put(k, adj);
                }
            }
            else if (totalChange > 3.0)
            {
                double factor = 3.0 / totalChange;
                for (String k : proposed.keySet())
                {
                    double adj = prevForThis.get(k) + (proposed.get(k) - prevForThis.get(k)) * factor;
                    proposed.put(k, adj);
                }
            }
        }

        double sum = 0.0;
        for (Double v : proposed.values()) sum += v;
        if (sum <= 0.0) sum = proposed.size();
        double scale = (double) maximumTotal / sum;
        proposed.replaceAll((k, v) -> v * scale);

        java.util.Map<String, Integer> rounded = new java.util.HashMap<>();
        java.util.List<java.util.Map.Entry<String, Double>> remainderList = new ArrayList<>();
        int totalRounded = 0;
        for (String k : proposed.keySet())
        {
            double value = proposed.get(k);
            int floorValue = (int) Math.floor(value);
            rounded.put(k, floorValue);
            totalRounded += floorValue;
            remainderList.add(new java.util.AbstractMap.SimpleEntry<>(k, value - floorValue));
        }

        int diff = maximumTotal - totalRounded;
        if (diff > 0)
        {
            remainderList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            for (int i = 0; i < remainderList.size() && diff > 0; i++, diff--)
            {
                String key = remainderList.get(i).getKey();
                rounded.put(key, rounded.get(key) + 1);
            }
        }
        else if (diff < 0)
        {
            remainderList.sort(Comparator.comparingDouble(Map.Entry::getValue));
            for (int i = 0; i < remainderList.size() && diff < 0; i++, diff++)
            {
                String key = remainderList.get(i).getKey();
                rounded.compute(key, (k, current) -> Math.max(0, current == null ? 0 : current - 1));
            }
        }

        return rounded;
    }

    private static boolean hasSignificantChange(java.util.List<InternalElement> avail, java.util.Map<String, Double> prevPerc, java.util.Map<String, ? extends Number> proposed)
    {
        if (avail == null || avail.isEmpty())
        {
            return false;
        }
        for (InternalElement e : avail)
        {
            String key = elementKey(e);
            double prev = prevPerc.getOrDefault(key, 0.0);
            Number nextValue = proposed.get(key);
            double next = nextValue == null ? 0.0 : nextValue.doubleValue();
            if (Math.abs(next - prev) >= 0.5)
            {
                return true;
            }
        }
        return false;
    }

    private static String elementKey(InternalElement e)
    {
        return e.registry + "@" + e.meta + "@" + e.baseLevel + (e.isMob ? "#m" : "");
    }

    public static class SetLevelDefinition
    {
        public int level;
        public int upgradeCost;
        public List<BlockEntryDefinition> blocks = new ArrayList<>();
        public List<MobEntryDefinition> mobs = new ArrayList<>();

        public BlockEntryDefinition pickRandom(Random random)
        {
            if (blocks == null || blocks.isEmpty())
            {
                return null;
            }

            int totalChance = 0;
            for (BlockEntryDefinition entry : blocks)
            {
                if (entry != null && entry.getChance() > 0 && entry.resolveBlock() != null)
                {
                    totalChance += entry.getChance();
                }
            }

            if (totalChance <= 0)
            {
                return null;
            }

            int roll = random.nextInt(totalChance);
            int current = 0;
            for (BlockEntryDefinition entry : blocks)
            {
                if (entry != null && entry.getChance() > 0 && entry.resolveBlock() != null)
                {
                    current += entry.getChance();
                    if (roll < current)
                    {
                        return entry;
                    }
                }
            }

            return null;
        }

        public MobEntryDefinition pickMob(Random random)
        {
            if (mobs == null || mobs.isEmpty())
            {
                return null;
            }

            int totalChance = 0;
            for (MobEntryDefinition entry : mobs)
            {
                if (entry != null && entry.getChance() > 0)
                {
                    totalChance += entry.getChance();
                }
            }

            if (totalChance <= 0)
            {
                return null;
            }

            int roll = random.nextInt(Math.max(100, totalChance));
            int current = 0;
            for (MobEntryDefinition entry : mobs)
            {
                if (entry != null && entry.getChance() > 0)
                {
                    current += entry.getChance();
                    if (roll < current)
                    {
                        return entry;
                    }
                }
            }

            return null;
        }
    }

    public static class BlockEntryDefinition
    {
        public int id;
        public String registry;
        public int meta;
        public int chance;
        public int currency;
        public String dropItem = null;
        public NBTTagCompound nbtTags = new NBTTagCompound();

        public int getChance()
        {
            return chance > 0 ? chance : 1;
        }

        public Block resolveBlock()
        {
            if (registry == null || registry.isEmpty())
            {
                return null;
            }

            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registry));
        }

        public boolean isFluid() {
            net.minecraft.block.Block block = this.resolveBlock();
            if (block == null) return false;
            return block instanceof IFluidBlock || FluidRegistry.lookupFluidForBlock(block) != null;
        }

        public net.minecraft.item.ItemStack getPickBlock()
        {
            // If dropItem is specified, use it
            if (dropItem != null && !dropItem.isEmpty())
            {
                try
                {
                    net.minecraft.item.Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(dropItem));
                    if (item != null && item != net.minecraft.init.Items.AIR)
                    {
                        return new net.minecraft.item.ItemStack(item, 1, 0);
                    }
                }
                catch (Exception ignored) {}
            }

            Block block = resolveBlock();
            if (block != null)
            {
                try
                {
                    //noinspection deprecation
                    IBlockState state = block.getStateFromMeta(meta);
                    //noinspection DataFlowIssue
                    net.minecraft.item.ItemStack pickStack = block.getPickBlock(state, null, null, null, null);
                    if (!pickStack.isEmpty())
                    {
                        return pickStack;
                    }
                }
                catch (Exception ignored) {}

                net.minecraft.item.Item blockItem = net.minecraft.item.Item.getItemFromBlock(block);
                if (blockItem != net.minecraft.init.Items.AIR)
                {
                    try
                    {
                        return new net.minecraft.item.ItemStack(blockItem, 1, meta);
                    }
                    catch (Exception ignored) {}
                }
            }

            return net.minecraft.item.ItemStack.EMPTY;
        }
    }

    public static class MobEntryDefinition
    {
        public String registry;
        public int chance;
        public int count = 1;
        public NBTTagCompound nbtTags = new NBTTagCompound();

        public int getChance()
        {
            return chance;
        }
    }
}