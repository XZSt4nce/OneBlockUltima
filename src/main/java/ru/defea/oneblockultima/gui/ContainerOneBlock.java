package ru.defea.oneblockultima.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.event.ModEvents;
import ru.defea.oneblockultima.network.ModMessages;
import ru.defea.oneblockultima.network.PacketOneBlockAction;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

public class ContainerOneBlock extends Container
{
    private final World world;
    private final int generatorX;
    private final int generatorY;
    private final int generatorZ;
    private final EntityPlayer player;

    public ContainerOneBlock(EntityPlayer player, World world, int generatorX, int generatorY, int generatorZ)
    {
        this.world = world;
        this.generatorX = generatorX;
        this.generatorY = generatorY;
        this.generatorZ = generatorZ;
        this.player = player;

        if (!world.isRemote && player instanceof EntityPlayerMP)
        {
            TileEntityOneBlockGenerator generator = getGenerator();
            if (generator != null)
            {
                ModEvents.ensureGeneratorAccess(world, generatorX, generatorY, generatorZ, player, generator);
            }

            PacketSyncPlayerData.sendToPlayer(player);

            if (generator != null)
            {
                net.minecraft.network.play.server.S35PacketUpdateTileEntity packet = (net.minecraft.network.play.server.S35PacketUpdateTileEntity)generator.getDescriptionPacket();
                if (packet != null)
                {
                    ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return playerIn.getDistanceSq(generatorX + 0.5D, generatorY + 0.5D, generatorZ + 0.5D) <= 64.0D;
    }

    public void selectSet(String setId)
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.SELECT_SET, setId));
            return;
        }

