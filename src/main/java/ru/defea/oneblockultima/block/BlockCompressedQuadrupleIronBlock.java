package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleIronBlock extends BlockCompressedTripleIronBlock {
    public BlockCompressedQuadrupleIronBlock()
    {
        super("compressed_iron_block_4x");
    }

    protected BlockCompressedQuadrupleIronBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
