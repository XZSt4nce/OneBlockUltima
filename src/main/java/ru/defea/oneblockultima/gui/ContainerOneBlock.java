package ru.defea.oneblockultima.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.network.ModMessages;
import ru.defea.oneblockultima.network.PacketOneBlockAction;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

public class ContainerOneBlock extends Container
{
    private final World world;
    private final BlockPos generatorPos;
    private final EntityPlayer player;

    public ContainerOneBlock(InventoryPlayer inventory, World world, BlockPos generatorPos)
    {
        this.world = world;
        this.generatorPos = generatorPos;
        this.player = inventory.player;

        if (!world.isRemote && player instanceof EntityPlayerMP)
        {
            PacketSyncPlayerData.sendToPlayer(player);

            TileEntityOneBlockGenerator generator = getGenerator();
            if (generator != null)
            {
                net.minecraft.network.play.server.SPacketUpdateTileEntity packet = generator.getUpdatePacket();
                if (packet != null)
                {
                    ((EntityPlayerMP) player).connection.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return playerIn.getDistanceSq(generatorPos.getX() + 0.5D, generatorPos.getY() + 0.5D, generatorPos.getZ() + 0.5D) <= 64.0D;
    }

    public boolean selectSet(String setId)
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorPos, PacketOneBlockAction.Action.SELECT_SET, setId));
            return true; // Возвращаем true, чтобы клиент не показывал ошибку
        }

        return applySelectSet(setId);
    }

    public boolean upgradeSet(String setId)
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorPos, PacketOneBlockAction.Action.UPGRADE_SET, setId));
            return true; // Возвращаем true, чтобы клиент не показывал ошибку
        }

        return applyUpgradeSet(setId);
    }

    public boolean applySelectSet(String setId)
    {
        System.out.println("[OneBlock] applySelectSet called with setId: " + setId);

        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            System.out.println("[OneBlock] Generator is NULL!");
            return false;
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(setId);
        if (set == null)
        {
            System.out.println("[OneBlock] Set is NULL in config!");
            return false;
        }

        ru.defea.oneblockultima.capability.IOneBlockPlayerData data =
                ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            System.out.println("[OneBlock] Player data is NULL!");
            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).sendMessage(new TextComponentString("§cFailed to get player data!"));
            }
            return false;
        }

        int currentLevel = data.getSetLevel(setId);
        if (currentLevel <= 0)
        {
            System.out.println("[OneBlock] Set not unlocked! Level: " + currentLevel);
            return false;
        }

        // Проверяем, не выбран ли уже этот набор
        String currentSelected = generator.getSelectedSetId();
        if (setId.equals(currentSelected))
        {
            System.out.println("[OneBlock] Set already selected: " + setId);
            return false;
        }

        System.out.println("[OneBlock] Setting selectedSetId on generator...");
        generator.setSelectedSetId(setId);
        generator.setOwnerId(player.getUniqueID());

        System.out.println("[OneBlock] Generator selectedSetId is now: " + generator.getSelectedSetId());

        world.notifyBlockUpdate(generatorPos, world.getBlockState(generatorPos), world.getBlockState(generatorPos), 3);
        detectAndSendChanges();

        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;

            // Отправляем обновление тайла
            net.minecraft.network.play.server.SPacketUpdateTileEntity packet = generator.getUpdatePacket();
            if (packet != null)
            {
                playerMP.connection.sendPacket(packet);
            }

            // Синхронизируем данные игрока
            PacketSyncPlayerData.sendToPlayer(player);
        }
        return true;
    }

    public boolean applyUpgradeSet(String setId)
    {
        System.out.println("[OneBlock] applyUpgradeSet called with setId: " + setId);

        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            System.out.println("[OneBlock] Generator is NULL!");
            return false;
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(setId);
        if (set == null)
        {
            System.out.println("[OneBlock] Set is NULL in config!");
            return false;
        }

        ru.defea.oneblockultima.capability.IOneBlockPlayerData data =
                ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            System.out.println("[OneBlock] Player data is NULL!");
            if (player instanceof EntityPlayerMP)
            return false;
        }

        int currentLevel = data.getSetLevel(setId);
        System.out.println("[OneBlock] Current level: " + currentLevel);

        // Проверяем, разблокирован ли набор
        if (currentLevel <= 0)
        {
            // Попытка разблокировать набор
            int cost = set.unlockCost;
            int currency = data.getCurrency();

            System.out.println("[OneBlock] Unlock attempt - Have: " + currency + ", Need: " + cost);

            if (currency < cost)
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).sendMessage(new TextComponentString("§c" + I18n.format("gui.oneblockultima.msg.need") + ": " + cost + ", " + I18n.format("gui.oneblockultima.msg.have") + ": " + currency));
                }
                return false;
            }

            // Разблокируем набор
            boolean success = data.upgradeSet(setId, cost, set.getMaxLevel());
            if (!success)
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).sendMessage(new TextComponentString("§c" + I18n.format("gui.oneblockultima.msg.unlock_fail")));
                }
                return false;
            }

            // После разблокировки автоматически выбираем набор
            generator.setSelectedSetId(setId);
            generator.setOwnerId(player.getUniqueID());

            System.out.println("[OneBlock] Set unlocked and selected: " + setId);
            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).sendMessage(new TextComponentString("§a" + I18n.format("gui.oneblockultima.msg.unlocked")));
            }
        }
        else
        {
            // Попытка улучшить набор
            BlockSetConfig.SetLevelDefinition nextLevel = set.getLevel(currentLevel + 1);
            if (nextLevel == null)
            {
                System.out.println("[OneBlock] Max level reached!");
                return false;
            }

            int cost = nextLevel.upgradeCost;
            int currency = data.getCurrency();

            System.out.println("[OneBlock] Upgrade attempt - Have: " + currency + ", Need: " + cost);

            if (currency < cost)
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).sendMessage(new TextComponentString("§c" + I18n.format("gui.oneblockultima.msg.need") + ": " + cost + ", " + I18n.format("gui.oneblockultima.msg.have") + ": " + currency));
                }
                return false;
            }

            // Улучшаем набор
            boolean success = data.upgradeSet(setId, cost, set.getMaxLevel());
            if (!success)
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).sendMessage(new TextComponentString("§c" + I18n.format("gui.oneblockultima.msg.upgrade_fail")));
                }
                return false;
            }

            // После улучшения автоматически выбираем набор
            generator.setSelectedSetId(setId);
            generator.setOwnerId(player.getUniqueID());

            System.out.println("[OneBlock] Set upgraded to level " + (currentLevel + 1) + " and selected: " + setId);
        }

        // Обновляем мир
        world.notifyBlockUpdate(generatorPos, world.getBlockState(generatorPos), world.getBlockState(generatorPos), 3);
        detectAndSendChanges();

        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;

            // Отправляем обновление тайла
            net.minecraft.network.play.server.SPacketUpdateTileEntity packet = generator.getUpdatePacket();
            if (packet != null)
            {
                playerMP.connection.sendPacket(packet);
            }

            // Синхронизируем данные игрока
            PacketSyncPlayerData.sendToPlayer(player);
        }
        return true;
    }

    public TileEntityOneBlockGenerator getGenerator()
    {
        if (world.getTileEntity(generatorPos) instanceof TileEntityOneBlockGenerator)
        {
            return (TileEntityOneBlockGenerator) world.getTileEntity(generatorPos);
        }
        return null;
    }

    public BlockPos getGeneratorPos()
    {
        return generatorPos;
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }
}