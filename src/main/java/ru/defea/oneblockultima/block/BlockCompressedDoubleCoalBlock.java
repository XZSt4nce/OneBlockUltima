package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleCoalBlock extends BlockCompressedCoalBlock {
    public BlockCompressedDoubleCoalBlock()
    {
        super("compressed_coal_block_2x");
    }

    protected BlockCompressedDoubleCoalBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
