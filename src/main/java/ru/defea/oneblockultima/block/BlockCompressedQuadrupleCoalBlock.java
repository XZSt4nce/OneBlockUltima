package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleCoalBlock extends BlockCompressedTripleCoalBlock {
    public BlockCompressedQuadrupleCoalBlock()
    {
        super("compressed_coal_block_4x");
    }

    protected BlockCompressedQuadrupleCoalBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
