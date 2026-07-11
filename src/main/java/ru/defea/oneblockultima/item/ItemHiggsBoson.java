package ru.defea.oneblockultima.item;

import net.minecraft.item.Item;
import ru.defea.oneblockultima.OneBlockUltima;

public class ItemHiggsBoson extends Item {
    public ItemHiggsBoson() {
        setCreativeTab(OneBlockUltima.modTab);
        this.setRegistryName("higgs_boson");
        this.setUnlocalizedName("higgs_boson");
    }
}
