package ru.defea.oneblockultima.item;

import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemCompressedMineral extends Item {
    public ItemCompressedMineral() {
        setCreativeTab(OneBlockUltima.modTab);
        this.setRegistryName("compressed_mineral");
        this.setUnlocalizedName("compressed_mineral");
    }
}
