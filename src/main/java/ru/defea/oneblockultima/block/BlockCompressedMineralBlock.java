package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedMineralBlock extends Block {
    public BlockCompressedMineralBlock() {
        super(Material.rock);
        init("compressed_mineral_block");
    }
    protected BlockCompressedMineralBlock(String name) {
        super(Material.rock);
        init(name);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return 9.0F;
    }
    private void init(String name) {
        this.setHardness(5F * 9F);
        this.setResistance(10F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setBlockName(name);
        this.setBlockTextureName(OneBlockUltima.MODID + ":" + name);
    }
}
