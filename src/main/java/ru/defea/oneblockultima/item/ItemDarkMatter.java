package ru.defea.oneblockultima.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemDarkMatter extends Item
{
    public ItemDarkMatter()
    {
        super();
        setUnlocalizedName("oneblockultima.dark_matter");
        setCreativeTab(OneBlockUltima.modTab);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register)
    {
        this.itemIcon = register.registerIcon(OneBlockUltima.MODID + ":dark_matter");
    }
}
