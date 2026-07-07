package ru.defea.oneblockultima.event;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
import ru.defea.oneblockultima.gui.GuiOneBlock;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.command.CommandAcceptGeneratorInvite;
import ru.defea.oneblockultima.command.CommandDeclineGeneratorInvite;
import ru.defea.oneblockultima.util.BlockUtil;
import ru.defea.oneblockultima.world.GeneratedBlockRegistry;
import ru.defea.oneblockultima.world.OneBlockWorldType;
import ru.defea.oneblockultima.world.SpawnConfigData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.defea.oneblockultima.block.BlockOneBlockGenerator.*;

@Mod.EventBusSubscriber(modid = OneBlockUltima.MODID)
public final class ModEvents
{
    private ModEvents()
    {
    }

    private static final java.util.Map<BlockPos, Boolean> processingBlocks = new java.util.HashMap<>();
    private static final Map<BlockPos, UUID> pendingGeneratorOwners = new HashMap<>();
    private static final Map<String, Long> lastAccessDeniedMessageTicks = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private static final Map<UUID, Float> displayedCurrencyMap = new HashMap<>();
    private static final Map<UUID, Integer> lastDisplayedCurrency = new HashMap<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Text event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiOneBlock)
        {
            return;
        }

        World world = mc.world;
        if (world == null || !(world.getWorldType() instanceof OneBlockWorldType))
        {
            return;
        }

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

        int currency = getDisplayedCurrency(player);

        String balanceValue = String.valueOf(currency);
        int textWidth = mc.fontRenderer.getStringWidth(balanceValue);
        int coinSize = 8;
        int spaceBetween = 2;
        int radius = 3;
        int vMargin = 5 + radius;
        int hMargin = 8 + radius;

        int x = event.getResolution().getScaledWidth() - 70 - textWidth;
        int y = coinSize + 12;

        int bgWidth = coinSize + textWidth + spaceBetween + hMargin * 2;
        int bgHeight = coinSize + vMargin * 2;
        int bgX = x - hMargin;
        int bgY = y - vMargin;

        drawRoundedRect(bgX, bgY, bgWidth, bgHeight, 5, 0x99333333);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png"));
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, coinSize, coinSize, coinSize, coinSize);
        Minecraft.getMinecraft().fontRenderer.drawString(balanceValue, x + coinSize + spaceBetween, y, 0xFFD700);
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

    public static int getDisplayedCurrency(EntityPlayer player)
    {
        if (player == null)
        {
            return 0;
        }

        UUID playerUUID = player.getUniqueID();
        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
        int targetCurrency = data == null ? 0 : data.getCurrency();

        Integer lastCurrency = lastDisplayedCurrency.get(playerUUID);
        if (lastCurrency == null)
        {
            displayedCurrencyMap.put(playerUUID, (float) targetCurrency);
            lastDisplayedCurrency.put(playerUUID, targetCurrency);
            return targetCurrency;
        }

        if (lastCurrency != targetCurrency)
        {
            lastDisplayedCurrency.put(playerUUID, targetCurrency);
        }

        float currentDisplayed = displayedCurrencyMap.getOrDefault(playerUUID, (float) targetCurrency);
        float newDisplayed = currentDisplayed + (targetCurrency - currentDisplayed) * 0.14f;
        if (Math.abs(targetCurrency - newDisplayed) < 0.01f)
        {
            newDisplayed = targetCurrency;
        }

        displayedCurrencyMap.put(playerUUID, newDisplayed);
        return Math.round(newDisplayed);
    }

    public static void sendAccessDeniedMessage(EntityPlayer player)
    {
        if (player == null || !(player instanceof EntityPlayerMP))
        {
            return;
        }

        ((EntityPlayerMP) player).sendMessage(new net.minecraft.util.text.TextComponentString("§c" + I18n.format("generator.access.denied")));
    }

    public static boolean trySendAccessDeniedMessage(UUID playerId, BlockPos pos, long worldTick)
    {
        if (playerId == null || pos == null)
        {
            return false;
        }

        String key = playerId + ":" + pos.toLong();
        Long lastTick = lastAccessDeniedMessageTicks.get(key);
        if (lastTick != null && lastTick == worldTick)
        {
            return false;
        }

        lastAccessDeniedMessageTicks.put(key, worldTick);
        return true;
    }

    public static boolean trySendAccessDeniedMessage(EntityPlayer player, BlockPos pos, long worldTick)
    {
        if (player == null || !(player instanceof EntityPlayerMP))
        {
            return false;
        }

        if (!trySendAccessDeniedMessage(player.getUniqueID(), pos, worldTick))
        {
            return false;
        }

        ((EntityPlayerMP) player).sendMessage(new net.minecraft.util.text.TextComponentString("§c" + I18n.format("generator.access.denied")));
        return true;
    }

    public static boolean ensureGeneratorAccess(World world, BlockPos pos, EntityPlayer player, TileEntityOneBlockGenerator generator)
    {
        if (generator == null || player == null)
        {
            return false;
        }

        if (world != null && pos != null)
        {
            applyPendingGeneratorOwner(world, pos);
        }

        return generator.hasAccess(player);
    }

    public static void refreshOpenGui()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null)
        {
            return;
        }

        mc.addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                if (mc.currentScreen instanceof GuiOneBlock)
                {
                    mc.currentScreen.initGui();
                }
            }
        });
    }

    private static void drawRoundedRect(int x, int y, int width, int height, int radius, int color)
    {
        Gui.drawRect(x + radius, y, x + width - radius, y + height, color);
        Gui.drawRect(x, y + radius, x + width, y + height - radius, color);

        for (int i = 0; i < radius; i++)
        {
            for (int j = 0; j < radius; j++)
            {
                // Проверяем, находится ли пиксель внутри круга
                if (i * i + j * j < radius * radius)
                {
                    int right = x + width - radius + i + 1;
                    int left = x + radius - i - 1;
                    int bottom = y + height - radius + j + 1;
                    int top = y + radius - j - 1;
                    // Верхний левый угол
                    Gui.drawRect(left, top, left + 1, top + 1, color);
                    // Верхний правый угол
                    Gui.drawRect(right - 1, top, right, top + 1, color);
                    // Нижний левый угол
                    Gui.drawRect(left, bottom - 1, left + 1, bottom, color);
                    // Нижний правый угол
                    Gui.drawRect(right - 1, bottom - 1, right, bottom, color);
                }
            }
        }
    }

    public static void registerPendingGeneratorOwner(World world, BlockPos pos, UUID playerId)
    {
        if (world == null || pos == null || playerId == null)
        {
            return;
        }

        pendingGeneratorOwners.put(pos, playerId);
    }

    public static void applyPendingGeneratorOwner(World world, BlockPos pos)
    {
        if (world == null || pos == null)
        {
            return;
        }

        UUID playerId = pendingGeneratorOwners.get(pos);
        if (playerId == null)
        {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityOneBlockGenerator))
        {
            return;
        }

        TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
        if (generator.assignOwnerForPlacement(playerId))
        {
            pendingGeneratorOwners.remove(pos);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (!(event.getGui() instanceof GuiCreateWorld))
        {
            return;
        }

        GuiCreateWorld screen = (GuiCreateWorld) event.getGui();
        WorldType worldType = getCreateWorldType(screen);
        if (worldType != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        String bonusLabel = I18n.format("createWorld.customize.bonusItems");
        String structuresLabel = I18n.format("createWorld.customize.mapFeatures");
        for (GuiButton button : event.getButtonList())
        {
            if (button == null || button.displayString == null)
            {
                continue;
            }
            if (button.displayString.equals(bonusLabel) || button.displayString.equals(structuresLabel))
            {
                button.visible = false;
                button.enabled = false;
            }
        }
    }

    private static Field createWorldTypeField;

    private static WorldType getCreateWorldType(GuiCreateWorld screen)
    {
        if (createWorldTypeField == null)
        {
            createWorldTypeField = findFieldByNames(GuiCreateWorld.class, "worldType", "field_146336_f", "field_146335_a");
            if (createWorldTypeField != null)
            {
                createWorldTypeField.setAccessible(true);
            }
        }

        if (createWorldTypeField == null)
        {
            return null;
        }

        try
        {
            Object value = createWorldTypeField.get(screen);
            if (value instanceof WorldType)
            {
                return (WorldType) value;
            }
        }
        catch (IllegalAccessException ignored)
        {
        }

        return null;
    }

    private static Field findFieldByNames(Class<?> clazz, String... names)
    {
        for (String name : names)
        {
            try
            {
                Field field = clazz.getDeclaredField(name);
                if (field != null)
                {
                    return field;
                }
            }
            catch (NoSuchFieldException ignored)
            {
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.player == null || event.player.world == null || event.player.world.isRemote)
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

        // Проверяем, есть ли у игрока своя точка возрождения
        BlockPos bedPos = player.getBedLocation(0); // 0 - dimension ID (Overworld)
        boolean hasSpawn = player.isSpawnForced(0);

        // Если у игрока нет своей точки возрождения (кровать или команда)
        //noinspection ConstantConditions
        BlockPos spawnPos = new BlockPos(FLUID_BARRIER_POS);
        if (bedPos == null || !hasSpawn)
        {
            world.setSpawnPoint(spawnPos);
            player.setSpawnPoint(spawnPos, true);
            player.setSpawnChunk(spawnPos, true, player.dimension);
            player.setPositionAndUpdate(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            player.connection.setPlayerLocation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
        }

        // Синхронизируем данные игрока после респавна
        PacketSyncPlayerData.sendToPlayer(player);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        String folderName = world.getSaveHandler().getWorldDirectory().getName();

        createWorldIcon(folderName);

        SpawnConfigData data = SpawnConfigData.get(world);
        if (!data.spawnInitialized)
        {
            world.setSpawnPoint(new BlockPos(FLUID_BARRIER_POS));
            data.spawnInitialized = true;
            data.markDirty();
        }

        if (world.getBlockState(GENERATOR_POS).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            if (world.getBlockState(FLUID_BARRIER_POS).getBlock() == Blocks.AIR)
            {
                world.setBlockState(FLUID_BARRIER_POS, ModBlocks.FLUID_BARRIER.getDefaultState(), 2);
                OneBlockUltima.getLogger().info("[Generator] BARRIER placed at {} on world load", FLUID_BARRIER_POS);
            }

            TileEntity tileEntity = world.getTileEntity(GENERATOR_POS);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(world);

                if (!registry.isGenerated(GENERATED_BLOCK_POS))
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

            TileEntity createdGenerator = world.getTileEntity(GENERATOR_POS);
            if (createdGenerator instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) createdGenerator;
                generator.setOwnerId(null);
            }

            if (world.getBlockState(FLUID_BARRIER_POS).getBlock() == Blocks.AIR)
            {
                world.setBlockState(FLUID_BARRIER_POS, ModBlocks.FLUID_BARRIER.getDefaultState(), 2);
                OneBlockUltima.getLogger().info("[Generator] BARRIER placed at {} on generator creation", FLUID_BARRIER_POS);
            }

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

    private static void createWorldIcon(String worldName)
    {
        File worldDir = new File(Minecraft.getMinecraft().mcDataDir, "saves" + File.separator + worldName);
        File iconFile = new File(worldDir, "icon.png");
        OneBlockUltima.getLogger().info("[MEOW]: {} {}", worldDir, iconFile);

        // Если иконка уже есть - не перезаписываем
        if (iconFile.exists())
        {
            return;
        }

        try (InputStream input = ModEvents.class.getResourceAsStream("/assets/oneblockultima/textures/gui/oneblock_logo.png"))
        {
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

            // Копируем файл
            Files.copy(input, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            OneBlockUltima.getLogger().info("Icon created for world: {}", worldName);
        }
        catch (IOException e)
        {
            OneBlockUltima.getLogger().warn("Failed to create icon.png for OneBlock world {}", worldName, e);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player == null || event.player.world == null || event.player.world.isRemote)
        {
            return;
        }

        World world = event.player.world;
        if (world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(GENERATOR_POS);
        if (tileEntity instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
            if (generator.isFree())
            {
                generator.tryAssignOwnerIfEligible(event.player.getUniqueID());
            }
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (world.provider.getDimension() != 0 || world.getWorldInfo().getTerrainType() != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }

        SpawnConfigData data = SpawnConfigData.get(world);
        BlockPos spawnPos = new BlockPos(FLUID_BARRIER_POS);
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

        IOneBlockPlayerData playerData = OneBlockPlayerDataProvider.get(player);
        if (playerData != null)
        {
            OneBlockPlayerDataProvider.loadFromEntity(player, playerData);
        }

        if (!data.spawnTeleportDone)
        {
            player.setPositionAndUpdate(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            player.connection.setPlayerLocation(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
            data.spawnTeleportDone = true;
            data.markDirty();
        }

        PacketSyncPlayerData.sendToPlayer(player);
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
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != Phase.END || event.world == null || event.world.isRemote)
        {
            return;
        }

        for (TileEntity tileEntity : event.world.loadedTileEntityList)
        {
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                ((TileEntityOneBlockGenerator) tileEntity).tickInvites();
            }
        }
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
    public static void onBlockPlace(BlockEvent.PlaceEvent event)
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

        BlockPos pos = event.getPos();
        IBlockState placedState = event.getPlacedBlock();
        if (placedState.getBlock() == ModBlocks.ONE_BLOCK_GENERATOR && event.getPlayer() != null)
        {
            registerPendingGeneratorOwner(world, pos, event.getPlayer().getUniqueID());

            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
                if (generator.isFree())
                {
                    generator.tryAssignOwnerIfEligible(event.getPlayer().getUniqueID());
                }
            }
        }

        IBlockState replacementState = BlockUtil.getReplacementStateForGeneratorPlacement(placedState, world.getBlockState(pos.down()));
        if (replacementState != placedState)
        {
            world.setBlockState(pos, replacementState, 3);
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

        boolean denied = false;
        BlockPos targetGeneratorPos = null;

        if (event.getWorld().getBlockState(clickedPos).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            targetGeneratorPos = clickedPos;
        }
        else
        {
            targetGeneratorPos = registry.getGeneratorPos(clickedPos);
        }

        if (targetGeneratorPos != null)
        {
            TileEntity generatorTile = event.getWorld().getTileEntity(targetGeneratorPos);
            if (generatorTile instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatorTile;
                if (generator.isFree() && generator.canBeClaimedBy(player.getUniqueID()))
                {
                    event.setCanceled(true);
                    GuiHandler.openClaimScreen(player, targetGeneratorPos);
                    return;
                }

                if (!ensureGeneratorAccess(event.getWorld(), targetGeneratorPos, player, generator))
                {
                    denied = true;
                }
            }
        }

        if (denied)
        {
            event.setCanceled(true);
            trySendAccessDeniedMessage(player, targetGeneratorPos != null ? targetGeneratorPos : clickedPos, event.getWorld().getTotalWorldTime());
            return;
        }

        if (targetGeneratorPos != null)
        {
            event.setCanceled(true);
            GuiHandler.open(player, targetGeneratorPos);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        BlockPos pos = event.getPos();
        World world = event.getWorld();
        EntityPlayer breaker = event.getPlayer();
        OneBlockUltima.getLogger().warn("[BreakDebug] onBlockBreak entered at {} by player={} remote={}", pos, breaker != null ? breaker.getName() : "null", world.isRemote);
        System.out.println("[BreakDebug] onBlockBreak entered at " + pos + " by player=" + (breaker != null ? breaker.getName() : "null") + " remote=" + world.isRemote);

        if (processingBlocks.getOrDefault(event.getPos(), false))
        {
            return;
        }

        TileEntity generatorTile = world.getTileEntity(pos);
        if (generatorTile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatorTile;
            if (!generator.hasAccess(event.getPlayer()))
            {
                event.setCanceled(true);
                trySendAccessDeniedMessage(event.getPlayer(), pos, world.getTotalWorldTime());
                return;
            }
        }

        if (world.getBlockState(pos.down(2)).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            world.setBlockState(pos, ModBlocks.FLUID_BARRIER.getDefaultState(), 2);
            OneBlockUltima.getLogger().info("[Generator] BARRIER placed at {} after block break", pos);
            event.setCanceled(true);
            return;
        }

        GeneratedBlockRegistry registry = GeneratedBlockRegistry.get(event.getWorld());
        GeneratedBlockRegistry.GeneratedBlockEntry entry = registry.getEntry(event.getPos());
        if (entry == null)
        {
            OneBlockUltima.getLogger().warn("[BreakDebug] No generated entry found for broken block {}", event.getPos());
            return;
        }

        OneBlockUltima.getLogger().warn("[BreakDebug] Generated entry found for {} -> generator {} set={} level={}", event.getPos(), entry.generatorPos, entry.setId, entry.level);

        TileEntity generatedTile = world.getTileEntity(entry.generatorPos);
        EntityPlayer player = event.getPlayer();
        if (generatedTile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatedTile;
            if (player == null)
            {
                OneBlockUltima.getLogger().info("[BreakDebug] Non-player break detected at {} for generator {}", event.getPos(), entry.generatorPos);
                if (!generator.canProcessNonPlayerBreak(world.getTotalWorldTime()))
                {
                    OneBlockUltima.getLogger().info("[BreakDebug] Non-player break blocked by cooldown for generator {} at tick {}", entry.generatorPos, world.getTotalWorldTime());
                    event.setCanceled(true);
                    return;
                }

                generator.markNonPlayerBreak(world.getTotalWorldTime());
                OneBlockUltima.getLogger().info("[BreakDebug] Non-player break accepted for generator {} at tick {}", entry.generatorPos, world.getTotalWorldTime());
            }
            else if (!generator.hasAccess(player))
            {
                event.setCanceled(true);
                trySendAccessDeniedMessage(player, entry.generatorPos, world.getTotalWorldTime());
                return;
            }
        }

        if (world.getBlockState(event.getPos()).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            processingBlocks.put(event.getPos(), true);
        }

        registry.remove(event.getPos());
        spawnMobOnBlockBreak(world, event.getPos(), entry);

        if (player == null && generatedTile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatedTile;
            OneBlockUltima.getLogger().info("[BreakDebug] Invoking non-player generation for generator {} at tick {}", entry.generatorPos, world.getTotalWorldTime());
            generator.tryGenerateBlock(true);
        }
        else if (player != null)
        {
            OneBlockUltima.getLogger().info("[BreakDebug] Player break detected at {} for generator {}", event.getPos(), entry.generatorPos);
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
                PacketSyncPlayerData.sendToPlayer(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
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
            OneBlockUltima.getLogger().warn("[BreakDebug] onHarvestDrops entered at {} with harvester=null", event.getPos());
        }
        else
        {
            OneBlockUltima.getLogger().warn("[BreakDebug] onHarvestDrops entered at {} by player={}", event.getPos(), player.getName());
        }

        // Получаем стандартные дропы
        java.util.List<net.minecraft.item.ItemStack> drops = new java.util.ArrayList<>(event.getDrops());

        // Проверяем, есть ли реальные дропы
        boolean hasRealDrops = false;
        for (net.minecraft.item.ItemStack drop : drops)
        {
            if (!drop.isEmpty())
            {
                hasRealDrops = true;
                break;
            }
        }

        // Если дропов нет - проверяем, должен ли блок дропаться вообще
        if (!hasRealDrops)
        {
            net.minecraft.block.Block block = event.getState().getBlock();

            // Проверяем, может ли блок быть добыт без специальных условий
            // noinspection ConstantConditions
            if (block != null && block != Blocks.AIR)
            {
                // Список блоков, которые не должны дропаться без инструмента
                boolean shouldDropBlock = isShouldDropBlock(block, player, drops);

                if (shouldDropBlock)
                {
                    net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(block);
                    // noinspection ConstantConditions
                    if (item != null && item != net.minecraft.init.Items.AIR)
                    {
                        net.minecraft.item.ItemStack blockStack = new net.minecraft.item.ItemStack(item, 1, block.getMetaFromState(event.getState()));
                        drops.add(blockStack);
                    }
                }
            }
        }

        if (player == null)
        {
            OneBlockUltima.getLogger().warn("[BreakDebug] Non-player harvest detected at {} for generator {}", event.getPos(), entry.generatorPos);
            TileEntity generatedTile = event.getWorld().getTileEntity(entry.generatorPos);
            if (generatedTile instanceof TileEntityOneBlockGenerator)
            {
                TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) generatedTile;
                generator.markNonPlayerBreak(event.getWorld().getTotalWorldTime());
                generator.tryGenerateBlock(true);
            }
            return;
        }

        // Очищаем все дропы
        event.getDrops().clear();

        // Добавляем дропы в инвентарь игрока
        for (net.minecraft.item.ItemStack drop : drops)
        {
            if (drop.isEmpty()) continue;

            net.minecraft.item.ItemStack remaining = drop.copy();
            if (!player.inventory.addItemStackToInventory(remaining))
            {
                net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(
                        event.getWorld(),
                        event.getPos().getX() + 0.5D,
                        event.getPos().getY() + 0.5D,
                        event.getPos().getZ() + 0.5D,
                        remaining
                );
                event.getWorld().spawnEntity(entityItem);
            }
        }
    }

    private static boolean isShouldDropBlock(Block block, EntityPlayer player, List<ItemStack> drops) {
        boolean shouldDropBlock = true;

        if (player == null)
        {
            return shouldDropBlock;
        }

        // Проверяем на траву
        if (block == Blocks.TALLGRASS || block == Blocks.DOUBLE_PLANT || block == Blocks.DEADBUSH)
        {
            ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem == null || heldItem.isEmpty() || heldItem.getItem() != net.minecraft.init.Items.SHEARS)
            {
                shouldDropBlock = false;
            }
        }

        // Проверяем на листья
        if (block == Blocks.LEAVES || block == Blocks.LEAVES2)
        {
            ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem == null || heldItem.isEmpty() || heldItem.getItem() != net.minecraft.init.Items.SHEARS)
            {
                boolean hasSapling = false;
                for (ItemStack drop : drops)
                {
                    if (!drop.isEmpty() && drop.getItem() == net.minecraft.init.Items.DYE)
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

        // Проверяем на паутину
        if (block == Blocks.WEB)
        {
            ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem == null || heldItem.isEmpty() || heldItem.getItem() != net.minecraft.init.Items.SHEARS)
            {
                shouldDropBlock = false;
            }
        }
        return shouldDropBlock;
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

        // Проверяем только блок над генератором
        if (world.getBlockState(event.getPos().down()).getBlock() == ModBlocks.ONE_BLOCK_GENERATOR)
        {
            return;
        }

        // Проверяем, не AIR ли теперь блок
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != Blocks.AIR)
        {
            // Блок существует
            processingBlocks.put(pos, false);
        }
    }

    private static void spawnMobOnBlockBreak(World world, BlockPos pos, GeneratedBlockRegistry.GeneratedBlockEntry entry)
    {
        TileEntity tile = world.getTileEntity(entry.generatorPos);
        if (tile instanceof TileEntityOneBlockGenerator)
        {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tile;
            if (generator.isDisableMobGeneration())
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Mob generation disabled on generator at {}", entry.generatorPos);
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
            Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(mobEntry.registry), world);
            if (entity == null)
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Failed to create entity for registry: {}", mobEntry.registry);
                continue;
            }

            OneBlockUltima.getLogger().info("[Mob Spawn] Successfully spawned mob: {}", mobEntry.registry);
            entity.setPosition(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);

            // Применяем NBT теги к мобу если они есть
            if (mobEntry.nbtTags != null && !mobEntry.nbtTags.hasNoTags())
            {
                OneBlockUltima.getLogger().info("[Mob Spawn] Applying NBT tags to mob: {}", mobEntry.nbtTags);
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
        IOneBlockPlayerData oldData = OneBlockPlayerDataProvider.get(event.getOriginal());
        IOneBlockPlayerData newData = OneBlockPlayerDataProvider.get(event.getEntityPlayer());
        if (oldData instanceof ru.defea.oneblockultima.capability.OneBlockPlayerData
                && newData instanceof ru.defea.oneblockultima.capability.OneBlockPlayerData)
        {
            ru.defea.oneblockultima.capability.OneBlockPlayerData oldPlayerData =
                    (ru.defea.oneblockultima.capability.OneBlockPlayerData) oldData;
            ru.defea.oneblockultima.capability.OneBlockPlayerData newPlayerData =
                    (ru.defea.oneblockultima.capability.OneBlockPlayerData) newData;

            newPlayerData.copyFrom(oldPlayerData);
            OneBlockPlayerDataProvider.saveToEntity(event.getEntityPlayer(), newPlayerData);
        }
    }
}
