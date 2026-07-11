package ru.defea.oneblockultima.item;

import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemGraviton extends Item {
    public ItemGraviton() {
        setCreativeTab(OneBlockUltima.modTab);
        this.setRegistryName("graviton");
        this.setUnlocalizedName("graviton");
    }
}
