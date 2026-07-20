package ru.defea.oneblockultima;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry.GeneratedBlockEntry;

import static org.junit.Assert.*;

public class GeneratedBlockRegistryTest {

    private GeneratedBlockRegistry newRegistry() {
        return new GeneratedBlockRegistry();
    }

    @Test
    public void freshRegistryIsEmpty() {
        GeneratedBlockRegistry reg = newRegistry();
        assertFalse(reg.isGenerated(0, 0, 0));
        assertNull(reg.getEntry(0, 0, 0));
    }

    @Test
    public void markGeneratedMakesBlockTracked() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(1, 64, 2, 1, 63, 2, "classic", 10, 1, "minecraft:stone", 0);

        assertTrue(reg.isGenerated(1, 64, 2));
    }

    @Test
    public void getEntryReturnsCorrectData() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(5, 70, 5, 5, 69, 5, "nether", 25, 3, "minecraft:netherrack", 0);

        GeneratedBlockEntry entry = reg.getEntry(5, 70, 5);
        assertNotNull(entry);
        assertEquals(5, entry.gx);
        assertEquals(69, entry.gy);
        assertEquals(5, entry.gz);
        assertEquals("nether", entry.setId);
        assertEquals(25, entry.currency);
        assertEquals(3, entry.level);
        assertEquals("minecraft:netherrack", entry.blockRegistry);
        assertEquals(0, entry.blockMeta);
    }

    @Test
    public void getGeneratorPosReturnsCorrectPos() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(10, 64, 10, 10, 63, 10, "classic", 5, 1, "minecraft:dirt", 0);

        int[] genPos = reg.getGeneratorPos(10, 64, 10);
        assertNotNull(genPos);
        assertEquals(10, genPos[0]);
        assertEquals(63, genPos[1]);
        assertEquals(10, genPos[2]);
    }

    @Test
    public void getGeneratorPosReturnsNullForUnknown() {
        GeneratedBlockRegistry reg = newRegistry();
        assertNull(reg.getGeneratorPos(99, 99, 99));
    }

    @Test
    public void removeUntracksBlock() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(0, 64, 0, 0, 0, 0, "classic", 10, 1, "minecraft:stone", 0);
        assertTrue(reg.isGenerated(0, 64, 0));

        reg.remove(0, 64, 0);
        assertFalse(reg.isGenerated(0, 64, 0));
        assertNull(reg.getEntry(0, 64, 0));
    }

    @Test
    public void removeUnknownPosIsNoOp() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.remove(99, 99, 99);
        assertFalse(reg.isGenerated(99, 99, 99));
    }

    @Test
    public void multipleEntriesAreIndependent() {
        GeneratedBlockRegistry reg = newRegistry();

        reg.markGenerated(1, 64, 1, 0, 0, 0, "classic", 10, 1, "minecraft:stone", 0);
        reg.markGenerated(2, 64, 2, 0, 0, 0, "nether", 20, 2, "minecraft:netherrack", 0);

        assertEquals("classic", reg.getEntry(1, 64, 1).setId);
        assertEquals("nether", reg.getEntry(2, 64, 2).setId);

        reg.remove(1, 64, 1);
        assertFalse(reg.isGenerated(1, 64, 1));
        assertTrue(reg.isGenerated(2, 64, 2));
    }

    @Test
    public void markGeneratedOverwritesExistingEntry() {
        GeneratedBlockRegistry reg = newRegistry();

        reg.markGenerated(1, 64, 1, 0, 0, 0, "classic", 10, 1, "minecraft:stone", 0);
        reg.markGenerated(1, 64, 1, 0, 0, 0, "nether", 30, 5, "minecraft:diamond_block", 0);

        GeneratedBlockEntry entry = reg.getEntry(1, 64, 1);
        assertEquals("nether", entry.setId);
        assertEquals(30, entry.currency);
        assertEquals(5, entry.level);
        assertEquals("minecraft:diamond_block", entry.blockRegistry);
    }

    @Test
    public void nbtRoundtripPreservesAllEntries() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(1, 64, 1, 0, 0, 0, "classic", 10, 1, "minecraft:stone", 0);
        reg.markGenerated(2, 65, 2, 0, 0, 0, "nether", 25, 3, "minecraft:netherrack", 2);
        reg.markGenerated(3, 66, 3, 0, 0, 0, "end", 0, 1, null, 0);

        NBTTagCompound nbt = new NBTTagCompound();
        reg.writeToNBT(nbt);
        GeneratedBlockRegistry loaded = newRegistry();
        loaded.readFromNBT(nbt);

        assertTrue(loaded.isGenerated(1, 64, 1));
        assertTrue(loaded.isGenerated(2, 65, 2));
        assertTrue(loaded.isGenerated(3, 66, 3));

        GeneratedBlockEntry e1 = loaded.getEntry(1, 64, 1);
        assertNotNull(e1);
        assertEquals("classic", e1.setId);
        assertEquals(10, e1.currency);
        assertEquals(1, e1.level);
        assertEquals("minecraft:stone", e1.blockRegistry);
        assertEquals(0, e1.blockMeta);

        GeneratedBlockEntry e2 = loaded.getEntry(2, 65, 2);
        assertNotNull(e2);
        assertEquals("nether", e2.setId);
        assertEquals(25, e2.currency);
        assertEquals(3, e2.level);
        assertEquals(2, e2.blockMeta);

        GeneratedBlockEntry e3 = loaded.getEntry(3, 66, 3);
        assertNotNull(e3);
        assertEquals("end", e3.setId);
        assertNull(e3.blockRegistry);
    }

    @Test
    public void nbtRoundtripWithEmptyRegistry() {
        GeneratedBlockRegistry reg = newRegistry();
        NBTTagCompound nbt = new NBTTagCompound();
        reg.writeToNBT(nbt);
        GeneratedBlockRegistry loaded = newRegistry();
        loaded.readFromNBT(nbt);

        assertFalse(loaded.isGenerated(0, 0, 0));
    }

    @Test
    public void readFromNbtClearsExistingEntries() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(1, 64, 1, 0, 0, 0, "classic", 10, 1, "minecraft:stone", 0);
        assertTrue(reg.isGenerated(1, 64, 1));

        NBTTagCompound nbt = new NBTTagCompound();
        reg.readFromNBT(nbt);

        assertFalse(reg.isGenerated(1, 64, 1));
    }

    @Test
    public void entryDefaultConstructorSetsDefaults() {
        GeneratedBlockEntry entry = new GeneratedBlockEntry(0, 0, 0, "classic", 10, 1);
        assertEquals(0, entry.gx);
        assertEquals(0, entry.gy);
        assertEquals(0, entry.gz);
        assertEquals("classic", entry.setId);
        assertEquals(10, entry.currency);
        assertEquals(1, entry.level);
        assertNull(entry.blockRegistry);
        assertEquals(0, entry.blockMeta);
    }

    @Test
    public void entryFullConstructorSetsAllFields() {
        GeneratedBlockEntry entry = new GeneratedBlockEntry(
                5, 63, 5, "nether", 50, 7, "minecraft:obsidian", 3
        );
        assertEquals(5, entry.gx);
        assertEquals(63, entry.gy);
        assertEquals(5, entry.gz);
        assertEquals("nether", entry.setId);
        assertEquals(50, entry.currency);
        assertEquals(7, entry.level);
        assertEquals("minecraft:obsidian", entry.blockRegistry);
        assertEquals(3, entry.blockMeta);
    }

    @Test
    public void nbtRoundtripPreservesGeneratorPos() {
        GeneratedBlockRegistry reg = newRegistry();
        reg.markGenerated(10, 64, 10, 10, 63, 10, "classic", 5, 1, "minecraft:stone", 0);

        NBTTagCompound nbt = new NBTTagCompound();
        reg.writeToNBT(nbt);
        GeneratedBlockRegistry loaded = newRegistry();
        loaded.readFromNBT(nbt);

        int[] result = loaded.getGeneratorPos(10, 64, 10);
        assertNotNull(result);
        assertEquals(10, result[0]);
        assertEquals(63, result[1]);
        assertEquals(10, result[2]);
    }

}
