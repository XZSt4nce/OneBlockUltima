package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemEnergyOrb extends Item
{
    public ItemEnergyOrb()
    {
        super();
        setUnlocalizedName("oneblockultima.energy_orb");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":energy_orb");
    }
}
