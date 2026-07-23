package ru.defea.oneblockultima.block;

import net.minecraft.entity.Entity;

public class BlockCompressedDoubleEndStone extends BlockCompressedEndStone {
    public BlockCompressedDoubleEndStone() {
        super("compressed_end_stone_2x");
        this.setResistance(super.blockResistance * 9.0F);
    }
    protected BlockCompressedDoubleEndStone(String name) {
        super(name);
        this.setResistance(super.blockResistance * 9.0F);
    }
    @Override
    public float getExplosionResistance(Entity exploder) {
        return super.getExplosionResistance(exploder) * 9.0F;
    }
}
