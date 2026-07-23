package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleIronBlock extends BlockCompressedIronBlock {
    public BlockCompressedDoubleIronBlock() {
        super("compressed_iron_block_2x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedDoubleIronBlock(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
