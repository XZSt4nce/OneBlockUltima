package ru.defea.oneblockultima;

import com.google.gson.JsonParser;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.defea.oneblockultima.config.BlockSetConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BlockSetConfigGui extends GuiScreen
{
    private static final int STATE_CHOICES = 0;
    private static final int STATE_EDITOR = 1;

    private final GuiScreen parent;
    private int currentState = STATE_CHOICES;
    private MultiLineTextEditor jsonEditor;
    private MultiLineTextEditor helpTextBox;
    private GuiButton backButton;
    private GuiButton editButton;
    private GuiButton resetButton;
    private GuiButton saveButton;
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

        int panelPadding = Math.max(12, width / 40);
        int panelX = panelPadding;
        int panelY = panelPadding;
        int panelWidth = width - panelPadding * 2;
        int panelHeight = height - panelPadding * 2;
        int headerHeight = Math.max(28, fontRenderer.FONT_HEIGHT + 12);
        int buttonHeight = 20;
        int buttonGap = Math.max(10, panelWidth / 60);
        int contentX = panelX + panelPadding;
        int contentY = panelY + headerHeight + panelPadding;
        int contentWidth = panelWidth - panelPadding * 2;
        int contentHeight = panelHeight - headerHeight - panelPadding * 3 - buttonHeight;
        int buttonY = panelY + panelHeight - panelPadding - buttonHeight;

        if (currentState == STATE_EDITOR)
        {
            int centerX = width / 2;
            String saveLabel = I18n.format("gui.oneblockultima.save");
            String resetLabel = I18n.format("gui.oneblockultima.reset_default");
            String cancelLabel = I18n.format("gui.oneblockultima.cancel");

            int saveW = Math.max(90, fontRenderer.getStringWidth(saveLabel) + 20);
            int resetW = Math.max(110, fontRenderer.getStringWidth(resetLabel) + 20);
            int cancelW = Math.max(90, fontRenderer.getStringWidth(cancelLabel) + 20);
            int total = saveW + resetW + cancelW + buttonGap * 2;
            int startX = centerX - total / 2;

            saveButton = new GuiButton(0, startX, buttonY, saveW, buttonHeight, saveLabel);
            resetButton = new GuiButton(1, startX + saveW + buttonGap, buttonY, resetW, buttonHeight, resetLabel);
            cancelButton = new GuiButton(2, startX + saveW + resetW + buttonGap * 2, buttonY, cancelW, buttonHeight, cancelLabel);

            buttonList.add(saveButton);
            buttonList.add(resetButton);
            buttonList.add(cancelButton);

            jsonEditor = new MultiLineTextEditor(fontRenderer, contentX, contentY, contentWidth, contentHeight);
            jsonEditor.setFocused(true);
            loadCurrentJson();
            statusMessage = I18n.format("gui.oneblockultima.status.edit_instruction");
        }
        else
        {
            int backW = Math.max(90, fontRenderer.getStringWidth(I18n.format("gui.oneblockultima.settings.back")) + 20);
            int editW = Math.max(90, fontRenderer.getStringWidth(I18n.format("gui.oneblockultima.settings.edit")) + 20);
            int resetW = Math.max(110, fontRenderer.getStringWidth(I18n.format("gui.oneblockultima.reset_default")) + 20);
            int total = backW + editW + resetW + buttonGap * 2;
            int startX = width / 2 - total / 2;

            backButton = new GuiButton(10, startX, buttonY, backW, buttonHeight, I18n.format("gui.oneblockultima.settings.back"));
            editButton = new GuiButton(11, startX + backW + buttonGap, buttonY, editW, buttonHeight, I18n.format("gui.oneblockultima.settings.edit"));
            resetButton = new GuiButton(12, startX + backW + editW + buttonGap * 2, buttonY, resetW, buttonHeight, I18n.format("gui.oneblockultima.reset_default"));
            buttonList.add(backButton);
            buttonList.add(editButton);
            buttonList.add(resetButton);

            String description = I18n.format("gui.oneblockultima.config.description");
            helpTextBox = new MultiLineTextEditor(fontRenderer, contentX, contentY, contentWidth, contentHeight);
            helpTextBox.setReadOnly(true);
            helpTextBox.setText(description.replace("/nn", "\n"));
            helpTextBox.setScrollOffset(0);
            helpTextBox.setFocused(false);
            statusMessage = "";
        }
    }

    private void loadCurrentJson()
    {
        File file = BlockSetConfig.getConfigFile();
        String text = "";
        if (file != null && file.exists())
        {
            try
            {
                text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            }
            catch (Exception ignored)
            {
            }
        }

        if (jsonEditor != null)
        {
            jsonEditor.setText(normalizeLineEndings(text));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (currentState == STATE_EDITOR)
        {
            if (button == saveButton)
            {
                saveChanges();
            }
            else if (button == resetButton)
            {
                resetToDefault();
            }
            else if (button == cancelButton)
            {
                currentState = STATE_CHOICES;
                initGui();
            }
        }
        else
        {
            if (button == backButton)
            {
                mc.displayGuiScreen(parent);
            }
            else if (button == editButton)
            {
                currentState = STATE_EDITOR;
                initGui();
            }
            else if (button == resetButton)
            {
                resetToDefault();
            }
        }
    }

    private void saveChanges()
    {
        if (jsonEditor == null)
        {
            return;
        }

        String text = jsonEditor.getText();
        try
        {
            new JsonParser().parse(text);
            File file = BlockSetConfig.getConfigFile();
            if (file == null)
            {
                statusMessage = I18n.format("gui.oneblockultima.status.save_failed");
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            Files.write(file.toPath(), normalizeLineEndings(text).getBytes(StandardCharsets.UTF_8));
            BlockSetConfig.reload();
            statusMessage = I18n.format("gui.oneblockultima.status.reloaded");
        }
        catch (Exception e)
        {
            statusMessage = I18n.format("gui.oneblockultima.status.invalid_json");
        }
    }

    private void resetToDefault()
    {
        try
        {
            File file = BlockSetConfig.getConfigFile();
            if (file == null)
            {
                statusMessage = I18n.format("gui.oneblockultima.status.reset_failed");
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = BlockSetConfig.class.getResourceAsStream("/assets/oneblockultima/blocksets.json"))
            {
                if (input == null)
                {
                    statusMessage = I18n.format("gui.oneblockultima.status.reset_failed");
                    return;
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1)
                {
                    output.write(buffer, 0, read);
                }
                Files.write(file.toPath(), output.toByteArray());
            }
            BlockSetConfig.reload();
            loadCurrentJson();
            statusMessage = I18n.format("gui.oneblockultima.status.reset_success");
        }
        catch (Exception e)
        {
            statusMessage = I18n.format("gui.oneblockultima.status.reset_failed");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        if (currentState == STATE_EDITOR)
        {
            drawCenteredString(fontRenderer, I18n.format("gui.oneblockultima.settings.editor_title"), width / 2, 16, 0xFFFFFF);
            if (jsonEditor != null)
            {
                jsonEditor.drawTextBox();
            }
        }
        else
        {
            drawCenteredString(fontRenderer, I18n.format("gui.oneblockultima.settings.choice_title"), width / 2, 28, 0xFFFFFF);
            if (helpTextBox != null)
            {
                helpTextBox.drawTextBox();
            }
        }

        if (!statusMessage.isEmpty())
        {
            drawCenteredString(fontRenderer, statusMessage, width / 2, currentState == STATE_EDITOR ? height - 56 : height / 2 + 116, 0xA0A0A0);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (currentState == STATE_EDITOR && jsonEditor != null)
        {
            jsonEditor.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else if (currentState == STATE_CHOICES && helpTextBox != null)
        {
            helpTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (currentState == STATE_EDITOR && jsonEditor != null)
        {
            jsonEditor.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        else if (currentState == STATE_CHOICES && helpTextBox != null)
        {
            helpTextBox.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if (currentState == STATE_EDITOR && jsonEditor != null)
        {
            jsonEditor.mouseReleased(mouseX, mouseY, state);
        }
        else if (currentState == STATE_CHOICES && helpTextBox != null)
        {
            helpTextBox.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0)
        {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (currentState == STATE_EDITOR && jsonEditor != null && jsonEditor.contains(mouseX, mouseY))
            {
                jsonEditor.mouseScrolled(d);
            }
            else if (currentState == STATE_CHOICES && helpTextBox != null && helpTextBox.contains(mouseX, mouseY))
            {
                helpTextBox.mouseScrolled(d);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (currentState == STATE_EDITOR && jsonEditor != null)
        {
            jsonEditor.textboxKeyTyped(typedChar, keyCode);
        }
    }

    private String normalizeLineEndings(String input)
    {
        if (input == null)
        {
            return "";
        }
        return input.replace("\r\n", "\n").replace('\r', '\n');
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private class MultiLineTextEditor
    {
        private final FontRenderer fontRenderer;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int lineHeight;
        private final List<String> lines = new ArrayList<String>();
        private boolean focused;
        private int cursorLine;
        private int cursorColumn;
        private int scrollOffset;
        private int horizontalScrollOffset;
        private boolean readOnly;
        private int maxStringLength = 500000;
        private boolean draggingVertical;
        private boolean draggingHorizontal;
        private boolean ensureCursorVisible;
        private int dragStartMouseX;
        private int dragStartMouseY;
        private int dragStartScrollOffset;
        private int dragStartHorizontalOffset;
        private int lastVerticalTrackX;
        private int lastVerticalTrackY;
        private int lastVerticalTrackWidth;
        private int lastVerticalTrackHeight;
        private int lastVerticalThumbY;
        private int lastVerticalThumbHeight;
        private int lastHorizontalTrackX;
        private int lastHorizontalTrackY;
        private int lastHorizontalTrackWidth;
        private int lastHorizontalTrackHeight;
        private int lastHorizontalThumbX;
        private int lastHorizontalThumbWidth;

        private MultiLineTextEditor(FontRenderer fontRenderer, int x, int y, int width, int height)
        {
            this.fontRenderer = fontRenderer;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.lineHeight = fontRenderer.FONT_HEIGHT + 2;
        }

        private void setFocused(boolean focused)
        {
            if (!readOnly)
            {
                this.focused = focused;
            }
        }

        private void setReadOnly(boolean readOnly)
        {
            this.readOnly = readOnly;
        }

        private void setScrollOffset(int offset)
        {
            this.scrollOffset = Math.max(0, offset);
        }

        private void setText(String text)
        {
            String normalized = normalizeLineEndings(text == null ? "" : text);
            String[] split = normalized.split("\\n", -1);
            lines.clear();
            for (String part : split)
            {
                lines.add(part);
            }
            if (lines.isEmpty())
            {
                lines.add("");
            }
            cursorLine = 0;
            cursorColumn = 0;
            scrollOffset = 0;
            horizontalScrollOffset = 0;
            ensureCursorVisible = true;
        }

        private String getText()
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lines.size(); i++)
            {
                if (i > 0)
                {
                    builder.append('\n');
                }
                builder.append(lines.get(i));
            }
            return builder.toString();
        }

        private void drawTextBox()
        {
            drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF404040);
            drawRect(x, y, x + width, y + height, 0xFF202020);

            int borderPadding = 2;
            int textMargin = 4;
            int scrollbarSize = 6;
            int maxLineWidth = 0;
            for (String line : lines)
            {
                maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
            }

            int textX = x + borderPadding + textMargin;
            int textY = y + borderPadding + textMargin;
            int availableWidth = width - (borderPadding + textMargin) * 2;
            int availableHeight = height - (borderPadding + textMargin) * 2;
            boolean horizontalNeeded = maxLineWidth > availableWidth;
            boolean verticalNeeded = false;
            boolean changed;
            do
            {
                changed = false;
                int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                int visibleLines = Math.max(1, contentHeight / lineHeight);
                boolean newVerticalNeeded = lines.size() > visibleLines;
                boolean newHorizontalNeeded = maxLineWidth > contentWidth;
                if (newVerticalNeeded != verticalNeeded || newHorizontalNeeded != horizontalNeeded)
                {
                    changed = true;
                    verticalNeeded = newVerticalNeeded;
                    horizontalNeeded = newHorizontalNeeded;
                }
            }
            while (changed);

            int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
            int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
            int visibleLines = Math.max(1, contentHeight / lineHeight);
            int maxOffset = Math.max(0, lines.size() - visibleLines);
            if (scrollOffset > maxOffset)
            {
                scrollOffset = maxOffset;
            }
            if (!readOnly && ensureCursorVisible)
            {
                if (cursorLine < scrollOffset)
                {
                    scrollOffset = cursorLine;
                }
                else if (cursorLine >= scrollOffset + visibleLines)
                {
                    scrollOffset = cursorLine - visibleLines + 1;
                }
                ensureCursorVisible = false;
            }
            int startLine = scrollOffset;
            int maxHorizontalOffset = Math.max(0, maxLineWidth - contentWidth);
            if (horizontalScrollOffset > maxHorizontalOffset)
            {
                horizontalScrollOffset = maxHorizontalOffset;
            }

            for (int i = 0; i < visibleLines; i++)
            {
                int lineIndex = startLine + i;
                if (lineIndex >= lines.size())
                {
                    break;
                }
                String line = lines.get(lineIndex);
                if (horizontalScrollOffset <= 0)
                {
                    String visibleText = fontRenderer.trimStringToWidth(line, contentWidth);
                    fontRenderer.drawString(visibleText, textX, textY + i * lineHeight, 0xE0E0E0);
                }
                else
                {
                    int skipWidth = horizontalScrollOffset;
                    int startIndex = 0;
                    int skippedWidth = 0;
                    while (startIndex < line.length())
                    {
                        int charWidth = fontRenderer.getStringWidth(line.substring(startIndex, startIndex + 1));
                        if (skippedWidth + charWidth > skipWidth)
                        {
                            break;
                        }
                        skippedWidth += charWidth;
                        startIndex++;
                    }
                    int partialOffset = skipWidth - skippedWidth;
                    if (startIndex >= line.length())
                    {
                        continue;
                    }
                    String visibleText = fontRenderer.trimStringToWidth(line.substring(startIndex), contentWidth + partialOffset);
                    fontRenderer.drawString(visibleText, textX - partialOffset, textY + i * lineHeight, 0xE0E0E0);
                }
            }

            if (horizontalNeeded)
            {
                int trackX = textX;
                int trackY = y + height - borderPadding - scrollbarSize;
                int trackWidth = contentWidth;
                int trackHeight = scrollbarSize - 1;
                int thumbWidth = Math.max(10, trackWidth * contentWidth / Math.max(1, maxLineWidth));
                int thumbX = trackX + (maxHorizontalOffset == 0 ? 0 : (horizontalScrollOffset * (trackWidth - thumbWidth) / maxHorizontalOffset));
                drawRect(trackX - 1, trackY - 1, trackX + trackWidth + 1, trackY + trackHeight + 1, 0xFF303030);
                drawRect(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF404040);
                drawRect(thumbX, trackY, thumbX + thumbWidth, trackY + trackHeight, 0xFF808080);
            }

            if (verticalNeeded)
            {
                int trackX = x + width - borderPadding - scrollbarSize;
                int trackY = textY;
                int trackWidth = scrollbarSize - 1;
                int trackHeight = contentHeight;
                int thumbHeight = Math.max(10, trackHeight * visibleLines / Math.max(1, lines.size()));
                int thumbY = trackY + (scrollOffset * (trackHeight - thumbHeight) / Math.max(1, maxOffset));
                drawRect(trackX - 1, trackY - 1, trackX + trackWidth + 1, trackY + trackHeight + 1, 0xFF303030);
                drawRect(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF404040);
                drawRect(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, 0xFF808080);
            }

            if (focused)
            {
                int drawLine = Math.max(0, Math.min(lines.size() - 1, cursorLine - startLine));
                String currentLine = lines.get(Math.min(cursorLine, lines.size() - 1));
                int caretX = textX - horizontalScrollOffset + fontRenderer.getStringWidth(currentLine.substring(0, Math.min(cursorColumn, currentLine.length())));
                int caretY = textY + drawLine * lineHeight;
                drawRect(caretX, caretY, caretX + 1, caretY + fontRenderer.FONT_HEIGHT, 0xFFFFFFFF);
            }
        }

        private void mouseClicked(int mouseX, int mouseY, int mouseButton)
        {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
            {
                int borderPadding = 2;
                int textMargin = 4;
                int scrollbarSize = 6;
                int availableWidth = width - (borderPadding + textMargin) * 2;
                int availableHeight = height - (borderPadding + textMargin) * 2;
                int maxLineWidth = 0;
                for (String line : lines)
                {
                    maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
                }

                boolean horizontalNeeded = maxLineWidth > availableWidth;
                boolean verticalNeeded = false;
                boolean changed;
                do
                {
                    changed = false;
                    int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                    int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                    int visibleLines = Math.max(1, contentHeight / lineHeight);
                    boolean newVerticalNeeded = lines.size() > visibleLines;
                    boolean newHorizontalNeeded = maxLineWidth > contentWidth;
                    if (newVerticalNeeded != verticalNeeded || newHorizontalNeeded != horizontalNeeded)
                    {
                        changed = true;
                        verticalNeeded = newVerticalNeeded;
                        horizontalNeeded = newHorizontalNeeded;
                    }
                }
                while (changed);

                int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                int visibleLines = Math.max(1, contentHeight / lineHeight);
                int maxOffset = Math.max(0, lines.size() - visibleLines);
                int maxHorizontalOffset = Math.max(0, maxLineWidth - contentWidth);

                int textX = x + borderPadding + textMargin;
                int textY = y + borderPadding + textMargin;

                int verticalTrackX = x + width - borderPadding - scrollbarSize;
                int verticalTrackY = textY;
                int verticalTrackWidth = scrollbarSize - 1;
                int verticalTrackHeight = contentHeight;
                int verticalThumbHeight = Math.max(10, verticalTrackHeight * visibleLines / Math.max(1, lines.size()));
                int verticalThumbY = verticalTrackY + (maxOffset == 0 ? 0 : (scrollOffset * (verticalTrackHeight - verticalThumbHeight) / maxOffset));
                lastVerticalTrackX = verticalTrackX;
                lastVerticalTrackY = verticalTrackY;
                lastVerticalTrackWidth = verticalTrackWidth;
                lastVerticalTrackHeight = verticalTrackHeight;
                lastVerticalThumbY = verticalThumbY;
                lastVerticalThumbHeight = verticalThumbHeight;

                if (mouseX >= verticalTrackX && mouseX <= verticalTrackX + verticalTrackWidth && mouseY >= verticalTrackY && mouseY <= verticalTrackY + verticalTrackHeight)
                {
                    draggingVertical = true;
                    dragStartMouseY = mouseY;
                    dragStartScrollOffset = scrollOffset;
                    focused = false;
                    return;
                }

                if (horizontalNeeded)
                {
                    int horizontalTrackX = textX;
                    int horizontalTrackY = y + height - borderPadding - scrollbarSize;
                    int horizontalTrackWidth = contentWidth;
                    int horizontalTrackHeight = scrollbarSize - 1;
                    int horizontalThumbWidth = Math.max(10, horizontalTrackWidth * contentWidth / Math.max(1, maxLineWidth));
                    int horizontalThumbX = horizontalTrackX + (maxHorizontalOffset == 0 ? 0 : (horizontalScrollOffset * (horizontalTrackWidth - horizontalThumbWidth) / maxHorizontalOffset));
                    lastHorizontalTrackX = horizontalTrackX;
                    lastHorizontalTrackY = horizontalTrackY;
                    lastHorizontalTrackWidth = horizontalTrackWidth;
                    lastHorizontalTrackHeight = horizontalTrackHeight;
                    lastHorizontalThumbX = horizontalThumbX;
                    lastHorizontalThumbWidth = horizontalThumbWidth;

                    if (mouseX >= horizontalTrackX && mouseX <= horizontalTrackX + horizontalTrackWidth && mouseY >= horizontalTrackY && mouseY <= horizontalTrackY + horizontalTrackHeight)
                    {
                        draggingHorizontal = true;
                        dragStartMouseX = mouseX;
                        dragStartHorizontalOffset = horizontalScrollOffset;
                        focused = false;
                        return;
                    }
                }

                if (!readOnly)
                {
                    focused = true;
                    int relY = mouseY - textY;
                    int lineIndex = relY / lineHeight;
                    cursorLine = Math.max(0, Math.min(lines.size() - 1, lineIndex + scrollOffset));
                    int relX = mouseX - textX;
                    String currentLine = lines.get(cursorLine);
                    int best = 0;
                    int bestWidth = Integer.MAX_VALUE;
                    for (int i = 0; i <= currentLine.length(); i++)
                    {
                        int width = fontRenderer.getStringWidth(currentLine.substring(0, i));
                        if (Math.abs(width - relX) < bestWidth)
                        {
                            bestWidth = Math.abs(width - relX);
                            best = i;
                        }
                    }
                    cursorColumn = Math.max(0, Math.min(currentLine.length(), best));
                    ensureCursorVisible = true;
                }
            }
            else
            {
                focused = false;
            }
        }

        private void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
        {
            if (draggingVertical)
            {
                int borderPadding = 2;
                int textMargin = 4;
                int scrollbarSize = 6;
                int availableWidth = width - (borderPadding + textMargin) * 2;
                int availableHeight = height - (borderPadding + textMargin) * 2;
                int maxLineWidth = 0;
                for (String line : lines)
                {
                    maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
                }
                boolean horizontalNeeded = maxLineWidth > availableWidth;
                boolean verticalNeeded = false;
                boolean changed;
                do
                {
                    changed = false;
                    int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                    int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                    int visibleLines = Math.max(1, contentHeight / lineHeight);
                    boolean newVerticalNeeded = lines.size() > visibleLines;
                    boolean newHorizontalNeeded = maxLineWidth > contentWidth;
                    if (newVerticalNeeded != verticalNeeded || newHorizontalNeeded != horizontalNeeded)
                    {
                        changed = true;
                        verticalNeeded = newVerticalNeeded;
                        horizontalNeeded = newHorizontalNeeded;
                    }
                }
                while (changed);

                int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                int visibleLines = Math.max(1, contentHeight / lineHeight);
                int maxOffset = Math.max(0, lines.size() - visibleLines);
                int trackHeight = lastVerticalTrackHeight;
                int thumbHeight = lastVerticalThumbHeight;
                int trackMovable = Math.max(1, trackHeight - thumbHeight);
                int delta = mouseY - dragStartMouseY;
                int newScroll = dragStartScrollOffset + Math.round(delta * (float) maxOffset / trackMovable);
                scrollOffset = Math.max(0, Math.min(maxOffset, newScroll));
            }
            if (draggingHorizontal)
            {
                int borderPadding = 2;
                int textMargin = 4;
                int scrollbarSize = 6;
                int availableWidth = width - (borderPadding + textMargin) * 2;
                int availableHeight = height - (borderPadding + textMargin) * 2;
                int maxLineWidth = 0;
                for (String line : lines)
                {
                    maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
                }
                boolean horizontalNeeded = maxLineWidth > availableWidth;
                boolean verticalNeeded = false;
                boolean changed;
                do
                {
                    changed = false;
                    int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                    int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                    int visibleLines = Math.max(1, contentHeight / lineHeight);
                    boolean newVerticalNeeded = lines.size() > visibleLines;
                    boolean newHorizontalNeeded = maxLineWidth > contentWidth;
                    if (newVerticalNeeded != verticalNeeded || newHorizontalNeeded != horizontalNeeded)
                    {
                        changed = true;
                        verticalNeeded = newVerticalNeeded;
                        horizontalNeeded = newHorizontalNeeded;
                    }
                }
                while (changed);

                int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                int maxHorizontalOffset = Math.max(0, maxLineWidth - contentWidth);
                int trackWidth = lastHorizontalTrackWidth;
                int thumbWidth = lastHorizontalThumbWidth;
                int trackMovable = Math.max(1, trackWidth - thumbWidth);
                int delta = mouseX - dragStartMouseX;
                int newOffset = dragStartHorizontalOffset + Math.round(delta * (float) maxHorizontalOffset / trackMovable);
                horizontalScrollOffset = Math.max(0, Math.min(maxHorizontalOffset, newOffset));
            }
        }

        private void mouseReleased(int mouseX, int mouseY, int state)
        {
            draggingVertical = false;
            draggingHorizontal = false;
        }

        private void textboxKeyTyped(char typedChar, int keyCode)
        {
            if (!focused || readOnly)
            {
                return;
            }

            if (keyCode == Keyboard.KEY_RETURN)
            {
                insertText("\n");
                return;
            }
            if (keyCode == Keyboard.KEY_BACK)
            {
                deleteBeforeCursor();
                return;
            }
            if (keyCode == Keyboard.KEY_DELETE)
            {
                deleteAfterCursor();
                return;
            }
            if (keyCode == Keyboard.KEY_LEFT)
            {
                moveCursor(-1, 0);
                return;
            }
            if (keyCode == Keyboard.KEY_RIGHT)
            {
                moveCursor(1, 0);
                return;
            }
            if (keyCode == Keyboard.KEY_UP)
            {
                moveCursor(0, -1);
                return;
            }
            if (keyCode == Keyboard.KEY_DOWN)
            {
                moveCursor(0, 1);
                return;
            }
            if (keyCode == Keyboard.KEY_TAB)
            {
                insertText("    ");
                return;
            }
            if (typedChar == 0 || typedChar == 127)
            {
                return;
            }
            if (getText().length() >= maxStringLength)
            {
                return;
            }
            insertText(String.valueOf(typedChar));
        }

        private void insertText(String text)
        {
            if (text == null || text.isEmpty())
            {
                return;
            }
            String currentLine = lines.get(cursorLine);
            String prefix = currentLine.substring(0, Math.min(cursorColumn, currentLine.length()));
            String suffix = currentLine.substring(Math.min(cursorColumn, currentLine.length()));
            StringBuilder builder = new StringBuilder();
            builder.append(prefix);
            builder.append(text);
            builder.append(suffix);
            String newLine = builder.toString();
            lines.set(cursorLine, newLine);
            cursorColumn += text.length();
            if (text.contains("\n"))
            {
                String[] split = newLine.split("\\n", -1);
                if (split.length > 1)
                {
                    lines.set(cursorLine, split[0]);
                    List<String> inserted = new ArrayList<String>();
                    for (int i = 1; i < split.length; i++)
                    {
                        inserted.add(split[i]);
                    }
                    lines.addAll(cursorLine + 1, inserted);
                    cursorLine += 1;
                    cursorColumn = split[split.length - 1].length();
                }
            }
            horizontalScrollOffset = Math.min(horizontalScrollOffset, getMaxHorizontalOffset());
            ensureCursorVisible = true;
        }

        private void deleteBeforeCursor()
        {
            String currentLine = lines.get(cursorLine);
            if (cursorColumn > 0)
            {
                String prefix = currentLine.substring(0, cursorColumn - 1);
                String suffix = currentLine.substring(cursorColumn);
                lines.set(cursorLine, prefix + suffix);
                cursorColumn--;
            }
            else if (cursorLine > 0)
            {
                String previousLine = lines.get(cursorLine - 1);
                String current = lines.get(cursorLine);
                lines.remove(cursorLine);
                cursorLine--;
                cursorColumn = previousLine.length();
                lines.set(cursorLine, previousLine + current);
            }
            ensureCursorVisible = true;
        }

        private void deleteAfterCursor()
        {
            String currentLine = lines.get(cursorLine);
            if (cursorColumn < currentLine.length())
            {
                String prefix = currentLine.substring(0, cursorColumn);
                String suffix = currentLine.substring(cursorColumn + 1);
                lines.set(cursorLine, prefix + suffix);
                ensureCursorVisible = true;
                return;
            }
            if (cursorLine < lines.size() - 1)
            {
                String nextLine = lines.get(cursorLine + 1);
                lines.set(cursorLine, currentLine + nextLine);
                lines.remove(cursorLine + 1);
            }
            horizontalScrollOffset = Math.min(horizontalScrollOffset, getMaxHorizontalOffset());
            ensureCursorVisible = true;
        }

        private void moveCursor(int xDelta, int yDelta)
        {
            int targetLine = Math.max(0, Math.min(lines.size() - 1, cursorLine + yDelta));
            String targetLineText = lines.get(targetLine);
            int targetColumn = Math.max(0, Math.min(targetLineText.length(), cursorColumn + xDelta));
            cursorLine = targetLine;
            cursorColumn = targetColumn;
            horizontalScrollOffset = Math.min(horizontalScrollOffset, getMaxHorizontalOffset());
            ensureCursorVisible = true;
        }

        private int getMaxHorizontalOffset()
        {
            int borderPadding = 2;
            int textMargin = 4;
            int scrollbarSize = 6;
            int availableWidth = width - (borderPadding + textMargin) * 2;
            int availableHeight = height - (borderPadding + textMargin) * 2;
            int maxLineWidth = 0;
            for (String line : lines)
            {
                maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
            }

            boolean horizontalNeeded = maxLineWidth > availableWidth;
            boolean verticalNeeded = false;
            boolean changed;
            do
            {
                changed = false;
                int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
                int contentHeight = availableHeight - (horizontalNeeded ? scrollbarSize : 0);
                int visibleLines = Math.max(1, contentHeight / lineHeight);
                boolean newVerticalNeeded = lines.size() > visibleLines;
                boolean newHorizontalNeeded = maxLineWidth > contentWidth;
                if (newVerticalNeeded != verticalNeeded || newHorizontalNeeded != horizontalNeeded)
                {
                    changed = true;
                    verticalNeeded = newVerticalNeeded;
                    horizontalNeeded = newHorizontalNeeded;
                }
            }
            while (changed);

            int contentWidth = availableWidth - (verticalNeeded ? scrollbarSize : 0);
            return Math.max(0, maxLineWidth - contentWidth);
        }

        private boolean contains(int mouseX, int mouseY)
        {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        private void mouseScrolled(int delta)
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            {
                int scrollbarSize = 6;
                int borderPadding = 2;
                int textMargin = 4;
                int contentWidth = width - (borderPadding + textMargin) * 2;
                int maxHorizontalOffset = getMaxHorizontalOffset();
                if (delta > 0)
                {
                    horizontalScrollOffset = Math.max(0, horizontalScrollOffset - 10);
                }
                else if (delta < 0)
                {
                    horizontalScrollOffset = Math.min(maxHorizontalOffset, horizontalScrollOffset + 10);
                }
                return;
            }

            int borderPadding = 2;
            int textMargin = 4;
            int scrollbarSize = 6;
            int contentWidth = width - (borderPadding + textMargin) * 2;
            int maxLineWidth = 0;
            for (String line : lines)
            {
                maxLineWidth = Math.max(maxLineWidth, fontRenderer.getStringWidth(line));
            }
            boolean horizontalNeeded = maxLineWidth > contentWidth;
            int contentHeight = height - (borderPadding + textMargin) * 2 - (horizontalNeeded ? scrollbarSize : 0);
            int visibleLines = Math.max(1, contentHeight / lineHeight);
            int maxOffset = Math.max(0, lines.size() - visibleLines);
            if (delta > 0)
            {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            else if (delta < 0)
            {
                scrollOffset = Math.min(maxOffset, scrollOffset + 1);
            }
        }
    }
}
