package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import net.minecraft.util.IIcon;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemSuperProtein extends ItemFood
{
    public ItemSuperProtein()
    {
        super(6, 1.5F, true);
        setUnlocalizedName("oneblockultima.super_protein");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":item_super_protein");
    }
}
