package ru.defea.oneblockultima.event;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.network.ModMessages;
import ru.defea.oneblockultima.network.PacketSyncBlockSetConfig;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.update.UpdateChecker;
import ru.defea.oneblockultima.util.BlockUtil;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;
import ru.defea.oneblockultima.world.OneBlockWorldType;
import ru.defea.oneblockultima.world.SpawnConfigData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ru.defea.oneblockultima.block.BlockOneBlockGenerator.*;

public final class ModEvents
{
    public ModEvents()
    {
    }

    private static final Map<Long, Boolean> processingBlocks = new HashMap<Long, Boolean>();
    private static final Map<Long, UUID> pendingGeneratorOwners = new HashMap<Long, UUID>();
    private static final Map<String, Long> lastAccessDeniedMessageTicks = new HashMap<String, Long>();
    private static final Map<Long, GeneratedBlockRegistry.GeneratedBlockEntry> pendingMobSpawnEntries = new HashMap<Long, GeneratedBlockRegistry.GeneratedBlockEntry>();
    private static final Map<Long, UUID> lastBreakPlayers = new HashMap<Long, UUID>();
    private static int lastBreakCleanupTick = 0;
    private static final Set<Integer> pendingWorldSetup = new HashSet<Integer>();

    private static final Map<Integer, int[]> pendingGuiOpens = new HashMap<Integer, int[]>();
    private static final List<int[]> pendingBreakGenerations = new ArrayList<int[]>();
    private static final Map<Integer, int[]> pendingClaimOpens = new HashMap<Integer, int[]>();

    static final Map<UUID, Integer> lastDisplayedCurrency = new HashMap<UUID, Integer>();

    public static void register()
    {
        ModEvents instance = new ModEvents();
        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
        OneBlockUltima.getLogger().info("[Events] ModEvents registered");
    }

    public static void syncDisplayedCurrency(EntityPlayer player, int currency)
    {
        if (player == null)
        {
            return;
        }

        UUID playerUUID = player.getUniqueID();
        lastDisplayedCurrency.put(playerUUID, currency);
    }

    public static void sendAccessDeniedMessage(EntityPlayer player)
    {
        if (player == null || !(player instanceof EntityPlayerMP))
        {
            return;
        }

        net.minecraft.util.ChatComponentTranslation msg = new net.minecraft.util.ChatComponentTranslation("generator.access.denied");
        msg.setChatStyle(new net.minecraft.util.ChatStyle().setColor(net.minecraft.util.EnumChatFormatting.RED));
        ((EntityPlayerMP) player).addChatMessage(msg);
    }

    public static boolean trySendAccessDeniedMessage(UUID playerId, int x, int y, int z, long worldTick)
    {
        if (playerId == null)
        {
            return false;
        }

        long posLong = posToLong(x, y, z);
        String key = playerId + ":" + posLong;
        Long lastTick = lastAccessDeniedMessageTicks.get(key);
        if (lastTick != null && lastTick == worldTick)
        {
            return false;
        }

        lastAccessDeniedMessageTicks.put(key, worldTick);
        return true;
    }

    public static boolean trySendAccessDeniedMessage(EntityPlayer player, int x, int y, int z, long worldTick)
    {
        if (player == null || !(player instanceof EntityPlayerMP))
        {
            return false;
        }

        if (!trySendAccessDeniedMessage(player.getUniqueID(), x, y, z, worldTick))
        {
            return false;
        }

        net.minecraft.util.ChatComponentTranslation chatMsg = new net.minecraft.util.ChatComponentTranslation("generator.access.denied");
        chatMsg.setChatStyle(new net.minecraft.util.ChatStyle().setColor(net.minecraft.util.EnumChatFormatting.RED));
        ((EntityPlayerMP) player).addChatMessage(chatMsg);
        return true;
    }

    public static boolean ensureGeneratorAccess(World world, int x, int y, int z, EntityPlayer player, TileEntityOneBlockGenerator generator)
    {
        if (generator == null || player == null)
        {
            return false;
        }

        if (world != null)
        {
            applyPendingGeneratorOwner(world, x, y, z);
        }

        return generator.hasAccess(player);
    }

    public static void registerPendingGeneratorOwner(World world, int x, int y, int z, UUID playerId)
    {
        if (world == null || playerId == null)
        {
            return;
        }

        pendingGeneratorOwners.put(posToLong(x, y, z), playerId);
    }

