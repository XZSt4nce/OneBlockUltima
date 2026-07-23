package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuadrupleEndStone extends BlockCompressedTripleEndStone {
    public BlockCompressedQuadrupleEndStone() {
        super("compressed_end_stone_4x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuadrupleEndStone(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
