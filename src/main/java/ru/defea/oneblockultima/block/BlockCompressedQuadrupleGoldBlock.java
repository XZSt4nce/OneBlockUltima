package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleGoldBlock extends BlockCompressedTripleGoldBlock {
    public BlockCompressedQuadrupleGoldBlock() {
        super("compressed_gold_block_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuadrupleGoldBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
