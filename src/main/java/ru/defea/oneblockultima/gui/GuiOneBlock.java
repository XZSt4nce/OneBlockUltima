package ru.defea.oneblockultima.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.defea.oneblockultima.BlockSetConfigGui;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.util.BlockUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiOneBlock extends GuiContainer
{
    private static final int BUTTON_PREV_SET = 0;
    private static final int BUTTON_NEXT_SET = 1;
    private static final int BUTTON_SELECT_SET = 2;
    private static final int BUTTON_UPGRADE_SET = 3;
    private static final int BUTTON_TAB_SETS = 4;
    private static final int BUTTON_TAB_SETTINGS = 5;
    private static final int BUTTON_OPEN_CONFIG_EDITOR = 6;
    private static final int BUTTON_TOGGLE_FLUIDS = 7;
    private static final int BUTTON_TOGGLE_MOBS = 8;
    private static final int BUTTON_TOGGLE_CHESTS = 9;
    private static final int BUTTON_TOGGLE_SAPLINGS = 10;
    private static final int VIEW_SETS = 0;
    private static final int VIEW_SETTINGS = 1;

    private static final int GREEN_COLOR = 0x00EE00;
    private static final int ORANGE_COLOR = 0xFFAA00;
    private static final int RED_COLOR = 0xEE0000;

    private final ContainerOneBlock container;
    private final List<BlockSetConfig.BlockSetDefinition> visibleSets = new ArrayList<>();
    private int selectedSetIndex = 0;
    private GuiButton prevButton;
    private GuiButton nextButton;
    private GuiButton selectButton;
    private GuiButton upgradeButton;
    private GuiButton tabSetsButton;
    private GuiButton tabSettingsButton;
    private GuiButton openConfigEditorButton;
    private GuiButton toggleFluidButton;
    private GuiButton toggleMobsButton;
    private GuiButton toggleChestsButton;
    private GuiButton toggleSaplingsButton;
    private Boolean pendingDisableFluid = null;
    private Boolean pendingDisableMob = null;
    private Boolean pendingDisableChest = null;
    private Boolean pendingDisableSapling = null;
    private int activeView = VIEW_SETS;
    private int blockScroll = 0;
    private int mobScroll = 0;
    private int blockScrollNext = 0;
    private int mobScrollNext = 0;

    private BlockSetConfig.BlockEntryDefinition hoveredEntryLeft = null;
    private ItemStack hoveredStackLeft = ItemStack.EMPTY;
    private BlockSetConfig.MobEntryDefinition hoveredMobEntryLeft = null;
    private String hoveredMobNameLeft = null;

    private BlockSetConfig.BlockEntryDefinition hoveredEntryRight = null;
    private ItemStack hoveredStackRight = ItemStack.EMPTY;
    private BlockSetConfig.MobEntryDefinition hoveredMobEntryRight = null;
    private String hoveredMobNameRight = null;

    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png");

    private int cellSize = 18;
    private int cellPadding = 1;
    private int blockCols = 4;
    private int mobCols = 2;
    private final int SCROLLBAR_WIDTH = 6;
    private int panelGap = 8;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentWidth;
    private int leftPanelX;
    private int rightPanelX;
    private int tabRowY;
    private int textHeight;
    private int infoRowY;
    private int infoHeight;
    private final int buttonHeight = 20;
    private final int buttonGap = 6;
    private String clientActiveSetId = null;
    private boolean hasBrowsedSets = false;
    private int rowInterval;

    private final int INNER_PADDING = 6;
    private final int SECTION_GAP = 8;

    public GuiOneBlock(EntityPlayer player, World world, BlockPos generatorPos)
    {
        super(new ContainerOneBlock(player, world, generatorPos));
        this.container = (ContainerOneBlock) this.inventorySlots;
        this.xSize = 360;
        this.ySize = 280;
    }

    private void updateLayoutMetrics()
    {
        int horizontalMargin = Math.max(12, Math.min(24, xSize / 24));
        textHeight = fontRenderer.FONT_HEIGHT;

        tabRowY = textHeight * 3;
        int tabHeight = 24;
        infoRowY = tabRowY + tabHeight + textHeight;

        contentWidth = Math.max(120, xSize - horizontalMargin * 2);
        contentLeft = Math.max(horizontalMargin, (xSize - contentWidth) / 2);

        int infoInternalWidth = Math.max(80, contentWidth - 16);
        int infoLines = 0;
        if (activeView != VIEW_SETTINGS)
        {
            TileEntityOneBlockGenerator generator = container.getGenerator();
            String activeSetId = generator == null ? null : generator.getSelectedSetId();

            if (!visibleSets.isEmpty())
            {
                BlockSetConfig.BlockSetDefinition set = getBlockSetDefinition();
                if (set != null)
                {
                    int currentLevel = generator == null ? 0 : generator.getSetLevel(set.id);
                    String setTitle = getLocalizedSetName(set) + " " + I18n.format("gui.oneblockultima.lv") + currentLevel;
                    if (currentLevel <= 0)
                    {
                        setTitle += " (" + I18n.format("gui.oneblockultima.locked") + ")";
                    }
                    else if (set.id.equals(activeSetId))
                    {
                        setTitle += " (" + I18n.format("gui.oneblockultima.selected") + ")";
                    }
                    infoLines += fontRenderer.listFormattedStringToWidth(setTitle, infoInternalWidth).size();

                    String statusText;
                    if (currentLevel <= 0)
                    {
                        statusText = I18n.format("gui.oneblockultima.unlock_cost") + ": " + set.unlockCost;
                    }
                    else
                    {
                        BlockSetConfig.SetLevelDefinition nextLevel = set.getLevel(currentLevel + 1);
                        if (nextLevel != null)
                        {
                            statusText = I18n.format("gui.oneblockultima.upgrade_cost") + ": " + nextLevel.upgradeCost;
                        }
                        else
                        {
                            statusText = I18n.format("gui.oneblockultima.max_level");
                        }
                    }
                    infoLines += fontRenderer.listFormattedStringToWidth(statusText, infoInternalWidth).size();
                }
                else
                {
                    infoLines += 1;
                }
            }
            else
            {
                infoLines += 1;
            }
        }

        int infoButtonRows = activeView == VIEW_SETTINGS ? 3 : 2;
        int infoTextHeight = infoLines * fontRenderer.FONT_HEIGHT + Math.max(0, infoLines - 1) * 4;
        int infoButtonArea = infoButtonRows * buttonHeight + Math.max(0, infoButtonRows - 1) * buttonGap;
        infoHeight = infoTextHeight + infoButtonArea + textHeight;

        int contentHeight = ySize - infoRowY - infoHeight - 20;
        if (contentHeight < 100) {
            contentHeight = 100;
        }

        panelGap = Math.max(8, contentWidth / 40);
        panelWidth = Math.max(140, (contentWidth - panelGap) / 2);
        panelHeight = contentHeight - 10;
        leftPanelX = contentLeft;
        rightPanelX = contentLeft + panelWidth + panelGap;

        calculateColumns();
    }

    private void calculateColumns() {
        int availableWidth = Math.max(1, panelWidth - INNER_PADDING * 2);
        int halfWidth = (availableWidth - SECTION_GAP) / 2;
        int blockAreaWidth = Math.max(40, halfWidth - 8);
        int mobAreaWidth = Math.max(40, halfWidth - 8);

        int maxBlockCols = Math.max(2, blockAreaWidth / 20);
        blockCols = Math.min(6, maxBlockCols);

        mobCols = Math.min(blockCols, 3);

        cellPadding = Math.max(1, Math.min(2, blockAreaWidth / 80));
        cellSize = Math.max(14, Math.min(20, (blockAreaWidth - (blockCols - 1) * cellPadding) / blockCols));
        cellSize = Math.max(14, Math.min(20, (mobAreaWidth - (mobCols - 1) * cellPadding) / mobCols));
    }

    private int getBlocksAreaWidth() {
        return blockCols * (cellSize + cellPadding) - cellPadding;
    }

    private int getMobsAreaWidth() {
        return mobCols * (cellSize + cellPadding) - cellPadding;
    }

    private int getMobsStartX(int panelX) {
        return panelX + INNER_PADDING + getBlocksAreaWidth() + SECTION_GAP + 8;
    }

    private int getGridStartY(int panelY) {
        return panelY + rowInterval * 2;
    }

    private int getAreaHeight() {
        return panelHeight - rowInterval * 2 - cellSize;
    }

    private void renderLevelPanel(BlockSetConfig.SetLevelDefinition levelDefinition, int panelX, int panelY, boolean isLeft, int mouseX, int mouseY)
    {
        if (levelDefinition == null) return;

        int localBlockScroll = isLeft ? blockScroll : blockScrollNext;
        int localMobScroll = isLeft ? mobScroll : mobScrollNext;

        int blocksAreaWidth = getBlocksAreaWidth();
        int mobsAreaWidth = getMobsAreaWidth();
        int mobsStartX = getMobsStartX(panelX);
        int gridStartY = getGridStartY(panelY);
        int areaHeight = getAreaHeight();

        fontRenderer.drawString(I18n.format(isLeft ? "gui.oneblockultima.current_level" : "gui.oneblockultima.next_level") + ": " + levelDefinition.level,
                panelX + INNER_PADDING, panelY + 4, 0xA0B0C0);
        fontRenderer.drawString(I18n.format("gui.oneblockultima.possible_blocks") + ": ",
                panelX + INNER_PADDING, panelY + rowInterval, 0xA0B0C0);

        if (levelDefinition.blocks != null && !levelDefinition.blocks.isEmpty())
        {
            int total = levelDefinition.blocks.size();
            int rows = (total + blockCols - 1) / blockCols;

            int visibleRows = Math.min(rows, Math.max(1, areaHeight / (cellSize + cellPadding)));
            int maxScroll = Math.max(0, rows - visibleRows);
            if (localBlockScroll > maxScroll) localBlockScroll = maxScroll;
            if (isLeft)
            {
                blockScroll = localBlockScroll;
            }
            else
            {
                blockScrollNext = localBlockScroll;
            }

            int localMouseX = mouseX - guiLeft;
            int localMouseY = mouseY - guiTop;
            int hoveredIndex = -1;
            BlockSetConfig.BlockEntryDefinition hoveredEntry = null;
            ItemStack hoveredStack = ItemStack.EMPTY;

            // Рисуем блоки
            for (int row = 0; row < visibleRows; row++)
            {
                for (int col = 0; col < blockCols; col++)
                {
                    int realRow = row + localBlockScroll;
                    int index = realRow * blockCols + col;
                    if (index >= total) break;

                    BlockSetConfig.BlockEntryDefinition entry = levelDefinition.blocks.get(index);
                    if (entry == null) continue;

                    int cellX = panelX + INNER_PADDING + col * (cellSize + cellPadding);
                    int cellY = gridStartY + row * (cellSize + cellPadding);

                    if (cellY + cellSize < gridStartY || cellY > gridStartY + areaHeight) {
                        continue;
                    }

                    boolean isHovered = false;
                    if (localMouseX >= cellX && localMouseX < cellX + cellSize &&
                            localMouseY >= cellY && localMouseY < cellY + cellSize)
                    {
                        isHovered = true;
                        hoveredIndex = index;
                        hoveredEntry = entry;
                    }

                    ItemStack stack = entry.getPickBlock();
                    if (stack.isEmpty())
                    {
                        net.minecraft.block.Block blockForIcon = entry.resolveBlock();
                        Item itemForIcon = null;
                        if (blockForIcon != null)
                        {
                            itemForIcon = Item.getItemFromBlock(blockForIcon);
                        }
                        if (itemForIcon == null || itemForIcon == Items.AIR)
                        {
                            try { itemForIcon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.registry)); } catch (Exception ignored) { }
                        }
                        if (itemForIcon != null && itemForIcon != Items.AIR)
                        {
                            try {
                                stack = new ItemStack(itemForIcon, 1, entry.meta);
                                if (entry.nbtTags != null && !entry.nbtTags.hasNoTags()) {
                                    if (entry.registry != null && entry.registry.toLowerCase().contains("forestry")) {
                                        stack.setTagCompound(entry.nbtTags.copy());
                                    }
                                }
                            } catch (Exception ignored) {
                                stack = new ItemStack(itemForIcon);
                            }
                        }
                    }

                    net.minecraft.block.Block blockForIcon = entry.resolveBlock();

                    int bgColor = isHovered ? 0xFF3F5060 : 0xFF2A2F34;
                    int borderColor = isHovered ? 0xFFFFFFFF : 0xFF3A3F44;

                    drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, bgColor);
                    drawRect(cellX, cellY, cellX + cellSize, cellY + 1, borderColor);
                    drawRect(cellX, cellY + cellSize - 1, cellX + cellSize, cellY + cellSize, borderColor);
                    drawRect(cellX, cellY, cellX + 1, cellY + cellSize, borderColor);
                    drawRect(cellX + cellSize - 1, cellY, cellX + cellSize, cellY + cellSize, borderColor);

                    if (!stack.isEmpty())
                    {
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.enableDepth();

                        int iconX = cellX + (cellSize - 16) / 2;
                        int iconY = cellY + (cellSize - 16) / 2;

                        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
                        itemRender.renderItemAndEffectIntoGUI(stack, iconX, iconY);

                        GlStateManager.disableDepth();
                        RenderHelper.disableStandardItemLighting();
                    }
                    else
                    {
                        if (blockForIcon != null)
                        {
                            net.minecraft.block.state.IBlockState state = null;
                            try { state = blockForIcon.getStateFromMeta(entry.meta); } catch (Exception ex) { try { state = blockForIcon.getDefaultState(); } catch (Exception ignored) { } }
                            int iconX = cellX + (cellSize - 12) / 2;
                            int iconY = cellY + (cellSize - 12) / 2;

                            Fluid fluid = null;
                            if (blockForIcon instanceof IFluidBlock)
                            {
                                try { fluid = ((IFluidBlock) blockForIcon).getFluid(); } catch (Exception ignored) { }
                            }
                            if (fluid == null)
                            {
                                try { fluid = FluidRegistry.lookupFluidForBlock(blockForIcon); } catch (Exception ignored) { }
                            }

                            if (fluid != null)
                            {
                                renderFluidSprite(fluid, iconX, iconY, 12, 12);
                            }
                            else if (state != null)
                            {
                                renderBlockModelToGUI(state, iconX + 8, iconY + 8, 16);
                            }
                        }
                    }

                    int chance = entry.getChance();
                    String percent = chance + "%";
                    int percentWidth = fontRenderer.getStringWidth(percent);
                    int percentX = cellX + (cellSize - percentWidth) / 2;
                    int percentY = cellY + cellSize - fontRenderer.FONT_HEIGHT - 1;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    int color = chance < 10 ? RED_COLOR : chance < 20 ? ORANGE_COLOR : GREEN_COLOR;
                    fontRenderer.drawStringWithShadow(percent, percentX, percentY, color);

                    if (isHovered && hoveredEntry != null)
                    {
                        if (isLeft)
                        {
                            hoveredEntryLeft = hoveredEntry;
                            hoveredStackLeft = stack;
                        }
                        else
                        {
                            hoveredEntryRight = hoveredEntry;
                            hoveredStackRight = stack;
                        }
                    }
                }
            }

            // Скроллбар для блоков - рисуем его ВНУТРИ области
            int maxScrollBlocks = Math.max(0, rows - visibleRows);
            if (maxScrollBlocks > 0)
            {
                int blockScrollbarX = panelX + INNER_PADDING + blocksAreaWidth + 2;
                renderScrollBar(blockScrollbarX, gridStartY, areaHeight, localBlockScroll, maxScrollBlocks);
            }

            // --- Мобы ---
            if (levelDefinition.mobs != null && !levelDefinition.mobs.isEmpty())
            {
                // Заголовок мобов - слева от сетки мобов
                fontRenderer.drawString(I18n.format("gui.oneblockultima.mobs") + ":",
                        mobsStartX, panelY + rowInterval, 0xA0B0C0);

                int mobTotal = levelDefinition.mobs.size();
                int mobRows = (mobTotal + mobCols - 1) / mobCols;
                int mobVisibleRows = Math.min(mobRows, Math.max(1, areaHeight / (cellSize + cellPadding)));
                int mobMaxScroll = Math.max(0, mobRows - mobVisibleRows);
                if (localMobScroll > mobMaxScroll) localMobScroll = mobMaxScroll;
                if (isLeft)
                {
                    mobScroll = localMobScroll;
                }
                else
                {
                    mobScrollNext = localMobScroll;
                }

                // Рисуем мобов
                for (int row = 0; row < mobVisibleRows; row++)
                {
                    for (int col = 0; col < mobCols; col++)
                    {
                        int realMobIndex = (row + localMobScroll) * mobCols + col;
                        if (realMobIndex >= mobTotal) break;

                        BlockSetConfig.MobEntryDefinition mobEntry = levelDefinition.mobs.get(realMobIndex);
                        if (mobEntry == null) continue;

                        int cellX = mobsStartX + col * (cellSize + cellPadding);
                        int cellY = gridStartY + row * (cellSize + cellPadding);

                        if (cellY + cellSize < gridStartY || cellY > gridStartY + areaHeight) continue;

                        boolean isMobHovered = (localMouseX >= cellX && localMouseX < cellX + cellSize &&
                                localMouseY >= cellY && localMouseY < cellY + cellSize);

                        int mobBgColor = isMobHovered ? 0xFF3F5060 : 0xFF2A2F34;
                        int mobBorderColor = isMobHovered ? 0xFFFFFFFF : 0xFF3A3F44;

                        drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, mobBgColor);
                        drawRect(cellX, cellY, cellX + cellSize, cellY + 1, mobBorderColor);
                        drawRect(cellX, cellY + cellSize - 1, cellX + cellSize, cellY + cellSize, mobBorderColor);
                        drawRect(cellX, cellY, cellX + 1, cellY + cellSize, mobBorderColor);
                        drawRect(cellX + cellSize - 1, cellY, cellX + cellSize, cellY + cellSize, mobBorderColor);

                        Entity testEntity = null;
                        try
                        {
                            World mcWorld = Minecraft.getMinecraft().world;
                            testEntity = EntityList.createEntityByIDFromName(new ResourceLocation(mobEntry.registry), mcWorld);
                            if (testEntity != null && testEntity instanceof net.minecraft.entity.EntityLivingBase)
                            {
                                int centerX = cellX + cellSize / 2;
                                int centerY = cellY + cellSize / 2 + cellPadding;
                                drawEntityOnScreen(centerX, centerY, 18, testEntity);
                            }
                        }
                        catch (Exception ignored) { }

                        if (isMobHovered)
                        {
                            if (isLeft)
                            {
                                hoveredMobEntryLeft = mobEntry;
                                hoveredMobNameLeft = null;
                                if (testEntity != null)
                                {
                                    try { hoveredMobNameLeft = testEntity.getDisplayName().getUnformattedText(); } catch (Exception ignored) { }
                                }
                            }
                            else
                            {
                                hoveredMobEntryRight = mobEntry;
                                hoveredMobNameRight = null;
                                if (testEntity != null)
                                {
                                    try { hoveredMobNameRight = testEntity.getDisplayName().getUnformattedText(); } catch (Exception ignored) { }
                                }
                            }
                        }

                        int chance = mobEntry.getChance();
                        String percent = chance + "%";
                        int pw = fontRenderer.getStringWidth(percent);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        int color = chance < 10 ? RED_COLOR : chance < 20 ? ORANGE_COLOR : GREEN_COLOR;
                        fontRenderer.drawStringWithShadow(percent, cellX + (cellSize - pw) / 2, cellY + cellSize - fontRenderer.FONT_HEIGHT - 2, color);
                    }
                }

                // Скроллбар для мобов
                if (mobMaxScroll > 0)
                {
                    int mobScrollbarX = mobsStartX + mobsAreaWidth + 2;
                    renderMobScrollBar(mobScrollbarX, gridStartY, areaHeight, localMobScroll, mobMaxScroll);
                }
            }
        }
    }

    // Все остальные методы остаются без изменений
    private static void drawEntityOnScreen(int posX, int posY, int scale, Entity entity)
    {
        if (!(entity instanceof EntityLivingBase)) return;
        EntityLivingBase ent = (EntityLivingBase) entity;

        float origRenderYawOffset = ent.renderYawOffset;
        float origPrevRotationYaw = ent.prevRotationYaw;
        float origRotationYaw = ent.rotationYaw;
        float origRotationYawHead = ent.rotationYawHead;
        float origPrevRotationYawHead = ent.prevRotationYawHead;
        float origRotationPitch = ent.rotationPitch;
        float origPrevRotationPitch = ent.prevRotationPitch;
        float origLimbSwing = ent.limbSwing;
        float origLimbSwingAmount = ent.limbSwingAmount;
        float origPrevLimbSwingAmount = ent.prevLimbSwingAmount;

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        try
        {
            GlStateManager.translate(posX, posY, 50.0F);
            float sizeScale = (float) scale * 0.45F;
            GlStateManager.scale(-sizeScale, sizeScale, sizeScale);
            GlStateManager.rotate(170.0F, 0.3F, 0.0F, 1.0F);

            ent.renderYawOffset = 0.0F;
            ent.prevRotationYaw = 0.0F;
            ent.rotationYaw = 0.0F;
            ent.rotationYawHead = 0.0F;
            ent.prevRotationYawHead = 0.0F;
            ent.rotationPitch = 0.0F;
            ent.prevRotationPitch = 0.0F;
            ent.limbSwing = 0.0F;
            ent.limbSwingAmount = 0.0F;
            ent.prevLimbSwingAmount = 0.0F;

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            float prevPlayerViewY = renderManager.playerViewY;
            renderManager.setPlayerViewY(180.0F);
            renderManager.setRenderShadow(false);
            renderManager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            renderManager.setRenderShadow(true);
            renderManager.setPlayerViewY(prevPlayerViewY);

            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.disableRescaleNormal();
        }
        catch (Exception ignored) { }
        finally
        {
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableColorMaterial();
            GlStateManager.disableLighting();

            ent.renderYawOffset = origRenderYawOffset;
            ent.prevRotationYaw = origPrevRotationYaw;
            ent.rotationYaw = origRotationYaw;
            ent.rotationYawHead = origRotationYawHead;
            ent.prevRotationYawHead = origPrevRotationYawHead;
            ent.rotationPitch = origRotationPitch;
            ent.prevRotationPitch = origPrevRotationPitch;
            ent.limbSwing = origLimbSwing;
            ent.limbSwingAmount = origLimbSwingAmount;
            ent.prevLimbSwingAmount = origPrevLimbSwingAmount;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void renderBlockModelToGUI(net.minecraft.block.state.IBlockState state, int x, int y, int size)
    {
        try
        {
            Minecraft mc = Minecraft.getMinecraft();
            BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 100.0F);
            GlStateManager.scale(size / 16.0F, size / 16.0F, size / 16.0F);
            GlStateManager.rotate(180F, 1F, 0F, 0F);
            RenderHelper.enableGUIStandardItemLighting();
            blockRenderer.renderBlockBrightness(state, 1.0F);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        catch (Exception ignored) { }
    }

    private static void renderFluidSprite(net.minecraftforge.fluids.Fluid fluid, int x, int y, int w, int h)
    {
        if (fluid == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        TextureAtlasSprite sprite = null;
        try
        {
            ResourceLocation tex = fluid.getStill();
            if (tex == null) tex = fluid.getFlowing();
            if (tex != null)
            {
                TextureMap map = mc.getTextureMapBlocks();
                sprite = map.getAtlasSprite(tex.toString());
            }
        }
        catch (Exception ignored) { }

        if (sprite == null) return;

        try
        {
            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            float minU = sprite.getMinU();
            float maxU = sprite.getMaxU();
            float minV = sprite.getMinV();
            float maxV = sprite.getMaxV();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(x, y + h, 0.0D).tex(minU, maxV).endVertex();
            buf.pos(x + w, y + h, 0.0D).tex(maxU, maxV).endVertex();
            buf.pos(x + w, y, 0.0D).tex(maxU, minV).endVertex();
            buf.pos(x, y, 0.0D).tex(minU, minV).endVertex();
            tess.draw();

            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
        catch (Exception ignored) { }
    }

    @Override
    public void initGui()
    {
        BlockSetConfig.reload();

        this.xSize = this.width - 40;
        this.ySize = this.height - 40;
        super.initGui();
        visibleSets.clear();
        for (BlockSetConfig.BlockSetDefinition set : BlockSetConfig.get().getSets())
        {
            if (set != null && set.isAvailable())
            {
                visibleSets.add(set);
            }
        }
        hasBrowsedSets = false;

        refreshActiveSetFromGenerator();

        String setIdToFind = clientActiveSetId;
        selectedSetIndex = 0;
        if (setIdToFind != null)
        {
            for (int i = 0; i < visibleSets.size(); i++)
            {
                if (visibleSets.get(i).id.equals(setIdToFind))
                {
                    selectedSetIndex = i;
                    break;
                }
            }
        }

        updateLayoutMetrics();
        buttonList.clear();
        rowInterval = fontRenderer.FONT_HEIGHT + 4;
        int tabGap = xSize / 24;
        int tabWidth = xSize / 5;
        int totalTabWidth = tabWidth * 2 + tabGap;
        int tabX = guiLeft + (xSize - totalTabWidth) / 2;
        int tabY = guiTop + tabRowY + textHeight / 2;
        int infoButtonsY = guiTop + infoRowY + rowInterval * 2;
        int leftButtonX = guiLeft + contentLeft;
        int selectWidth = contentWidth / 2 - buttonGap;
        int rightButtonX = guiLeft + contentLeft + contentWidth - selectWidth;
        int settingWidth = contentWidth;

        tabSetsButton = new GuiButton(BUTTON_TAB_SETS, tabX, tabY, tabWidth, buttonHeight, I18n.format("gui.oneblockultima.tabs.sets"));
        tabSettingsButton = new GuiButton(BUTTON_TAB_SETTINGS, tabX + tabWidth + tabGap, tabY, tabWidth, buttonHeight, I18n.format("gui.oneblockultima.tabs.settings"));
        prevButton = new GuiButton(BUTTON_PREV_SET, leftButtonX, infoButtonsY, 20, buttonHeight, "<");
        nextButton = new GuiButton(BUTTON_NEXT_SET, leftButtonX + 26, infoButtonsY, 20, buttonHeight, ">");
        selectButton = new GuiButton(BUTTON_SELECT_SET, leftButtonX, infoButtonsY + buttonHeight + buttonGap, selectWidth, buttonHeight, I18n.format("gui.oneblockultima.select"));
        upgradeButton = new GuiButton(BUTTON_UPGRADE_SET, rightButtonX, infoButtonsY + buttonHeight + buttonGap, selectWidth, buttonHeight, I18n.format("gui.oneblockultima.upgrade"));

        int settingsButtonStartY = guiTop + infoRowY;
        int buttonWidth = settingWidth / 2 - buttonGap;
        toggleFluidButton = new GuiButton(BUTTON_TOGGLE_FLUIDS, guiLeft + contentLeft, settingsButtonStartY, buttonWidth, buttonHeight, "");
        toggleMobsButton = new GuiButton(BUTTON_TOGGLE_MOBS, guiLeft + contentLeft + settingWidth / 2 + buttonGap, settingsButtonStartY, buttonWidth, buttonHeight, "");
        toggleChestsButton = new GuiButton(BUTTON_TOGGLE_CHESTS, guiLeft + contentLeft, settingsButtonStartY + buttonHeight + buttonGap, buttonWidth, buttonHeight, "");
        toggleSaplingsButton = new GuiButton(BUTTON_TOGGLE_SAPLINGS, guiLeft + contentLeft + settingWidth / 2 + buttonGap, settingsButtonStartY + buttonHeight + buttonGap, buttonWidth, buttonHeight, "");
        openConfigEditorButton = new GuiButton(BUTTON_OPEN_CONFIG_EDITOR, guiLeft + contentLeft + settingWidth / 2 + buttonGap, settingsButtonStartY + (buttonHeight + buttonGap) * 2, buttonWidth, buttonHeight, I18n.format("gui.oneblockultima.settings.open_editor"));

        buttonList.add(tabSetsButton);
        buttonList.add(tabSettingsButton);
        buttonList.add(prevButton);
        buttonList.add(nextButton);
        buttonList.add(selectButton);
        buttonList.add(upgradeButton);
        buttonList.add(openConfigEditorButton);
        buttonList.add(toggleFluidButton);
        buttonList.add(toggleMobsButton);
        buttonList.add(toggleChestsButton);
        buttonList.add(toggleSaplingsButton);
        updateViewButtons();
    }

    private void updateViewButtons()
    {
        boolean setsView = activeView == VIEW_SETS;
        if (prevButton != null) prevButton.visible = setsView;
        if (nextButton != null) nextButton.visible = setsView;
        if (selectButton != null) selectButton.visible = setsView;
        if (upgradeButton != null) upgradeButton.visible = setsView;
        if (tabSetsButton != null) tabSetsButton.enabled = !setsView;
        if (tabSettingsButton != null) tabSettingsButton.enabled = setsView;
        if (openConfigEditorButton != null) openConfigEditorButton.visible = !setsView;
        if (toggleFluidButton != null) toggleFluidButton.visible = !setsView;
        if (toggleMobsButton != null) toggleMobsButton.visible = !setsView;
        if (toggleChestsButton != null) toggleChestsButton.visible = !setsView;
        if (toggleSaplingsButton != null) toggleSaplingsButton.visible = !setsView;

        TileEntityOneBlockGenerator generator = container.getGenerator();
        if (toggleFluidButton != null)
        {
            boolean disabled = generator != null && generator.isDisableFluidGeneration();
            if (pendingDisableFluid != null)
            {
                disabled = pendingDisableFluid;
            }
            toggleFluidButton.displayString = I18n.format("gui.oneblockultima.settings.fluid") + ": " + (disabled ? I18n.format("gui.oneblockultima.settings.disabled") : I18n.format("gui.oneblockultima.settings.enabled"));
        }
        if (toggleMobsButton != null)
        {
            boolean disabled = generator != null && generator.isDisableMobGeneration();
            if (pendingDisableMob != null)
            {
                disabled = pendingDisableMob;
            }
            toggleMobsButton.displayString = I18n.format("gui.oneblockultima.settings.mobs") + ": " + (disabled ? I18n.format("gui.oneblockultima.settings.disabled") : I18n.format("gui.oneblockultima.settings.enabled"));
        }
        if (toggleChestsButton != null)
        {
            boolean disabled = generator != null && generator.isDisableChestGeneration();
            if (pendingDisableChest != null)
            {
                disabled = pendingDisableChest;
            }
            toggleChestsButton.displayString = I18n.format("gui.oneblockultima.settings.chests") + ": " + (disabled ? I18n.format("gui.oneblockultima.settings.disabled") : I18n.format("gui.oneblockultima.settings.enabled"));
        }
        if (toggleSaplingsButton != null)
        {
            boolean disabled = generator != null && generator.isDisableSaplingGeneration();
            if (pendingDisableSapling != null)
            {
                disabled = pendingDisableSapling;
            }
            toggleSaplingsButton.displayString = I18n.format("gui.oneblockultima.settings.saplings") + ": " + (disabled ? I18n.format("gui.oneblockultima.settings.disabled") : I18n.format("gui.oneblockultima.settings.enabled"));
        }
    }

    private void refreshActiveSetFromGenerator()
    {
        TileEntityOneBlockGenerator generator = container.getGenerator();
        if (generator == null)
        {
            return;
        }

        String activeSetId = generator.getSelectedSetId();
        if (activeSetId == null)
        {
            return;
        }

        if (!activeSetId.equals(clientActiveSetId))
        {
            clientActiveSetId = activeSetId;
            hasBrowsedSets = false;
        }
    }

    private String getLocalizedSetName(BlockSetConfig.BlockSetDefinition set)
    {
        if (set == null || set.id == null)
        {
            return "-";
        }

        String key = "gui.oneblockultima.set." + set.id;
        String localized = I18n.format(key);
        return localized.equals(key) ? set.id : localized;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (visibleSets.isEmpty())
        {
            return;
        }

        if (button.id == BUTTON_TAB_SETS)
        {
            activeView = VIEW_SETS;
            updateViewButtons();
        }
        else if (button.id == BUTTON_TAB_SETTINGS)
        {
            activeView = VIEW_SETTINGS;
            updateViewButtons();
        }
        else if (button.id == BUTTON_TOGGLE_FLUIDS)
        {
            boolean currentDisabled = container.getGenerator() != null && container.getGenerator().isDisableFluidGeneration();
            pendingDisableFluid = !currentDisabled;
            container.toggleFluidGeneration();
            updateViewButtons();
        }
        else if (button.id == BUTTON_TOGGLE_MOBS)
        {
            boolean currentDisabled = container.getGenerator() != null && container.getGenerator().isDisableMobGeneration();
            pendingDisableMob = !currentDisabled;
            container.toggleMobGeneration();
            updateViewButtons();
        }
        else if (button.id == BUTTON_TOGGLE_CHESTS)
        {
            boolean currentDisabled = container.getGenerator() != null && container.getGenerator().isDisableChestGeneration();
            pendingDisableChest = !currentDisabled;
            container.toggleChestGeneration();
            updateViewButtons();
        }
        else if (button.id == BUTTON_TOGGLE_SAPLINGS)
        {
            boolean currentDisabled = container.getGenerator() != null && container.getGenerator().isDisableSaplingGeneration();
            pendingDisableSapling = !currentDisabled;
            container.toggleSaplingGeneration();
            updateViewButtons();
        }
        else if (button.id == BUTTON_OPEN_CONFIG_EDITOR)
        {
            mc.displayGuiScreen(new BlockSetConfigGui(this));
        }
        else if (button.id == BUTTON_PREV_SET)
        {
            selectedSetIndex = (selectedSetIndex - 1 + visibleSets.size()) % visibleSets.size();
            hasBrowsedSets = true;
        }
        else if (button.id == BUTTON_NEXT_SET)
        {
            selectedSetIndex = (selectedSetIndex + 1) % visibleSets.size();
            hasBrowsedSets = true;
        }
        else if (button.id == BUTTON_SELECT_SET)
        {
            BlockSetConfig.BlockSetDefinition selectedSet = getBlockSetDefinition();
            if (selectedSet != null)
            {
                container.selectSet(selectedSet.id);
            }
        }
        else if (button.id == BUTTON_UPGRADE_SET)
        {
            BlockSetConfig.BlockSetDefinition selectedSet = getBlockSetDefinition();
            if (selectedSet != null)
            {
                container.upgradeSet(selectedSet.id);
            }
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        refreshActiveSetFromGenerator();
        TileEntityOneBlockGenerator generator = container.getGenerator();
        if (generator != null)
        {
            if (pendingDisableFluid != null && generator.isDisableFluidGeneration() == pendingDisableFluid)
            {
                pendingDisableFluid = null;
            }
            if (pendingDisableMob != null && generator.isDisableMobGeneration() == pendingDisableMob)
            {
                pendingDisableMob = null;
            }
            if (pendingDisableChest != null && generator.isDisableChestGeneration() == pendingDisableChest)
            {
                pendingDisableChest = null;
            }
            if (pendingDisableSapling != null && generator.isDisableSaplingGeneration() == pendingDisableSapling)
            {
                pendingDisableSapling = null;
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        hoveredEntryLeft = null;
        hoveredStackLeft = ItemStack.EMPTY;
        hoveredMobEntryLeft = null;
        hoveredMobNameLeft = null;
        hoveredEntryRight = null;
        hoveredStackRight = ItemStack.EMPTY;
        hoveredMobEntryRight = null;
        hoveredMobNameRight = null;

        updateLayoutMetrics();

        String title = I18n.format("tile.one_block_generator.name");
        drawCenteredString(fontRenderer, title, xSize / 2, textHeight, 0xFFFFFF);

        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(container.getPlayer());
        int currency = ru.defea.oneblockultima.event.ModEvents.getDisplayedCurrency(container.getPlayer());
        String balanceValue = String.valueOf(currency);
        int balanceWidth = fontRenderer.getStringWidth(balanceValue);
        int iconSize = 12;
        int padding = 4;
        int boxWidth = iconSize + 2 + balanceWidth + padding * 2;
        int boxX = xSize - 10 - boxWidth;
        int boxY = 4;
        int iconX = boxX + padding;
        int iconY = boxY + padding;
        int numberX = iconX + iconSize + 2;
        int numberY = iconY + 2;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(COIN_TEXTURE);
        drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        fontRenderer.drawString(balanceValue, numberX, numberY, 0xFFD700);

        TileEntityOneBlockGenerator generator = container.getGenerator();
        String activeSetId = generator == null ? null : generator.getSelectedSetId();
        String activeSetName = "-";
        if (activeSetId != null)
        {
            BlockSetConfig.BlockSetDefinition activeSetDef = BlockSetConfig.get().getSet(activeSetId);
            activeSetName = activeSetDef == null ? activeSetId : getLocalizedSetName(activeSetDef);
        }

        if (activeView != VIEW_SETTINGS)
        {
            String activeSetString = I18n.format("gui.oneblockultima.active_set");
            int rightTextX = contentWidth - fontRenderer.getStringWidth(activeSetString);
            fontRenderer.drawString(activeSetString + ":", rightTextX, infoRowY, 0xA0B0C0);
            fontRenderer.drawString(activeSetName, rightTextX, infoRowY + fontRenderer.FONT_HEIGHT + 2, 0xFFFFFF);

            // Центральная часть - условия разблокировки для выбранного набора
            if (!visibleSets.isEmpty())
            {
                BlockSetConfig.BlockSetDefinition set = getBlockSetDefinition();
                if (set != null)
                {
                    if (data != null)
                    {
                        int currentLevel = generator == null ? 0 : generator.getSetLevel(set.id);
                        // Показываем условия только если набор НЕ разблокирован
                        if (currentLevel <= 0 && set.unlockConditions != null &&
                                !set.unlockConditions.conditions.isEmpty())
                        {
                            // Вычисляем центр
                            int totalWidth = contentWidth;
                            int centerX = contentLeft + totalWidth / 2;

                            // Находим максимальную ширину текста условий
                            int maxConditionWidth = 0;
                            for (BlockSetConfig.UnlockConditionDefinition condition : set.unlockConditions.conditions)
                            {
                                if (condition == null) continue;
                                String text = " - " + formatUnlockCondition(condition, data, generator) + " ✓";
                                int width = fontRenderer.getStringWidth(text);
                                if (width > maxConditionWidth) maxConditionWidth = width;
                            }

                            // Добавляем отступы для заголовка
                            String unlockConditionsTitle = I18n.format("gui.oneblockultima.unlock_conditions");
                            int titleWidth = fontRenderer.getStringWidth(unlockConditionsTitle);
                            if (titleWidth > maxConditionWidth) maxConditionWidth = titleWidth;

                            // Увеличиваем немного для комфорта
                            maxConditionWidth += 20;

                            // Рисуем условия по центру
                            int conditionsX = centerX - maxConditionWidth / 2;
                            int conditionsY = infoRowY;
                            drawUnlockConditions(set, conditionsX, conditionsY, maxConditionWidth, generator);
                        }
                    }
                }
            }
        }

        if (activeView == VIEW_SETTINGS)
        {
            return;
        }

        if (!visibleSets.isEmpty())
        {
            BlockSetConfig.BlockSetDefinition set = getBlockSetDefinition();

            if (set == null) return;

            int currentLevel = generator == null ? 0 : generator.getSetLevel(set.id);
            boolean isActiveSet = (set.id.equals(clientActiveSetId));

            int setColor = currentLevel <= 0 ? 0xFF7D7D : isActiveSet ? 0x7CEC9F : 0xFFFFFF;
            String setTitle = getLocalizedSetName(set) + " " + I18n.format("gui.oneblockultima.lv") + currentLevel;
            if (isActiveSet)
            {
                setTitle += " (" + I18n.format("gui.oneblockultima.selected") + ")";
            }
            else if (currentLevel <= 0)
            {
                setTitle += " (" + I18n.format("gui.oneblockultima.locked") + ")";
            }
            int setTitleY = infoRowY;
            fontRenderer.drawString(setTitle, contentLeft, setTitleY, setColor);

            int statusY = setTitleY + rowInterval;
            if (currentLevel <= 0)
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.unlock_cost") + ": " + set.unlockCost, contentLeft + 4, statusY, 0xC0C0C0);
            }
            else
            {
                BlockSetConfig.SetLevelDefinition nextLevel = set.getLevel(currentLevel + 1);
                if (nextLevel != null)
                {
                    fontRenderer.drawString(I18n.format("gui.oneblockultima.upgrade_cost") + ": " + nextLevel.upgradeCost, contentLeft + 4, statusY, 0xC0C0C0);
                }
                else
                {
                    fontRenderer.drawString(I18n.format("gui.oneblockultima.max_level"), contentLeft + 4, statusY, 0xC0C0C0);
                }
            }

            if (selectButton != null)
            {
                selectButton.displayString = currentLevel <= 0 ? I18n.format("gui.oneblockultima.locked") : (isActiveSet ? I18n.format("gui.oneblockultima.selected") : I18n.format("gui.oneblockultima.select"));
                selectButton.enabled = currentLevel > 0 && !isActiveSet;
            }
            if (upgradeButton != null)
            {
                BlockSetConfig.SetLevelDefinition nextLevel = set.getLevel(currentLevel + 1);
                if (currentLevel <= 0)
                {
                    upgradeButton.displayString = I18n.format("gui.oneblockultima.unlock");
                    upgradeButton.enabled = true;
                }
                else if (nextLevel != null)
                {
                    upgradeButton.displayString = I18n.format("gui.oneblockultima.upgrade");
                    upgradeButton.enabled = true;
                }
                else
                {
                    upgradeButton.displayString = I18n.format("gui.oneblockultima.max");
                    upgradeButton.enabled = false;
                }
            }

            boolean canShowCurrent = currentLevel > 0;
            BlockSetConfig.SetLevelDefinition currentDef = canShowCurrent ? set.getLevel(currentLevel) : null;
            int panelY = infoRowY + infoHeight;
            if (canShowCurrent)
            {
                renderLevelPanel(currentDef, leftPanelX, panelY, true, mouseX, mouseY);
            }
            int rightStartX = canShowCurrent ? rightPanelX : leftPanelX;
            BlockSetConfig.SetLevelDefinition nextDef = set.getLevel(currentLevel <= 0 ? 1 : currentLevel + 1);
            renderLevelPanel(nextDef, rightStartX, panelY, false, mouseX, mouseY);

            if (canShowCurrent && nextDef != null)
            {
                int separatorX = leftPanelX + panelWidth + panelGap / 2;
                int separatorTop = infoHeight + infoRowY + 5;
                int separatorBottom = ySize - 5;
                drawRect(separatorX, separatorTop, separatorX + 1, separatorBottom, 0xFF4C5560);
            }

            if (hoveredEntryLeft == null && hoveredStackLeft.isEmpty() && hoveredMobEntryLeft == null &&
                    hoveredEntryRight == null && hoveredStackRight.isEmpty() && hoveredMobEntryRight == null) {
                return; // Нечего показывать
            }

            if (!hoveredStackLeft.isEmpty())
            {
                java.util.List<String> tooltip = hoveredStackLeft.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + (hoveredEntryLeft != null ? hoveredEntryLeft.getChance() : 0) + "%");
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
            else if (hoveredEntryLeft != null && hoveredEntryLeft.isFluid())
            {
                java.util.List<String> tooltip = BlockUtil.getTooltip(hoveredEntryLeft, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + hoveredEntryLeft.getChance() + "%");
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
            else if (hoveredMobEntryLeft != null)
            {
                java.util.List<String> tooltip = new java.util.ArrayList<String>();
                String mobName = hoveredMobNameLeft;
                if (mobName == null || mobName.isEmpty())
                {
                    try
                    {
                        String translationKey = EntityList.getTranslationName(new ResourceLocation(hoveredMobEntryLeft.registry));
                        if (translationKey != null && !translationKey.isEmpty())
                        {
                            mobName = I18n.format(translationKey);
                        }
                    }
                    catch (Exception ignored) { }
                }
                if (mobName == null || mobName.isEmpty())
                {
                    mobName = hoveredMobEntryLeft.registry;
                }
                tooltip.add(mobName);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + hoveredMobEntryLeft.getChance() + "%");
                if (hoveredMobEntryLeft.count > 1)
                {
                    tooltip.add("x" + hoveredMobEntryLeft.count);
                }
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
            else if (!hoveredStackRight.isEmpty())
            {
                java.util.List<String> tooltip = hoveredStackRight.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + (hoveredEntryRight != null ? hoveredEntryRight.getChance() : 0) + "%");
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
            else if (hoveredEntryRight != null && hoveredEntryRight.isFluid())
            {
                java.util.List<String> tooltip = BlockUtil.getTooltip(hoveredEntryRight, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + hoveredEntryRight.getChance() + "%");
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
            else if (hoveredMobEntryRight != null)
            {
                java.util.List<String> tooltip = new java.util.ArrayList<String>();
                String mobName = hoveredMobNameRight;
                if (mobName == null || mobName.isEmpty())
                {
                    try
                    {
                        String translationKey = EntityList.getTranslationName(new ResourceLocation(hoveredMobEntryRight.registry));
                        if (translationKey != null && !translationKey.isEmpty())
                        {
                            mobName = I18n.format(translationKey);
                        }
                    }
                    catch (Exception ignored) { }
                }
                if (mobName == null || mobName.isEmpty())
                {
                    mobName = hoveredMobEntryRight.registry;
                }
                tooltip.add(mobName);
                tooltip.add(I18n.format("gui.oneblockultima.chance") + ": " + hoveredMobEntryRight.getChance() + "%");
                if (hoveredMobEntryRight.count > 1)
                {
                    tooltip.add("x" + hoveredMobEntryRight.count);
                }
                drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRenderer);
            }
        }
    }

    private void drawUnlockConditions(BlockSetConfig.BlockSetDefinition set, int x, int y, int maxWidth, TileEntityOneBlockGenerator generator)
    {
        if (set == null || set.unlockConditions == null || set.unlockConditions.conditions == null ||
                set.unlockConditions.conditions.isEmpty())
        {
            return;
        }

        // Получаем данные игрока
        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(container.getPlayer());
        if (data == null) return;

        // Проверяем, разблокирован ли уже набор
        int currentLevel = generator == null ? 0 : generator.getSetLevel(set.id);
        if (currentLevel > 0) return; // Если разблокирован - не показываем условия

        String prefix = "all".equalsIgnoreCase(set.unlockConditions.mode) ?
                I18n.format("gui.oneblockultima.conditions.require_all") :
                I18n.format("gui.oneblockultima.conditions.require_any");

        // Центрируем текст
        String title = I18n.format("gui.oneblockultima.unlock_conditions");
        int titleWidth = fontRenderer.getStringWidth(title);
        int startX = x + (maxWidth - titleWidth) / 2;
        fontRenderer.drawString(title, startX, y, 0xA0B0C0);
        y += fontRenderer.FONT_HEIGHT + 2;

        for (BlockSetConfig.UnlockConditionDefinition condition : set.unlockConditions.conditions)
        {
            if (condition == null) continue;

            String conditionText = formatUnlockCondition(condition, data, generator);
            boolean satisfied = condition.isSatisfied(data, generator);
            int color = satisfied ? 0x7CEC9F : 0xFF7D7D;
            String status = satisfied ? " ✓" : " ✗";
            String fullText = " - " + conditionText + status;

            // Центрируем каждое условие
            int textWidth = fontRenderer.getStringWidth(fullText);
            int textX = x + (maxWidth - textWidth) / 2;
            fontRenderer.drawString(fullText, textX, y, color);
            y += fontRenderer.FONT_HEIGHT + 1;
        }
    }

    // 2. Исправленный метод formatUnlockCondition
    private String formatUnlockCondition(BlockSetConfig.UnlockConditionDefinition condition, IOneBlockPlayerData data, TileEntityOneBlockGenerator generator)
    {
        if (condition == null) return "";

        switch (condition.type == null ? "" : condition.type.toLowerCase(Locale.ROOT))
        {
            case "set_level":
                String setId = condition.setId != null ? condition.setId : "?";
                BlockSetConfig.BlockSetDefinition set = BlockSetConfig.get().getSet(setId);
                String setName = set != null ? getLocalizedSetName(set) : setId;
                int levelValue = generator == null ? data.getSetLevel(setId) : generator.getSetLevel(setId);
                return I18n.format("gui.oneblockultima.condition.set_level", setName, levelValue, condition.level);
            case "broken_blocks":
                String targetSetId = condition.setId != null ? condition.setId : "?";
                BlockSetConfig.BlockSetDefinition targetSet = BlockSetConfig.get().getSet(targetSetId);
                String targetSetName = targetSet != null ? getLocalizedSetName(targetSet) : targetSetId;
                return I18n.format("gui.oneblockultima.condition.broken_blocks", String.format("%d/%d", data.getBrokenBlocksCount(targetSetId), condition.count), targetSetName);
            case "broken_blocks_total":
                return I18n.format("gui.oneblockultima.condition.broken_blocks_total", String.format("%s/%s", data.getBrokenBlocksCount(), condition.count));
            default:
                return I18n.format("gui.oneblockultima.condition.unknown", condition.type);
        }
    }

    private BlockSetConfig.BlockSetDefinition getBlockSetDefinition() {
        if (selectedSetIndex >= 0 && selectedSetIndex < visibleSets.size())
        {
            return visibleSets.get(selectedSetIndex);
        }

        return null;
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0)
        {
            int delta = d > 0 ? -1 : 1;
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

            int localMouseX = mouseX - guiLeft;
            int localMouseY = mouseY - guiTop;

            updateLayoutMetrics();

            if (!visibleSets.isEmpty())
            {
                BlockSetConfig.BlockSetDefinition set = visibleSets.get(selectedSetIndex);
                TileEntityOneBlockGenerator generator = container.getGenerator();
                int currentLevel = generator == null ? 0 : generator.getSetLevel(set.id);
                boolean canShowCurrent = currentLevel > 0;

                int panelY = infoRowY + infoHeight;

                int blocksAreaWidth = getBlocksAreaWidth();
                int mobsAreaWidth = getMobsAreaWidth();
                int gridStartY = getGridStartY(panelY);
                int areaHeight = getAreaHeight();

                if (canShowCurrent)
                {
                    if (localMouseX >= leftPanelX + INNER_PADDING &&
                            localMouseX <= leftPanelX + INNER_PADDING + blocksAreaWidth &&
                            localMouseY >= gridStartY && localMouseY <= gridStartY + areaHeight)
                    {
                        int maxScroll = getMaxBlockScroll(set, currentLevel);
                        blockScroll = Math.max(0, Math.min(blockScroll + delta, maxScroll));
                        return;
                    }

                    int mobsStartX = getMobsStartX(leftPanelX);
                    if (localMouseX >= mobsStartX &&
                            localMouseX <= mobsStartX + mobsAreaWidth &&
                            localMouseY >= gridStartY && localMouseY <= gridStartY + areaHeight)
                    {
                        int maxScroll = getMaxMobScroll(set, currentLevel);
                        mobScroll = Math.max(0, Math.min(mobScroll + delta, maxScroll));
                        return;
                    }
                }

                int rightStartX = canShowCurrent ? rightPanelX : leftPanelX;
                int nextLevel = currentLevel <= 0 ? 1 : currentLevel + 1;

                if (localMouseX >= rightStartX + INNER_PADDING &&
                        localMouseX <= rightStartX + INNER_PADDING + blocksAreaWidth &&
                        localMouseY >= gridStartY && localMouseY <= gridStartY + areaHeight)
                {
                    int maxScroll = getMaxBlockScroll(set, nextLevel);
                    blockScrollNext = Math.max(0, Math.min(blockScrollNext + delta, maxScroll));
                    return;
                }

                int rightMobsStartX = getMobsStartX(rightStartX);
                if (localMouseX >= rightMobsStartX &&
                        localMouseX <= rightMobsStartX + mobsAreaWidth &&
                        localMouseY >= gridStartY && localMouseY <= gridStartY + areaHeight)
                {
                    int maxScroll = getMaxMobScroll(set, nextLevel);
                    mobScrollNext = Math.max(0, Math.min(mobScrollNext + delta, maxScroll));
                    return;
                }
            }
        }
    }

    private int getMaxBlockScroll(BlockSetConfig.BlockSetDefinition set, int level)
    {
        if (set == null) return 0;
        BlockSetConfig.SetLevelDefinition levelDef = set.getLevel(level);
        if (levelDef == null || levelDef.blocks == null || levelDef.blocks.isEmpty()) return 0;

        int areaHeight = getAreaHeight();

        int total = levelDef.blocks.size();
        int rows = (total + blockCols - 1) / blockCols;
        int visibleRows = Math.min(rows, Math.max(1, areaHeight / (cellSize + cellPadding)));

        return Math.max(0, rows - visibleRows);
    }

    private int getMaxMobScroll(BlockSetConfig.BlockSetDefinition set, int level)
    {
        if (set == null) return 0;
        BlockSetConfig.SetLevelDefinition levelDef = set.getLevel(level);
        if (levelDef == null || levelDef.mobs == null || levelDef.mobs.isEmpty()) return 0;

        int areaHeight = getAreaHeight();

        int total = levelDef.mobs.size();
        int rows = (total + mobCols - 1) / mobCols;
        int visibleRows = Math.min(rows, Math.max(1, areaHeight / (cellSize + cellPadding)));

        return Math.max(0, rows - visibleRows);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        updateLayoutMetrics();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int titleBottom = guiTop + textHeight * 3;
        int tabsBottom = guiTop + tabRowY * 2;
        int infoBottom = guiTop + infoRowY + infoHeight;
        int setContentBottom = this.height - guiTop;

        if (activeView == VIEW_SETTINGS) {
            drawRect(guiLeft, guiTop, guiLeft + xSize, titleBottom, 0xFF22272E);
            drawRect(guiLeft, titleBottom, guiLeft + xSize, tabsBottom + (buttonHeight + buttonGap) * 3 + buttonGap, 0xFF252A30);
            drawRect(guiLeft + panelGap, titleBottom, guiLeft + xSize - panelGap, tabsBottom, 0xFF2E3A45);
        }
        else {
            drawRect(guiLeft, guiTop, guiLeft + xSize, titleBottom, 0xFF22272E);
            drawRect(guiLeft, titleBottom, guiLeft + xSize, infoBottom, 0xFF252A30);
            drawRect(guiLeft + panelGap, titleBottom, guiLeft + xSize - panelGap, tabsBottom, 0xFF2E3A45);
            drawRect(guiLeft, infoBottom, guiLeft + xSize, setContentBottom, 0xFF1F2328);
        }
    }

    private void renderScrollBar(int scrollX, int scrollY, int scrollHeight, int currentScroll, int maxScroll)
    {
        if (maxScroll <= 0) return;

        int barHeight = Math.max(10, (scrollHeight * scrollHeight) / (scrollHeight + maxScroll * cellSize));
        int barY = scrollY + (currentScroll * (scrollHeight - barHeight)) / maxScroll;

        drawRect(scrollX, scrollY, scrollX + SCROLLBAR_WIDTH, scrollY + scrollHeight, 0xFF2A2F34);
        drawRect(scrollX, barY, scrollX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF7A7F84);
    }

    private void renderMobScrollBar(int scrollX, int scrollY, int scrollHeight, int currentScroll, int maxScroll)
    {
        if (maxScroll <= 0) return;

        int barHeight = Math.max(10, (scrollHeight * scrollHeight) / (scrollHeight + maxScroll * cellSize));
        int barY = scrollY + (currentScroll * (scrollHeight - barHeight)) / maxScroll;

        drawRect(scrollX, scrollY, scrollX + SCROLLBAR_WIDTH, scrollY + scrollHeight, 0xFF2A2F34);
        drawRect(scrollX, barY, scrollX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF7A7F84);
    }
}