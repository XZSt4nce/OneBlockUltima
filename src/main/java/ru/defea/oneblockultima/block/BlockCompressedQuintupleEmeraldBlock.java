package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleEmeraldBlock extends BlockCompressedQuadrupleEmeraldBlock {
    public BlockCompressedQuintupleEmeraldBlock()
    {
        super("compressed_emerald_block_5x");
    }

    protected BlockCompressedQuintupleEmeraldBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
