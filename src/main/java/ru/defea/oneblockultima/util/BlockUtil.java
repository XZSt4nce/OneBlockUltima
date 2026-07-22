package ru.defea.oneblockultima.util;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BlockUtil
{
    private BlockUtil()
    {
    }

    public static int[] getReplacementBlockForGeneratorPlacement(int blockId, int meta, int belowBlockId, int belowMeta)
    {
        if (belowBlockId != Block.getIdFromBlock(ModBlocks.ONE_BLOCK_GENERATOR))
        {
            return null;
        }

        if (blockId == Block.getIdFromBlock(Blocks.bedrock))
        {
            return new int[]{ Block.getIdFromBlock(ModBlocks.CUSTOM_BEDROCK), 0 };
        }

        if (blockId == Block.getIdFromBlock(Blocks.end_portal_frame))
        {
            return new int[]{ Block.getIdFromBlock(ModBlocks.CUSTOM_PORTAL_FRAME), meta };
        }

        return null;
    }

    public static void placeBlockWithNBT(World world, int x, int y, int z, int blockId, int meta, @Nullable NBTTagCompound nbtTags)
    {
        if (world == null || blockId <= 0)
        {
            return;
        }

        Block block = Block.getBlockById(blockId);
        if (block == null)
        {
            return;
        }

        blockId = normalizeLiquidBlockId(blockId);
        block = Block.getBlockById(blockId);
        if (block == null) return;

        int belowBlockId = Block.getIdFromBlock(world.getBlock(x, y - 1, z));
        int belowMeta = world.getBlockMetadata(x, y - 1, z);
        int[] replacement = getReplacementBlockForGeneratorPlacement(blockId, meta, belowBlockId, belowMeta);
        if (replacement != null)
        {
            blockId = replacement[0];
            meta = replacement[1];
            block = Block.getBlockById(blockId);
        }

        TileEntity preCreatedTileEntity = null;
        if (nbtTags != null && !nbtTags.hasNoTags() && block instanceof BlockContainer)
        {
            try
            {
                TileEntity tileEntity = block.createTileEntity(world, meta);

                if (tileEntity != null)
                {
                    NBTTagCompound tileNbt = new NBTTagCompound();
                    tileEntity.writeToNBT(tileNbt);
                    for (Object obj : nbtTags.func_150296_c())
                    {
                        String key = (String) obj;
                        NBTBase tag = nbtTags.getTag(key);
                        if (tag != null)
                        {
                            tileNbt.setTag(key, tag.copy());
                        }
                    }
                    tileEntity.readFromNBT(tileNbt);

                    tileEntity.xCoord = x;
                    tileEntity.yCoord = y;
                    tileEntity.zCoord = z;

                    if (tileEntity instanceof IInventory)
                    {
                        IInventory inv = (IInventory) tileEntity;
                        NBTTagCompound nbt = new NBTTagCompound();
                        tileEntity.writeToNBT(nbt);

                        if (nbt.hasKey("LootTable", 8))
                        {
                            String lootTableId = nbt.getString("LootTable");
                            OneBlockUltima.getLogger().info("[Generator] Found LootTable tag '{}' for TileEntity, loading via loot table", lootTableId);
                            nbt.removeTag("LootTable");
                            preCreatedTileEntity = tileEntity;
                            tileEntity.markDirty();
                        }
                    }

                    if (preCreatedTileEntity == null)
                    {
                        preCreatedTileEntity = tileEntity;
                    }

                    OneBlockUltima.getLogger().info("[Generator] Pre-configured TileEntity at ({},{},{}) with NBT tags", x, y, z);
                }
                else
                {
                    OneBlockUltima.getLogger().warn("[Generator] createTileEntity returned null for block {}", block.getUnlocalizedName());
                }
            }
            catch (Exception e)
            {
                OneBlockUltima.getLogger().error("[Generator] Failed to pre-configure TileEntity for block at ({},{},{})", x, y, z, e);
            }
        }

        world.setBlock(x, y, z, Block.getBlockById(blockId), meta, 3);

        if (preCreatedTileEntity != null)
        {
            world.setTileEntity(x, y, z, preCreatedTileEntity);
            preCreatedTileEntity.xCoord = x;
            preCreatedTileEntity.yCoord = y;
            preCreatedTileEntity.zCoord = z;
            preCreatedTileEntity.markDirty();
        }

        if (nbtTags != null && !nbtTags.hasNoTags() && !(block instanceof BlockContainer))
        {
            applyNbtToBlock(world, x, y, z, nbtTags);
        }
    }

    public static String getDisplayName(Block block, NBTTagCompound nbtTagCompound)
    {
        NBTTagCompound displayTag = (nbtTagCompound != null && nbtTagCompound.hasKey("display", 10))
                ? nbtTagCompound.getCompoundTag("display")
                : null;

        if (displayTag != null)
        {
            if (displayTag.hasKey("Name", 8))
            {
                return displayTag.getString("Name");
            }

            if (displayTag.hasKey("LocName", 8))
            {
                return StatCollector.translateToLocal(displayTag.getString("LocName"));
            }
        }

        return StatCollector.translateToLocal(block.getUnlocalizedName() + ".name").trim();
    }

    public static List<String> getTooltip(BlockSetConfig.BlockEntryDefinition hoveredEntry, boolean advanced)
    {
        List<String> tooltip = new ArrayList<String>();
        Block resolvedBlock = hoveredEntry.resolveBlock();
        if (resolvedBlock == null)
        {
            return tooltip;
        }

        boolean hasTagCompound = hoveredEntry.nbtTags != null;
        NBTTagCompound nbtTagCompound = (hasTagCompound && hoveredEntry.nbtTags.hasKey("display", 10))
                ? hoveredEntry.nbtTags.getCompoundTag("display")
                : null;
        boolean hasDisplayName = nbtTagCompound != null && nbtTagCompound.hasKey("Name", 8);

        String s = getDisplayName(resolvedBlock, nbtTagCompound);
        if (s == null || s.isEmpty())
        {
            s = hoveredEntry.registry;
        }

        if (advanced)
        {
            String s1 = "";

            if (!s.isEmpty())
            {
                s = s + " (";
                s1 = ")";
            }

            int i = Block.getIdFromBlock(resolvedBlock);
            int meta = hoveredEntry.meta;

            if (meta > 0)
            {
                s = s + String.format("#%04d/%d%s", i, meta, s1);
            }
            else
            {
                s = s + String.format("#%04d%s", i, s1);
            }
        }
        else if (!hasDisplayName)
        {
            s = s + " #" + hoveredEntry.meta;
        }

        tooltip.add(s);

        int i1 = 0;

        try
        {
            if (nbtTagCompound != null && nbtTagCompound.hasKey("HideFlags", 99))
            {
                i1 = nbtTagCompound.getInteger("HideFlags");
            }
        }
        catch (Exception ignored)
        {
        }

        if ((i1 & 1) == 0)
        {
            try
            {
                NBTTagList nbttaglist = (nbtTagCompound != null && nbtTagCompound.hasKey("ench"))
                        ? nbtTagCompound.getTagList("ench", 10)
                        : new NBTTagList();

                for (int j = 0; j < nbttaglist.tagCount(); ++j)
                {
                    NBTTagCompound nbttagcompound = (NBTTagCompound) nbttaglist.getCompoundTagAt(j);
                    int k = nbttagcompound.getShort("id");
                    int l = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.enchantmentsList[k];

                    if (enchantment != null)
                    {
                        tooltip.add(enchantment.getTranslatedName(l));
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }

        if (hasDisplayName && nbtTagCompound != null)
        {
            NBTTagCompound displayTag = nbtTagCompound.getCompoundTag("display");

            if (displayTag.hasKey("color", 99))
            {
                if (advanced)
                {
                    tooltip.add(StatCollector.translateToLocalFormatted("block.color", String.format("#%06X", displayTag.getInteger("color"))));
                }
                else
                {
                    tooltip.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("block.dyed"));
                }
            }

            if (displayTag.hasKey("Lore", 9))
            {
                NBTTagList loreList = displayTag.getTagList("Lore", 9);

                if (loreList.tagCount() > 0)
                {
                    for (int l1 = 0; l1 < loreList.tagCount(); ++l1)
                    {
                        tooltip.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + loreList.getStringTagAt(l1));
                    }
                }
            }
        }

        if (advanced)
        {
            tooltip.add(EnumChatFormatting.DARK_GRAY + resolvedBlock.getUnlocalizedName());

            if (hasDisplayName && nbtTagCompound != null)
            {
                tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocalFormatted("block.nbt_tags", ((java.util.Set<?>) nbtTagCompound.func_150296_c()).size()));
            }
        }

        return tooltip;
    }

    private static int normalizeLiquidBlockId(int blockId)
    {
        if (blockId == Block.getIdFromBlock(Blocks.water))
        {
            return Block.getIdFromBlock(Blocks.water);
        }

        if (blockId == Block.getIdFromBlock(Blocks.lava))
        {
            return Block.getIdFromBlock(Blocks.lava);
        }

        return blockId;
    }

    public static void applyNbtToBlock(World world, int x, int y, int z, NBTTagCompound nbtTags)
    {
        if (world == null || nbtTags == null || nbtTags.hasNoTags())
        {
            return;
        }

        try
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity != null)
            {
                NBTTagCompound tileNbt = new NBTTagCompound();
                tileEntity.writeToNBT(tileNbt);

                for (Object obj : nbtTags.func_150296_c())
                {
                    String key = (String) obj;
                    NBTBase tag = nbtTags.getTag(key);
                    if (tag != null)
                    {
                        tileNbt.setTag(key, tag.copy());
                    }
                }

                tileEntity.readFromNBT(tileNbt);
                tileEntity.markDirty();

                world.markBlockForUpdate(x, y, z);

                OneBlockUltima.getLogger().info("[Generator] Applied NBT tags to TileEntity at ({},{},{})", x, y, z);
            }
            else
            {
                OneBlockUltima.getLogger().debug("[Generator] No TileEntity found at ({},{},{}) for NBT application", x, y, z);
            }
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("[Generator] Failed to apply NBT tags to block at ({},{},{})", x, y, z, e);
        }
    }

    private static void mergeNbtTags(NBTTagCompound target, NBTTagCompound source)
    {
        if (source == null || source.hasNoTags())
        {
            return;
        }

        for (Object obj : source.func_150296_c())
        {
            String key = (String) obj;
            NBTBase sourceTag = source.getTag(key);
            if (sourceTag == null)
            {
                continue;
            }

            if (target.hasKey(key))
            {
                NBTBase targetTag = target.getTag(key);
                if (targetTag instanceof NBTTagCompound && sourceTag instanceof NBTTagCompound)
                {
                    mergeNbtTags((NBTTagCompound) targetTag, (NBTTagCompound) sourceTag);
                    continue;
                }
            }

            target.setTag(key, sourceTag.copy());
        }
    }

    public static boolean canReplaceForGeneration(World world, int x, int y, int z)
    {
        int blockId = Block.getIdFromBlock(world.getBlock(x, y, z));
        Block block = Block.getBlockById(blockId);

        if (block == null || blockId == 0)
        {
            return true;
        }

        if (block.getMaterial().isReplaceable())
        {
            return true;
        }

        return GeneratedBlockRegistry.get(world).isGenerated(x, y, z);
    }

    public static int[] toBlockAndMeta(BlockSetConfig.BlockEntryDefinition entry)
    {
        Block block = entry.resolveBlock();
        if (block == null || Block.getIdFromBlock(block) == 0)
        {
            if (entry.registry != null && entry.registry.toLowerCase().contains("forestry"))
            {
                try
                {
                    String[] parsed = parseRegistryName(entry.registry);
                    Item item = GameRegistry.findItem(parsed[0], parsed[1]);
                    if (item instanceof ItemBlock)
                    {
                        Block forestryBlock = Block.getBlockFromItem(item);
                        if (forestryBlock != null && Block.getIdFromBlock(forestryBlock) != 0)
                        {
                            OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry block via ItemBlock: {}", forestryBlock.getUnlocalizedName());
                            block = forestryBlock;
                        }
                    }
                }
                catch (Exception ignored)
                {
                }
            }

            if (block == null || Block.getIdFromBlock(block) == 0)
            {
                block = resolveSpecialPlantBlock(entry.registry);
            }

            if (block == null || Block.getIdFromBlock(block) == 0)
            {
                OneBlockUltima.getLogger().warn("[BlockUtil] Could not resolve block for registry: {}", entry.registry);
                return null;
            }
        }

        try
        {
            int meta = entry.meta;

            if (entry.registry != null && entry.registry.toLowerCase().contains("forestry") &&
                    entry.registry.toLowerCase().contains("sapling"))
            {
                meta = 0;
                OneBlockUltima.getLogger().info("[BlockUtil] Using meta 0 for Forestry sapling");
            }

            if (Block.getIdFromBlock(block) == 0)
            {
                return null;
            }

            if (block instanceof BlockCrops)
            {
                meta = 0;
            }

            OneBlockUltima.getLogger().debug("[BlockUtil] Resolved block: {} -> {} with meta: {}", entry.registry, block.getUnlocalizedName(), meta);
            return new int[]{ Block.getIdFromBlock(block), meta };
        }
        catch (Exception ex)
        {
            OneBlockUltima.getLogger().debug("[BlockUtil] Exception resolving block {}, using air", entry.registry, ex);
            return null;
        }
    }

    private static Block resolveSpecialPlantBlock(String registry)
    {
        if (registry == null || registry.isEmpty())
        {
            return null;
        }

        String normalized = registry.toLowerCase(Locale.ROOT);

        if (normalized.equals("minecraft:carrot") || normalized.equals("carrot"))
        {
            return GameRegistry.findBlock("minecraft", "carrots");
        }
        if (normalized.equals("minecraft:potato") || normalized.equals("potato"))
        {
            return GameRegistry.findBlock("minecraft", "potatoes");
        }
        if (normalized.equals("minecraft:wheat_seeds") || normalized.equals("wheat_seeds") ||
                normalized.equals("minecraft:wheat") || normalized.equals("wheat"))
        {
            return GameRegistry.findBlock("minecraft", "wheat");
        }
        if (normalized.equals("minecraft:reeds") || normalized.equals("reeds") ||
                normalized.equals("minecraft:sugar_cane") || normalized.equals("sugar_cane"))
        {
            Block reeds = GameRegistry.findBlock("minecraft", "reeds");
            return reeds != null ? reeds : GameRegistry.findBlock("minecraft", "sugar_cane");
        }

        if (normalized.contains("forestry") && normalized.contains("sapling"))
        {
            OneBlockUltima.getLogger().info("[BlockUtil] Trying to resolve Forestry sapling: {}", registry);

            try
            {
                String[] parsed = parseRegistryName(registry);
                Item item = GameRegistry.findItem(parsed[0], parsed[1]);
                if (item instanceof ItemBlock)
                {
                    Block block = Block.getBlockFromItem(item);
                    if (block != null && Block.getIdFromBlock(block) != 0)
                    {
                        OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry sapling block: {}", block.getUnlocalizedName());
                        return block;
                    }
                }
            }
            catch (Exception ignored)
            {
            }

            try
            {
                Block b = GameRegistry.findBlock("forestry", "sapling");
                if (b != null && Block.getIdFromBlock(b) != 0)
                {
                    OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry sapling block via direct lookup: {}", b.getUnlocalizedName());
                    return b;
                }
            }
            catch (Exception ignored)
            {
            }
        }

        return null;
    }

    private static String[] parseRegistryName(String registry)
    {
        if (registry == null || registry.isEmpty())
        {
            return new String[]{ "minecraft", "" };
        }

        int colonIndex = registry.indexOf(':');
        if (colonIndex > 0)
        {
            return new String[]{ registry.substring(0, colonIndex), registry.substring(colonIndex + 1) };
        }

        return new String[]{ "minecraft", registry };
    }

    public static void applyNbtToEntity(Entity entity, @Nullable NBTTagCompound nbtTags)
    {
        if (entity == null || nbtTags == null || nbtTags.hasNoTags())
        {
            return;
        }

        try
        {
            NBTTagCompound entityNbt = new NBTTagCompound();
            entity.writeToNBT(entityNbt);

            mergeNbtTags(entityNbt, nbtTags);

            entity.readFromNBT(entityNbt);

            OneBlockUltima.getLogger().info("[Mob Spawn] Applied NBT tags to entity: {}", entity.getCommandSenderName());
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("[Mob Spawn] Failed to apply NBT tags to entity: {}", entity.getCommandSenderName(), e);
        }
    }
}
