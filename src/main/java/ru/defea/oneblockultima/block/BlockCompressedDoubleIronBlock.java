package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleIronBlock extends BlockCompressedIronBlock {
    public BlockCompressedDoubleIronBlock()
    {
        super("compressed_iron_block_2x");
    }

    protected BlockCompressedDoubleIronBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
