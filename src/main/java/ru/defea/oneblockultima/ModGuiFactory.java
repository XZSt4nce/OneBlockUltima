package ru.defea.oneblockultima;

import net.minecraft.client.gui.GuiScreen;
import ru.defea.oneblockultima.gui.GuiSetsConfig;

public class ModGuiFactory
{
    public static GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new GuiSetsConfig(parentScreen);
    }
}
