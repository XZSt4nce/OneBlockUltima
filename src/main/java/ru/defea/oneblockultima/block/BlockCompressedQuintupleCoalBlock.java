package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleCoalBlock extends BlockCompressedQuadrupleCoalBlock {
    public BlockCompressedQuintupleCoalBlock()
    {
        super("compressed_coal_block_5x");
    }

    protected BlockCompressedQuintupleCoalBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
