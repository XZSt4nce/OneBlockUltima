package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedSextupleLapisBlock extends BlockCompressedQuintupleLapisBlock {
    public BlockCompressedSextupleLapisBlock() {
        super("compressed_lapis_block_6x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedSextupleLapisBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
