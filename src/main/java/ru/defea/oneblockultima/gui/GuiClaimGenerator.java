package ru.defea.oneblockultima.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.defea.oneblockultima.network.ModMessages;
import ru.defea.oneblockultima.network.PacketOneBlockAction;

public class GuiClaimGenerator extends GuiContainer
{
    private static final int BUTTON_CLAIM = 0;
    private final ContainerClaimGenerator container;

    public GuiClaimGenerator(EntityPlayer player, World world, int generatorX, int generatorY, int generatorZ)
    {
        super(new ContainerClaimGenerator(player, world, generatorX, generatorY, generatorZ));
        this.container = (ContainerClaimGenerator) this.inventorySlots;
        this.xSize = 220;
        this.ySize = 140;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        buttonList.add(new GuiButton(BUTTON_CLAIM, guiLeft + 40, guiTop + 70, 140, 20, StatCollector.translateToLocal("gui.oneblockultima.claim_owner")));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.claim_title"), width / 2, guiTop + 24, 0xFFFFFF);
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.oneblockultima.claim_description"), width / 2, guiTop + 44, 0xCCCCCC);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == BUTTON_CLAIM)
        {
            ModMessages.sendToServer(new PacketOneBlockAction(container.getGeneratorX(), container.getGeneratorY(), container.getGeneratorZ(), PacketOneBlockAction.Action.CLAIM_OWNER, ""));
            mc.thePlayer.closeScreen();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