    public static void applyPendingGeneratorOwner(World world, int x, int y, int z)
    {
        if (world == null)
        {
            return;
        }

        long posKey = posToLong(x, y, z);
        UUID playerId = pendingGeneratorOwners.get(posKey);
        if (playerId == null)
        {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (!(tileEntity instanceof TileEntityOneBlockGenerator))
        {
            OneBlockUltima.getLogger().warn("[OwnerDebug] applyPendingGeneratorOwner: TE at ({},{},{}) is NOT a generator! it's {}", x, y, z, tileEntity != null ? tileEntity.getClass().getSimpleName() : "NULL");
            return;
        }

        TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
        OneBlockUltima.getLogger().info("[OwnerDebug] applyPendingGeneratorOwner: pos={},{},{} playerId={}, currentOwnerId={}, isFree={}", x, y, z, playerId, generator.getOwnerId(), generator.isFree());
        if (generator.assignOwnerForPlacement(playerId))
        {
            OneBlockUltima.getLogger().info("[OwnerDebug] applyPendingGeneratorOwner: SUCCESS, new ownerId={}", generator.getOwnerId());
            pendingGeneratorOwners.remove(posKey);
        }
        else
        {
            OneBlockUltima.getLogger().warn("[OwnerDebug] applyPendingGeneratorOwner: FAILED to assign owner");
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.player == null || event.player.worldObj == null || event.player.worldObj.isRemote)
        {
            return;
        }

        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(event.player);
        if (data instanceof ru.defea.oneblockultima.capability.OneBlockPlayerData)
        {
            OneBlockPlayerDataProvider.loadFromEntity(event.player, data);
            PacketSyncPlayerData.sendToPlayer(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.player.worldObj.isRemote)
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.worldObj;
        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        ChunkCoordinates bedPos = player.getBedLocation(0);
        boolean hasSpawn = player.isSpawnForced(0);

        ChunkCoordinates spawnCoords = new ChunkCoordinates(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
        if (bedPos == null || !hasSpawn)
        {
            world.setSpawnLocation(spawnCoords.posX, spawnCoords.posY, spawnCoords.posZ);
            player.setSpawnChunk(spawnCoords, true);
            player.setPositionAndUpdate(spawnCoords.posX + 0.5D, spawnCoords.posY, spawnCoords.posZ + 0.5D);
            player.playerNetServerHandler.setPlayerLocation(spawnCoords.posX + 0.5D, spawnCoords.posY, spawnCoords.posZ + 0.5D, player.rotationYaw, player.rotationPitch);
        }

        PacketSyncPlayerData.sendToPlayer(player);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        OneBlockUltima.getLogger().info("[Events] onWorldLoad fired: dim={}, terrainType={}, expectedId={}",
                world.provider.dimensionId, world.getWorldInfo().getTerrainType(), OneBlockWorldType.ONE_BLOCK.getWorldTypeName());
        if (world.isRemote || world.provider.dimensionId != 0)
        {
            return;
        }

        String worldTypeName = world.getWorldInfo().getTerrainType() != null
                ? world.getWorldInfo().getTerrainType().getWorldTypeName() : "null";
        if (!OneBlockWorldType.ONE_BLOCK.getWorldTypeName().equals(worldTypeName))
        {
            return;
        }

        pendingWorldSetup.add(world.provider.dimensionId);
        OneBlockUltima.getLogger().info("[Events] onWorldLoad: world added to pending setup queue");

        String folderName = world.getSaveHandler().getWorldDirectory().getName();

        createWorldIcon(world);

        SpawnConfigData data = SpawnConfigData.get(world);
        if (!data.spawnInitialized)
        {
            world.setSpawnLocation(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
            data.spawnInitialized = true;
            data.markDirty();
        }

        if (world.getBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            if (world.isAirBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z))
            {
                world.setBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z, ModBlocks.FLUID_BARRIER, 0, 2);
                OneBlockUltima.getLogger().info("[Generator] BARRIER placed at ({},{},{}) on world load", GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
            }

            TileEntity tileEntity = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);

                if (!registry.isGenerated(GENERATOR_X, GENERATED_BLOCK_Y, GENERATOR_Z))
                {
                    ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
                }
            }
            else
            {
                world.scheduleBlockUpdate(GENERATOR_X, GENERATOR_Y, GENERATOR_Z, ModBlocks.ONE_BLOCK_GENERATOR, 1);
            }
            return;
        }

        Block existingBlock = world.getBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
        boolean isReplaceable = existingBlock == null
                || existingBlock.getMaterial().isReplaceable();

        if (isReplaceable)
        {
            world.setBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z, ModBlocks.ONE_BLOCK_GENERATOR, 0, 3);

            TileEntity createdGenerator = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
            if (createdGenerator instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) createdGenerator;
                generator.setOwnerId(null);
            }

            if (world.isAirBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z))
            {
                world.setBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z, ModBlocks.FLUID_BARRIER, 0, 2);
                OneBlockUltima.getLogger().info("[Generator] BARRIER placed at ({},{},{}) on generator creation", GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
            }

