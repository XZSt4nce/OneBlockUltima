package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleGoldBlock extends BlockCompressedDoubleGoldBlock {
    public BlockCompressedTripleGoldBlock()
    {
        super("compressed_gold_block_3x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedTripleGoldBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
