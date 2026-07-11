package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleDiamondBlock extends BlockCompressedDiamondBlock {
    public BlockCompressedDoubleDiamondBlock()
    {
        super("compressed_diamond_block_2x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedDoubleDiamondBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
