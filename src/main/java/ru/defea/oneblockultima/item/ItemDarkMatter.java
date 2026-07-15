package ru.defea.oneblockultima.item;

import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemDarkMatter extends Item {
    public ItemDarkMatter() {
        setCreativeTab(OneBlockUltima.modTab);
        this.setRegistryName("dark_matter");
        this.setUnlocalizedName("dark_matter");
    }
}
