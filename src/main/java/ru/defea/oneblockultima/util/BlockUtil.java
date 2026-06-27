package ru.defea.oneblockultima.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import javax.annotation.Nullable;
import java.util.Locale;

public final class BlockUtil
{
    private BlockUtil()
    {
    }

    /**
     * Размещает блок с применением NBT тегов одновременно (атомарно)
     * Теги применяются ДО размещения блока для BlockContainer блоков
     */
    public static void placeBlockWithNBT(World world, BlockPos pos, IBlockState state, @javax.annotation.Nullable NBTTagCompound nbtTags)
    {
        if (world == null || pos == null || state == null)
        {
            return;
        }

        // Обработка жидкостей
        if (state.getMaterial().isLiquid())
        {
            state = normalizeLiquidState(state);
        }

        Block block = state.getBlock();
        
        // Для BlockContainer блоков с NBT тегами - создаем TileEntity ДО размещения
        if (nbtTags != null && !nbtTags.hasNoTags() && block instanceof net.minecraft.block.BlockContainer)
        {
            try
            {
                // Создаем TileEntity с полными NBT данными ДО размещения блока
                TileEntity tileEntity = ((net.minecraft.block.BlockContainer) block).createNewTileEntity(world, block.getMetaFromState(state));
                
                if (tileEntity != null)
                {
                    // Подготавливаем полный NBT для TileEntity с позицией
                    NBTTagCompound tileNbt = new NBTTagCompound();
                    tileNbt.setInteger("x", pos.getX());
                    tileNbt.setInteger("y", pos.getY());
                    tileNbt.setInteger("z", pos.getZ());
                    
                    // Применяем все теги из конфига (перезаписываем существующие)
                    for (String key : nbtTags.getKeySet())
                    {
                        NBTBase tag = nbtTags.getTag(key);
                        tileNbt.setTag(key, tag.copy());
                    }
                    
                    // Загружаем подготовленные данные в TileEntity
                    tileEntity.readFromNBT(tileNbt);
                    
                    // Добавляем TileEntity в мир ДО размещения блока
                    world.setTileEntity(pos, tileEntity);
                    
                    OneBlockUltima.getLogger().info("[Generator] Pre-configured TileEntity at " + pos + " with NBT tags");
                }
                else
                {
                    OneBlockUltima.getLogger().warn("[Generator] createNewTileEntity returned null for block " + block.getRegistryName());
                }
            }
            catch (Exception e)
            {
                OneBlockUltima.getLogger().error("[Generator] Failed to pre-configure TileEntity for block at " + pos, e);
            }
        }
        
        // Размещаем блок
        world.setBlockState(pos, state, 3);

        // Если есть NBT теги но блок не BlockContainer, пытаемся применить их после размещения
        if (nbtTags != null && !nbtTags.hasNoTags() && !(block instanceof net.minecraft.block.BlockContainer))
        {
            applyNbtToBlock(world, pos, nbtTags);
        }
    }

    /**
     * Нормализирует жидкости (вода и лава) в их неподвижные состояния
     */
    private static IBlockState normalizeLiquidState(IBlockState state)
    {
        if (!state.getMaterial().isLiquid())
        {
            return state;
        }

        Block block = state.getBlock();
        if (block == Blocks.FLOWING_WATER)
        {
            return Blocks.WATER.getDefaultState();
        }

        if (block == Blocks.FLOWING_LAVA)
        {
            return Blocks.LAVA.getDefaultState();
        }

        if (block instanceof IFluidBlock)
        {
            Fluid fluid = ((IFluidBlock) block).getFluid();
            if (fluid != null)
            {
                Block stillBlock = fluid.getBlock();
                if (stillBlock != null && stillBlock != block)
                {
                    return stillBlock.getDefaultState();
                }
            }
        }

        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if (fluid != null)
        {
            Block stillBlock = fluid.getBlock();
            if (stillBlock != null && stillBlock != block)
            {
                return stillBlock.getDefaultState();
            }
        }

        return state;
    }

