package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleCoalBlock extends BlockCompressedDoubleCoalBlock {
    public BlockCompressedTripleCoalBlock()
    {
        super("compressed_coal_block_3x");
    }

    protected BlockCompressedTripleCoalBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
