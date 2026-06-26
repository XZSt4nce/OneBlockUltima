package ru.defea.oneblockultima;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import ru.defea.oneblockultima.config.BlockSetConfig;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public class BlockSetConfigGui extends GuiScreen
{
    private final GuiScreen parent;
    private GuiButton openFolderButton;
    private GuiButton resetButton;
    private GuiButton cancelButton;
    private String statusMessage = "";

    public BlockSetConfigGui(GuiScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        int centerX = width / 2;
        int centerY = height / 2;
        
        String openLabel = I18n.format("gui.oneblockultima.open");
        String resetLabel = I18n.format("gui.oneblockultima.reset_default");
        String cancelLabel = I18n.format("gui.oneblockultima.cancel");

        int openW = Math.max(80, fontRenderer.getStringWidth(openLabel) + 20);
        int resetW = Math.max(80, fontRenderer.getStringWidth(resetLabel) + 20);
        int cancelW = Math.max(80, fontRenderer.getStringWidth(cancelLabel) + 20);

        int gap = 10;
        int total = openW + resetW + cancelW + gap * 2;
        int startX = centerX - total / 2;

        openFolderButton = new GuiButton(0, startX, centerY + 40, openW, 20, openLabel);
        resetButton = new GuiButton(1, startX + openW + gap, centerY + 40, resetW, 20, resetLabel);
        cancelButton = new GuiButton(2, startX + openW + resetW + gap * 2, centerY + 40, cancelW, 20, cancelLabel);
        
        buttonList.add(openFolderButton);
        buttonList.add(resetButton);
        buttonList.add(cancelButton);

        statusMessage = I18n.format("");
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button == openFolderButton)
        {
            File file = BlockSetConfig.getConfigFile();
            if (file != null)
            {
                File dir = file.getParentFile();
                try
                {
                    if (Desktop.isDesktopSupported())
                    {
                        Desktop.getDesktop().open(dir);
                        statusMessage = I18n.format("gui.oneblockultima.status.folder_opened");
                    }
                    else
                    {
                        statusMessage = I18n.format("gui.oneblockultima.status.desktop_not_supported");
                    }
                }
                catch (Exception e)
                {
                    statusMessage = I18n.format("gui.oneblockultima.status.open_failed");
                }
            }
            else
            {
                statusMessage = I18n.format("gui.oneblockultima.status.folder_missing");
            }
        }
        else if (button == resetButton)
        {
            try
            {
                File file = BlockSetConfig.getConfigFile();
                if (file != null && file.exists())
                {
                    // Создаем резервную копию
                    File backup = new File(file.getParent(), "blocksets_backup.json");
                    if (backup.exists())
                    {
                        backup.delete();
                    }
                    Files.copy(file.toPath(), backup.toPath());
                    
                    // Удаляем файл конфигурации
                    file.delete();
                }
                
                // Перезагружаем конфигурацию (создастся файл со значениями по умолчанию если его нет)
                boolean success = BlockSetConfig.reload();
                if (success)
                {
                    statusMessage = I18n.format("gui.oneblockultima.status.reset_success");
                }
                else
                {
                    statusMessage = I18n.format("gui.oneblockultima.status.reset_failed");
                }
            }
            catch (Exception e)
            {
                statusMessage = I18n.format("gui.oneblockultima.status.reset_error");
            }
        }
        else if (button == cancelButton)
        {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("gui.oneblockultima.config.title"), width / 2, 20, 0xFFFFFF);

        String[] description = I18n.format("gui.oneblockultima.config.description").split("/nn");
        for(int i = 0; i < description.length; i++) {
            drawCenteredString(fontRenderer, description[i], width / 2, 60 + 8 * i, 0xA0A0A0);
        }

        if (!statusMessage.isEmpty())
        {
            drawCenteredString(fontRenderer, statusMessage, width / 2, height - 40, 0xA0A0A0);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}