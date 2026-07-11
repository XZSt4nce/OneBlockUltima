package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleDiamondBlock extends BlockCompressedDoubleDiamondBlock {
    public BlockCompressedTripleDiamondBlock()
    {
        super("compressed_diamond_block_3x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedTripleDiamondBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
