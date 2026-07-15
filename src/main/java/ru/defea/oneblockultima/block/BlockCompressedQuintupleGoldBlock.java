package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleGoldBlock extends BlockCompressedQuadrupleGoldBlock {
    public BlockCompressedQuintupleGoldBlock()
    {
        super("compressed_gold_block_5x");
        this.setResistance(super.blockResistance * 9F);
    }

    protected BlockCompressedQuintupleGoldBlock(String name)
    {
        super(name);
        this.setResistance(super.blockResistance * 9F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
