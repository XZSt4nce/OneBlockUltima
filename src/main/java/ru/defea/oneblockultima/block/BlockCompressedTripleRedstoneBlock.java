package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleRedstoneBlock extends BlockCompressedDoubleRedstoneBlock {
    public BlockCompressedTripleRedstoneBlock() {
        super("compressed_redstone_block_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedTripleRedstoneBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