    /**
     * Универсальное применение NBT тегов к блоку на указанной позиции
     */
    public static void applyNbtToBlock(World world, BlockPos pos, NBTTagCompound nbtTags)
    {
        if (world == null || pos == null || nbtTags == null || nbtTags.hasNoTags())
        {
            return;
        }

        try
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null)
            {
                // Читаем текущее состояние TileEntity
                NBTTagCompound tileNbt = new NBTTagCompound();
                tileEntity.writeToNBT(tileNbt);

                // Добавляем все теги из nbtTags в tileNbt (перезаписываем если уже есть)
                for (String key : nbtTags.getKeySet())
                {
                    NBTBase tag = nbtTags.getTag(key);
                    // noinspection ConstantConditions
                    if (tag != null)
                    {
                        tileNbt.setTag(key, tag.copy());
                    }
                }

                // Применяем обновленные теги
                tileEntity.readFromNBT(tileNbt);
                tileEntity.markDirty();

                // Обновляем блок
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);

                OneBlockUltima.getLogger().info("[Generator] Applied NBT tags to TileEntity at " + pos + ": " + nbtTags);
            }
            else
            {
                OneBlockUltima.getLogger().debug("[Generator] No TileEntity found at " + pos + " for NBT application");
            }
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("[Generator] Failed to apply NBT tags to block at " + pos, e);
        }
    }

    /**
     * Рекурсивное объединение NBT тегов
     */
    private static void mergeNbtTags(NBTTagCompound target, NBTTagCompound source)
    {
        if (source == null || source.hasNoTags())
        {
            return;
        }

        for (String key : source.getKeySet())
        {
            NBTBase sourceTag = source.getTag(key);
            // noinspection ConstantConditions
            if (sourceTag == null)
            {
                continue;
            }

            // Если в целевом объекте уже есть такой ключ и оба - CompoundTag, объединяем рекурсивно
            if (target.hasKey(key))
            {
                NBTBase targetTag = target.getTag(key);
                if (targetTag instanceof NBTTagCompound && sourceTag instanceof NBTTagCompound)
                {
                    mergeNbtTags((NBTTagCompound) targetTag, (NBTTagCompound) sourceTag);
                    continue;
                }
            }

            // В остальных случаях просто копируем (заменяем)
            target.setTag(key, sourceTag.copy());
        }
    }

    public static boolean canReplaceForGeneration(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getMaterial().isReplaceable())
        {
            return true;
        }

        return GeneratedBlockRegistry.get(world).isGenerated(pos);
    }

    public static IBlockState toState(BlockSetConfig.BlockEntryDefinition entry)
    {
        Block block = entry.resolveBlock();
        if (block == null || block == Blocks.AIR)
        {
            // Специальная обработка для Forestry
            if (entry.registry != null && entry.registry.toLowerCase().contains("forestry")) {
                // Пробуем найти блок через ItemBlock
                try {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.registry));
                    if (item instanceof ItemBlock) {
                        Block forestryBlock = ((ItemBlock) item).getBlock();
                        // noinspection ConstantConditions
                        if (forestryBlock != null && forestryBlock != Blocks.AIR) {
                            OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry block via ItemBlock: " + forestryBlock.getRegistryName());
                            block = forestryBlock;
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (block == null || block == Blocks.AIR) {
                block = resolveSpecialPlantBlock(entry.registry);
            }

            if (block == null || block == Blocks.AIR)
            {
                OneBlockUltima.getLogger().warn("[BlockUtil] Could not resolve block for registry: " + entry.registry);
                return null;
            }
        }

        try
        {
            IBlockState state = null;

            // Для Forestry саженцев всегда используем default state (meta игнорируется)
            if (entry.registry != null && entry.registry.toLowerCase().contains("forestry") &&
                    entry.registry.toLowerCase().contains("sapling"))
            {
                state = block.getDefaultState();
                OneBlockUltima.getLogger().info("[BlockUtil] Using default state for Forestry sapling: " + state);
            }
            else
            {
                state = block.getStateFromMeta(entry.meta);
            }

            if (state == null || state.getBlock() == Blocks.AIR)
            {
                state = block.getDefaultState();
            }
            if (state == null || state.getBlock() == Blocks.AIR)
            {
                return null;
            }

            if (block instanceof BlockCrops && state.getBlock() == block)
            {
                return block.getDefaultState();
            }

            OneBlockUltima.getLogger().debug("[BlockUtil] Resolved block: " + entry.registry + " -> " + block.getRegistryName() + " with meta: " + entry.meta);
            return state;
        }
        catch (Exception ex)
        {
            OneBlockUltima.getLogger().debug("[BlockUtil] Exception getting state from meta for " + entry.registry + ", using default state", ex);
            IBlockState defaultState = block.getDefaultState();
            return defaultState == null || defaultState.getBlock() == Blocks.AIR ? null : defaultState;
        }
    }

    private static Block resolveSpecialPlantBlock(String registry)
    {
        if (registry == null || registry.isEmpty())
        {
            return null;
        }

        String normalized = registry.toLowerCase(Locale.ROOT);
        if ("minecraft:carrot".equals(normalized) || "carrot".equals(normalized))
        {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:carrots"));
        }
        if ("minecraft:potato".equals(normalized) || "potato".equals(normalized))
        {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:potatoes"));
        }
        if ("minecraft:wheat_seeds".equals(normalized) || "wheat_seeds".equals(normalized) || "minecraft:wheat".equals(normalized) || "wheat".equals(normalized))
        {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:wheat"));
        }
        if ("minecraft:beetroot_seeds".equals(normalized) || "beetroot_seeds".equals(normalized) || "minecraft:beetroot".equals(normalized) || "beetroot".equals(normalized))
        {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:beetroots"));
        }
        if ("minecraft:reeds".equals(normalized) || "reeds".equals(normalized) || "minecraft:sugar_cane".equals(normalized) || "sugar_cane".equals(normalized))
        {
            Block reeds = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:reeds"));
            if (reeds != null)
            {
                return reeds;
            }
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft:sugar_cane"));
        }

        if (normalized.contains("forestry") && normalized.contains("sapling"))
        {
            OneBlockUltima.getLogger().info("[BlockUtil] Trying to resolve Forestry sapling: " + registry);

            // Самый надежный способ - через ItemBlock
            try {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registry));
                if (item instanceof ItemBlock) {
                    Block block = ((ItemBlock) item).getBlock();
                    if (block != null && block != Blocks.AIR) {
                        OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry sapling block via ItemBlock: " + block.getRegistryName());
                        return block;
                    }
                }
            } catch (Exception ignored) {}

            // Если не получилось через ItemBlock, пробуем прямой поиск
            try {
                Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("forestry:sapling"));
                if (b != null && b != Blocks.AIR) {
                    OneBlockUltima.getLogger().info("[BlockUtil] Found Forestry sapling block via direct lookup: " + b.getRegistryName());
                    return b;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Применяет NBT теги к сущности (мобу)
     * Используется при спауне мобов для применения кастомных свойств
     */
    public static void applyNbtToEntity(net.minecraft.entity.Entity entity, @Nullable NBTTagCompound nbtTags)
    {
        if (entity == null || nbtTags == null || nbtTags.hasNoTags())
        {
            return;
        }

        try
        {
            // Получаем текущие NBT теги сущности
            NBTTagCompound entityNbt = new NBTTagCompound();
            entity.writeToNBT(entityNbt);

            // Рекурсивно объединяем теги
            mergeNbtTags(entityNbt, nbtTags);

            // Применяем обновленные теги
            entity.readFromNBT(entityNbt);

            OneBlockUltima.getLogger().info("[Mob Spawn] Applied NBT tags to entity: " + entity.getName());
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("[Mob Spawn] Failed to apply NBT tags to entity: " + entity.getName(), e);
        }
    }
}