        applySelectSet(setId);
    }

    public void upgradeSet(String setId)
    {
        if (world.isRemote)
        {
            applyLocalBalancePreview(setId);
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.UPGRADE_SET, setId));
            return;
        }

        applyUpgradeSet(setId);
    }

    private void applyLocalBalancePreview(String setId)
    {
        if (!world.isRemote)
        {
            return;
        }

        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            return;
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(setId);
        if (set == null)
        {
            return;
        }

        int currentLevel = generator.getSetLevel(setId);
        int cost = currentLevel <= 0 ? set.unlockCost : set.getLevel(currentLevel + 1) != null ? set.getLevel(currentLevel + 1).upgradeCost : 0;
        if (cost <= 0)
        {
            return;
        }

        ru.defea.oneblockultima.capability.IOneBlockPlayerData data = ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.get(player);
        if (data == null || data.getCurrency() < cost)
        {
            return;
        }

        data.spendCurrency(cost);
        ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.saveToEntity(player, data);
        ModEvents.syncDisplayedCurrency(player, data.getCurrency());
    }

    public void toggleFluidGeneration()
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.TOGGLE_FLUIDS, ""));
            return;
        }

        applyToggleFluidGeneration();
    }

    public void toggleMobGeneration()
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.TOGGLE_MOBS, ""));
            return;
        }

        applyToggleMobGeneration();
    }

    public void toggleChestGeneration()
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.TOGGLE_CHESTS, ""));
            return;
        }

        applyToggleChestGeneration();
    }

    public void toggleSaplingGeneration()
    {
        if (world.isRemote)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(generatorX, generatorY, generatorZ, PacketOneBlockAction.Action.TOGGLE_SAPLINGS, ""));
            return;
        }

        applyToggleSaplingGeneration();
    }

    public void applySelectSet(String setId)
    {
        System.out.println("[OneBlock] applySelectSet called with setId: " + setId);

        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            System.out.println("[OneBlock] Generator is NULL!");
            return;
        }

        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(setId);
        if (set == null)
        {
            System.out.println("[OneBlock] Set is NULL in config!");
            return;
        }

        int currentLevel = generator.getSetLevel(setId);
        if (currentLevel <= 0)
        {
            System.out.println("[OneBlock] Set not unlocked! Level: " + currentLevel);
            return;
        }

        String currentSelected = generator.getSelectedSetId();
        if (setId.equals(currentSelected))
        {
            System.out.println("[OneBlock] Set already selected: " + setId);
            return;
        }

        System.out.println("[OneBlock] Setting selectedSetId on generator...");
        generator.setSelectedSetId(setId);
        if (!generator.ensureOwnership(player.getUniqueID()))
        {
            generator.setSelectedSetId(currentSelected);
            return;
        }

        System.out.println("[OneBlock] Generator selectedSetId is now: " + generator.getSelectedSetId());

        if (world.isAirBlock(generator.xCoord, generator.yCoord + 1, generator.zCoord))
        {
            generator.tryGenerateBlock();
        }
        detectAndSendChanges();

        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;

            net.minecraft.network.play.server.S35PacketUpdateTileEntity packet = (net.minecraft.network.play.server.S35PacketUpdateTileEntity)generator.getDescriptionPacket();
            if (packet != null)
            {
                playerMP.playerNetServerHandler.sendPacket(packet);
            }

            PacketSyncPlayerData.sendToPlayer(player);
        }
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

        String currentSelected = generator.getSelectedSetId();

        ru.defea.oneblockultima.capability.IOneBlockPlayerData data =
                ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            System.out.println("[OneBlock] Player data is NULL!");
            if (player instanceof EntityPlayerMP)
                return false;
        }

        int currentLevel = generator.getSetLevel(setId);
        System.out.println("[OneBlock] Current level: " + currentLevel);

        if (currentLevel <= 0)
        {
            if (!set.hasUnlockRequirementsMet(data, generator))
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.unlock_requirements")));
                }
                return false;
            }

            int cost = set.unlockCost;
            int currency = data.getCurrency();

            System.out.println("[OneBlock] Unlock attempt - Have: " + currency + ", Need: " + cost);

            if (currency < cost)
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.need") + ": " + cost + ", " + StatCollector.translateToLocal("gui.oneblockultima.msg.have") + ": " + currency));
                }
                return false;
            }

            if (!data.spendCurrency(cost))
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.unlock_fail")));
                }
                return false;
            }

            OneBlockPlayerDataProvider.saveToEntity(player, data);

            boolean success = generator.upgradeSet(setId, cost, set.getMaxLevel());
            if (!success)
            {
                data.addCurrency(cost);
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.unlock_fail")));
                }
                return false;
            }

            generator.setSelectedSetId(setId);
            if (!generator.ensureOwnership(player.getUniqueID()))
            {
                generator.setSelectedSetId(currentSelected);
                return false;
            }

            System.out.println("[OneBlock] Set unlocked and selected: " + setId);
            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7a" + StatCollector.translateToLocal("gui.oneblockultima.msg.unlocked")));
            }
        }
        else
        {
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
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.need") + ": " + cost + ", " + StatCollector.translateToLocal("gui.oneblockultima.msg.have") + ": " + currency));
                }
                return false;
            }

            if (!data.spendCurrency(cost))
            {
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.upgrade_fail")));
                }
                return false;
            }

            OneBlockPlayerDataProvider.saveToEntity(player, data);

            boolean success = generator.upgradeSet(setId, cost, set.getMaxLevel());
            if (!success)
            {
                data.addCurrency(cost);
                if (player instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) player).addChatMessage(new ChatComponentText("\u00a7c" + StatCollector.translateToLocal("gui.oneblockultima.msg.upgrade_fail")));
                }
                return false;
            }

            generator.setSelectedSetId(setId);
            if (!generator.ensureOwnership(player.getUniqueID()))
            {
                generator.setSelectedSetId(currentSelected);
                return false;
            }

            System.out.println("[OneBlock] Set upgraded to level " + (currentLevel + 1) + " and selected: " + setId);
        }

        detectAndSendChanges();

        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;

            ModEvents.ensureGeneratorAccess(world, generatorX, generatorY, generatorZ, player, generator);

            net.minecraft.network.play.server.S35PacketUpdateTileEntity packet = (net.minecraft.network.play.server.S35PacketUpdateTileEntity)generator.getDescriptionPacket();
            if (packet != null)
            {
                playerMP.playerNetServerHandler.sendPacket(packet);
            }

            PacketSyncPlayerData.sendToPlayer(player);
            OneBlockPlayerDataProvider.saveToEntity(player, data);
        }
        return true;
    }

    public void applyToggleFluidGeneration()
    {
        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            return;
        }

        generator.setDisableFluidGeneration(!generator.isDisableFluidGeneration());
        if (world.isAirBlock(generator.xCoord, generator.yCoord + 1, generator.zCoord))
        {
            generator.tryGenerateBlock();
        }
        updateTileEntity(generator);
    }

    public void applyToggleMobGeneration()
    {
        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            return;
        }

        generator.setDisableMobGeneration(!generator.isDisableMobGeneration());
        if (world.isAirBlock(generator.xCoord, generator.yCoord + 1, generator.zCoord))
        {
            generator.tryGenerateBlock();
        }
        updateTileEntity(generator);
    }

    public void applyToggleChestGeneration()
    {
        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            return;
        }

        generator.setDisableChestGeneration(!generator.isDisableChestGeneration());
        if (world.isAirBlock(generator.xCoord, generator.yCoord + 1, generator.zCoord))
        {
            generator.tryGenerateBlock();
        }
        updateTileEntity(generator);
    }

    public void applyToggleSaplingGeneration()
    {
        TileEntityOneBlockGenerator generator = getGenerator();
        if (generator == null)
        {
            return;
        }

        generator.setDisableSaplingGeneration(!generator.isDisableSaplingGeneration());
        if (world.isAirBlock(generator.xCoord, generator.yCoord + 1, generator.zCoord))
        {
            generator.tryGenerateBlock();
        }
        updateTileEntity(generator);
    }

    private void updateTileEntity(TileEntityOneBlockGenerator generator) {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            net.minecraft.network.play.server.S35PacketUpdateTileEntity packet = (net.minecraft.network.play.server.S35PacketUpdateTileEntity)generator.getDescriptionPacket();
            if (packet != null)
            {
                playerMP.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public TileEntityOneBlockGenerator getGenerator()
    {
        if (world.getTileEntity(generatorX, generatorY, generatorZ) instanceof TileEntityOneBlockGenerator)
        {
            return (TileEntityOneBlockGenerator) world.getTileEntity(generatorX, generatorY, generatorZ);
        }
        return null;
    }

    public int getGeneratorX()
    {
        return generatorX;
    }

    public int getGeneratorY()
    {
        return generatorY;
    }

    public int getGeneratorZ()
    {
        return generatorZ;
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }
}
