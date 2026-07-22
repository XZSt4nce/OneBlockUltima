package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemUltimateProtein extends ItemFood
{
    public ItemUltimateProtein()
    {
        super(10, 3.0F, true);
        setUnlocalizedName("oneblockultima.ultimate_protein");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":item_ultimate_protein");
    }
}
