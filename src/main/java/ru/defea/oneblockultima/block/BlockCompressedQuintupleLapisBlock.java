package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleLapisBlock extends BlockCompressedQuadrupleLapisBlock {
    public BlockCompressedQuintupleLapisBlock() {
        super("compressed_lapis_block_5x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuintupleLapisBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
