package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedEndStone extends Block {
    public BlockCompressedEndStone() {
        super(Material.rock);
        init("compressed_end_stone_1x");
    }
    protected BlockCompressedEndStone(String name) {
        super(Material.rock);
        init(name);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.end_stone.getExplosionResistance(exploder) * 9.0F;
    }
    private void init(String name) {
        this.setHardness(3F * 9F);
        this.setResistance(15F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
        this.setBlockTextureName(OneBlockUltima.MODID + ":" + name);
    }
}
