package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleLapisBlock extends BlockCompressedDoubleLapisBlock {
    public BlockCompressedTripleLapisBlock() {
        super("compressed_lapis_block_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedTripleLapisBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
