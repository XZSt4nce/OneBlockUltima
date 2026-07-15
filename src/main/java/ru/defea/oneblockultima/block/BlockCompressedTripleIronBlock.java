package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleIronBlock extends BlockCompressedDoubleIronBlock {
    public BlockCompressedTripleIronBlock()
    {
        super("compressed_iron_block_3x");
    }

    protected BlockCompressedTripleIronBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
