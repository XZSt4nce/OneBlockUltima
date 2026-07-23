package ru.defea.oneblockultima.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

public class ModMainSettings extends GuiScreen
{
    private static final int BUTTON_CONFIG_EDITOR = 0;
    private static final int BUTTON_MOD_SETTINGS = 1;
    private static final int BUTTON_BACK = 2;

    private final GuiScreen parent;

    public ModMainSettings(GuiScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public void initGui()
    {
        int centerX = width / 2;
        int btnWidth = 200;
        int btnHeight = 20;
        int startY = height / 2 - 35;

        buttonList.add(new GuiButton(BUTTON_CONFIG_EDITOR, centerX - btnWidth / 2, startY, btnWidth, btnHeight,
                StatCollector.translateToLocal("gui.oneblockultima.settings.open_editor")));
        buttonList.add(new GuiButton(BUTTON_MOD_SETTINGS, centerX - btnWidth / 2, startY + btnHeight + 8, btnWidth, btnHeight,
                StatCollector.translateToLocal("gui.oneblockultima.settings.mod_settings")));
        buttonList.add(new GuiButton(BUTTON_BACK, centerX - btnWidth / 2, startY + (btnHeight + 8) * 2, btnWidth, btnHeight,
                StatCollector.translateToLocal("gui.oneblockultima.cancel")));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == BUTTON_CONFIG_EDITOR)
        {
            mc.displayGuiScreen(new GuiSetsConfig(this));
        }
        else if (button.id == BUTTON_MOD_SETTINGS)
        {
            mc.displayGuiScreen(new GuiModSettings(this));
        }
        else if (button.id == BUTTON_BACK)
        {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.mod_settings.title"), width / 2, 14, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
