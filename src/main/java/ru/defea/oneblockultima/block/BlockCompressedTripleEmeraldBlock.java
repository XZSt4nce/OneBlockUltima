package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleEmeraldBlock extends BlockCompressedDoubleEmeraldBlock {
    public BlockCompressedTripleEmeraldBlock()
    {
        super("compressed_emerald_block_3x");
    }

    protected BlockCompressedTripleEmeraldBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
