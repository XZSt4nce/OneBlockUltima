package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleEmeraldBlock extends BlockCompressedTripleEmeraldBlock {
    public BlockCompressedQuadrupleEmeraldBlock()
    {
        super("compressed_emerald_block_4x");
    }

    protected BlockCompressedQuadrupleEmeraldBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
