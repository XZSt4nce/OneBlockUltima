package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleRedstoneBlock extends BlockCompressedTripleRedstoneBlock {
    public BlockCompressedQuadrupleRedstoneBlock() {
        super("compressed_redstone_block_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }

    protected BlockCompressedQuadrupleRedstoneBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
