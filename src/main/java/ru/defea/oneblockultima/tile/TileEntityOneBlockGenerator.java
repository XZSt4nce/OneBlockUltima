package ru.defea.oneblockultima.tile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.util.BlockUtil;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityOneBlockGenerator extends TileEntity
{
    private String selectedSetId;
    private UUID ownerId;

    public TileEntityOneBlockGenerator()
    {
    }

    public void tryGenerateBlock()
    {

        if (selectedSetId == null || selectedSetId.isEmpty())
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(selectedSetId);
        if (set == null)
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
            set = BlockSetConfig.get().getSet(selectedSetId);
            if (set == null)
            {
                OneBlockUltima.getLogger().info("[Generator] No set found even after trying default");
                return;
            }
        }

        int level = resolveGenerationLevel();
        OneBlockUltima.getLogger().info("[Generator] Resolved level: " + level + " for setId: " + selectedSetId);
        if (level <= 0)
        {
            OneBlockUltima.getLogger().info("[Generator] Level is " + level + ", resetting to default set");
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
            set = BlockSetConfig.get().getSet(selectedSetId);
            if (set == null)
            {
                return;
            }
            level = resolveGenerationLevel();
        }

        BlockSetConfig.SetLevelDefinition levelDefinition = set.getLevel(level);
        if (levelDefinition == null)
        {
            OneBlockUltima.getLogger().info("[Generator] Level definition is null for level=" + level);
            return;
        }

        BlockSetConfig.BlockEntryDefinition entry = levelDefinition.pickRandom(world.rand);
        if (entry == null)
        {
            OneBlockUltima.getLogger().info("[Generator] pickRandom returned null");
            return;
        }

        OneBlockUltima.getLogger().info("[Generator] Trying to generate entry registry=" + entry.registry + " meta=" + entry.meta + " chance=" + entry.getChance());
        IBlockState state = BlockUtil.toState(entry);
        OneBlockUltima.getLogger().info("[Generator] BlockUtil.toState returned " + (state == null ? "null" : state.getBlock().getRegistryName()));
        if (state == null)
        {
            OneBlockUltima.getLogger().info("[Generator] Attempting item->block fallback for registry=" + entry.registry);
        }
            if (state == null)
            {
                // Try to resolve entry as an item that corresponds to a placeable block (carrots, wheat, reeds etc.)
                try
                {
                    Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation(entry.registry));
                    OneBlockUltima.getLogger().info("[Generator] Fallback item lookup for registry=" + entry.registry + " -> item=" + (item == null ? "null" : item.getRegistryName()));
                    Block resolvedBlock = null;
                    if (item instanceof ItemBlock)
                    {
                        resolvedBlock = ((ItemBlock) item).getBlock();
                    }
                    else if (item != null)
                    {
                        try {
                            resolvedBlock = ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation(entry.registry));
                        } catch (Exception ignored) { }

                        if (resolvedBlock == null)
                        {
                            try { resolvedBlock = ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation(entry.registry + "s")); } catch (Exception ignored) { }
                        }
                    }

                    if (resolvedBlock != null)
                    {
                        try { state = resolvedBlock.getStateFromMeta(entry.meta); } catch (Exception ex) { state = resolvedBlock.getDefaultState(); }
                        OneBlockUltima.getLogger().info("[Generator] Fallback resolved block=" + (state == null ? "null" : state.getBlock().getRegistryName()));
                    }
                }
                catch (Exception ex)
                {
                    OneBlockUltima.getLogger().error("[Generator] Exception during item->block fallback for " + entry.registry, ex);
                }

                if (state == null)
                {
                    OneBlockUltima.getLogger().info("[Generator] Could not resolve placeable block for registry=" + entry.registry);
                    return;
                }
            }

        BlockPos targetPos = pos.up();

        // Вместо спавна предмета, пытаемся разместить блок
        if (state == null || state.getBlock() == Blocks.AIR)
        {
            OneBlockUltima.getLogger().info("[Generator] State is null or AIR for registry=" + entry.registry + ", trying to place as block anyway");

            // Пытаемся найти блок через различные способы
            Block resolvedBlock = null;

            // 1. Пробуем через ItemBlock
            try {
                Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation(entry.registry));
                if (item instanceof ItemBlock) {
                    resolvedBlock = ((ItemBlock) item).getBlock();
                    OneBlockUltima.getLogger().info("[Generator] Found block via ItemBlock: " + (resolvedBlock == null ? "null" : resolvedBlock.getRegistryName()));
                }
            } catch (Exception ex) {
                OneBlockUltima.getLogger().error("[Generator] Error getting ItemBlock", ex);
            }

            // 2. Если не нашли, пробуем через BlockUtil (с обновленной обработкой Forestry)
            if (resolvedBlock == null || resolvedBlock == Blocks.AIR) {
                Block tempBlock = BlockUtil.toState(entry) != null ? BlockUtil.toState(entry).getBlock() : null;
                if (tempBlock != null && tempBlock != Blocks.AIR) {
                    resolvedBlock = tempBlock;
                    OneBlockUltima.getLogger().info("[Generator] Found block via BlockUtil: " + resolvedBlock.getRegistryName());
                }
            }

            // 3. Если всё ещё не нашли, пробуем прямой поиск по registry
            if (resolvedBlock == null || resolvedBlock == Blocks.AIR) {
                try {
                    resolvedBlock = ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation(entry.registry));
                    OneBlockUltima.getLogger().info("[Generator] Found block via direct registry lookup: " + (resolvedBlock == null ? "null" : resolvedBlock.getRegistryName()));
                } catch (Exception ex) {
                    OneBlockUltima.getLogger().error("[Generator] Error in direct registry lookup", ex);
                }
            }

            // Если нашли блок - размещаем его
            if (resolvedBlock != null && resolvedBlock != Blocks.AIR) {
                try {
                    // Получаем состояние блока
                    IBlockState newState;
                    try {
                        newState = resolvedBlock.getStateFromMeta(entry.meta);
                        if (newState == null || newState.getBlock() == Blocks.AIR) {
                            newState = resolvedBlock.getDefaultState();
                        }
                    } catch (Exception ex) {
                        newState = resolvedBlock.getDefaultState();
                    }

                    if (newState != null && newState.getBlock() != Blocks.AIR) {
                        OneBlockUltima.getLogger().info("[Generator] Placing block: " + newState.getBlock().getRegistryName() + " at " + targetPos);

                        // Размещаем блок с NBT
                        BlockUtil.placeBlockWithNBT(world, targetPos, newState, entry.nbtTags);

                        // Отмечаем как сгенерированное
                        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
                        registry.markGenerated(targetPos, pos, selectedSetId, entry.currency, level, entry.registry, entry.meta);

                        return; // Успешно разместили блок
                    }
                } catch (Exception ex) {
                    OneBlockUltima.getLogger().error("[Generator] Failed to place block", ex);
                }
            }

            // Если ничего не сработало - спавним как предмет (fallback)
            OneBlockUltima.getLogger().warn("[Generator] Could not place as block, spawning as item fallback for " + entry.registry);
            try {
                Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation(entry.registry));
                if (item != null) {
                    net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(item, 1);
                    if (entry.nbtTags != null && !entry.nbtTags.hasNoTags()) {
                        itemStack.setTagCompound(entry.nbtTags.copy());
                    }

                    net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(
                            world, targetPos.getX(), targetPos.getY(), targetPos.getZ(), itemStack
                    );
                    world.spawnEntity(entityItem);

                    GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
                    registry.markGenerated(targetPos, pos, selectedSetId, entry.currency, level, entry.registry, entry.meta);
                }
            } catch (Exception ex) {
                OneBlockUltima.getLogger().error("[Generator] Failed to spawn item fallback", ex);
            }

            OneBlockUltima.getLogger().info("[Generator] Retrying generation after item spawn");
            tryGenerateBlock();

            return;
        }

        OneBlockUltima.getLogger().info("[Generator] Target position=" + targetPos + ", current block=" + world.getBlockState(targetPos).getBlock().getRegistryName());
        if (!BlockUtil.canReplaceForGeneration(world, targetPos))
        {
            OneBlockUltima.getLogger().info("[Generator] Cannot replace target position=" + targetPos);
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
        if (registry.isGenerated(targetPos))
        {
            registry.remove(targetPos);
        }

        OneBlockUltima.getLogger().info("[Generator] Placing state=" + state.getBlock().getRegistryName() + " at " + targetPos);
        if (entry.nbtTags != null && !entry.nbtTags.hasNoTags())
        {
            OneBlockUltima.getLogger().info("[Generator] Placing block with NBT tags at " + targetPos + ": " + entry.nbtTags);
        }
        // Размещаем блок и применяем NBT теги одновременно
        BlockUtil.placeBlockWithNBT(world, targetPos, state, entry.nbtTags);
        OneBlockUltima.getLogger().info("[Generator] After place block at " + targetPos + ", now=" + world.getBlockState(targetPos).getBlock().getRegistryName());
        registry.markGenerated(targetPos, pos, selectedSetId, entry.currency, level, entry.registry, entry.meta);
    }

    private int resolveGenerationLevel()
    {
        if (ownerId == null)
        {
            return 1;
        }

        net.minecraft.entity.player.EntityPlayer owner = world.getPlayerEntityByUUID(ownerId);
        if (owner == null)
        {
            return 1;
        }

        ru.defea.oneblockultima.capability.IOneBlockPlayerData data =
                ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.get(owner);
        if (data == null)
        {
            return 1;
        }

        return data.getSetLevel(selectedSetId);
    }

    public String getSelectedSetId()
    {
        if (selectedSetId == null || selectedSetId.isEmpty())
        {
            selectedSetId = BlockSetConfig.get().getDefaultSetId();
        }
        return selectedSetId;
    }

    public void setSelectedSetId(String selectedSetId)
    {
        System.out.println("[DEBUG] TileEntity setSelectedSetId: " + selectedSetId + " at pos " + pos);
        this.selectedSetId = selectedSetId;
        markDirty();

        if (world != null && !world.isRemote)
        {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            System.out.println("[DEBUG] TileEntity notifyBlockUpdate sent");
        }
    }

    public UUID getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId)
    {
        this.ownerId = ownerId;
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("selectedSetId", getSelectedSetId());
        if (ownerId != null)
        {
            compound.setUniqueId("ownerId", ownerId);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        selectedSetId = compound.getString("selectedSetId");
        if (compound.hasUniqueId("ownerId"))
        {
            ownerId = compound.getUniqueId("ownerId");
        }
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        readFromNBT(tag);
    }
}