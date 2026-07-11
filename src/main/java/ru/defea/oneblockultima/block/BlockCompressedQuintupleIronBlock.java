package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleIronBlock extends BlockCompressedQuadrupleIronBlock {
    public BlockCompressedQuintupleIronBlock()
    {
        super("compressed_iron_block_5x");
    }

    protected BlockCompressedQuintupleIronBlock(String name)
    {
        super(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
