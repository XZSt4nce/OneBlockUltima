package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemSuperMashedVegetables extends ItemFood
{
    public ItemSuperMashedVegetables()
    {
        super(4, 0.3F, false);
        setUnlocalizedName("oneblockultima.super_mashed_vegetables");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":super_mashed_vegetables");
    }
}
