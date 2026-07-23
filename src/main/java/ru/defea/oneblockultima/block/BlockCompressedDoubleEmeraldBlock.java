package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleEmeraldBlock extends BlockCompressedEmeraldBlock {
    public BlockCompressedDoubleEmeraldBlock() {
        super("compressed_emerald_block_2x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedDoubleEmeraldBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