            TileEntity tileEntity = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
            else
            {
                world.scheduleBlockUpdate(GENERATOR_X, GENERATOR_Y, GENERATOR_Z, ModBlocks.ONE_BLOCK_GENERATOR, 1);
            }
        }
    }

    private static void createWorldIcon(World world)
    {
        File worldDir = world.getSaveHandler().getWorldDirectory();
        File iconFile = new File(worldDir, "icon.png");

        if (iconFile.exists())
        {
            return;
        }

        InputStream input = null;
        try
        {
            input = ModEvents.class.getResourceAsStream("/assets/oneblockultima/textures/gui/oneblock_logo.png");
            if (input == null)
            {
                OneBlockUltima.getLogger().warn("Could not find logo texture!");
                return;
            }

            if (!worldDir.exists() && !worldDir.mkdirs())
            {
                OneBlockUltima.getLogger().warn("Could not create world directory: {}", worldDir.getAbsolutePath());
                return;
            }

            java.io.FileOutputStream fos = new java.io.FileOutputStream(iconFile);
            try
            {
                byte[] buf = new byte[4096];
                int len;
                while ((len = input.read(buf)) > 0)
                {
                    fos.write(buf, 0, len);
                }
            }
            finally
            {
                fos.close();
            }
            OneBlockUltima.getLogger().info("Icon created for world: {}", worldDir.getName());
        }
        catch (IOException e)
        {
            OneBlockUltima.getLogger().warn("Failed to create icon.png for OneBlock world {}", worldDir.getName(), e);
        }
        finally
        {
            if (input != null)
            {
                try { input.close(); } catch (IOException ignored) { }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player == null || event.player.worldObj == null || event.player.worldObj.isRemote)
        {
            return;
        }

        UpdateChecker.checkForUpdates((EntityPlayerMP) event.player);

        try
        {
            String json = BlockSetConfig.get().toJson();
            OneBlockUltima.getLogger().info("[Sync] BlockSetConfig JSON length: " + (json != null ? json.length() : "null"));
            if (json != null && !json.isEmpty())
            {
                PacketSyncBlockSetConfig packet = new PacketSyncBlockSetConfig(json);
                OneBlockUltima.getLogger().info("[Sync] Sending BlockSetConfig packet to " + event.player.getCommandSenderName());
                ModMessages.sendToPlayer(packet, (EntityPlayerMP) event.player);
            }
        }
        catch (Exception e)
        {
            OneBlockUltima.getLogger().error("[Sync] Failed to send BlockSetConfig", e);
        }

        World world = event.player.worldObj;
        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
            if (generator.isFree())
            {
                generator.tryAssignOwnerIfEligible(event.player.getUniqueID());
            }
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        SpawnConfigData data = SpawnConfigData.get(world);
        ChunkCoordinates spawnCoords = new ChunkCoordinates(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
        if (!data.spawnInitialized)
        {
            world.setSpawnLocation(spawnCoords.posX, spawnCoords.posY, spawnCoords.posZ);
            data.spawnInitialized = true;
            data.markDirty();
        }

        ChunkCoordinates currentSpawn = world.getSpawnPoint();
        if (currentSpawn == null || currentSpawn.posX != spawnCoords.posX || currentSpawn.posY != spawnCoords.posY || currentSpawn.posZ != spawnCoords.posZ)
        {
            world.setSpawnLocation(spawnCoords.posX, spawnCoords.posY, spawnCoords.posZ);
        }

        player.setSpawnChunk(spawnCoords, true);

        IOneBlockPlayerData playerData = OneBlockPlayerDataProvider.get(player);
        if (playerData != null)
        {
            OneBlockPlayerDataProvider.loadFromEntity(player, playerData);
        }

        if (!data.spawnTeleportDone)
        {
            player.setPositionAndUpdate(spawnCoords.posX + 0.5D, spawnCoords.posY, spawnCoords.posZ + 0.5D);
            player.playerNetServerHandler.setPlayerLocation(spawnCoords.posX + 0.5D, spawnCoords.posY, spawnCoords.posZ + 0.5D, player.rotationYaw, player.rotationPitch);
            data.spawnTeleportDone = true;
            data.markDirty();
        }

        PacketSyncPlayerData.sendToPlayer(player);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != Phase.END) return;
        if (event.player == null || event.player.worldObj == null) return;

        try
        {
            EntityPlayer player = event.player;
            World world = player.worldObj;

            if (!world.isRemote)
            {
                int[] claimPos = pendingClaimOpens.remove(player.getEntityId());
                if (claimPos != null)
                {
                    GuiHandler.openClaimScreen(player, claimPos[0], claimPos[1], claimPos[2]);
                }
                int[] guiPos = pendingGuiOpens.remove(player.getEntityId());
                if (guiPos != null)
                {
                    GuiHandler.open(player, guiPos[0], guiPos[1], guiPos[2]);
                }
            }

            GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);

            int baseY = (int) Math.floor(player.posY - 0.1D);
            for (int dy = 0; dy <= 1; dy++)
            {
                int checkX = (int) Math.floor(player.posX);
                int checkY = baseY - dy;
                int checkZ = (int) Math.floor(player.posZ);
                if (!registry.isGenerated(checkX, checkY, checkZ)) continue;

                Block block = world.getBlock(checkX, checkY, checkZ);
                boolean hasCollision = block != null
                        && block.getCollisionBoundingBoxFromPool(world, checkX, checkY, checkZ) != null
                        && !block.getMaterial().isReplaceable();
                double relY = player.posY - checkY;
                if (!hasCollision && player.motionY < 0 && relY <= 1.2D)
                {
                    player.motionY = 0.0D;
                    player.fallDistance = 0.0F;
                    player.onGround = true;
                    player.setPosition(player.posX, checkY + 1.0D, player.posZ);
                    break;
                }
                if (hasCollision && relY <= 1.2D && relY >= 0.0D)
                {
                    player.onGround = true;
                    break;
                }
            }
        }
        catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != Phase.END || event.world == null || event.world.isRemote)
        {
            return;
        }

        World world = event.world;

        if (pendingWorldSetup.contains(world.provider.dimensionId))
        {
            if (world.getTotalWorldTime() >= 5)
            {
                pendingWorldSetup.remove(world.provider.dimensionId);
                tryInitializeWorld(world);
            }
            return;
        }

        for (Object obj : world.loadedTileEntityList)
        {
            TileEntity tileEntity = (TileEntity) obj;
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tickInvites();
            }
        }

        if (!pendingBreakGenerations.isEmpty())
        {
            Iterator<int[]> it = pendingBreakGenerations.iterator();
            while (it.hasNext())
            {
                int[] genInfo = it.next();
                if (genInfo[0] == world.provider.dimensionId)
                {
                    TileEntity te = world.getTileEntity(genInfo[1], genInfo[2], genInfo[3]);
                    if (te instanceof TileEntityOneBlockGenerator)
                    {
                        ((TileEntityOneBlockGenerator) te).tryGenerateBlock();
                    }
                    it.remove();
                }
            }
        }

        if (!lastBreakPlayers.isEmpty() && ++lastBreakCleanupTick >= 200)
        {
            lastBreakCleanupTick = 0;
            lastBreakPlayers.clear();
        }
    }

    private static void tryInitializeWorld(World world)
    {
        OneBlockUltima.getLogger().info("[Setup] tryInitializeWorld called for dim={} at tick={}", world.provider.dimensionId, world.getTotalWorldTime());

        if (world.getBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z) != ModBlocks.ONE_BLOCK_GENERATOR)
        {
            Block existingBlock = world.getBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
            boolean isReplaceable = existingBlock == null || existingBlock.getMaterial().isReplaceable();
            if (isReplaceable)
            {
                boolean placed = world.setBlock(GENERATOR_X, GENERATOR_Y, GENERATOR_Z, ModBlocks.ONE_BLOCK_GENERATOR, 0, 3);
                OneBlockUltima.getLogger().info("[Setup] Generator placement: success={} at ({},{},{})", placed, GENERATOR_X, GENERATOR_Y, GENERATOR_Z);

                if (placed)
                {
                    TileEntity te = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
                    if (te instanceof TileEntityOneBlockGenerator)
                    {
                        ((TileEntityOneBlockGenerator) te).setOwnerId(null);
                    }
                }
            }
            else
            {
                OneBlockUltima.getLogger().warn("[Setup] Block at generator position is not replaceable: {}", existingBlock);
            }
        }
        else
        {
            OneBlockUltima.getLogger().info("[Setup] Generator already exists at ({},{},{})", GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
        }

        SpawnConfigData data = SpawnConfigData.get(world);
        if (!data.spawnInitialized)
        {
            world.setSpawnLocation(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
            data.spawnInitialized = true;
            data.markDirty();
            OneBlockUltima.getLogger().info("[Setup] Spawn set to ({},{},{})", GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
        }

        if (world.isAirBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z))
        {
            world.setBlock(GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z, ModBlocks.FLUID_BARRIER, 0, 2);
            OneBlockUltima.getLogger().info("[Setup] BARRIER placed at ({},{},{})", GENERATOR_X, FLUID_BARRIER_Y, GENERATOR_Z);
        }

        TileEntity tileEntity = world.getTileEntity(GENERATOR_X, GENERATOR_Y, GENERATOR_Z);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
            if (!registry.isGenerated(GENERATOR_X, GENERATED_BLOCK_Y, GENERATOR_Z))
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
        }
        else
        {
            world.scheduleBlockUpdate(GENERATOR_X, GENERATOR_Y, GENERATOR_Z, ModBlocks.ONE_BLOCK_GENERATOR, 1);
        }

        OneBlockUltima.getLogger().info("[Setup] tryInitializeWorld complete for dim={}", world.provider.dimensionId);
    }

    @SubscribeEvent
    public void onLivingSpawn(LivingSpawnEvent.CheckSpawn event)
    {
        World world = event.entity.worldObj;
        if (world.isRemote)
        {
            return;
        }

        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        // CheckSpawn in 1.7.10 does not have a spawner field; deny all non-player spawns on OneBlock worlds
        event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(net.minecraftforge.event.entity.EntityJoinWorldEvent event)
    {
        World world = event.entity.worldObj;
        if (world.isRemote)
        {
            return;
        }

        if (!(event.entity instanceof net.minecraft.entity.item.EntityItem))
        {
            return;
        }

        net.minecraft.entity.item.EntityItem entityItem = (net.minecraft.entity.item.EntityItem) event.entity;

        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        int ix = (int) Math.floor(entityItem.posX);
        int iy = (int) Math.floor(entityItem.posY);
        int iz = (int) Math.floor(entityItem.posZ);
        long itemPosKey = posToLong(ix, iy, iz);
        UUID breakerId = lastBreakPlayers.get(itemPosKey);
        if (breakerId == null)
        {
            return;
        }

        EntityPlayer player = getPlayerByUUID(world, breakerId);
        if (player == null)
        {
            return;
        }

        ItemStack stack = entityItem.getEntityItem();
        if (stack == null || stack.stackSize <= 0)
        {
            return;
        }

        player.inventory.addItemStackToInventory(stack);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }

        World world = event.world;
        if (world.provider.dimensionId != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        int x = event.x;
        int y = event.y;
        int z = event.z;
        Block placedBlock = event.block;
        int placedMeta = event.blockMetadata;

        if (placedBlock == ModBlocks.ONE_BLOCK_GENERATOR && event.player != null)
        {
            OneBlockUltima.getLogger().info("[OwnerDebug] PlaceEvent fired for generator at ({},{},{}) by player {}", x, y, z, event.player.getCommandSenderName());
            registerPendingGeneratorOwner(world, x, y, z, event.player.getUniqueID());

            TileEntity tileEntity = world.getTileEntity(x, y, z);
            OneBlockUltima.getLogger().info("[OwnerDebug] tileEntity at pos {},{},{}: {}", x, y, z, tileEntity != null ? tileEntity.getClass().getSimpleName() : "NULL");
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
                OneBlockUltima.getLogger().info("[OwnerDebug] generator.isFree()={}, ownerId={}, player={}", generator.isFree(), generator.getOwnerId(), event.player.getCommandSenderName());
                if (generator.isFree())
                {
                    boolean result = generator.assignOwnerForPlacement(event.player.getUniqueID());
                    OneBlockUltima.getLogger().info("[OwnerDebug] assignOwnerForPlacement result={}, new ownerId={}", result, generator.getOwnerId());
                }
                else
                {
                    OneBlockUltima.getLogger().warn("[OwnerDebug] generator is NOT free, skipping assignOwnerForPlacement! ownerId={}", generator.getOwnerId());
                }
            }
        }

        Block belowBlock = world.getBlock(x, y - 1, z);
        if (belowBlock == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            if (placedBlock == Blocks.bedrock)
            {
                world.setBlock(x, y, z, ModBlocks.CUSTOM_BEDROCK, placedMeta, 3);
            }
            else if (placedBlock == Blocks.end_portal_frame)
            {
                int customMeta = placedMeta;
                world.setBlock(x, y, z, ModBlocks.CUSTOM_PORTAL_FRAME, customMeta, 3);
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent event)
    {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        if (event.world.isRemote || event.entityPlayer == null)
        {
            return;
        }

        EntityPlayer player = event.entityPlayer;
        int x = event.x;
        int y = event.y;
        int z = event.z;
        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.world);

        int generatorX = -1;
        int generatorY = -1;
        int generatorZ = -1;
        if (event.world.getBlock(x, y, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            generatorX = x;
            generatorY = y;
            generatorZ = z;
        }
        else if (event.world.getBlock(x, y - 1, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            generatorX = x;
            generatorY = y - 1;
            generatorZ = z;
        }
        else
        {
            int[] genPos = registry.getGeneratorPos(x, y, z);
            if (genPos != null)
            {
                generatorX = genPos[0];
                generatorY = genPos[1];
                generatorZ = genPos[2];
            }
        }

        if (player.isSneaking())
        {
            if (generatorX >= 0)
            {
                ItemStack heldItem = player.getHeldItem();
                if (heldItem != null && heldItem.stackSize > 0 && heldItem.getItem() instanceof net.minecraft.item.ItemBlock)
                {
                    TileEntity te = event.world.getTileEntity(generatorX, generatorY, generatorZ);
                    if (te instanceof TileEntityOneBlockGenerator)
                    {
                        TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) te;
                        if (generator.hasAccess(player))
                        {
                            int placeX, placeY, placeZ;
                            switch (event.face)
                            {
                                case 0: placeX = generatorX; placeY = generatorY - 1; placeZ = generatorZ; break;
                                case 1: placeX = generatorX; placeY = generatorY + 2; placeZ = generatorZ; break;
                                case 2: placeX = generatorX; placeY = generatorY + 1; placeZ = generatorZ - 1; break;
                                case 3: placeX = generatorX; placeY = generatorY + 1; placeZ = generatorZ + 1; break;
                                case 4: placeX = generatorX - 1; placeY = generatorY + 1; placeZ = generatorZ; break;
                                case 5: placeX = generatorX + 1; placeY = generatorY + 1; placeZ = generatorZ; break;
                                default: placeX = generatorX; placeY = generatorY + 1; placeZ = generatorZ; break;
                            }

                            if (placeX != generatorX || placeY != generatorY || placeZ != generatorZ)
                            {
                                net.minecraft.item.ItemBlock itemBlock = (net.minecraft.item.ItemBlock) heldItem.getItem();
                                Block placeBlock = itemBlock.field_150939_a;
                                int placeMeta = heldItem.getItemDamage();
                                if (event.world.isAirBlock(placeX, placeY, placeZ) || event.world.getBlock(placeX, placeY, placeZ).getMaterial().isReplaceable())
                                {
                                    event.world.setBlock(placeX, placeY, placeZ, placeBlock, placeMeta, 3);
                                    if (!player.capabilities.isCreativeMode)
                                    {
                                        --heldItem.stackSize;
                                    }

                                    TileEntity placedTE = event.world.getTileEntity(placeX, placeY, placeZ);
                                    if (placedTE instanceof TileEntityOneBlockGenerator)
                                    {
                                        ((TileEntityOneBlockGenerator) placedTE).assignOwnerForPlacement(player.getUniqueID());
                                    }

                                    event.setCanceled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        if (generatorX >= 0)
        {
            if (event.world.getBlock(event.x, event.y, event.z) == ModBlocks.ONE_BLOCK_GENERATOR)
            {
                return;
            }

            TileEntity generatorTile = event.world.getTileEntity(generatorX, generatorY, generatorZ);
            if (generatorTile instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatorTile;
                OneBlockUltima.getLogger().info("[OwnerDebug] RightClick generated block at ({},{},{}), generator at ({},{},{}), isFree={}, ownerId={}, player={}", event.x, event.y, event.z, generatorX, generatorY, generatorZ, generator.isFree(), generator.getOwnerId(), player.getCommandSenderName());

                if (generator.isFree() && generator.canBeClaimedBy(player.getUniqueID()))
                {
                    pendingClaimOpens.put(player.getEntityId(), new int[]{generatorX, generatorY, generatorZ});
                }
                else if (ensureGeneratorAccess(event.world, generatorX, generatorY, generatorZ, player, generator))
                {
                    pendingGuiOpens.put(player.getEntityId(), new int[]{generatorX, generatorY, generatorZ});
                }
                else
                {
                    OneBlockUltima.getLogger().warn("[OwnerDebug] ACCESS DENIED for player {} on generator at ({},{},{})", player.getCommandSenderName(), generatorX, generatorY, generatorZ);
                    trySendAccessDeniedMessage(player, generatorX, generatorY, generatorZ, event.world.getTotalWorldTime());
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }

        int x = event.x;
        int y = event.y;
        int z = event.z;
        World world = event.world;
        EntityPlayer breaker = event.getPlayer();

        if (breaker != null && world.provider.dimensionId == 0 && world.getWorldInfo().getTerrainType() == OneBlockWorldType.ONE_BLOCK)
        {
            lastBreakPlayers.put(posToLong(x, y, z), breaker.getUniqueID());
        }

        if (world.getBlock(x, y, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            event.setCanceled(true);
            return;
        }

        long posKey = posToLong(x, y, z);
        Boolean processing = processingBlocks.get(posKey);
        if (processing != null && processing)
        {
            return;
        }

        TileEntity generatorTile = world.getTileEntity(x, y, z);
        if (generatorTile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatorTile;
            if (!generator.hasAccess(event.getPlayer()))
            {
                event.setCanceled(true);
                trySendAccessDeniedMessage(event.getPlayer(), x, y, z, world.getTotalWorldTime());
                return;
            }
        }

        if (world.getBlock(x, y - 2, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            world.setBlock(x, y, z, ModBlocks.FLUID_BARRIER, 0, 2);
            OneBlockUltima.getLogger().info("[Generator] BARRIER placed at ({},{},{}) after block break", x, y, z);
            event.setCanceled(true);
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.world);
        GeneratedBlockRegistry.GeneratedBlockEntry entry = registry.getEntry(x, y, z);
        if (entry == null)
        {
            OneBlockUltima.getLogger().warn("[BreakDebug] No generated entry found for broken block ({},{},{})", x, y, z);
            return;
        }

        if (breaker != null)
        {
            int[] genPos = new int[]{entry.gx, entry.gy, entry.gz};
            TileEntity genTile = world.getTileEntity(genPos[0], genPos[1], genPos[2]);
            if (genTile instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator gen = (TileEntityOneBlockGenerator) genTile;
                if (!gen.hasAccess(breaker))
                {
                    event.setCanceled(true);
                    trySendAccessDeniedMessage(breaker, genPos[0], genPos[1], genPos[2], world.getTotalWorldTime());
                    return;
                }
            }
        }

        OneBlockUltima.getLogger().warn("[BreakDebug] Generated entry found for ({},{},{}) -> generator [{},{},{}] set={} level={}", x, y, z, entry.gx, entry.gy, entry.gz, entry.setId, entry.level);

        pendingMobSpawnEntries.put(posKey, entry);

        spawnMobOnBlockBreak(world, x, y, z, entry);
        registry.remove(x, y, z);

        int[] genPos = new int[]{entry.gx, entry.gy, entry.gz};
        TileEntity generatedTile = world.getTileEntity(genPos[0], genPos[1], genPos[2]);
        EntityPlayer player = event.getPlayer();
        if (generatedTile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatedTile;
            if (player == null)
            {
                OneBlockUltima.getLogger().info("[BreakDebug] Non-player break detected at ({},{},{}) for generator [{},{},{}]", x, y, z, entry.gx, entry.gy, entry.gz);
                if (!generator.canProcessNonPlayerBreak(world.getTotalWorldTime()))
                {
                    OneBlockUltima.getLogger().info("[BreakDebug] Non-player break blocked by cooldown for generator [{},{},{}] at tick {}", entry.gx, entry.gy, entry.gz, world.getTotalWorldTime());
                    event.setCanceled(true);
                    return;
                }

                generator.markNonPlayerBreak(world.getTotalWorldTime());
                OneBlockUltima.getLogger().info("[BreakDebug] Non-player break accepted for generator [{},{},{}] at tick {}", entry.gx, entry.gy, entry.gz, world.getTotalWorldTime());
            }
        }

        if (world.getBlock(x, y, z) == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            processingBlocks.put(posKey, true);
        }

        if (generatedTile instanceof TileEntityOneBlockGenerator)
        {
            if (player == null)
            {
                OneBlockUltima.getLogger().info("[BreakDebug] Invoking non-player generation for generator [{},{},{}] at tick {}", entry.gx, entry.gy, entry.gz, world.getTotalWorldTime());
            }
            else
            {
                OneBlockUltima.getLogger().info("[BreakDebug] Player break detected at ({},{},{}) for generator [{},{},{}]", x, y, z, entry.gx, entry.gy, entry.gz);
            }
            pendingBreakGenerations.add(new int[]{world.provider.dimensionId, entry.gx, entry.gy, entry.gz});
        }

        if (player != null)
        {
            IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
            if (data != null)
            {
                if (entry.currency > 0)
                {
                    data.addCurrency(entry.currency);
                }
                data.addBrokenBlocks(entry.setId, 1);
                OneBlockPlayerDataProvider.saveToEntity(player, data);
                PacketSyncPlayerData.sendToPlayer(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }

        World world = event.world;
        int x = event.x;
        int y = event.y;
        int z = event.z;
        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
        GeneratedBlockRegistry.GeneratedBlockEntry entry = registry.getEntry(x, y, z);
        GeneratedBlockRegistry.GeneratedBlockEntry savedEntry = pendingMobSpawnEntries.remove(posToLong(x, y, z));

        boolean isTrackedBlock = entry != null || savedEntry != null;
        GeneratedBlockRegistry.GeneratedBlockEntry mobSpawnEntry = entry != null ? entry : savedEntry;

        if (!isTrackedBlock)
        {
            return;
        }

        UUID breakerId = lastBreakPlayers.remove(posToLong(x, y, z));
        EntityPlayer player = breakerId != null ? getPlayerByUUID(world, breakerId) : event.harvester;

        List<ItemStack> drops = new ArrayList<ItemStack>(event.drops);

        boolean hasRealDrops = false;
        for (ItemStack drop : drops)
        {
            if (drop != null && drop.stackSize > 0)
            {
                hasRealDrops = true;
                break;
            }
        }

        if (!hasRealDrops)
        {
            Block block = event.block;

            if (block != null && Block.getIdFromBlock(block) != 0)
            {
                boolean shouldDropBlock = isShouldDropBlock(block, player, drops);

                if (shouldDropBlock)
                {
                    Item item = Item.getItemFromBlock(block);
                    if (item != null)
                    {
                        ItemStack blockStack = new ItemStack(block, 1, event.blockMetadata);
                        drops.add(blockStack);
                    }
                }
            }
        }

        if (player == null)
        {
            if (mobSpawnEntry != null)
            {
                int[] genPos = new int[]{mobSpawnEntry.gx, mobSpawnEntry.gy, mobSpawnEntry.gz};
                TileEntity generatedTile = world.getTileEntity(genPos[0], genPos[1], genPos[2]);
                if (generatedTile instanceof TileEntityOneBlockGenerator)
                {
                    TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatedTile;
                    generator.markNonPlayerBreak(world.getTotalWorldTime());
                    generator.tryGenerateBlock();
                }
            }
            return;
        }

        event.drops.clear();

        for (ItemStack drop : drops)
        {
            if (drop == null || drop.stackSize <= 0) continue;

            ItemStack remaining = drop.copy();
            if (!player.inventory.addItemStackToInventory(remaining))
            {
                net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(
                        event.world,
                        x + 0.5D,
                        y + 0.5D,
                        z + 0.5D,
                        remaining
                );
                event.world.spawnEntityInWorld(entityItem);
            }
        }
    }

    private static boolean isShouldDropBlock(Block block, EntityPlayer player, List<ItemStack> drops)
    {
        boolean shouldDropBlock = true;

        if (player == null)
        {
            return shouldDropBlock;
        }

        if (block == Blocks.tallgrass || block == Blocks.double_plant || block == Blocks.deadbush)
        {
            ItemStack heldItem = player.getHeldItem();
            if (heldItem == null || heldItem.stackSize <= 0 || heldItem.getItem() != Items.shears)
            {
                shouldDropBlock = false;
            }
        }

        if (block == Blocks.leaves || block == Blocks.leaves2)
        {
            ItemStack heldItem = player.getHeldItem();
            if (heldItem == null || heldItem.stackSize <= 0 || heldItem.getItem() != Items.shears)
            {
                boolean hasSapling = false;
                for (ItemStack drop : drops)
                {
                    if (drop != null && drop.stackSize > 0 && drop.getItem() == Items.dye)
                    {
                        hasSapling = true;
                        break;
                    }
                }
                if (!hasSapling)
                {
                    shouldDropBlock = false;
                }
            }
        }

        if (block == Blocks.web)
        {
            ItemStack heldItem = player.getHeldItem();
            if (heldItem == null || heldItem.stackSize <= 0 || heldItem.getItem() != Items.shears)
            {
                shouldDropBlock = false;
            }
        }
        return shouldDropBlock;
    }

    private static void spawnMobOnBlockBreak(World world, int x, int y, int z, GeneratedBlockRegistry.GeneratedBlockEntry entry)
    {
        int[] genPos = new int[]{entry.gx, entry.gy, entry.gz};
        TileEntity tile = world.getTileEntity(genPos[0], genPos[1], genPos[2]);
        if (tile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tile;
            if (generator.isDisableMobGeneration())
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Mob generation disabled on generator at ({},{},{})", genPos[0], genPos[1], genPos[2]);
                return;
            }
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Entry: setId={}, level={}", entry.setId, entry.level);
        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(entry.setId);
        if (set == null)
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] Set is null for setId={}", entry.setId);
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Set found: {}", set.id);
        BlockSetConfig.SetLevelDefinition levelDefinition = set.getLevel(entry.level);
        if (levelDefinition == null)
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] Level definition is null for level={}", entry.level);
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Level definition found. Total mobs: {}", levelDefinition.mobs != null ? levelDefinition.mobs.size() : 0);
        BlockSetConfig.MobEntryDefinition mobEntry = levelDefinition.pickMob(world.rand);
        if (mobEntry == null || mobEntry.registry == null || mobEntry.registry.isEmpty())
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] pickMob returned null or empty registry");
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Attempting to spawn mob: {} count={}", mobEntry.registry, mobEntry.count);
        for (int i = 0; i < Math.max(1, mobEntry.count); i++)
        {
            Entity entity = EntityList.createEntityByName(mobEntry.registry, world);
            if (entity == null)
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Failed to create entity for registry: {}", mobEntry.registry);
                continue;
            }

            OneBlockUltima.getLogger().info("[Mob Spawn] Successfully spawned mob: {}", mobEntry.registry);
            entity.setPosition(x + 0.5D, y + 1.0D, z + 0.5D);

            if (mobEntry.nbtTags != null && !mobEntry.nbtTags.hasNoTags())
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Applying NBT tags to mob: {}", mobEntry.nbtTags);
                BlockUtil.applyNbtToEntity(entity, mobEntry.nbtTags);
            }

            world.spawnEntityInWorld(entity);
        }
    }

    @SubscribeEvent
    public void clonePlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
    {
        NBTTagCompound oldData = event.original.getEntityData();
        NBTTagCompound newData = event.entityPlayer.getEntityData();
        if (oldData.hasKey("oneblockultima_player_data"))
        {
            newData.setTag("oneblockultima_player_data", oldData.getCompoundTag("oneblockultima_player_data"));
        }
    }

    public static long posToLong(int x, int y, int z)
    {
        return ((long) x & 0x3FFFFFF) | (((long) z & 0x3FFFFFF) << 26) | (((long) y & 0xFFF) << 52);
    }

    public static EntityPlayer getPlayerByUUID(World world, UUID uuid)
    {
        for (Object obj : world.playerEntities)
        {
            EntityPlayer player = (EntityPlayer) obj;
            if (player.getUniqueID().equals(uuid))
            {
                return player;
            }
        }
        return null;
    }
}
