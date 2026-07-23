package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleIronBlock extends BlockCompressedDoubleIronBlock {
    public BlockCompressedTripleIronBlock() {
        super("compressed_iron_block_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedTripleIronBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
