package ru.defea.oneblockultima.event;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.gui.GuiOneBlock;
import ru.defea.oneblockultima.gui.GuiSetsConfig;
import ru.defea.oneblockultima.world.OneBlockWorldType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ModEventsClient
{
    public ModEventsClient()
    {
    }

    public static void register()
    {
        ModEventsClient instance = new ModEventsClient();
        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
        OneBlockUltima.getLogger().info("[Events] ModEventsClient registered");
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        BlockSetConfig.reload();
    }

    private static final Map<UUID, Float> displayedCurrencyMap = new HashMap<UUID, Float>();

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiOneBlock)
        {
            return;
        }

        if (mc.currentScreen instanceof GuiSetsConfig)
        {
            return;
        }

        net.minecraft.world.World world = mc.theWorld;
        if (world == null || !(world.getWorldInfo().getTerrainType() instanceof OneBlockWorldType))
        {
            return;
        }

        EntityPlayer player = mc.thePlayer;
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

        int x = event.resolution.getScaledWidth() - 70 - textWidth;
        int y = coinSize + 12;

        int bgWidth = coinSize + textWidth + spaceBetween + hMargin * 2;
        int bgHeight = coinSize + vMargin * 2;
        int bgX = x - hMargin;
        int bgY = y - vMargin;

        drawRoundedRect(bgX, bgY, bgWidth, bgHeight, 5, 0x99333333);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(OneBlockUltima.MODID, "textures/gui/coin.png"));
        float f = 1.0F / coinSize;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + coinSize, 0, 0, coinSize * f);
        tessellator.addVertexWithUV(x + coinSize, y + coinSize, 0, coinSize * f, coinSize * f);
        tessellator.addVertexWithUV(x + coinSize, y, 0, coinSize * f, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
        Minecraft.getMinecraft().fontRenderer.drawString(balanceValue, x + coinSize + spaceBetween, y, 0xFFD700);
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

        Integer lastCurrency = ModEvents.lastDisplayedCurrency.get(playerUUID);
        if (lastCurrency == null)
        {
            displayedCurrencyMap.put(playerUUID, (float) targetCurrency);
            ModEvents.lastDisplayedCurrency.put(playerUUID, targetCurrency);
            return targetCurrency;
        }

        if (lastCurrency != targetCurrency)
        {
            ModEvents.lastDisplayedCurrency.put(playerUUID, targetCurrency);
        }

        float currentDisplayed = displayedCurrencyMap.containsKey(playerUUID) ? displayedCurrencyMap.get(playerUUID) : (float) targetCurrency;
        float newDisplayed = currentDisplayed + (targetCurrency - currentDisplayed) * 0.14f;
        if (Math.abs(targetCurrency - newDisplayed) < 0.01f)
        {
            newDisplayed = targetCurrency;
        }

        displayedCurrencyMap.put(playerUUID, newDisplayed);
        return Math.round(newDisplayed);
    }

    public static void refreshOpenGui()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null)
        {
            return;
        }

        if (mc.currentScreen instanceof GuiOneBlock)
        {
            mc.currentScreen.initGui();
        }
    }

    private static void drawRoundedRect(int x, int y, int width, int height, int radius, int color)
    {
        Gui.drawRect(x + radius, y, x + width - radius, y + height, color);
        Gui.drawRect(x, y + radius, x + width, y + height - radius, color);

        for (int i = 0; i < radius; i++)
        {
            for (int j = 0; j < radius; j++)
            {
                if (i * i + j * j < radius * radius)
                {
                    int right = x + width - radius + i + 1;
                    int left = x + radius - i - 1;
                    int bottom = y + height - radius + j + 1;
                    int top = y + radius - j - 1;
                    Gui.drawRect(left, top, left + 1, top + 1, color);
                    Gui.drawRect(right - 1, top, right, top + 1, color);
                    Gui.drawRect(left, bottom - 1, left + 1, bottom, color);
                    Gui.drawRect(right - 1, bottom - 1, right, bottom, color);
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (!(event.gui instanceof GuiCreateWorld))
        {
            return;
        }

        GuiCreateWorld screen = (GuiCreateWorld) event.gui;
        WorldType worldType = getCreateWorldType(screen);
        OneBlockUltima.getLogger().info("[GUI] onGuiInit: worldType={}, expected={}",
                worldType != null ? worldType.getWorldTypeName() : "null",
                OneBlockWorldType.ONE_BLOCK.getWorldTypeName());
        if (worldType != OneBlockWorldType.ONE_BLOCK)
        {
            return;
        }
        OneBlockUltima.getLogger().info("[GUI] OneBlock world type detected, hiding buttons");

        String bonusLabel = StatCollector.translateToLocal("createWorld.customize.bonusItems");
        String structuresLabel = StatCollector.translateToLocal("createWorld.customize.mapFeatures");
        for (Object obj : event.buttonList)
        {
            GuiButton button = (GuiButton) obj;
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

    private static Field createWorldTypeIdxField;

    private static WorldType getCreateWorldType(GuiCreateWorld screen)
    {
        if (createWorldTypeIdxField == null)
        {
            createWorldTypeIdxField = findFieldByNames(GuiCreateWorld.class, "field_146331_K", "worldType", "selectedWorldType");
            if (createWorldTypeIdxField != null)
            {
                createWorldTypeIdxField.setAccessible(true);
            }
        }

        if (createWorldTypeIdxField == null)
        {
            return null;
        }

        try
        {
            int idx;
            if (createWorldTypeIdxField.getType() == int.class)
            {
                idx = createWorldTypeIdxField.getInt(screen);
            }
            else
            {
                Object value = createWorldTypeIdxField.get(screen);
                if (value instanceof WorldType)
                {
                    return (WorldType) value;
                }
                if (value instanceof Number)
                {
                    idx = ((Number) value).intValue();
                }
                else
                {
                    return null;
                }
            }
            if (idx >= 0 && idx < WorldType.worldTypes.length)
            {
                return WorldType.worldTypes[idx];
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
}
