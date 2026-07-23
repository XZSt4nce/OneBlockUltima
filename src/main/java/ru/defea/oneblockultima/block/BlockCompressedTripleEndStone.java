package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedTripleEndStone extends BlockCompressedDoubleEndStone {
    public BlockCompressedTripleEndStone() {
        super("compressed_end_stone_3x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedTripleEndStone(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
