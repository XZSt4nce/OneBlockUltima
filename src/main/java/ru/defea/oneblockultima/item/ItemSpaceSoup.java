package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemFood;
import net.minecraft.util.IIcon;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemSpaceSoup extends ItemFood
{
    public ItemSpaceSoup()
    {
        super(100, 10.0F, false);
        setUnlocalizedName("oneblockultima.space_soup");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":item_space_soup");
    }
}
