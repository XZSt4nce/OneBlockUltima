package ru.defea.oneblockultima.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import ru.defea.oneblockultima.OneBlockUltima;

import java.util.HashMap;
import java.util.Map;

public class GeneratedBlockRegistry extends WorldSavedData
{
    private static final String DATA_NAME = OneBlockUltima.MODID + "_generated_blocks";

    private final HashMap<Long, GeneratedBlockEntry> entries = new HashMap<Long, GeneratedBlockEntry>();

    public GeneratedBlockRegistry()
    {
        this(DATA_NAME);
    }

    public GeneratedBlockRegistry(String name)
    {
        super(name);
    }

    private static long posToKey(int x, int y, int z)
    {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFF) << 26 | ((long) z & 0x3FFFFFFL);
    }

    public static GeneratedBlockRegistry get(World world)
    {
        MapStorage storage = world.mapStorage;
        GeneratedBlockRegistry data = (GeneratedBlockRegistry) storage.loadData(GeneratedBlockRegistry.class, DATA_NAME);

        if (data == null)
        {
            data = new GeneratedBlockRegistry();
            storage.setData(DATA_NAME, data);
        }

        return data;
    }

    public void markGenerated(int x, int y, int z, int gx, int gy, int gz, String setId, int currency, int level, String blockRegistry, int blockMeta)
    {
        entries.put(posToKey(x, y, z), new GeneratedBlockEntry(gx, gy, gz, setId, currency, level, blockRegistry, blockMeta));
        markDirty();
    }

    public boolean isGenerated(int x, int y, int z)
    {
        return entries.containsKey(posToKey(x, y, z));
    }

    public GeneratedBlockEntry getEntry(int x, int y, int z)
    {
        return entries.get(posToKey(x, y, z));
    }

    public int[] getGeneratorPos(int x, int y, int z)
    {
        GeneratedBlockEntry entry = entries.get(posToKey(x, y, z));
        return entry == null ? null : new int[]{entry.gx, entry.gy, entry.gz};
    }

    public void remove(int x, int y, int z)
    {
        if (entries.remove(posToKey(x, y, z)) != null)
        {
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        entries.clear();
        NBTTagList list = nbt.getTagList("entries", 10);

        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound entryTag = (NBTTagCompound) list.getCompoundTagAt(i);
            int x = entryTag.getInteger("x");
            int y = entryTag.getInteger("y");
            int z = entryTag.getInteger("z");
            int gx = entryTag.getInteger("gx");
            int gy = entryTag.getInteger("gy");
            int gz = entryTag.getInteger("gz");
            String blockRegistry = entryTag.getString("blockRegistry");
            int blockMeta = entryTag.getInteger("blockMeta");
            entries.put(posToKey(x, y, z), new GeneratedBlockEntry(
                    gx, gy, gz,
                    entryTag.getString("setId"),
                    entryTag.getInteger("currency"),
                    entryTag.getInteger("level"),
                    blockRegistry.isEmpty() ? null : blockRegistry,
                    blockMeta
            ));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList list = new NBTTagList();

        for (Map.Entry<Long, GeneratedBlockEntry> entry : entries.entrySet())
        {
            NBTTagCompound entryTag = new NBTTagCompound();
            long key = entry.getKey();
            int x = (int) (key >> 38);
            int y = (int) (key >> 26) & 0xFF;
            int z = (int) (key & 0x3FFFFFF);
            GeneratedBlockEntry value = entry.getValue();

            entryTag.setInteger("x", x);
            entryTag.setInteger("y", y);
            entryTag.setInteger("z", z);
            entryTag.setInteger("gx", value.gx);
            entryTag.setInteger("gy", value.gy);
            entryTag.setInteger("gz", value.gz);
            entryTag.setString("setId", value.setId);
            entryTag.setInteger("currency", value.currency);
            entryTag.setInteger("level", value.level);
            entryTag.setString("blockRegistry", value.blockRegistry == null ? "" : value.blockRegistry);
            entryTag.setInteger("blockMeta", value.blockMeta);
            list.appendTag(entryTag);
        }

        compound.setTag("entries", list);
    }

    public static class GeneratedBlockEntry
    {
        public final int gx;
        public final int gy;
        public final int gz;
        public final String setId;
        public final int currency;
        public final int level;
        public final String blockRegistry;
        public final int blockMeta;

        public GeneratedBlockEntry(int gx, int gy, int gz, String setId, int currency, int level)
        {
            this(gx, gy, gz, setId, currency, level, null, 0);
        }

        public GeneratedBlockEntry(int gx, int gy, int gz, String setId, int currency, int level, String blockRegistry, int blockMeta)
        {
            this.gx = gx;
            this.gy = gy;
            this.gz = gz;
            this.setId = setId;
            this.currency = currency;
            this.level = level;
            this.blockRegistry = blockRegistry;
            this.blockMeta = blockMeta;
        }
    }
}
