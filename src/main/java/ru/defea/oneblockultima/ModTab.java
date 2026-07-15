package ru.defea.oneblockultima;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import ru.defea.oneblockultima.block.ModBlocks;

public class ModTab extends CreativeTabs {

    public ModTab(String label) {
        super(label);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModBlocks.ONE_BLOCK_GENERATOR);
    }
}