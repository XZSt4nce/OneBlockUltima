package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleEmeraldBlock extends BlockCompressedEmeraldBlock {
    public BlockCompressedDoubleEmeraldBlock()
    {
        super("compressed_emerald_block_2x");
    }

    protected BlockCompressedDoubleEmeraldBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
