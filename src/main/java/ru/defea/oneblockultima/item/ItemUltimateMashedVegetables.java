package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemUltimateMashedVegetables extends ItemFood
{
    public ItemUltimateMashedVegetables()
    {
        super(6, 0.3F, false);
        setUnlocalizedName("oneblockultima.ultimate_mashed_vegetables");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":item_ultimate_mashed_vegetables");
    }
}
