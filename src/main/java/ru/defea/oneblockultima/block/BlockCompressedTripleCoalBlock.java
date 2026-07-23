package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleCoalBlock extends BlockCompressedDoubleCoalBlock {
    public BlockCompressedTripleCoalBlock() {
        super("compressed_coal_block_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedTripleCoalBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
