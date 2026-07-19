package ru.defea.oneblockultima.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import ru.defea.oneblockultima.OneBlockUltima;

public class GuiHandler implements IGuiHandler
{
    public static final int ONE_BLOCK_GUI = 0;
    public static final int CLAIM_GENERATOR_GUI = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == ONE_BLOCK_GUI)
        {
            return new ContainerOneBlock(player, world, x, y, z);
        }

        if (ID == CLAIM_GENERATOR_GUI)
        {
            return new ContainerClaimGenerator(player, world, x, y, z);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == ONE_BLOCK_GUI)
        {
            return new GuiOneBlock(player, world, x, y, z);
        }

        if (ID == CLAIM_GENERATOR_GUI)
        {
            return new GuiClaimGenerator(player, world, x, y, z);
        }

        return null;
    }

    public static void open(EntityPlayer player, int gx, int gy, int gz)
    {
        player.openGui(OneBlockUltima.instance, ONE_BLOCK_GUI, player.worldObj, gx, gy, gz);
    }

    public static void openClaimScreen(EntityPlayer player, int gx, int gy, int gz)
    {
        player.openGui(OneBlockUltima.instance, CLAIM_GENERATOR_GUI, player.worldObj, gx, gy, gz);
    }
}
