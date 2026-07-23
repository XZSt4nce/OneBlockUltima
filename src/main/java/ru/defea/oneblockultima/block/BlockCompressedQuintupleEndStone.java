package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedQuintupleEndStone extends BlockCompressedQuadrupleEndStone {
    public BlockCompressedQuintupleEndStone() {
        super("compressed_end_stone_5x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedQuintupleEndStone(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
