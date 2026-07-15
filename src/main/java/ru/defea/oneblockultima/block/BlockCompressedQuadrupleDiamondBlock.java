package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleDiamondBlock extends BlockCompressedTripleDiamondBlock {
    public BlockCompressedQuadrupleDiamondBlock()
    {
        super("compressed_diamond_block_4x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedQuadrupleDiamondBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
