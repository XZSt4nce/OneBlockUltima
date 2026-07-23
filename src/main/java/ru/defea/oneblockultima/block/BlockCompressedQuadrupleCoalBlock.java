package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleCoalBlock extends BlockCompressedTripleCoalBlock {
    public BlockCompressedQuadrupleCoalBlock() {
        super("compressed_coal_block_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuadrupleCoalBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
