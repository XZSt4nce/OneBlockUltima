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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
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
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;
import ru.defea.oneblockultima.util.BlockUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiOneBlock extends GuiContainer
{
    private static final int BUTTON_PREV_SET = 0;
    private static final int BUTTON_NEXT_SET = 1;
    private static final int BUTTON_SELECT_SET = 2;
    private static final int BUTTON_UPGRADE_SET = 3;

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
    private int blockScroll = 0;
    private int mobScroll = 0;
    private int blockScrollNext = 0;
    private int mobScrollNext = 0;
    // hovered state for left (current) panel exported for tooltips
    private int hoveredIndexLeft = -1;
    private BlockSetConfig.BlockEntryDefinition hoveredEntryLeft = null;
    private ItemStack hoveredStackLeft = ItemStack.EMPTY;
    private int hoveredMobIndexLeft = -1;
    private BlockSetConfig.MobEntryDefinition hoveredMobEntryLeft = null;
    private String hoveredMobNameLeft = null;

    private int hoveredIndexRight = -1;
    private BlockSetConfig.BlockEntryDefinition hoveredEntryRight = null;
    private ItemStack hoveredStackRight = ItemStack.EMPTY;
    private int hoveredMobIndexRight = -1;
    private BlockSetConfig.MobEntryDefinition hoveredMobEntryRight = null;
    private String hoveredMobNameRight = null;
    private static final ResourceLocation COIN_TEXTURE = new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png");
    private final int iconCols = 4;
    private final int cellSize = 20;
    private final int cellPadding = 1;
    private final int mobCellSize = 28;
    private final int SCROLLBAR_WIDTH = 6;
    private String clientActiveSetId = null;
    private boolean hasBrowsedSets = false;

    public GuiOneBlock(InventoryPlayer inventory, World world, BlockPos generatorPos)
    {
        super(new ContainerOneBlock(inventory, world, generatorPos));
        this.container = (ContainerOneBlock) this.inventorySlots;
        this.xSize = 360;
        this.ySize = 200;
    }

    private void renderLevelPanel(BlockSetConfig.BlockSetDefinition set, BlockSetConfig.SetLevelDefinition levelDefinition, int gridStartX, int gridStartY, boolean isLeft, int mouseX, int mouseY)
    {
        if (levelDefinition == null) return;

        int localBlockScroll = isLeft ? blockScroll : blockScrollNext;
        int localMobScroll = isLeft ? mobScroll : mobScrollNext;

        fontRenderer.drawString(I18n.format(isLeft ? "gui.oneblockultima.current_level" : "gui.oneblockultima.next_level") + ": " + levelDefinition.level, gridStartX, gridStartY - 24, 0xA0B0C0);

        if (levelDefinition.blocks != null && !levelDefinition.blocks.isEmpty())
        {
            int total = levelDefinition.blocks.size();
            int cols = iconCols;
            int rows = (total + cols - 1) / cols;

            int areaHeight = ySize - gridStartY - 10;
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

            for (int row = 0; row < visibleRows; row++)
            {
                for (int col = 0; col < cols; col++)
                {
                    int realRow = row + localBlockScroll;
                    int index = realRow * cols + col;
                    if (index >= total) break;

                    int cellX = gridStartX + col * (cellSize + cellPadding);
                    int cellY = gridStartY + row * (cellSize + cellPadding);

                    if (localMouseX >= cellX && localMouseX < cellX + cellSize &&
                            localMouseY >= cellY && localMouseY < cellY + cellSize)
                    {
                        hoveredIndex = index;
                        hoveredEntry = levelDefinition.blocks.get(index);
                        if (hoveredEntry != null)
                        {
                            net.minecraft.block.Block hoverBlock;
                            if (hoveredEntry.dropItem != null) {
                                ResourceLocation location = new ResourceLocation(hoveredEntry.dropItem);
                                hoverBlock = ForgeRegistries.BLOCKS.getValue(location);
                            }
                            else
                            {
                                hoverBlock = hoveredEntry.resolveBlock();
                            }

                            if (hoverBlock != null && hoverBlock != Blocks.AIR)
                            {
                                Item item = Item.getItemFromBlock(hoverBlock);
                                if (item != null && item != Items.AIR)
                                {
                                    try {
                                        hoveredStack = new ItemStack(item, 1, hoveredEntry.meta);
                                        // ПРИМЕНЯЕМ NBT ТЕГИ ДЛЯ ХОВЕРА
                                        if (hoveredEntry.nbtTags != null && !hoveredEntry.nbtTags.hasNoTags()) {
                                            if (hoveredEntry.registry != null && hoveredEntry.registry.toLowerCase().contains("forestry")) {
                                                hoveredStack.setTagCompound(hoveredEntry.nbtTags.copy());
                                            }
                                        }
                                    } catch (Exception ex) { hoveredStack = new ItemStack(item); }
                                }
                                else
                                {
                                    hoveredStack = ItemStack.EMPTY;
                                }
                            }
                            else
                            {
                                // Try resolving as an item (e.g. carrot, seeds)
                                try
                                {
                                    String itemRegistry = hoveredEntry.dropItem == null ? hoveredEntry.registry : hoveredEntry.dropItem;
                                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRegistry));
                                    if (item != null && item != Items.AIR)
                                    {
                                        try {
                                            hoveredStack = new ItemStack(item, 1, hoveredEntry.meta);
                                            // ПРИМЕНЯЕМ NBT ТЕГИ ДЛЯ ХОВЕРА
                                            if (hoveredEntry.nbtTags != null && !hoveredEntry.nbtTags.hasNoTags()) {
                                                if (hoveredEntry.registry != null && hoveredEntry.registry.toLowerCase().contains("forestry")) {
                                                    hoveredStack.setTagCompound(hoveredEntry.nbtTags.copy());
                                                }
                                            }
                                        } catch (Exception ex) { hoveredStack = new ItemStack(item); }
                                    }
                                    else
                                    {
                                        hoveredStack = ItemStack.EMPTY;
                                    }
                                }
                                catch (Exception ex)
                                {
                                    hoveredStack = ItemStack.EMPTY;
                                }
                            }
                        }
                        if (isLeft)
                        {
                            hoveredIndexLeft = hoveredIndex;
                            hoveredEntryLeft = hoveredEntry;
                            hoveredStackLeft = hoveredStack;
                        }
                        else
                        {
                            hoveredIndexRight = hoveredIndex;
                            hoveredEntryRight = hoveredEntry;
                            hoveredStackRight = hoveredStack;
                        }
                        break;
                    }
                }
                if (hoveredIndex >= 0) break;
            }

            for (int row = 0; row < visibleRows; row++)
            {
                for (int col = 0; col < cols; col++)
                {
                    int realRow = row + localBlockScroll;
                    int index = realRow * cols + col;
                    if (index >= total) break;

                    BlockSetConfig.BlockEntryDefinition entry = levelDefinition.blocks.get(index);
                    if (entry == null) continue;

                    // Use getPickBlock to get the correct item for GUI display
                    ItemStack stack = entry.getPickBlock();

// Fallback to old logic if getPickBlock fails
                    if (stack.isEmpty())
                    {
                        net.minecraft.block.Block blockForIcon = entry.resolveBlock();
                        Item itemForIcon = null;
                        if (blockForIcon != null)
                        {
                            itemForIcon = Item.getItemFromBlock(blockForIcon);
                        }
                        // If not a block item, try resolving directly as an item registry (carrot, seeds, etc.)
                        if (itemForIcon == null || itemForIcon == Items.AIR)
                        {
                            try { itemForIcon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.registry)); } catch (Exception ignored) { }
                        }
                        if (itemForIcon != null && itemForIcon != Items.AIR)
                        {
                            try {
                                stack = new ItemStack(itemForIcon, 1, entry.meta);
                                // ПРИМЕНЯЕМ NBT ТЕГИ ДЛЯ ИКОНКИ
                                if (entry.nbtTags != null && !entry.nbtTags.hasNoTags()) {
                                    // Для Forestry саженцев применяем NBT теги к иконке
                                    if (entry.registry != null && entry.registry.toLowerCase().contains("forestry")) {
                                        stack.setTagCompound(entry.nbtTags.copy());
                                    }
                                }
                            } catch (Exception ignored) {
                                stack = new ItemStack(itemForIcon);
                            }
                        }
                    }

                    // Declare blockForIcon for fallback rendering of fluids/blocks without item form
                    net.minecraft.block.Block blockForIcon = entry.resolveBlock();

                    int cellX = gridStartX + col * (cellSize + cellPadding);
                    int cellY = gridStartY + row * (cellSize + cellPadding);
                    boolean isHovered = (index == hoveredIndex);
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
                        // No ItemStack available (e.g. flowing liquids or blocks without item form) - render fluid sprite or block model
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
                    int percentX = gridStartX + col * (cellSize + cellPadding) + (cellSize - percentWidth) / 2;
                    int percentY = gridStartY + row * (cellSize + cellPadding) + cellSize - fontRenderer.FONT_HEIGHT - 1;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    int color = chance < 10 ? RED_COLOR : chance < 20 ? ORANGE_COLOR : GREEN_COLOR;
                    fontRenderer.drawStringWithShadow(percent, percentX, percentY, color);
                }
            }

            // mobs area
            int mobsAreaX = gridStartX + cols * (cellSize + cellPadding) + 14;
            if (levelDefinition.mobs != null && !levelDefinition.mobs.isEmpty())
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.mobs") + ":", mobsAreaX, gridStartY - 12, 0xA0B0C0);

                int mobTotal = levelDefinition.mobs.size();
                int mobCols = 2;
                int mobRows = (mobTotal + mobCols - 1) / mobCols;
                int mobVisibleRows = Math.min(mobRows, Math.max(1, (ySize - gridStartY - 10) / (mobCellSize + 4)));
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

                int mobCellX = mobsAreaX;
                int mobCellY = gridStartY;

                for (int row = 0; row < mobVisibleRows; row++)
                {
                    for (int col = 0; col < mobCols; col++)
                    {
                        int realMobIndex = (row + localMobScroll) * mobCols + col;
                        if (realMobIndex >= mobTotal) break;

                        BlockSetConfig.MobEntryDefinition mobEntry = levelDefinition.mobs.get(realMobIndex);
                        if (mobEntry == null) continue;

                        int cellX = mobCellX + col * (mobCellSize + 4);
                        int cellY = mobCellY + row * (mobCellSize + 4);
                        boolean isMobHovered = (localMouseX >= cellX && localMouseX < cellX + mobCellSize && localMouseY >= cellY && localMouseY < cellY + mobCellSize);
                        int mobBgColor = isMobHovered ? 0xFF3F5060 : 0xFF2A2F34;
                        int mobBorderColor = isMobHovered ? 0xFFFFFFFF : 0xFF3A3F44;

                        drawRect(cellX, cellY, cellX + mobCellSize, cellY + mobCellSize, mobBgColor);
                        drawRect(cellX, cellY, cellX + mobCellSize, cellY + 1, mobBorderColor);
                        drawRect(cellX, cellY + mobCellSize - 1, cellX + mobCellSize, cellY + mobCellSize, mobBorderColor);
                        drawRect(cellX, cellY, cellX + 1, cellY + mobCellSize, mobBorderColor);
                        drawRect(cellX + mobCellSize - 1, cellY, cellX + mobCellSize, cellY + mobCellSize, mobBorderColor);

                        Entity testEntity = null;
                        try
                        {
                            World mcWorld = Minecraft.getMinecraft().world;
                            testEntity = EntityList.createEntityByIDFromName(new ResourceLocation(mobEntry.registry), mcWorld);
                            if (testEntity != null && testEntity instanceof net.minecraft.entity.EntityLivingBase)
                            {
                                int centerX = cellX + mobCellSize / 2;
                                int centerY = cellY + mobCellSize / 2 + 4;
                                drawEntityOnScreen(centerX, centerY, 18, testEntity);
                            }
                        }
                        catch (Exception ignored) { }

                        if (isMobHovered)
                        {
                            if (isLeft)
                            {
                                hoveredMobIndexLeft = realMobIndex;
                                hoveredMobEntryLeft = mobEntry;
                                hoveredMobNameLeft = null;
                                if (testEntity != null)
                                {
                                    try { hoveredMobNameLeft = testEntity.getDisplayName().getUnformattedText(); } catch (Exception ignored) { }
                                }
                            }
                            else
                            {
                                hoveredMobIndexRight = realMobIndex;
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
                        fontRenderer.drawStringWithShadow(percent, cellX + (mobCellSize - pw) / 2, cellY + mobCellSize - fontRenderer.FONT_HEIGHT - 2, color);
                    }
                }
            }
        }

        // Draw scrollbars
        if (levelDefinition.blocks != null && !levelDefinition.blocks.isEmpty())
        {
            int total = levelDefinition.blocks.size();
            int cols = iconCols;
            int rows = (total + cols - 1) / cols;
            int areaHeight = ySize - gridStartY - 10;
            int visibleRows = Math.min(rows, Math.max(1, areaHeight / (cellSize + cellPadding)));
            int maxScroll = Math.max(0, rows - visibleRows);

            if (maxScroll > 0)
            {
                // Block scrollbar - right of blocks grid
                int blockScrollbarX = gridStartX + iconCols * (cellSize + cellPadding) + 2;
                renderScrollBar(blockScrollbarX, gridStartY, areaHeight, localBlockScroll, maxScroll);
            }

            // Mob scrollbar
            if (levelDefinition.mobs != null && !levelDefinition.mobs.isEmpty())
            {
                int mobTotal = levelDefinition.mobs.size();
                int mobCols = 2;
                int mobRows = (mobTotal + mobCols - 1) / mobCols;
                int mobVisibleRows = Math.min(mobRows, Math.max(1, (ySize - gridStartY - 10) / (mobCellSize + 4)));
                int mobMaxScroll = Math.max(0, mobRows - mobVisibleRows);

                if (mobMaxScroll > 0)
                {
                    // Mob scrollbar - right of mobs
                    int mobsAreaX = gridStartX + cols * (cellSize + cellPadding) + 14;
                    int mobScrollbarX = mobsAreaX + mobCols * mobCellSize + (mobCols - 1) * 4 + 2;
                    renderMobScrollBar(mobScrollbarX, gridStartY, areaHeight, localMobScroll, mobMaxScroll);
                }
            }
        }
    }

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
        // Перезагружаем конфиг перед открытием GUI
        BlockSetConfig.reload();
        
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

        buttonList.clear();
        prevButton = new GuiButton(BUTTON_PREV_SET, guiLeft + 10, guiTop + 85, 20, 20, "<");
        nextButton = new GuiButton(BUTTON_NEXT_SET, guiLeft + 36, guiTop + 85, 20, 20, ">");
        selectButton = new GuiButton(BUTTON_SELECT_SET, guiLeft + 10, guiTop + 109, 90, 20, "Select");
        upgradeButton = new GuiButton(BUTTON_UPGRADE_SET, guiLeft + xSize - 100, guiTop + 109, 90, 20, "Upgrade");
        buttonList.add(prevButton);
        buttonList.add(nextButton);
        buttonList.add(selectButton);
        buttonList.add(upgradeButton);
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

        // Если активный набор изменился
        if (!activeSetId.equals(clientActiveSetId))
        {
            clientActiveSetId = activeSetId;

            // Обновляем selectedSetIndex
            for (int i = 0; i < visibleSets.size(); i++)
            {
                if (visibleSets.get(i).id.equals(activeSetId))
                {
                    selectedSetIndex = i;
                    break;
                }
            }

            // Сбрасываем hasBrowsedSets, чтобы показать активный набор
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

        if (button.id == BUTTON_PREV_SET)
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
            String setId = visibleSets.get(selectedSetIndex).id;
            container.selectSet(setId);
        }
        else if (button.id == BUTTON_UPGRADE_SET)
        {
            String setId = visibleSets.get(selectedSetIndex).id;
            container.upgradeSet(setId);
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        refreshActiveSetFromGenerator();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String title = I18n.format("tile.one_block_generator.name");
        drawCenteredString(fontRenderer, title, xSize / 2, 8, 0xFFFFFF);

        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(container.getPlayer());
        int currency = data == null ? 0 : data.getCurrency();
        String balanceValue = String.valueOf(currency);
        int balanceWidth = fontRenderer.getStringWidth(balanceValue);
        int iconSize = 12;
        int padding = 4;
        int boxWidth = iconSize + 2 + balanceWidth + padding * 2;
        int boxHeight = iconSize + padding * 2;
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

        int infoStartY = 32;
        fontRenderer.drawString(I18n.format("gui.oneblockultima.active_set") + ":", 14, infoStartY, 0xA0B0C0);
        fontRenderer.drawString(activeSetName, 14, infoStartY + 12, 0xFFFFFF);

        if (!visibleSets.isEmpty())
        {
            BlockSetConfig.BlockSetDefinition set = getBlockSetDefinition();

            if (set == null) return;

            int currentLevel = data == null ? 0 : data.getSetLevel(set.id);
            // Используем clientActiveSetId для немедленного отклика
            boolean isActiveSet = (clientActiveSetId != null && set.id.equals(clientActiveSetId));

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
            fontRenderer.drawString(setTitle, 14, 60, setColor);

            int statusY = 72;
            if (currentLevel <= 0)
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.unlock_cost") + ": " + set.unlockCost, 14, statusY, 0xC0C0C0);
            }
            else
            {
                BlockSetConfig.SetLevelDefinition nextLevel = set.getLevel(currentLevel + 1);
                if (nextLevel != null)
                {
                    fontRenderer.drawString(I18n.format("gui.oneblockultima.upgrade_cost") + ": " + nextLevel.upgradeCost, 14, statusY, 0xC0C0C0);
                }
                else
                {
                    fontRenderer.drawString(I18n.format("gui.oneblockultima.max_level"), 14, statusY, 0xC0C0C0);
                }
            }

            // Обновляем кнопки
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

            int gridStartX = 14;
            int gridStartY = 160;

            // Render current (left) and next (right) level panels
            hoveredIndexLeft = -1; hoveredEntryLeft = null; hoveredStackLeft = ItemStack.EMPTY; hoveredMobIndexLeft = -1; hoveredMobEntryLeft = null; hoveredMobNameLeft = null;
            hoveredIndexRight = -1; hoveredEntryRight = null; hoveredStackRight = ItemStack.EMPTY; hoveredMobIndexRight = -1; hoveredMobEntryRight = null; hoveredMobNameRight = null;
            boolean canShowCurrent = currentLevel > 0;
            BlockSetConfig.SetLevelDefinition currentDef = canShowCurrent ? set.getLevel(currentLevel) : null;
            if (canShowCurrent)
            {
                renderLevelPanel(set, currentDef, gridStartX, gridStartY, true, mouseX, mouseY);
            }
            int rightStartX = canShowCurrent ? this.xSize / 2 + 8 : gridStartX;
            BlockSetConfig.SetLevelDefinition nextDef = set.getLevel(currentLevel <= 0 ? 1 : currentLevel + 1);
            renderLevelPanel(set, nextDef, rightStartX, gridStartY, false, mouseX, mouseY);

            if (canShowCurrent && nextDef != null)
            {
                int separatorX = this.xSize / 2 + 2;
                int separatorTop = gridStartY - 24;
                int separatorBottom = ySize - 10;
                drawRect(separatorX, separatorTop, separatorX + 1, separatorBottom, 0xFF4C5560);
            }

            if (canShowCurrent)
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.possible_blocks"), gridStartX, gridStartY - 12, 0xA0B0C0);
            }
            if (nextDef != null && rightStartX != gridStartX)
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.possible_blocks"), rightStartX, gridStartY - 12, 0xA0B0C0);
            }
            else if (!canShowCurrent && nextDef != null)
            {
                fontRenderer.drawString(I18n.format("gui.oneblockultima.possible_blocks"), gridStartX, gridStartY - 12, 0xA0B0C0);
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

    private BlockSetConfig.BlockSetDefinition getBlockSetDefinition() {
        BlockSetConfig.BlockSetDefinition set = null;

        // Если пользователь не листал наборы, показываем активный
        if (!hasBrowsedSets && clientActiveSetId != null)
        {
            for (BlockSetConfig.BlockSetDefinition s : visibleSets)
            {
                if (s.id.equals(clientActiveSetId))
                {
                    set = s;
                    break;
                }
            }
        }

        // Если не нашли или пользователь листал, используем selectedSetIndex
        if (set == null && selectedSetIndex >= 0 && selectedSetIndex < visibleSets.size())
        {
            set = visibleSets.get(selectedSetIndex);
        }

        return set;
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

            int gridStartX = 14;
            int gridStartY = 160;
            int blocksAreaWidth = iconCols * (cellSize + cellPadding) + 10;
            int leftMobsAreaX = gridStartX + iconCols * (cellSize + cellPadding) + 14;
            
            // Определяем, видна ли левая панель
            if (!visibleSets.isEmpty())
            {
                BlockSetConfig.BlockSetDefinition set = visibleSets.get(selectedSetIndex);
                IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(container.getPlayer());
                int currentLevel = data == null ? 0 : data.getSetLevel(set.id);
                boolean canShowCurrent = currentLevel > 0;
                int rightStartX = canShowCurrent ? this.xSize / 2 + 8 : gridStartX;
                int rightMobsAreaX = rightStartX + iconCols * (cellSize + cellPadding) + 14;

                if (canShowCurrent && localMouseX >= gridStartX && localMouseX <= gridStartX + blocksAreaWidth &&
                        localMouseY >= gridStartY && localMouseY <= ySize - 10)
                {
                    blockScroll = Math.max(0, blockScroll + delta);
                }
                else if (canShowCurrent && localMouseX >= leftMobsAreaX && localMouseX < leftMobsAreaX + mobCellSize * 2 + 4 &&
                        localMouseY >= gridStartY && localMouseY <= ySize - 10)
                {
                    mobScroll = Math.max(0, mobScroll + delta);
                }
                else if (localMouseX >= rightStartX && localMouseX <= rightStartX + blocksAreaWidth &&
                        localMouseY >= gridStartY && localMouseY <= ySize - 10)
                {
                    blockScrollNext = Math.max(0, blockScrollNext + delta);
                }
                else if (localMouseX >= rightMobsAreaX && localMouseX < rightMobsAreaX + mobCellSize * 2 + 4 &&
                        localMouseY >= gridStartY && localMouseY <= ySize - 10)
                {
                    mobScrollNext = Math.max(0, mobScrollNext + delta);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF1B1D21);
        drawRect(guiLeft + 4, guiTop + 4, guiLeft + xSize - 4, guiTop + ySize - 4, 0xFF22272E);
        drawRect(guiLeft + 4, guiTop + 4, guiLeft + xSize - 4, guiTop + 24, 0xFF2E3A45);
        drawRect(guiLeft + 6, guiTop + 28, guiLeft + xSize - 6, guiTop + 133, 0xFF252A30);
        drawRect(guiLeft + 6, guiTop + 135, guiLeft + xSize - 6, guiTop + ySize - 6, 0xFF1D2025);
        drawRect(guiLeft + 8, guiTop + 30, guiLeft + xSize - 8, guiTop + 131, 0xFF2F3741);
        drawRect(guiLeft + 8, guiTop + 136, guiLeft + xSize - 8, guiTop + ySize - 8, 0xFF1F2328);
    }

    private String getFluidDisplayName(BlockSetConfig.BlockEntryDefinition entry)
    {
        if (entry.registry == null) return "Unknown Fluid";

        // Пытаемся получить локализованное имя через FluidRegistry
        try {
            net.minecraft.block.Block block = entry.resolveBlock();
            if (block != null) {
                net.minecraftforge.fluids.Fluid fluid = net.minecraftforge.fluids.FluidRegistry.lookupFluidForBlock(block);
                if (fluid != null) {
                    // Пробуем получить локализованное имя жидкости
                    String unlocalizedName = fluid.getUnlocalizedName();
                    if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                        String localized = net.minecraft.client.resources.I18n.format(unlocalizedName);
                        if (localized != null && !localized.equals(unlocalizedName)) {
                            return localized;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Fallback: пробуем как блок
        try {
            net.minecraft.block.Block block = entry.resolveBlock();
            if (block != null) {
                String unlocalizedName = block.getUnlocalizedName();
                if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
                    String localized = net.minecraft.client.resources.I18n.format(unlocalizedName + ".name");
                    if (localized != null && !localized.equals(unlocalizedName + ".name")) {
                        return localized;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Fallback: из registry
        return entry.registry;
    }

    private void renderScrollBar(int scrollX, int scrollY, int scrollHeight, int currentScroll, int maxScroll)
    {
        if (maxScroll <= 0) return;

        int barHeight = Math.max(10, (scrollHeight * scrollHeight) / (scrollHeight + maxScroll * cellSize));
        int barY = scrollY + (currentScroll * (scrollHeight - barHeight)) / Math.max(1, maxScroll);

        // Background
        drawRect(scrollX, scrollY, scrollX + SCROLLBAR_WIDTH, scrollY + scrollHeight, 0xFF2A2F34);
        // Scrollbar thumb
        drawRect(scrollX, barY, scrollX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF7A7F84);
    }

    private void renderMobScrollBar(int scrollX, int scrollY, int scrollHeight, int currentScroll, int maxScroll)
    {
        if (maxScroll <= 0) return;

        int barHeight = Math.max(10, (scrollHeight * scrollHeight) / (scrollHeight + maxScroll * (mobCellSize + 4)));
        int barY = scrollY + (currentScroll * (scrollHeight - barHeight)) / Math.max(1, maxScroll);

        // Background
        drawRect(scrollX, scrollY, scrollX + SCROLLBAR_WIDTH, scrollY + scrollHeight, 0xFF2A2F34);
        // Scrollbar thumb
        drawRect(scrollX, barY, scrollX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF7A7F84);
    }
}