package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleEmeraldBlock extends BlockCompressedTripleEmeraldBlock {
    public BlockCompressedQuadrupleEmeraldBlock() {
        super("compressed_emerald_block_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuadrupleEmeraldBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
