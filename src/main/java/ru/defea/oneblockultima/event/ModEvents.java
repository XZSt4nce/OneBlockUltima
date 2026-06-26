package ru.defea.oneblockultima.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;
import ru.defea.oneblockultima.world.OneBlockWorldType;
import ru.defea.oneblockultima.world.SpawnConfigData;

import static ru.defea.oneblockultima.block.BlockOneBlockGenerator.GENERATED_BLOCK_POS;
import static ru.defea.oneblockultima.block.BlockOneBlockGenerator.GENERATOR_POS;

@Mod.EventBusSubscriber(modid = OneBlockUltima.MODID)
public final class ModEvents
{
    private ModEvents()
    {
    }

    private static final java.util.Map<BlockPos, Boolean> processingBlocks = new java.util.HashMap<>();

    @SideOnly(Side.CLIENT)
    private static float displayedCurrency = 0.0f;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Text event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT)
        {
            return;
        }

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null)
        {
            return;
        }

        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            return;
        }

        int targetCurrency = data.getCurrency();
        displayedCurrency += (targetCurrency - displayedCurrency) * 0.14f;
        int currency = Math.round(displayedCurrency);
        String balanceValue = String.valueOf(currency);
        int x = event.getResolution().getScaledWidth() - 50;
        int y = 8;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png"));
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16.0F, 16.0F);
        Minecraft.getMinecraft().fontRenderer.drawString(balanceValue, x + 18, y + 3, 0xFFD700);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        SpawnConfigData data = SpawnConfigData.get(world);
        if (!data.spawnInitialized)
        {
            world.setSpawnPoint(new BlockPos(0, 65, 0));
            data.spawnInitialized = true;
            data.markDirty();
        }

        if (world.getBlockState(GENERATOR_POS).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            TileEntity tileEntity = world.getTileEntity(GENERATOR_POS);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);
                BlockPos blockAbove = GENERATOR_POS.up();

                if (!registry.isGenerated(blockAbove))
                {
                    ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
                }
            }
            else
            {
                world.scheduleUpdate(GENERATOR_POS, ModBlocks.ONE_BLOCK_GENERATOR, 1);
            }
            return;
        }

        if (world.getBlockState(GENERATOR_POS).getMaterial().isReplaceable()
                || world.getBlockState(GENERATOR_POS).getBlock().isAir(world.getBlockState(GENERATOR_POS), world, GENERATOR_POS))
        {
            world.setBlockState(GENERATOR_POS, ModBlocks.ONE_BLOCK_GENERATOR.getDefaultState(), 3);
            TileEntity tileEntity = world.getTileEntity(GENERATOR_POS);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
            else
            {
                world.scheduleUpdate(GENERATOR_POS, ModBlocks.ONE_BLOCK_GENERATOR, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player.world.isRemote)
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;
        if (world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        SpawnConfigData data = SpawnConfigData.get(world);
        BlockPos spawnPos = new BlockPos(0, 65, 0);
        if (!data.spawnInitialized)
        {
            world.setSpawnPoint(spawnPos);
            data.spawnInitialized = true;
            data.markDirty();
        }

        BlockPos currentSpawn = world.getSpawnPoint();
        if (!currentSpawn.equals(spawnPos))
        {
            world.setSpawnPoint(spawnPos);
        }

        player.setSpawnPoint(spawnPos, true);
        player.setSpawnChunk(spawnPos, true, player.dimension);

        if (!data.spawnTeleportDone)
        {
            player.setPositionAndUpdate(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            player.connection.setPlayerLocation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
            data.spawnTeleportDone = true;
            data.markDirty();
        }

        PacketSyncPlayerData.sendToPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.player.world.isRemote)
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        World world = player.world;
        if (world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        BlockPos spawnPos = new BlockPos(0, 65, 0);
        world.setSpawnPoint(spawnPos);
        player.setSpawnPoint(spawnPos, true);
        player.setSpawnChunk(spawnPos, true, player.dimension);
        player.setPositionAndUpdate(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        player.connection.setPlayerLocation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != Phase.END) return;
        if (event.player == null || event.player.world == null) return;

        try
        {
            net.minecraft.entity.player.EntityPlayer player = event.player;
            net.minecraft.world.World world = player.world;
            GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);

            int baseY = (int) Math.floor(player.posY - 0.1D);
            // Check the block at player's feet and the block immediately below it
            for (int dy = 0; dy <= 1; dy++)
            {
                BlockPos checkPos = new BlockPos(player.posX, baseY - dy, player.posZ);
                if (!registry.isGenerated(checkPos)) continue;

                net.minecraft.block.state.IBlockState state = world.getBlockState(checkPos);
                boolean hasCollision = state.getCollisionBoundingBox(world, checkPos) != null && !state.getMaterial().isReplaceable();
                double relY = player.posY - checkPos.getY();
                // If there's no collision and player is falling into the block space, stop the fall and snap player above
                if (!hasCollision && player.motionY < 0 && relY <= 1.2D)
                {
                    player.motionY = 0.0D;
                    player.fallDistance = 0.0F;
                    player.onGround = true;
                    player.setPosition(player.posX, checkPos.getY() + 1.0D, player.posZ);
                    break;
                }
                // If there is collision, ensure player's onGround is set correctly when standing
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
    public static void onLivingSpawn(LivingSpawnEvent.CheckSpawn event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        World world = event.getWorld();
        if (world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        if (!event.isSpawner())
        {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getWorld().isRemote || event.getEntityPlayer() == null)
        {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        if (player.isSneaking())
        {
            return;
        }

        BlockPos clickedPos = event.getPos();
        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.getWorld());

        if (event.getWorld().getBlockState(clickedPos).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            event.setCanceled(true);
            GuiHandler.open(player, clickedPos);
            return;
        }

        BlockPos generatorPos = registry.getGeneratorPos(clickedPos);
        if (generatorPos != null)
        {
            event.setCanceled(true);
            GuiHandler.open(player, generatorPos);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        if (event.getState().getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            event.setCanceled(true);
            return;
        }

        if (processingBlocks.getOrDefault(event.getPos(), false))
        {
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.getWorld());
        GeneratedBlockRegistry.GeneratedBlockEntry entry = registry.getEntry(event.getPos());
        if (entry == null)
        {
            return;
        }

        processingBlocks.put(event.getPos(), true);

        World world = event.getWorld();
        registry.remove(event.getPos());
        spawnMobOnBlockBreak(world, event.getPos(), entry);

        if (entry.generatorPos != null)
        {
            TileEntity tileEntity = world.getTileEntity(entry.generatorPos);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tryGenerateBlock();
            }
            else
            {
                world.scheduleUpdate(entry.generatorPos, ModBlocks.ONE_BLOCK_GENERATOR, 1);
            }
        }

        EntityPlayer player = event.getPlayer();
        if (player != null)
        {
            IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
            if (data != null && entry.currency > 0)
            {
                data.addCurrency(entry.currency);
                PacketSyncPlayerData.sendToPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.getWorld());
        GeneratedBlockRegistry.GeneratedBlockEntry entry = registry.getEntry(event.getPos());
        if (entry == null)
        {
            return;
        }

        EntityPlayer player = event.getHarvester();
        if (player == null)
        {
            return;
        }

        java.util.List<net.minecraft.item.ItemStack> drops = new java.util.ArrayList<>(event.getDrops());
        event.getDrops().clear();

        for (net.minecraft.item.ItemStack drop : drops)
        {
            net.minecraft.item.ItemStack remaining = drop.copy();
            if (!player.inventory.addItemStackToInventory(remaining))
            {
                net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(event.getWorld(), event.getPos().getX() + 0.5D, event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D, remaining);
                event.getWorld().spawnEntity(entityItem);
            }
        }
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (pos == GENERATED_BLOCK_POS)
        {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != Blocks.AIR)
            {
                processingBlocks.put(pos, false);
            }
        }
    }

    private static void spawnMobOnBlockBreak(World world, BlockPos pos, GeneratedBlockRegistry.GeneratedBlockEntry entry)
    {
        OneBlockUltima.getLogger().info("[Mob Spawn] Entry: setId=" + entry.setId + ", level=" + entry.level);
        BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(entry.setId);
        if (set == null)
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] Set is null for setId=" + entry.setId);
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Set found: " + set.id);
        BlockSetConfig.SetLevelDefinition levelDefinition = set.getLevel(entry.level);
        if (levelDefinition == null)
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] Level definition is null for level=" + entry.level);
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Level definition found. Total mobs: " + (levelDefinition.mobs != null ? levelDefinition.mobs.size() : 0));
        BlockSetConfig.MobEntryDefinition mobEntry = levelDefinition.pickMob(world.rand);
        if (mobEntry == null || mobEntry.registry == null || mobEntry.registry.isEmpty())
        {
            OneBlockUltima.getLogger().info("[Mob Spawn] pickMob returned null or empty registry");
            return;
        }

        OneBlockUltima.getLogger().info("[Mob Spawn] Attempting to spawn mob: " + mobEntry.registry + " count=" + mobEntry.count);
        for (int i = 0; i < Math.max(1, mobEntry.count); i++)
        {
            Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(mobEntry.registry), world);
            if (entity == null)
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Failed to create entity for registry: " + mobEntry.registry);
                continue;
            }

            OneBlockUltima.getLogger().info("[Mob Spawn] Successfully spawned mob: " + mobEntry.registry);
            entity.setPosition(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            
            // Применяем NBT теги к мобу если они есть
            if (mobEntry.nbtTags != null && !mobEntry.nbtTags.hasNoTags())
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Applying NBT tags to mob: " + mobEntry.nbtTags);
                ru.defea.oneblockultima.util.BlockUtil.applyNbtToEntity(entity, mobEntry.nbtTags);
            }
            
            world.spawnEntity(entity);
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.entity.Entity> event)
    {
        if (event.getObject() instanceof EntityPlayer)
        {
            event.addCapability(
                    new ResourceLocation(OneBlockUltima.MODID, "player_data"),
                    new OneBlockPlayerDataProvider()
            );
        }
    }

    @SubscribeEvent
    public static void clonePlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            IOneBlockPlayerData oldData = OneBlockPlayerDataProvider.get(event.getOriginal());
            IOneBlockPlayerData newData = OneBlockPlayerDataProvider.get(event.getEntityPlayer());
            if (oldData instanceof ru.defea.oneblockultima.capability.OneBlockPlayerData
                    && newData instanceof ru.defea.oneblockultima.capability.OneBlockPlayerData)
            {
                ru.defea.oneblockultima.capability.OneBlockPlayerData oldPlayerData =
                        (ru.defea.oneblockultima.capability.OneBlockPlayerData) oldData;
                ru.defea.oneblockultima.capability.OneBlockPlayerData newPlayerData =
                        (ru.defea.oneblockultima.capability.OneBlockPlayerData) newData;

                newPlayerData.setCurrency(oldPlayerData.getCurrency());
                newPlayerData.getSetLevels().clear();
                newPlayerData.getSetLevels().putAll(oldPlayerData.getSetLevels());
            }
        }
    }
}
